pluginManagement {
    repositories {
        google()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        google()
        mavenCentral()
    }
}

rootProject.name = "RidePilot"
include(":app")
