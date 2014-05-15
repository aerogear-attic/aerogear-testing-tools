package org.jboss.aerogear.test.utils

import org.apache.commons.lang.SystemUtils
import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.impl.CommandTool
import org.slf4j.LoggerFactory

/**
 * Helper class that will clean up environment by killing all processes that might collide with test execution
 */
class KillJavas extends Task<Object, Void>{
    def static final log = LoggerFactory.getLogger('Killer')

    // definition of what would be killed
    def processNames = [
        "org.jboss.Main",
        "org.jboss.as",
        "jboss-modules.jar",
        "surefire",
        "Selenium",
        "selenium-server-standalone"
    ]

    def processPorts = [4444, 14444, 8080, 9999]


    @Override
    protected Void process(Object input) throws Exception {
        try {
            ["-SIGTERM", "-9"].each { signal ->
                def totalKilled = 0

                println "Sending kill ${signal} to all java processes named ${processNames}"
                processNames.each { proc ->
                    totalKilled += jpsKill(proc, signal)
                }

                println "Sending kill ${signal} to all processes listening on ports ${processPorts}"
                processPorts.each { port ->
                    totalKilled += netstatKill(port, signal)
                }

                // wait for process to finish if not forced kill
                if(signal == "-SIGTERM" && totalKilled) {
                    sleep 10000
                }
            }
        }
        catch (Throwable e) {
            println "Cleanup environment failed with ${e.getMessage()}"
            e.printStackTrace()
        }
    }

    def executeBash(String command) {
        Tasks.prepare(CommandTool).programName("bash").parameters("-c").splitToParameters(command).execute().await().output()
    }

    def executeCmd(String command) {
        Tasks.prepare(CommandTool).programName("cmd").parameters("/C").splitToParameters(command).execute().await().output()
    }

    def jpsKill(String name, String signal) {
        def JPS_PATTERN = java.util.regex.Pattern.compile('^([0-9]+).*?' + name + '.*$')

        def pids = executeBash("jps -l").inject(new ArrayList()) { list, line ->
            def m = line =~ JPS_PATTERN
            if(m) {
                list << m[0][1]
            }
        }

        def totalKilled = pids.inject(0) {total, pid ->
            kill(pid, signal)
            total++
        }

        totalKilled
    }

    def netstatKill(int port, String signal) {
        def NETSTAT_PATTERN = java.util.regex.Pattern.compile('^.*:' + port + ' .*?([0-9]+)/.*$')

        // for some reason injecting [] does not work
        def pids = executeBash("netstat -a -n -p").inject(new ArrayList()) { list, line ->
            def m = line =~ NETSTAT_PATTERN
            if(m) {
                list << m[0][1]
            }
        }

        def totalKilled = pids.inject(0) {total, pid ->
            kill(pid, signal)
            total++
        }

        totalKilled
    }

    def kill(String pid, String signal) {

        if (SystemUtils.IS_OS_WINDOWS) {
            executeBash("kill ${signal} ${pid}")
        }
        else {
            executeCmd("taskkill /F /T /PID ${pid}")
        }

        println "Killed ${pid}"
    }
}