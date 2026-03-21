plugins {
    id("dev.jaronline.minimash.project")
    id("dev.jaronline.minimash.publish")
    id("dev.jaronline.minimash.test")
    alias(libs.plugins.moddevgradle)
}

base {
    archivesName = "${minimash.modId.get()}-${minimash.mcVersion.version}-common"
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    include("dev/jaronline/minimash/**")
    include("dev/jaronline/minimash/lib/**")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("commonJar") {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))
        }
    }
}