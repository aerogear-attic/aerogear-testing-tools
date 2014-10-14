package org.arquillian.spacelift.gradle

import static org.junit.Assert.*;

import org.junit.Test;
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TestReportTest {

	@Test
	public void executeTestReport() {
		Project project = ProjectBuilder.builder().build()

		project.apply plugin: 'spacelift'

		project.repositories {
			mavenCentral()
		}
				
		project.configurations {
			junitreport
		}
		
		project.dependencies {
			junitreport 'org.apache.ant:ant-junit:1.9.4'
		}

		project.spacelift {
			tests {				
			}
			tools {
			}
			profiles {
			}
			installations {
			}
		}
		
		GradleSpacelift.currentProject(project)
		
		// execute testreport task
		project.getTasks()['testreport'].execute()
		
	}
}