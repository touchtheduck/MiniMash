plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("minimashBuild") {
            id = "dev.jaronline.minimash.build"
            implementationClass = "dev.jaronline.minimash.gradle.MiniMashBuildPlugin"
        }
        create("minimashProject") {
            id = "dev.jaronline.minimash.project"
            implementationClass = "dev.jaronline.minimash.gradle.MiniMashProjectPlugin"
        }
        create("minimashPublish") {
            id = "dev.jaronline.minimash.publish"
            implementationClass = "dev.jaronline.minimash.gradle.MiniMashPublishPlugin"
        }
        create("minimashTest") {
            id = "dev.jaronline.minimash.test"
            implementationClass = "dev.jaronline.minimash.gradle.MiniMashTestPlugin"
        }
    }
}

sourceSets {
    named("main") {
        java.setSrcDirs(emptyList<String>())
        groovy.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    }
    named("test") {
        java.setSrcDirs(emptyList<String>())
        groovy.setSrcDirs(emptyList<String>())
        kotlin.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    }
}