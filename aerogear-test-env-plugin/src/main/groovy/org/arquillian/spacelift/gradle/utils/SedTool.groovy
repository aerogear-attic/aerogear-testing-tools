package org.arquillian.spacelift.gradle.utils

import java.io.File

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.Command
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.impl.CommandTool

/**
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class SedTool extends Task<File, Void> {

    private File file

    private boolean global = true

    private String replace

    private String replaceWith

    def SedTool file(File file) {
        this.file = file
        this
    }

    def SedTool file(String file) {
        file(new File(file))
    }

    /**
     * When called, it will replace matched occurrences only for the first string on a line, ignoring the rest.
     * 
     * By default, it is replaced globally.
     * 
     * @return this
     */
    def SedTool firstOccurrence() {
        global = false
        this
    }

    /**
     * 
     * @param replace regular expression
     * @return this
     */
    def SedTool replace(String replace) {
        this.replace = replace
        this
    }

    /**
     * 
     * @param replaceWith replacement string
     * @return
     */
    def SedTool replaceWith(String replaceWith) {
        this.replaceWith = replaceWith
        this
    }

    @Override
    protected Void process(File input) throws Exception {

        if (EnvironmentUtils.runsOnWindows()) {
            throw new UnsupportedOperationException("Spacelift SED tool is not available for Windows machines yet.")
        }

        if (file == null && input == null) {
            throw new IllegalStateException("Please chain or specify a file to operate on.")
        }

        if (file == null && input != null) {
            file = input
        }

        validate(file)

        Command sedCommand = getSedCommand()
        
        Tasks.prepare(CommandTool).command(sedCommand).execute().await()

        null
    }

    private Command getSedCommand() {

        def sb = new StringBuilder()

        sb.append("s/").append(replace).append("/").append(replaceWith).append("/")

        if (global) {
            sb.append("g") // replace it globally or just the first occurrence on a line?
        }

        // -i for inplace replacement (in the same file)
        def sedBuilder = new CommandBuilder("sed").parameter("-i").parameter(sb.toString()).parameter(file.getAbsolutePath())

        sedBuilder.build()
    }

    private void validate(File file) {
        if (file == null) {
            throw new IllegalStateException("You have to specify a file to operate on.")
        }

        if (!file.exists()) {
            throw new IllegalStateException("You have specified file to operate on which does not exist: " + file.getAbsolutePath())
        }

        if (!file.canWrite()) {
            throw new IllegalStateException("You do not have write permissions to a file you specify: " + file.getAbsolutePath())
        }
    }
}
