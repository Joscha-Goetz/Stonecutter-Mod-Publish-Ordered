plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "de.mirador"
version = "1.1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("publishOrdered") {
            id = "de.mirador.publish-ordered"
            implementationClass = "de.mirador.PublishOrderedPlugin"
        }
    }
}
