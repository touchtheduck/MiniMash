package dev.jaronline.minimash.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.the

class MiniMashPublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMiniMashPublish()
    }
}

private fun Project.configureMiniMashPublish() {
    project.pluginManager.apply("dev.jaronline.minimash.build")
    project.pluginManager.apply("maven-publish")

    val minimash = the<MiniMashExtension>()
    val publishing = the<PublishingExtension>()

    publishing.repositories.maven {
        name = "GithubPackages"
        url = uri("https://maven.pkg.github.com/touchtheduck/MiniMash")
        credentials {
            username = minimash.githubActor
            password = minimash.githubToken
        }
    }
}