pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("${rootDir}/maven")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("${rootDir}/maven")
        }
    }

}

rootProject.name = "vpn-modules"
include(":vpnduckapp")
include(":vpndonkeyapp")
include(":indianvpn")
include(":common")
include(":vpn")
include(":dunta_sdk")
include(":data")
include(":domain")
