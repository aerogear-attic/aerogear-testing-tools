package org.jboss.aerogear.test.cli;

import io.airlift.command.Command;

import java.util.logging.Logger;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@Command(name = "cleanup", description = "Deletes all data from an UPS instance")
public class CleanupCommand extends AbstractOpenShiftCommand {

    private static final Logger log = Logger.getLogger(CleanupCommand.class.getName());

    @Override
    public void run() {
        Response response = RestAssured.given().
                baseUri(getUnifiedpushTestExtensionUri()).
                get("/cleanup/applications");
        
        log.info(response.prettyPrint());
    }

}