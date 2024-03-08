package com.fermion.android.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import com.github.kinquirer.components.promptInput
import com.squareup.kotlinpoet.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

class TemplateWithDaggerCLI : CliktCommand() {
    val terminal = Terminal()
    private val repeatTimer = 2

    val a = terminal.textAnimation<Int>() { frame ->
        (1..frame).joinToString("") {
            val hue = (frame + it) * 3 % 360
            TextColors.hsv(hue, 1, 1)(".")
        }

    }

    override fun run() {
        val path = Paths.get("").toAbsolutePath().toString()
        println("Working Directory = $path")
        var appName: String = KInquirer.promptInput(
            Res.StyledRes.enterAppName
        ).trim()
        var validPackage = true
        var packageName = ""
        var mainFileName = ""
        do {
            mainFileName = KInquirer.promptInput(
                Res.StyledRes.enterAppClassName,
                hint = "Name will be used for naming module, main classes of you application eg SampleApplication,SampleActivity and name for other resource."
            ).trim()

            if (mainFileName.contains("Activity", ignoreCase = true)) {
                validPackage = false
                terminal.println("Name should not contain activity word.")
                continue
            }
            if (mainFileName.contains(" ", ignoreCase = true)) {
                validPackage = false
                terminal.println("Name should not contain spaces.")
                continue
            }
            val pattern = Regex("[^A-Za-z0-9 ]")
            if (pattern.containsMatchIn(mainFileName)) {
                validPackage = false
                terminal.println("Name should not contain special symbols.")
                continue
            }
            if (mainFileName.contains("Fragment", ignoreCase = true)) {
                validPackage = false
                terminal.println("Name should not contain fragment word.")
                continue
            }


            packageName = KInquirer.promptInput(Res.StyledRes.enterPackageName).trim()

            if (!packageName.contains(".")) {
                validPackage = false
                terminal.println("Package name is not in valid format and does not contain.")
                continue
            }

            if (packageName.split(".").size <= 1) {
                validPackage = false
                terminal.println("Package name length too small.")
                continue
            }

            val index = packageName.lastIndexOf(".")
            if (index != -1) {
                val substring = packageName.substring(index)
                if (substring == ".") {
                    validPackage = false
                    terminal.println("Package name is not in valid format and ends with.")
                    continue
                }
            }

            if (Regex("[A-Z]").containsMatchIn(packageName)) {
                validPackage = false
                terminal.println("Package name is not in valid format and contains upper case characters.")
                continue
            }

            validPackage = true
        } while (!validPackage)

        appName =
            appName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val projectFolderPath = buildString {
            append(path)
            append("\\")
            append(mainFileName.lowercase())
        }

        terminal.println("Your app will be created in following path: $projectFolderPath ")
        createProjectFolderAndStartProjectSetup(appName, packageName, projectFolderPath, path, mainFileName)

    }

    private fun createProjectFolderAndStartProjectSetup(
        appName: String, packageName: String, projectFolderPath: String, originalPath: String, mainFileName: String
    ) {
        val projectFilePath = File(projectFolderPath)
        if (projectFilePath.exists()) {
            terminal.println("Project with same name already exists, Do you want to delete it")
            val confirmation = KInquirer.promptConfirm("Confirm", default = false)
            if (confirmation) {
                val deleted = projectFilePath.deleteRecursively()
                if (!deleted) {
                    terminal.println("Deletion of existing files failed. Please delete manually and retry!!".showErrorCliMessage())
                } else {
                    terminal.println("Deletion of existing files successful.".showSuccessCliMessage())
                }
            } else {
                terminal.println("As project with same name already exists can't create project in same folder.".showErrorCliMessage())
                terminal.println("Please choose different directory for project setup".showInfoCliMessage())
                return
            }
        }

        "git clone https://github.com/audibhavesh-fermion/android-native-dagger.git ${mainFileName.lowercase()}".runCommand(
            File(
                originalPath
            ),
            onFailure = {
                terminal.println("Can't change directory".showErrorCliMessage())
            },
            onSuccess = {
                var i = 1
                repeat(repeatTimer) {
                    a.update(i)
                    i += 1
                    if (i == repeatTimer) {
                        i = 1
                    }
                    Thread.sleep(100)
                }
                a.clear()
                terminal.println("Git repo cloned".showSuccessCliMessage())
                callWhenDaggerRepoCloned(projectFolderPath, originalPath, appName, packageName, mainFileName)
            })
    }

    private fun callWhenDaggerRepoCloned(
        projectFolderPath: String, originalPath: String, appName: String, packageName: String, mainFileName: String
    ) {

        val projectFilePath = File("$projectFolderPath/app/${mainFileName.lowercase()}")
        if (!projectFilePath.exists()) {
            if (projectFilePath.mkdirs()) {
                if (File("$projectFolderPath/app/${mainFileName.lowercase()}/src").mkdirs()) {
                    createAndroidTestingDirectory(projectFolderPath, appName, packageName, originalPath, mainFileName)
                    createTestingDirectory(projectFolderPath, appName, packageName, originalPath, mainFileName)
                    createMainDirectory(projectFolderPath, appName, packageName, originalPath, mainFileName)

                } else {
                    deleteRepo(projectFilePath)
                }
            } else {
                terminal.println("Folder creation failed, removing the cloned repository".showErrorCliMessage())
                deleteRepo(projectFilePath)
            }
        }
    }

    private fun createMainDirectory(
        projectFolderPath: String, appName: String, packageName: String, originalPath: String, mainFileName: String
    ) {
        val packageNameList = packageName.split(".")
        val packagePath = packageNameList.joinToString("/")
        val mainPath = "$projectFolderPath/app/${mainFileName.lowercase()}/src/main/java/$packagePath"

        val mainNameCamelCase =
            mainFileName.replaceFirstChar { if (it.isUpperCase()) it.lowercase(Locale.getDefault()) else it.toString() }

        //creating main package folder
        File(mainPath).mkdirs()

        // creating sub directories
        val uiPath = "$mainPath/ui"
        val configPath = "$mainPath/config"
        val diPath = "$mainPath/di"
        val networkPath = "$mainPath/network"
        val mainUiPath = "$mainPath/ui/main"
        val homeUiPath = "$mainPath/ui/home"

        listOf(uiPath, configPath, diPath, networkPath, mainUiPath, homeUiPath).forEach {
            File(it).mkdirs()
        }


        createFile(
            "$configPath/AppConfig.kt", """
                  package ${packageName}.config

                  import com.fermion.android.base.config.RunConfig
                  import javax.inject.Singleton


                  /**
                   * Created by Bhavesh Auodichya.
                   *
                   *AppConfig extending RunConfig
                   *
                   *@since 1.0.0
                   */

                  @Singleton
                  open class AppConfig : RunConfig()


              """.trimIndent()
        )

        createFile(
            "$diPath/${mainFileName}Application.kt", """
                        package ${packageName}.di
                        import com.fermion.android.base.helper.CrashReportingTree
                        import ${packageName}.BuildConfig
                        import ${packageName}.di.DaggerAppComponent
                        import dagger.android.AndroidInjector
                        import dagger.android.DaggerApplication
                        import timber.log.Timber
                        
                        class ${mainFileName}Application : DaggerApplication() {
                            override fun onCreate() {
                                super.onCreate()
                        //        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
                        
                                if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
                                else Timber.plant(CrashReportingTree())
                        
                            }
                        
                            override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
                                return DaggerAppComponent.builder().withApplication(this)
                                    .build()
                            }
                        }
                """
        )
        createFile(
            "$diPath/AppComponent.kt", """
                        package ${packageName}.di
                                              
                        import android.app.Application
                        import com.fermion.android.base.di.BaseAppModule
                        import com.fermion.android.dagger_processor.DaggerAppComponent
                        import ${packageName}.di.generated.CommonBuilderModule
                        import ${packageName}.network.${mainFileName}NetworkModule
                        import ${packageName}.di.${mainFileName}Application
                        import dagger.BindsInstance
                        import dagger.Component
                        import dagger.android.AndroidInjector
                        import dagger.android.support.AndroidSupportInjectionModule
                        import javax.inject.Singleton
                        
                        
                        @Singleton
                        @Component(
                            modules = [
                                AndroidSupportInjectionModule::class,
                                BaseAppModule::class,
                                ${mainFileName}NetworkModule::class,
                                CommonBuilderModule::class,
                            ],
                        )
                        
                        @DaggerAppComponent
                        interface AppComponent : AndroidInjector<${mainFileName}Application> {
                        
                            @Component.Builder
                            interface Builder {
                                @BindsInstance
                                fun withApplication(application: Application): Builder
                                fun build(): AppComponent
                        
                            }
                        }
                """
        )

        createFile(
            "$networkPath/${mainFileName}NetworkModule.kt", """
                        package ${packageName}.network
                        
                        import androidx.annotation.NonNull
                        import com.fermion.android.base.network.NetworkFactory
                        import ${packageName}.config.AppConfig
                        import dagger.Module
                        import dagger.Provides
                        
                        
                        @Module
                        class ${mainFileName}NetworkModule {
                        
                            private var appConfig: AppConfig = AppConfig()
                        
                            @Provides
                            @NonNull
                            fun provide${mainFileName}ApiService(
                                @NonNull serviceFactory: NetworkFactory
                            ): ${mainFileName}Service {
                                return serviceFactory.create(
                                    appConfig.baseUrl, ${mainFileName}Service::class.java
                                )
                            }
                        }
                """
        )
        createFile(
            "$networkPath/${mainFileName}Repository.kt", """
                        package ${packageName}.network
                        import com.fermion.android.base.network.BaseDataRepository
                        import com.fermion.android.base.network.NetworkResult
                        import javax.inject.Inject
                        import javax.inject.Singleton
                        
                        @Singleton
                        class ${mainFileName}Repository @Inject constructor(private val ${mainNameCamelCase}Service: ${mainFileName}Service) :
                            BaseDataRepository(${mainNameCamelCase}Service) {
                                         
                        }
                """
        )
        createFile(
            "$networkPath/${mainFileName}Service.kt", """
                       package ${packageName}.network
                       import com.fermion.android.base.network.BaseApiService
                       import retrofit2.Response
                       import retrofit2.http.GET
                        
                       interface ${mainFileName}Service : BaseApiService {
                        
                        }
                """
        )


        //ui files
        createFile(
            "$mainUiPath/${mainFileName}Activity.kt", """
              package ${packageName}.ui.main
            
            import android.os.Bundle
            import android.view.LayoutInflater
            import androidx.navigation.NavController
            import androidx.navigation.findNavController
            import androidx.navigation.ui.AppBarConfiguration
            import androidx.navigation.ui.navigateUp
            import com.fermion.android.base.view.BaseActivity
            import com.fermion.android.dagger_processor.InjectView
            import ${packageName}.R
            import ${packageName}.databinding.Activity${mainFileName}Binding


            @InjectView
            class ${mainFileName}Activity :
                BaseActivity<Activity${mainFileName}Binding, ${mainFileName}ViewModel>() {

                private lateinit var appBarConfiguration: AppBarConfiguration
                private lateinit var navController: NavController
                override fun onStart() {
                    super.onStart()

                }

                override val bindingInflater: (LayoutInflater) -> Activity${mainFileName}Binding
                    get() = Activity${mainFileName}Binding::inflate

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    navController = findNavController(R.id.nav_fragment)
                    appBarConfiguration = AppBarConfiguration(navController.graph)


                }

                override fun onSupportNavigateUp(): Boolean {
                    navController = findNavController(R.id.nav_fragment)
                    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
                }
            }
                    """.trimIndent()
        )

        createFile(
            "$mainUiPath/${mainFileName}ViewModel.kt", """
           
                    package ${packageName}.ui.main
                    import com.fermion.android.base.view.BaseViewModel
                    import com.fermion.android.dagger_processor.InjectViewModel
                    import javax.inject.Inject


                    @InjectViewModel
                    class ${mainFileName}ViewModel @Inject constructor() : BaseViewModel() {

                    }
                               """.trimIndent()
        )

        createFile(
            "$homeUiPath/HomeFragment.kt", """
                package ${packageName}.ui.home
                import android.os.Bundle
                import android.view.LayoutInflater
                import android.view.View
                import android.view.ViewGroup
                import androidx.navigation.fragment.findNavController
                import com.fermion.android.base.view.BaseFragment
                import com.fermion.android.base.view.BaseViewModel
                import com.fermion.android.dagger_processor.InjectView
                import ${packageName}.databinding.FragmentHomeBinding


                @InjectView
                class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
                    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
                        get() = FragmentHomeBinding::inflate


                    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                        super.onViewCreated(view, savedInstanceState)
                        
                    }
                }
                    """.trimIndent()
        )
        createFile(
            "$homeUiPath/HomeViewModel.kt", """
           
                    package ${packageName}.ui.home
                    import com.fermion.android.base.view.BaseViewModel
                    import com.fermion.android.dagger_processor.InjectViewModel
                    import javax.inject.Inject


                    @InjectViewModel
                    class HomeViewModel @Inject constructor() : BaseViewModel() {

                    }
                               """.trimIndent()
        )


        createResFolder(originalPath, projectFolderPath, packageName, appName, mainFileName)


    }

    private fun createResFolder(
        originalPath: String, projectFolderPath: String, packageName: String, appName: String, mainFileName: String
    ) {
        val resPath = "$projectFolderPath/app/${mainFileName.lowercase()}/src/main/res"

        File(resPath).mkdirs()

        listOf(
            "drawable",
            "font",
            "layout",
            "mipmap-anydpi",
            "mipmap-hdpi",
            "mipmap-mdpi",
            "mipmap-xhdpi",
            "mipmap-xxhdpi",
            "mipmap-xxxhdpi",
            "navigation",
            "raw",
            "values",
            "values-night"
        ).forEach {
            File("$resPath/$it").mkdirs()
        }

        val resCopyPath = "$projectFolderPath/examples/cat-facts/src/main/res"

        listOf(
            "drawable",
            "font",
            "mipmap-anydpi",
            "mipmap-hdpi",
            "mipmap-mdpi",
            "mipmap-xhdpi",
            "mipmap-xxhdpi",
            "mipmap-xxxhdpi",
        ).forEach {
            copyDir(File("${resCopyPath}/$it").toPath(), File("${resPath}/$it").toPath())
        }

        File("$resPath/layout/${mainFileName.toActivityString()}.xml").apply {
            createNewFile()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:app="http://schemas.android.com/apk/res-auto"
                    xmlns:tools="http://schemas.android.com/tools"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    tools:context=".ui.main.${mainFileName}Activity">

                    <fragment
                        android:id="@+id/nav_fragment"
                        android:name="androidx.navigation.fragment.NavHostFragment"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        app:defaultNavHost="true"
                        app:layout_constraintBottom_toBottomOf="parent"
                        app:layout_constraintEnd_toEndOf="parent"
                        app:layout_constraintStart_toStartOf="parent"
                        app:layout_constraintTop_toTopOf="parent"
                        app:navGraph="@navigation/nav_graph" />

                </androidx.constraintlayout.widget.ConstraintLayout>
            """.trimIndent()
            )
        }

        File("$resPath/layout/fragment_home.xml").apply {
            createNewFile()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:app="http://schemas.android.com/apk/res-auto"
                    xmlns:tools="http://schemas.android.com/tools"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    tools:context=".ui.home.HomeFragment">                 
                </androidx.constraintlayout.widget.ConstraintLayout>
            """.trimIndent()
            )
        }

        File("$resPath/navigation/nav_graph.xml").apply {
            createNewFile()
            writeText(
                """
                 <?xml version="1.0" encoding="utf-8"?>
                 <navigation xmlns:android="http://schemas.android.com/apk/res/android"
                     xmlns:app="http://schemas.android.com/apk/res-auto"
                     xmlns:tools="http://schemas.android.com/tools"
                     android:id="@+id/nav_graph"
                     app:startDestination="@id/homeFragment">
                     <fragment
                         android:id="@+id/homeFragment"
                         android:name="${packageName}.ui.home.HomeFragment"
                         android:label="fragment_home"
                         tools:layout="@layout/fragment_home">
                    
                     </fragment>
            
                 </navigation>
            """.trimIndent()
            )
        }

        File("$resPath/values/colors.xml").apply {
            createNewFile()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <color name="black">#FF000000</color>
                    <color name="white">#FFFFFFFF</color>
                </resources>
            """.trimIndent()
            )
        }


        File("$resPath/values/strings.xml").apply {
            createNewFile()
            writeText(
                """
               <resources>
                   <string name="app_name">${appName}</string>
               </resources>
            """.trimIndent()
            )
        }

        File("$resPath/values/themes.xml").apply {
            createNewFile()
            writeText(
                """
                <resources>
                    <!-- Base application theme. -->
                    <style name="Base.Theme.${mainFileName}" parent="Theme.Material3.DayNight.NoActionBar">
                        <!-- Customize your light theme here. -->
                        <!-- <item name="colorPrimary">@color/my_light_primary</item> -->
                    </style>

                    <style name="Theme.${mainFileName}" parent="Base.Theme.${mainFileName}" />
                </resources>
            """.trimIndent()
            )
        }

        File("$resPath/values-night/themes.xml").apply {
            createNewFile()
            writeText(
                """
               <resources>
                   <!-- Base application theme. -->
                   <style name="Base.Theme.${mainFileName}" parent="Theme.Material3.DayNight.NoActionBar">
                       <!-- Customize your dark theme here. -->
                       <!-- <item name="colorPrimary">@color/my_dark_primary</item> -->
                   </style>
               </resources>
            """.trimIndent()
            )
        }

        createAndroidManifestFile(originalPath, projectFolderPath, packageName, appName, mainFileName)

    }

    private fun createAndroidManifestFile(
        originalPath: String, projectFolderPath: String, packageName: String, appName: String, mainFileName: String
    ) {
        val mainPath = "$projectFolderPath/app/${mainFileName.lowercase()}/src/main"
        File("$mainPath/AndroidManifest.xml").apply {
            createNewFile()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">

                    <uses-permission android:name="android.permission.INTERNET" />
                    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

                    <application
                        android:allowBackup="true"
                        android:icon="@mipmap/ic_launcher"
                        android:label="@string/app_name"
                        android:roundIcon="@mipmap/ic_launcher_round"
                        android:supportsRtl="true"
                        android:name=".di.${mainFileName}Application"
                        android:theme="@style/Theme.${mainFileName}">
                        <activity
                            android:name=".ui.main.${mainFileName}Activity"
                            android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />

                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                        </activity>
                    </application>

                </manifest>
            """.trimIndent()
            )
        }

        createGradleAndOtherFiles(originalPath, projectFolderPath, packageName, appName, mainFileName)

    }

    private fun createGradleAndOtherFiles(
        originalPath: String, projectFolderPath: String, packageName: String, appName: String, mainFileName: String
    ) {
        File("$projectFolderPath/app/${mainFileName.lowercase()}/.gitignore").apply {
            createNewFile()
            writeText(
                """
              /build
              /release
            """.trimIndent()
            )
        }

        File("$projectFolderPath/app/${mainFileName.lowercase()}/build.gradle.kts").apply {
            createNewFile()
            writeText(
                """
             

             android {
                 namespace = "$packageName"
                 compileSdk = 34

                 defaultConfig {
                     applicationId = "$packageName"

                     testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                 }

                 buildTypes {
                     release {
                         isMinifyEnabled = false
                         proguardFiles(
                             getDefaultProguardFile("proguard-android-optimize.txt"),
                             "proguard-rules.pro"
                         )
                     }
                 }
                 compileOptions {
                     sourceCompatibility = JavaVersion.VERSION_17
                     targetCompatibility = JavaVersion.VERSION_17
                 }

                 kotlinOptions {
                     jvmTarget = "17"
                 }
             }

            """.trimIndent()
            )
        }

        File("$projectFolderPath/app/${mainFileName.lowercase()}/proguard-rules.pro").apply {
            createNewFile()
            writeText(
                """
                    # Add project specific ProGuard rules here.
                    # You can control the set of applied configuration files using the
                    # proguardFiles setting in build.gradle.
                    #
                    # For more details, see
                    #   http://developer.android.com/guide/developing/tools/proguard.html

                    # If your project uses WebView with JS, uncomment the following
                    # and specify the fully qualified class name to the JavaScript interface
                    # class:
                    #-keepclassmembers class fqcn.of.javascript.interface.for.webview {
                    #   public *;
                    #}

                    # Uncomment this to preserve the line number information for
                    # debugging stack traces.
                    #-keepattributes SourceFile,LineNumberTable

                    # If you keep the line number information, uncomment this to
                    # hide the original source file name.
                    #-renamesourcefileattribute SourceFile
            """.trimIndent()
            )
        }

        File("$projectFolderPath/settings.gradle.kts").apply {
            writeText(
                """
                @file:Suppress("UnstableApiUsage")

                import java.net.URI


                pluginManagement {
                    repositories {
                        google()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                    repositories {
                        google()
                        mavenCentral()
                        maven {
                            url= URI("https://jitpack.io")
                        }
                    }
                }

                rootProject.name = "$mainFileName"
                include(":library:base")
                include(":examples:cat-facts")
                include(":app")
                include(":app:${mainFileName.lowercase()}")

            """.trimIndent()
            )
        }

        val gitDir = File("$projectFolderPath/.git")
        if (gitDir.exists()) {
            gitDir.deleteRecursively()
        }


    }

    fun String.toActivityString(): String {
        return "activity_" + this.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").toLowerCase()
    }

    private fun createAndroidTestingDirectory(
        projectFolderPath: String, appName: String, packageName: String, originalPath: String, mainFileName: String
    ) {
        val packageNameList = packageName.split(".")
        val packagePath = packageNameList.joinToString("/")
        val androidTestPath = "$projectFolderPath/app/${mainFileName.lowercase()}/src/androidTest/java/"
        File(androidTestPath).mkdirs()
        val file = FileSpec.builder(packageName, "ExampleInstrumentedTest")
            .addImport("androidx.test.platform.app", "InstrumentationRegistry")
            .addImport("androidx.test.ext.junit.runners", "AndroidJUnit4").addImport("org.junit.Assert", "assertEquals")
            .addType(
                TypeSpec.classBuilder("ExampleInstrumentedTest").addAnnotation(
                    AnnotationSpec.builder(RunWith::class).addMember("AndroidJUnit4::class").build()
                ).addFunction(
                    FunSpec.builder("useAppContext").addAnnotation(Test::class).addCode(
                        CodeBlock.of(
                            "val appContext = InstrumentationRegistry.getInstrumentation().targetContext\n" + "assertEquals(\"${packageName}\", appContext.packageName)"
                        )
                    ).build()
                ).build()
            ).build()

        file.writeTo(File(androidTestPath))
    }

    private fun createTestingDirectory(
        projectFolderPath: String, appName: String, packageName: String, originalPath: String, mainFileName: String
    ) {
        val packageNameList = packageName.split(".")
        val packagePath = packageNameList.joinToString("/")
        val androidTestPath = "$projectFolderPath/app/${mainFileName.lowercase()}/src/test/java/"
        File(androidTestPath).mkdirs()
        val file =
            FileSpec.builder(packageName, "ExampleUnitTest").addImport("org.junit.Assert", "assertEquals").addType(
                TypeSpec.classBuilder("ExampleUnitTest").addFunction(
                    FunSpec.builder("addition_isCorrect").addAnnotation(Test::class).addCode(
                        CodeBlock.of(
                            "assertEquals(4, 2 + 2)\n"
                        )
                    ).build()
                ).build()
            ).build()

        file.writeTo(File(androidTestPath))
    }

    private fun deleteRepo(projectFilePath: File) {
        if (projectFilePath.exists()) {
            val deleted = projectFilePath.deleteRecursively()
            if (!deleted) {
                terminal.println("Deletion of existing files failed. Please delete manually and retry!!".showErrorCliMessage())
            } else {
                terminal.println("Deletion of existing files successful.".showSuccessCliMessage())
            }
        }
    }

    fun createFile(filepath: String, content: String) {
        File(filepath).apply {
            createNewFile()
            writeText(
                content.trimIndent()
            )
        }

    }

    fun copyDir(src: Path, dest: Path) {
        Files.walk(src).forEach {
            Files.copy(
                it, dest.resolve(src.relativize(it)), StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

}
