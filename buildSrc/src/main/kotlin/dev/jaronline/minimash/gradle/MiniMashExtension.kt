package dev.jaronline.minimash.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the

abstract class MiniMashExtension(val project: Project) {
    val libs = findVersionCatalog("libs")

    val modId = gradleProperty("mod_id")
    val modName = gradleProperty("mod_name")
    val modLicense = gradleProperty("mod_license")
    val modGroupId = gradleProperty("mod_group_id")
    val modAuthors = gradleListProperty("mod_authors")
    val modVendor = gradleProperty("mod_vendor")
    val modCredits = gradleProperty("mod_credits")
    val modDescription = gradleProperty("mod_description")
    val githubUrl = gradleProperty("github_url")

    val modrinthId = gradleProperty("modrinth_id")
    val curseforgeSlug = gradleProperty("curseforge_slug")
    val curseforgeId = gradleProperty("curseforge_id")

    val modVersion = versionLookup("minimash")
    val mcVersion = versionLookup("minecraft")
    val neoformVersion = versionLookup("neoform")
    val javaVersion = versionLookup("java")

    val fabricVersion = versionLookup("fabric")
    val fabricVersionRange = versionLookup("fabric.range")
    val fabricApiVersion = versionLookup("fabric.api")
    val fabricApiVersionRange = versionLookup("fabric.api.range")

    val modrinthToken = environmentVariable("MODRINTH_TOKEN")
    val curseforgeApiKey = environmentVariable("CURSEFORGE_API_KEY").orElse("0")

    val githubActor = environmentVariable("GITHUB_ACTOR").orNull
    val githubToken = environmentVariable("GITHUB_TOKEN").orNull

    fun gradleListProperty(propertyName: String): Provider<List<String>> {
        return gradleProperty(propertyName).map { it.split(", *".toRegex()) }
    }

    fun gradleProperty(propertyName: String): Provider<String> {
        return project.providers.gradleProperty(propertyName)
    }

    fun environmentVariable(variableName: String): Provider<String> {
        return project.providers.environmentVariable(variableName)
    }

    fun versionLookup(id: String): GeoVersionConstraint {
        return GeoVersionConstraint(project.provider {
            libs.findVersion(id)
                .orElseThrow {
                    NoSuchElementException(
                        "Version '${
                            id.replace(
                                '.',
                                '-'
                            )
                        }' does not exist in version catalogue '${libs.name}'!"
                    )
                }
        })
    }

    fun findVersionCatalog(name: String): VersionCatalog {
        return project.the<VersionCatalogsExtension>().find(name).orElseThrow {
            NoSuchElementException("No version catalogue '$name' exists!")
        }
    }

    class GeoVersionConstraint(private val constraint: Provider<VersionConstraint>) :
        Provider<VersionConstraint> by constraint {
        val version: String
            get() {
                val constraint = get()
                val version = constraint.preferredVersion

                if (version.isEmpty())
                    return constraint.requiredVersion

                return version
            }

        val range: String
            get() {
                val constraint = get()
                var range = constraint.requiredVersion

                if (range.isEmpty()) {
                    range = constraint.strictVersion

                    if (range.isEmpty())
                        throw IllegalArgumentException("Version constraint '${constraint.displayName}' does not have a required or strict version declared!")
                }

                return range
            }

        override fun toString(): String {
            return version
        }
    }
}