package org.jboss.aerogear.test;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.utils.Session;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class GitHubRepository {

    private final String organization;
    private final String repository;

    public GitHubRepository(String organization, String repository) {
        this.organization = organization;
        this.repository = repository;
    }

    public String getLastestCommit(String branch) {

        Response response = Session.newSession("https://api.github.com")
            .given()
            .get("/repos/{organization}/{repository}/git/refs/heads/{branch}",
                organization,
                repository,
                branch);

        // FIXME needs better handling
        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException(response.getStatusLine());
        }

        JsonPath json = response.jsonPath();
        // FIXME needs better handling
        return json.getString("object.sha");
    }
}
