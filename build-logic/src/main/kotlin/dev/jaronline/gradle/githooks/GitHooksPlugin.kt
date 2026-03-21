package dev.jaronline.gradle.githooks

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.internal.cc.base.logger

class GitHooksPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.configureGitHooks()
    }
}

private fun Settings.configureGitHooks() {
    runCatching {
        logger.info("Configuring git hooks")

        providers.exec {
            commandLine("git", "config", "extensions.worktreeConfig", "true")
        }.result.get()

        providers.exec {
            commandLine("git", "config", "--worktree", "core.bare", "false")
        }.result.get()

        providers.exec {
            commandLine("git", "config", "--worktree", "core.hooksPath", ".githooks")
        }.result.get()

        logger.info("Git hooks configured")
    }.onFailure {
        logger.error("Could not auto-configure Git hooks: ${it.message}")
    }
}