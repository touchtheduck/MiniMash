import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
    id("dev.jaronline.minimash.build")
    alias(libs.plugins.gitchangelog)
}

val changelogUntaggedName = minimash.modVersion.map { "Current release $it" }

@Suppress("TaskMissingDescription")
val makeHtmlChangelog = tasks.register<GitChangelogTask>("makeHtmlChangelog") {
    val output = layout.buildDirectory.file("CHANGELOG.html")

    fromRepo.set(rootProject.rootDir.absolutePath)
    file.set(output.get().asFile)
    untaggedName.set(changelogUntaggedName)
    fromRevision.set("HEAD~30")
    toRevision.set("HEAD")
    templateContent.set(file("changelog.mustache").readText())

    outputs.file(output)
}

@Suppress("TaskMissingDescription")
val makeMarkdownChangelog = tasks.register<GitChangelogTask>("makeMarkdownChangelog") {
    val output = layout.buildDirectory.file("CHANGELOG.md")

    fromRepo.set(rootProject.rootDir.absolutePath)
    file.set(output.get().asFile)
    untaggedName.set(changelogUntaggedName)
    fromRevision.set("HEAD~30")
    toRevision.set("HEAD")
    templateContent.set(file("changelog.mustache").readText())

    outputs.file(output)
}

tasks.withType<GitChangelogTask> {
	outputs.upToDateWhen { false }
}

val changelogHtml = configurations.create("changelogHtml") {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
	}
	outgoing.artifact(makeHtmlChangelog.map { it.outputs.files.singleFile }) {
		type = "html"
	}
}

val changelogMarkdown = configurations.create("changelogMarkdown") {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogMarkdown"))
	}
	outgoing.artifact(makeMarkdownChangelog.map { it.outputs.files.singleFile }) {
		type = "markdown"
	}
}