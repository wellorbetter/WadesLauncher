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
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WadesLauncher"

include(":app")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:ai")
include(":feature:home")
include(":feature:drawer")
include(":feature:widget")
include(":feature:assistant")
