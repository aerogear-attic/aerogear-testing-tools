package org.jboss.aerogear.test.android

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.ProcessInteractionBuilder
import org.arquillian.spacelift.process.impl.CommandTool
import org.jboss.aerogear.test.GradleSpacelift
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AndroidSdkUpdater extends Task<Object, Void>{
    static final Logger log = LoggerFactory.getLogger('AndroidSdk')

    private String target

    public AndroidSdkUpdater target(String target) {
        this.target = target
        this
    }

    @Override
    protected Void process(Object input) throws Exception {

        log.info("Checking installed Android target: ${target}")

        // first, check whether Android version is already installed
        def availableTargets = GradleSpacelift.tools('android').parameters([
            "list",
            "target",
            "-c"
        ])
        .interaction(new ProcessInteractionBuilder()
        .when('^(?!Error).*$').printToErr())
        .execute().await().output()

        if(availableTargets.find { "${it}" == "${target}"}) {
            log.info("There was already Android ${target} installed, skipping update")
            return
        }

        log.info("Updating Android SDK to contain version ${target}")

        // android version we need was not installed, let's install it
        // use "android list sdk --all --extended" to get what should be installed
        def androidVersion = androidVersionInteger(target)

        def exitCodes = 1..255

        GradleSpacelift.tools('android').parameters([
            "update",
            "sdk",
            "--filter",
            //
            // The version of build-tools is statically set to 19.0.1 - the latest one by 8/1/2014.
            // Investigate, how to get the latest build-tools version programmatically.
            //
            // Backed by JIRA: https://issues.jboss.org/browse/MP-209
            //
            "platform-tool,android-${androidVersion},sysimg-${androidVersion},addon-google_apis-google-${androidVersion},addon-google_apis_x86-google-${androidVersion},build-tools-19.0.3",
            "--all",
            "--no-ui"]
        ).interaction(new ProcessInteractionBuilder()
        .outputPrefix("")
        .when('^Do you accept the license.*: ').replyWith('y')
        .when('^\\s*Done.*installed\\.$').terminate()
        .when('^(?!Error).*$').printToOut()
        .when('^!Error.*$').printToErr()
        )
        .shouldExitWith(0, exitCodes.toArray(new Integer[0]))
        .execute().await()

        log.info("Android SDK was updated.")
    }

    Integer androidVersionInteger(androidTarget) {

        if(androidTarget.isInteger()) {
            return androidTarget.toInteger()
        }

        def androidVersionCandidates = androidTarget.tokenize('-')
        if(androidVersionCandidates.last().isInteger()) {
            return androidVersionCandidates.last().toInteger()
        }

        androidVersionCandidates = androidTarget.tokenize(':')
        if(androidVersionCandidates.last().isInteger()) {
            return androidVersionCandidates.last().toInteger()
        }

        throw new IllegalArgumentException("Android target expected in form android-XYZ or Google Inc.:Google APIs:XYZ")
    }
}
