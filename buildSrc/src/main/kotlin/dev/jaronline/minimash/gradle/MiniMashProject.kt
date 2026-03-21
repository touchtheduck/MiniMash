package dev.jaronline.minimash.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.model.IdeaModel

class MiniMashProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMiniMashProject()
    }
}

private fun Project.configureMiniMashProject() {
    project.pluginManager.apply("dev.jaronline.minimash.build")
    project.pluginManager.apply("java")
    project.pluginManager.apply("idea")
    project.pluginManager.apply("eclipse")

    val minimash = the<MiniMashExtension>()
    val java = the<JavaPluginExtension>()
    val idea = the<IdeaModel>()
    val eclipse = the<EclipseModel>()

    version = minimash.modVersion.version
    group = minimash.modGroupId.get()

    java.toolchain {
        languageVersion.set(JavaLanguageVersion.of(minimash.javaVersion.version))
    }

    idea.module {
        isDownloadSources = true
        isDownloadJavadoc = true
        excludeDirs.addAll(listOf("build", "run", "run-data", "out", "logs").map { file(it) })
    }

    eclipse.classpath {
        isDownloadSources = true
        isDownloadJavadoc = true
    }

    tasks.withType<Javadoc>().configureEach {
		// workaround cast for https://github.com/gradle/gradle/issues/7038
		val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
		// prevent java 8's strict doclint for javadocs from failing builds
		standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
		// Omit bundled web fonts to reduce the size of the generated documentation.
		standardJavadocDocletOptions.addBooleanOption("-no-fonts", true)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(JavaVersion.toVersion(minimash.javaVersion.version).majorVersion.toInt())
        options.isDeprecation = true
        options.compilerArgs.add("-Xlint:unchecked")
    }

    tasks.withType<Jar>().configureEach {
        manifest {
            attributes(mapOf(
                "Specification-Title" to minimash.modName.get(),
                "Specification-Vendor" to minimash.modVendor.get(),
                "Specification-Version" to minimash.modVersion.version,
                "Implementation-Title" to name,
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to minimash.modVendor.get()
            ))
        }
    }

    tasks.withType<ProcessResources>().configureEach {
        val resourceProperties = mapOf(
            "fabric_api_version" to minimash.fabricApiVersion.version,
            "fabric_api_version_range" to minimash.fabricApiVersionRange.version,
            "fabric_loader_version" to minimash.fabricVersion.version,
            "fabric_loader_version_range" to minimash.fabricVersionRange.version,
            "github_url" to minimash.githubUrl.get(),
            "minecraft_version" to minimash.mcVersion.version,
            "minecraft_version_range" to minimash.mcVersion.range,
            "mod_license" to minimash.modLicense.get(),
            "mod_authors" to minimash.modAuthors.get().joinToString(", "),
            "mod_authors_json" to minimash.modAuthors.get().joinToString(", ") { "\"$it\"" },
            "mod_credits" to minimash.modCredits.get(),
            "mod_description" to minimash.modDescription.get(),
            "mod_id" to minimash.modId.get(),
            "java_version" to minimash.javaVersion.version,
            "mod_name" to minimash.modName.get(),
            "version" to version
        )
        inputs.properties(resourceProperties)
        filesMatching(listOf("pack.mcmeta", "fabric.mod.json")) {
            expand(resourceProperties)
        }
    }

	// Activate reproducible builds
	// https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}