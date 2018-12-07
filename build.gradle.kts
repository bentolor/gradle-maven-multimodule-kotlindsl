@file:Suppress("UNUSED_VARIABLE") // needed to declare task names as variables

import org.gradle.jvm.tasks.Jar

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()
    group = "de.bentolor.sampleproject"
    version = "0.1.0"

    //val subProjectDir = this.projectDir

    repositories {
        jcenter()
    }

    dependencies {
        // Avoid the otherwise necessary "implementation"("…")
        val implementation by configurations
        val compileOnly by configurations
        val testImplementation by configurations
        val testCompile by configurations

        // Dependencies used in EVERY module
        // Production deps
        implementation("commons-logging:commons-logging:1.2")
        compileOnly("com.google.code.findbugs:jsr305:3.0.2")
        compileOnly("com.google.code.findbugs:findbugs-annotations:3.0.1")

        // Test deps
        testImplementation("junit:junit:4.12")
        testImplementation("org.jmock:jmock-junit4:2.9.0")
        testImplementation("org.jmock:jmock-legacy:2.9.0")
        testImplementation("com.google.code.findbugs:jsr305:3.0.2")
        testCompile("com.google.code.findbugs:findbugs-annotations:3.0.1")

    }

    tasks {
        // i18ngenerator
        //   Declare and use the Ant I18N Task Generator-Task
        /*register("i18ngenerator") {
            group = "de.bentolor.toolbox.i18n.generator"
            doLast {
                ant.withGroovyBuilder {
                    "taskdef"("name" to "i18ngenerator",
                            "classname" to "de.bentolor.toolbox.i18n.generator.GeneratorTask",
                    "i18ngenerator"(
                            "configfile" to "$subProjectDir/etc/config.rexx",
                            "targetdir" to "$subProjectDir/build/src/i18ngenerator/java",
                            "resourcedir" to "$subProjectDir/src/main/resources"
                    )
                }
            }
        }*/

        // declare a common "-source.jar"-Task. Needed i.e. for Maven publishing
        val sourcesJar by creating(Jar::class) {
            val sourceSets: SourceSetContainer by project
            from(sourceSets["main"].allJava)
            classifier = "sources"
        }

        val javadoc by getting(Javadoc::class)
        val javadocJar by creating(Jar::class) {
            from(javadoc)
            classifier = "javadoc"
        }

    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])

                // If you configured them before
                val sourcesJar by tasks.getting(Jar::class)
                val javadocJar by tasks.getting(Jar::class)

                artifact(sourcesJar)
                artifact(javadocJar)
            }
        }

        repositories {
            mavenLocal()
        }
    }
}

/*configure(subprojects.filterNot { it.name == "core-toolbox" }) {
    // Add the output directory of the `i18ngenerator` task to all subprojects
    val mainSourceSet = extensions.getByType(SourceSetContainer::class.java).getByName("main")
    mainSourceSet.java.srcDir("build/src/i18ngenerator/java")

    // depend Gradles compileJava on our generator task
    tasks.withType<JavaCompile> { dependsOn("i18ngenerator") }
}*/
