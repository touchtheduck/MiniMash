@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        fun exclusiveMaven(url: String, vararg groupPrefixes: String) =
            exclusiveContent {
                forRepository { maven(url) }
                filter {
                    groupPrefixes.forEach(::includeGroupAndSubgroups)
                }
            }
		exclusiveContent {
			forRepository { maven("https://maven.blamejared.com/") }
			filter {
				includeGroup("net.mezzdev.java-formatting")
				includeModule("net.mezzdev.gradle", "JavaFormatting")
			}
		}
        exclusiveMaven("https://maven.fabricmc.net/", "net.fabricmc")
		exclusiveMaven("https://maven.neoforged.net/releases", "net.neoforged", "codechicken", "net.covers1624")
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            content {
                includeGroupAndSubgroups("org.spongepowered")
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
    includeBuild("build-logic")
}

plugins {
    id("dev.jaronline.git-hooks")
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "minimash-26.3"
include(
    "Changelog",
    "Common",
    "Fabric",
    "VanillaClient"
)