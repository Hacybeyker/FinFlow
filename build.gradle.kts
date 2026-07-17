// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "com.hacybeyker.finflow")
        property("sonar.organization", "hacybeyker")
        property("sonar.projectName", "app-finflow-android")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.projectDescription",
            "Personal finance Android app: income/expense tracking with categories, charts, " +
                "biometric lock, encrypted local database, home-screen widget and CSV export."
        )
        property("sonar.projectVersion", libs.versions.appVersion.get())
    }
}

tasks.named("sonar") {
    dependsOn(":app:lint", ":app:detekt", ":app:ktlintCheck", ":app:koverXmlReportDebug")
}
