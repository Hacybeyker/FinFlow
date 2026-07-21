plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
}

android {
    lint {
        abortOnError = false
        warningsAsErrors = false
        checkDependencies = true
        checkReleaseBuilds = true
        lintConfig = file("$rootDir/lint.xml")
        htmlReport = true
        sarifReport = true
        textReport = true
    }
    namespace = "com.hacybeyker.finflow"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.hacybeyker.finflow"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = libs.versions.appVersion.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                // Extra R8 rules live in src/main/keepRules/ (AGP 9.x source-folder convention).
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Robolectric inflates real resources (strings, themes) on the JVM.
            isIncludeAndroidResources = true
            all { test ->
                test.maxHeapSize = "2g"
            }
        }
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
}

ksp {
    // Room exports the schema JSON here (exportSchema = true) so migrations can be diffed/tested.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.sqlcipher.android)
    implementation(libs.tink.android)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    compileOnly(libs.errorprone.annotations)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    annotationProcessor(libs.kotlin.metadata.jvm)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    basePath = rootDir
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks.register("codeQuality") {
    group = "verification"
    description = "Ejecuta Android Lint + ktlint + detekt en un solo comando."
    dependsOn("ktlintCheck", "detekt", "lint")
}

listOf("ktlintCheck", "detekt", "lint").forEach { check ->
    tasks.named(check) { mustRunAfter("ktlintFormat") }
}

tasks.register("formatAndAnalyze") {
    group = "verification"
    description = "Formatea el codigo (ktlintFormat) y luego verifica todo (ktlintCheck + detekt + lint)."
    dependsOn("ktlintFormat", "codeQuality")
}

roborazzi {
    // Goldens live in the repo: verifyRoborazziDebug diffs against them, recordRoborazziDebug
    // re-baselines after an intentional visual change.
    outputDir.set(file("src/test/screenshots"))
}

sonar {
    properties {
        property("sonar.androidLint.reportPaths", "build/reports/lint-results-debug.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
        property(
            "sonar.kotlin.ktlint.reportPaths",
            listOf(
                "build/reports/ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.xml",
                "build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.xml",
                "build/reports/ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.xml",
                "build/reports/ktlint/ktlintAndroidTestSourceSetCheck/ktlintAndroidTestSourceSetCheck.xml"
            ).joinToString(",")
        )
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/reportDebug.xml")
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/ui/**",
                "**/navigation/**",
                "**/core/database/**",
                "**/core/di/**",
                "**/MainActivity.kt",
                "**/FinFlowApplication.kt",
                "**/feature/transactions/data/RoomTransactionRepository.kt",
                "**/feature/transactions/data/RoomCategoryRepository.kt",
                "**/feature/settings/data/ContentResolverCsvSaver.kt",
                "**/feature/reminders/data/WorkManagerReminderScheduler.kt",
                "**/feature/reminders/data/ReminderWorker.kt"
            ).joinToString(",")
        )
    }
}

kover {
    reports {
        filters {
            includes {
                classes(
                    "com.hacybeyker.finflow.*.domain.*",
                    "com.hacybeyker.finflow.*.data.*",
                    "com.hacybeyker.finflow.*ViewModel*"
                )
            }
            excludes {
                classes(
                    "*_Impl",
                    "*_Impl$*",
                    "*_Factory",
                    "*_Factory$*",
                    "*Module",
                    "*Module$*",
                    "*Module_*",
                    "*_HiltModules*"
                )
                classes(
                    "com.hacybeyker.finflow.feature.transactions.data.RoomTransactionRepository",
                    "com.hacybeyker.finflow.feature.transactions.data.RoomCategoryRepository",
                    "com.hacybeyker.finflow.feature.settings.data.ContentResolverCsvSaver*",
                    "com.hacybeyker.finflow.feature.reminders.data.WorkManagerReminderScheduler",
                    "com.hacybeyker.finflow.feature.reminders.data.ReminderWorker*"
                )
            }
        }
        verify {
            rule("Line coverage of measured classes (domain, data, ViewModels)") {
                minBound(95)
            }
        }
    }
}
