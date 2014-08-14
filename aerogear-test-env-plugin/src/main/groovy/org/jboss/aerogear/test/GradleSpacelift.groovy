package org.jboss.aerogear.test

import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory
import org.arquillian.spacelift.process.ProcessInteraction
import org.arquillian.spacelift.process.ProcessInteractionBuilder
import org.arquillian.spacelift.tool.Tool
import org.arquillian.spacelift.tool.ToolRegistry
import org.arquillian.spacelift.tool.impl.ToolRegistryImpl
import org.gradle.api.Project

/**
 * A 'singleton' that holds current project. This allows Task to inject project instance
 * and reference other variables defines by Project.
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class GradleSpacelift {

    // FIXME enabling DOTALL pattern - https://issues.jboss.org/browse/ARQ-1822
    public static final ProcessInteraction ECHO_OUTPUT = new ProcessInteractionBuilder().outputPrefix("").when("(?s).*").printToOut().build()

    private static final class ProjectHolder {
        private static Project project;
        private static ToolRegistry tools;
    }

    public static void currentProject(Project project) {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory())
        ProjectHolder.project = project;
        ProjectHolder.tools = new ToolRegistryImpl()
        // register all default tools here
        project.aerogearTestEnv.tools.each { tool ->
            tool.registerInSpacelift(ProjectHolder.tools)
        }
    }

    public static Project currentProject() {
        if(ProjectHolder.project==null) {
            throw new IllegalStateException("Current project was not set via plugin.")
        }
        return ProjectHolder.project;
    }

    public static ToolRegistry toolRegistry() {
        return ProjectHolder.tools;
    }

    public static Tool tools(alias) {
        if(ProjectHolder.tools==null) {
            throw new IllegalStateException("Current project was not set via plugin.")
        }
        return ProjectHolder.tools.find(alias);
    }
}
