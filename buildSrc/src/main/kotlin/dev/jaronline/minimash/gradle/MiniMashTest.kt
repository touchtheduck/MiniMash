package dev.jaronline.minimash.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.named

class MiniMashTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMiniMashTest()
    }
}

private fun Project.configureMiniMashTest() {
    tasks.named<Test>("test") {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}