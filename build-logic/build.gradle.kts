plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("git-hooks") {
            id = "dev.jaronline.git-hooks"
            implementationClass = "dev.jaronline.gradle.githooks.GitHooksPlugin"
        }
    }
}

sourceSets {
    named("main") {
        java.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    }
    named("test") {
        java.setSrcDirs(emptyList<String>())
        kotlin.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    }
}