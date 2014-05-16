package org.jboss.aerogear.test

import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.ProcessResult
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assume;
import org.junit.Test
import org.junit.Assert
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.impl.CommandTool

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

class SpaceliftToolBinaryTest {

    @Test
    public void addEnvironmentPropertyToTool() {

        Assume.assumeNotNull(System.getenv("ANT_HOME"))

        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
                ant {
                    command = {
                        Tasks.prepare(CommandTool)
                            .command(new CommandBuilder("ant"))
                            .addEnvironment("ANT_HOME", System.getenv("ANT_HOME"))
                    }
                }
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find ant tool
        def antTool = GradleSpacelift.tools("ant")
        assertThat antTool, is(notNullValue())

        // call ant help
        GradleSpacelift.tools("ant").parameters("-help").interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()
    }

    @Test
    public void multipleTools() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
                ant { command = { Tasks.prepare(CommandTool).command(new CommandBuilder("ant")) }}
                mvn { command = { Tasks.prepare(CommandTool).command(new CommandBuilder("mvn")) }}
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find ant tool
        def antTool = GradleSpacelift.tools("ant")
        assertThat antTool, is(notNullValue())

        // call ant help
        GradleSpacelift.tools("ant").parameters("-help").interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()

        // find mvn tool
        def mvnTool = GradleSpacelift.tools("mvn")
        assertThat mvnTool, is(notNullValue())

        // call mvn help
        GradleSpacelift.tools("mvn").parameters("-help").interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()

    }

    @Test
    public void binaryAsString() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
                ant {
                    command = "ant"
                }
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find ant tool
        def antTool = GradleSpacelift.tools("ant")
        assertThat antTool, is(notNullValue())

        // call ant help
        GradleSpacelift.tools("ant").parameters("-help").interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()
    }

    @Test
    public void binaryAsMap() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
                ant {
                    command = [linux:"ant", windows:"ant.bat"]
                }
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find ant tool
        def antTool = GradleSpacelift.tools("ant")
        assertThat antTool, is(notNullValue())

        // call ant help
        GradleSpacelift.tools("ant").parameters("-help").interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()
    }

    @Test
    public void binaryAsClosure() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
                ant {
                    command = {
                        def antHome = System.getenv("ANT_HOME")
                        if (antHome != null && !antHome.isEmpty()) {
                            return Tasks.prepare(CommandTool).command(new CommandBuilder(antHome + "/bin/ant"))
                        } else {
                            return Tasks.prepare(CommandTool).command(new CommandBuilder("ant"))
                        }
                    }
                }
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find ant tool
        def antTool = GradleSpacelift.tools("ant")
        assertThat antTool, is(notNullValue())

        // call ant help
        ProcessResult result = GradleSpacelift.tools("ant").parameters("-help").interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()
        assertThat result.exitValue(), is(0)
    }

    @Test
    public void binaryAsMapOfClosures() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.setProperty("androidHome", "foobar")

        project.aerogearTestEnv {
            tools {
                android {
                    command = [
                        linux: {
                            Tasks.prepare(CommandTool).command(new CommandBuilder(new File(project.androidHome, "tools/android.bat").getAbsolutePath()))
                        },
                        windows: {
                            Tasks.prepare(CommandTool).command(new CommandBuilder("cmd.exe", "/C", new File(project.androidHome, "tools/android.bat").getAbsolutePath()))
                        }
                    ]
                }
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find android tool
        def antTool = GradleSpacelift.tools("android")
        assertThat antTool, is(notNullValue())
    }

    @Test
    public void binaryAsMapOfArrays() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.setProperty("androidHome", "foobar")

        project.aerogearTestEnv {
            tools {
                android {
                    command = [
                        linux: [
                            new File(project.androidHome, "tools/android.bat").getAbsolutePath()
                        ],
                        windows: [
                            "cmd.exe",
                            "/C",
                            new File(project.androidHome, "tools/android.bat").getAbsolutePath()
                        ]
                    ]
                }
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // find android tool
        def antTool = GradleSpacelift.tools("android")
        assertThat antTool, is(notNullValue())
    }
}
