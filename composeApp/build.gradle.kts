import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val coworkAppVersion = providers.gradleProperty("coworkAppVersion")
    .orElse("1.0.0")
    .get()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.cowork.desktop.client"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // Decompose
            implementation(libs.decompose.core)
            implementation(libs.decompose.compose)

            // MVIKotlin
            implementation(libs.mvikotlin.core)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.coroutines)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Ktor Client
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.serializationJson)

            // Kotlinx
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlinx.coroutinesCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.clientCio)
            implementation(libs.socketio.client)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.cowork.desktop.client.MainKt"

        jvmArgs("-Xdock:icon=${project.file("icons/AppIcon.icns").absolutePath}")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cowork"
            packageVersion = coworkAppVersion

            macOS {
                iconFile.set(project.file("icons/AppIcon.icns"))
                bundleID = "com.cowork.desktop.client"
                infoPlist {
                    extraKeysRawXml = """
                        <key>CFBundleURLTypes</key>
                        <array>
                            <dict>
                                <key>CFBundleURLName</key>
                                <string>cowork OAuth Callback</string>
                                <key>CFBundleURLSchemes</key>
                                <array>
                                    <string>cowork</string>
                                </array>
                            </dict>
                        </array>
                    """.trimIndent()
                }
            }

            windows {
                iconFile.set(project.file("icons/AppIcon_windows.ico"))
            }
        }

        buildTypes.release.proguard {
            isEnabled = false
        }
    }
}
