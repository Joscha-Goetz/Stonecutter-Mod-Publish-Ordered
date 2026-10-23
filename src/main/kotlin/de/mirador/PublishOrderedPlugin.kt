package de.mirador

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations
import javax.inject.Inject

class PublishOrderedPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project != project.rootProject) {
            throw GradleException("de.mirador.publish-ordered muss im Hauptprojekt angewendet werden, nicht in ${project.path}")
        }

        val task = project.tasks.register("publishOrdered", PublishOrderedTask::class.java) {
            group = "publishing"
            description = "Veröffentlicht alle Versionen nacheinander, älteste zuerst"
            platform.set(project.providers.gradleProperty("publishPlatform").orElse("Mods"))
            dryRun.set(project.providers.gradleProperty("publishDryRun").map { true }.orElse(false))
            rootDirectory.set(project.layout.projectDirectory)
        }

        project.gradle.projectsEvaluated {
            val ordered = project.allprojects
                .filter { it.tasks.findByName("publishMods") != null }
                .mapNotNull { p -> parse(p)?.let { (version, loader) -> Triple(p.path, version, loader) } }
                .sortedWith(compareBy(VersionOrder) { it: Triple<String, String, String> -> it.second }.thenBy { it.third })
                .map { it.first }
            task.configure { projectPaths.set(ordered) }
        }
    }

    private val versionName = Regex("""^(\d+(?:\.\d+)*)(?:-(.+))?$""")

    private fun parse(p: Project): Pair<String, String>? {
        val match = versionName.matchEntire(p.name) ?: return null
        val version = match.groupValues[1]
        val loader = match.groupValues[2].ifEmpty {
            p.parent?.takeIf { it != p.rootProject }?.name ?: ""
        }
        return version to loader
    }
}

@UntrackedTask(because = "Lädt bei jedem Lauf hoch, ein Ergebnis zum Cachen gibt es nicht")
abstract class PublishOrderedTask : DefaultTask() {
    @get:Inject abstract val execOperations: ExecOperations
    @get:Input abstract val platform: Property<String>
    @get:Input abstract val dryRun: Property<Boolean>
    @get:Input abstract val projectPaths: ListProperty<String>
    @get:Internal abstract val rootDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val paths = projectPaths.get()
        if (paths.isEmpty()) {
            throw GradleException("Keine Projekte mit der Task publishMods gefunden, die nach einer Minecraft-Version benannt sind")
        }

        val root = rootDirectory.get().asFile
        val windows = System.getProperty("os.name").lowercase().contains("win")
        val wrapper = root.resolve(if (windows) "gradlew.bat" else "gradlew").absolutePath
        val failed = mutableListOf<String>()

        for (path in paths) {
            logger.lifecycle("== $path ==")
            val result = execOperations.exec {
                workingDir = root
                isIgnoreExitValue = true
                commandLine(buildList {
                    add(wrapper)
                    add("$path:publish${platform.get()}")
                    add("--console=plain")
                    add("-q")
                    if (dryRun.get()) add("-PpublishDryRun")
                })
            }
            if (result.exitValue != 0) failed += path
        }

        logger.lifecycle("== fehlgeschlagen: ${failed.ifEmpty { listOf("keine") }.joinToString(" ")} ==")
        if (failed.isNotEmpty()) throw GradleException("Fehlgeschlagen: ${failed.joinToString(", ")}")
    }
}

object VersionOrder : Comparator<String> {
    private fun key(v: String) = v.split('.').map { it.toIntOrNull() ?: 0 }

    override fun compare(a: String, b: String): Int {
        val x = key(a)
        val y = key(b)
        for (i in 0 until maxOf(x.size, y.size)) {
            val c = x.getOrElse(i) { 0 }.compareTo(y.getOrElse(i) { 0 })
            if (c != 0) return c
        }
        return 0
    }
}
