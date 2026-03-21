plugins {
    alias(libs.plugins.java.formatting)
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.loom.companion)
    alias(libs.plugins.moddevgradle) apply false
    alias(libs.plugins.modpublish) apply false
}

val javaSourceTrees = listOf(
    "Fabric"
).map { moduleName ->
    layout.projectDirectory.dir("$moduleName/src").asFileTree.matching {
        include("*/java/**/*.java")
    }
}

javaFormatting {
    val customTargets = providers.environmentVariable("SPOTLESS_TARGETS")
        .map { it ->
            it.split("\n")
                .filter { it.endsWith(".java") }
        }
        .orNull

    if (customTargets == null)
        target("*/src/*/java/dev/jaronline/minimash/**/*.java")
    else
        target(javaSourceTrees)

    all()
}

repositories {
    mavenCentral()
}