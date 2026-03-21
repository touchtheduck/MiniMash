import org.slf4j.event.Level

plugins {
    id("dev.jaronline.minimash.project")
    alias(libs.plugins.moddevgradle)
}

neoForge {
    neoFormVersion = minimash.neoformVersion.version
    runs {
        create("vanillaClient") {
            client()
            gameDirectory = file("run/vanillaClient")
            logLevel = Level.INFO
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
        resources.setSrcDirs(emptyList<String>())
    }
}