import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `kotlin-dsl`
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "de.joscha-goetz"
version = "1.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website = "https://github.com/Joscha-Goetz/Stonecutter-Mod-Publish-Ordered"
    vcsUrl = "https://github.com/Joscha-Goetz/Stonecutter-Mod-Publish-Ordered"
    plugins {
        create("publishOrdered") {
            id = "de.joscha-goetz.publish-ordered"
            implementationClass = "de.mirador.PublishOrderedPlugin"
            displayName = "Publish Ordered"
            description = "Publishes multi-version Minecraft mods one version after another, oldest first"
        }
    }
}

mavenPublishing {
    configure(GradlePlugin(javadocJar = JavadocJar.Empty(), sourcesJar = true))
    publishToMavenCentral()
    signAllPublications()

    pom {
        name = "Publish Ordered"
        description = "Gradle plugin that publishes multi-version Minecraft mods in version order"
        url = "https://github.com/Joscha-Goetz/Stonecutter-Mod-Publish-Ordered"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "Joscha-Goetz"
                name = "Joscha Goetz"
                url = "https://github.com/Joscha-Goetz"
            }
        }
        scm {
            url = "https://github.com/Joscha-Goetz/Stonecutter-Mod-Publish-Ordered"
            connection = "scm:git:git://github.com/Joscha-Goetz/Stonecutter-Mod-Publish-Ordered.git"
            developerConnection = "scm:git:ssh://git@github.com/Joscha-Goetz/Stonecutter-Mod-Publish-Ordered.git"
        }
    }
}
