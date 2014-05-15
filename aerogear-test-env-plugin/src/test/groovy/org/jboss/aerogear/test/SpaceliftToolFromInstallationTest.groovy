package org.jboss.aerogear.test;

import org.arquillian.spacelift.execution.Tasks
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.aerogear.test.android.AndroidSdkOptForStats
import org.jboss.aerogear.test.arquillian.ArquillianXmlUpdater
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

public class SpaceliftToolFromInstallationTest {

    @Test
    public void installAndroidSDKAndProvideTool() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.ext.set("androidTargets", ["19"])

        project.aerogearTestEnv {
            workspace = new File(System.getProperty("user.dir"), "workspace")
            installations {
                androidSdk {
                    product = 'aerogear'
                    version = '1.0.0'
                    remoteUrl = [linux:"http://dl.google.com/android/android-sdk_r22.6.2-linux.tgz", windows:"http://dl.google.com/android/android-sdk_r22.6.2-windows.zip", mac:"http://dl.google.com/android/android-sdk_r22.6.2-macosx.zip"]
                    fileName = [linux:"android-sdk_r22.6.2-linux.tgz", windows:"android-sdk_r22.6.2-windows.zip", mac:"android-sdk_r22.6.2-macosx.zip"]
                    home = [linux:"android-sdk-linux", windows:"android-sdk-windows", mac:"android-sdk-macosx"]
                    // tools provided by installation
                    tool {
                        name ="android"
                        command = [
                            linux: ["${home}/tools/android"],
                            windows: [
                                "cmd.exe",
                                "/C",
                                "${home}/tools/android.bat"
                            ]
                        ]
                    }
                    // actions performed after extraction
                    postActions {
                        // fix executable flags
                        project.ant.chmod(dir: "${home}/tools", perm:"a+x", includes:"*", excludes:"*.txt")

                        // update Android SDK, download / update each specified Android SDK version
                        //project.androidTargets.each { v ->
                        //    Tasks.prepare(AndroidSdkUpdater).target(v).execute().await()
                        //}

                        // opt out for stats
                        Tasks.prepare(AndroidSdkOptForStats).execute().await()

                        // update arquillian.xml files with Android homes
                        Tasks.chain([
                            androidHome: "${home}",
                            androidSdkHome: "${project.aerogearTestEnv.workspace}"
                        ], ArquillianXmlUpdater).dir(project.aerogearTestEnv.workspace).container('android').execute().await()
                    }
                }
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        project.aerogearTestEnv.installations.each { installation ->  installation.install() }

        // find android tool
        def androidTool = GradleSpacelift.tools("android")
        assertThat "Android tool is available after installation", androidTool, is(notNullValue())
    }
}
