import dev.jaronline.minimash.gradle.UnpackArchives
import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.jaronline.minimash.project")
    id("dev.jaronline.minimash.publish")
    id("dev.jaronline.minimash.test")
    alias(libs.plugins.loom)
    alias(libs.plugins.modpublish)
}

base {
    archivesName = "${minimash.modId.get()}-${minimash.mcVersion.version}-fabric"
}

repositories {
    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/")
    }
}

val dependencyProjectPaths = listOf(":Common")

java {
    withSourcesJar()
}

val changelogHtml = configurations.create("changelogHtml") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
    }
}

val changelogMarkdown = configurations.create("changelogMarkdown") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogMarkdown"))
    }
}

val dependencyClasses = configurations.create("dependencyClasses") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.CLASSES))
    }
}

val dependencyResources = configurations.create("dependencyResources") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.RESOURCES))
    }
}

val dependencySources = configurations.create("dependencySources") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
    }
}

fun Configuration.singleFileContents(): Provider<String> =
    incoming
        .files
        .elements
        .map { elements -> elements.single() }
        .map { it.asFile.readText() }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    javaToolchains {
        compilerFor {
            languageVersion.set(JavaLanguageVersion.of(minimash.javaVersion.version))
        }
    }
}

dependencies {
    minecraft(libs.loom.minecraft)
    implementation(libs.fabric)
    implementation(libs.fabric.api)

    dependencyProjectPaths.forEach {
        implementation(project(it))
        dependencyClasses(project(it)) {
            isTransitive = false
        }
        dependencyResources(project(it)) {
            isTransitive = false
        }
        dependencySources(project(it)) {
            isTransitive = false
        }
    }

    runtimeOnly(libs.modmenu)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    @Suppress("AvoidDuplicateDependencies")
    changelogHtml(project(":Changelog"))
    @Suppress("AvoidDuplicateDependencies")
    changelogMarkdown(project(":Changelog"))
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create(minimash.modId.get()) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }

    runs {
        val loomRunDir = File("run")

        named("client") {
            client()
            displayName = "Fabric Client"
            generateRunConfig = true
            runDirectory = loomRunDir.resolve("client")
            jvmArguments.addAll("-Dfabric.log.level=info")
        }

        named("server") {
            server()
            displayName = "Fabric Server"
            generateRunConfig = true
            runDirectory = loomRunDir.resolve("server")
            jvmArguments.addAll("-Dfabric.log.level=info")
        }

        create("client debug") {
            client()
            displayName = "Fabric Client Debug"
            generateRunConfig = true
            runDirectory = loomRunDir.resolve("client")
            jvmArguments.addAll("-Dfabric.log.level=debug")
        }

        create("server debug") {
            server()
            displayName = "Fabric Server Debug"
            generateRunConfig = true
            runDirectory = loomRunDir.resolve("server")
            jvmArguments.addAll("-Dfabric.log.level=debug")
        }
    }
}

sourceSets {
    named("main") {
        resources.srcDir(dependencyResources)
    }
}

fabricApi {
    configureDataGeneration {
        client = true
        modId = minimash.modId.get()
    }
}

tasks.jar {
    from(sourceSets.main.get().output)
    from(dependencyClasses)
    from(dependencyResources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    from(dependencyResources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

@Suppress("TaskMissingDescription")
val unpackDependencySources = tasks.register<UnpackArchives>("unpackDependencySources") {
    archives.from(dependencySources)
    excludedPatterns.add("META-INF/MANIFEST.MF")
    outputDirectory = layout.buildDirectory.dir("generated/dependencySources")
}

tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    from(unpackDependencySources)
    exclude("**/Readme.md")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier = "sources"
}

publishMods {
    val publishType = providers.environmentVariable("PUBLISH_TYPE").orNull
        ?: return@publishMods

    file = tasks.jar.flatMap { it.archiveFile }
    changelog = provider { file("../Changelog/changelog.md").readText() }
    type = ReleaseType.of(publishType.uppercase())
    modLoaders.add("fabric")
    displayName = "${project.version} for Fabric ${minimash.mcVersion.version}"
    version = project.version.toString()

    modrinth {
        projectId = minimash.modrinthId
        accessToken = minimash.modrinthToken
        changelog = changelogMarkdown.singleFileContents()
        minecraftVersionRange {
            start = minimash.mcVersion.version
            end = minimash.mcVersion.version
        }
        environment = SERVER_ONLY_CLIENT_OPTIONAL
        dryRun = !minimash.modrinthToken.isPresent
    }

    curseforge {
        projectId = minimash.curseforgeId
        projectSlug = minimash.curseforgeSlug
        accessToken = minimash.curseforgeApiKey
        changelog = changelogHtml.singleFileContents()
        changelogType = "html"
        minecraftVersionRange {
            start = minimash.mcVersion.version
            end = minimash.mcVersion.version
        }
        javaVersions.add(JavaVersion.toVersion(minimash.javaVersion.version))
        client = false
        server = true
        dryRun = !minimash.curseforgeApiKey.isPresent
    }
}

tasks.test {
    include("dev/jaronline/minimash/**")
    include("dev/jaronline/minimash/lib/**")
}

tasks.assemble {
    dependsOn(tasks.named("sourcesJar"))
}

publishing {
    publications {
        register<MavenPublication>("fabricJar") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}
