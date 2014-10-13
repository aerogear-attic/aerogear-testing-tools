package org.arquillian.spacelift.gradle.android

import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.gradle.GradleSpacelift;
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

class AndroidSdkOptForStats extends Task {
    private static final Logger log = LoggerFactory.getLogger('AndroidSdk')

    @Override
    protected Object process(Object input) throws Exception {

        def androidSdkHomes = [
            androidSdkHome().getAbsolutePath()
        ]

        androidSdkHomes.each { androidSdkHome ->
            if(androidSdkHome) {
                log.info("Opting out for statistics coverage in ${androidSdkHome}/.android")
                try {
                    def dir = new File(new File(androidSdkHome), ".android")
                    dir.mkdirs()
                    // on purpose, we are destroying previous content of the file
                    new File(dir, "ddms.cfg").withWriter { writer ->
                        writer.println("pingOptIn=false")
                    }
                }
                catch (IOException e) {
                    // ignore this
                }
            }
        }

        return null

    }

    // ANDROID_SDK_HOME
    // in this directory where .android is created
    File androidSdkHome() {
        return GradleSpacelift.currentProject().spacelift.workspace
    }

}
