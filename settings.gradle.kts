pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // هذا السطر ضروري جداً لجلب الـ plugins
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Medical-Asset-Manager"
include(":app")
