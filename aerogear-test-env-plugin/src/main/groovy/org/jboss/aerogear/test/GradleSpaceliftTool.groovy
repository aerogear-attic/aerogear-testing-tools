package org.jboss.aerogear.test

import org.apache.commons.lang3.SystemUtils
import org.apache.log4j.Logger
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.impl.CommandTool
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.tool.ToolRegistry
import org.gradle.api.Project

/**
 * A tool that can be dynamically registered in Spacelift in Gradle build
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class GradleSpaceliftTool {
    private static final Logger logger = Logger.getLogger("Spacelift")

    // required by gradle to be defined
    String name

    // prepared command tool
    def command

    private Project project

    GradleSpaceliftTool(String toolName, Project project) {
        this.name = toolName
        this.project = project
    }

    GradleSpaceliftTool(Project project) {
        this.project = project
    }


    void registerInSpacelift(ToolRegistry registry, Object delegate) {

        // dynamically construct class that represent the tool
        def toolClass = """
            import org.arquillian.spacelift.process.CommandBuilder
            import org.arquillian.spacelift.process.impl.CommandTool
            import java.util.Map;

            class ToolBinary_${name} extends org.arquillian.spacelift.process.impl.CommandTool {

                static CommandTool commandTool

                ToolBinary_${name}() {
                    if (commandTool != null) {
                        // set environment
                        for (Map.Entry<String, String> entry : commandTool.environment) {
                            super.environment.put(entry.getKey(), entry.getValue());
                        }
                        // set command builder
                        super.commandBuilder = commandTool.commandBuilder;
                    }
                }

                protected java.util.Collection aliases() {
                    return ["${name}"]
                }
            }
        """

        def classLoader = new GroovyClassLoader()
        def clazz = classLoader.parseClass(toolClass)
        def instance = clazz.newInstance()

        // FIXME here we access static fields via instance as we don't have class object
        instance.commandTool = getOsSpecificCommand(command, delegate)

        // register tool using dynamically constructed class
        registry.register(clazz)

        logger.info("Tool ${name} was registered")
    }

    // get command tool
    CommandTool getOsSpecificCommand(mapClosureOrCollection, delegate) {

        // if this is a closure, execute it
        if(mapClosureOrCollection instanceof Closure) {
            mapClosureOrCollection.delegate = this
            return mapClosureOrCollection.doCall()
        }
        // if this is a single value, just return it
        else if(mapClosureOrCollection instanceof Map) {
            // expecting we have a map here

            // try to figure out value given the family
            if(SystemUtils.IS_OS_WINDOWS) {
                return getOsSpecificCommand(mapClosureOrCollection['windows'], delegate)
            }
            else if(SystemUtils.IS_OS_MAC_OSX) {
                return getOsSpecificCommand(mapClosureOrCollection['mac'], delegate)
            }
            else if(SystemUtils.IS_OS_LINUX) {
                return getOsSpecificCommand(mapClosureOrCollection['linux'], delegate)
            }
            else {
                throw new IllegalStateException("Unknown system ${System.getProperty('os.name')}")
            }
        }
        else if(mapClosureOrCollection instanceof Collection) {
            CommandBuilder command = new CommandBuilder(mapClosureOrCollection[0].toString())
            mapClosureOrCollection.eachWithIndex { param, i ->
                if(i!=0) {
                    command.parameters(param.toString())
                }
            }
            return Tasks.prepare(CommandTool).command(command)
        }

        return Tasks.prepare(CommandTool).command(new CommandBuilder(mapClosureOrCollection));
    }

    @Override
    public String toString() {
        return "SpaceliftTool ${name}, commandTool: ${getOsSpecificCommand(commandTool)}"
    }
}
