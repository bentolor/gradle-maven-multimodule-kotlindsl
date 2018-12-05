import org.gradle.jvm.tasks.Jar

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    group = "de.bentolor.sampleproject"
    version = "0.1.0"

    //val subProjectDir = this.projectDir

    repositories {
        jcenter()
    }

    dependencies {
        // Dependencies used in EVERY module
        // Production deps
        "compile"("commons-logging:commons-logging:1.2")
        "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")
        "compileOnly"("com.google.code.findbugs:findbugs-annotations:3.0.1")

        // Test deps
        "testImplementation"("junit:junit:4.12")
        "testImplementation"("org.jmock:jmock-junit4:2.9.0")
        "testImplementation"("org.jmock:jmock-legacy:2.9.0")
        "testCompileOnly"("com.google.code.findbugs:jsr305:3.0.2")
        "testCompileOnly"("com.google.code.findbugs:findbugs-annotations:3.0.1")
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

        /*register("sourcesJar", Jar::class.java) {
            from(sourceSets.main.get().allJava)
            classifier = "sources"
        }*/

        /*val sourcesJar by creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            classifier = "sources"
            from(sourceSets["main"].allSource)
        }*/

        val javadocJar = creating(org.gradle.api.tasks.bundling.Jar::class) {
            dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
            val javadoc by this@tasks
            classifier = "javadoc"
            from(javadoc)
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
            }
        }

        repositories {
            mavenLocal()
        }

        artifacts {
            //add("archives", sourcesJar)
            //add("archives", javadocJar)
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
