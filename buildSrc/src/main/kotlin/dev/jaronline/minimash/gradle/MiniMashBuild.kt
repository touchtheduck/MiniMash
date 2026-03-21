package dev.jaronline.minimash.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class MiniMashBuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create<MiniMashExtension>(
            "minimash",
            project
        )
    }
}

