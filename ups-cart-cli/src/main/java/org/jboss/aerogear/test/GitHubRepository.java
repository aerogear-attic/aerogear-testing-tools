package org.jboss.aerogear.test;

import org.apache.http.HttpStatus;

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

        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException(response.getStatusLine());
        }

        String latestCommit = getLatestCommitSHA(response);

        if (latestCommit == null) {
            throw new RuntimeException("Unable to get latest commit.");
        }

        return latestCommit;
    }

    private String getLatestCommitSHA(Response response) {

        if (response != null) {
            JsonPath json = response.jsonPath();
            if (json != null) {
                return json.getString("object.sha");
            }
        }

        return null;
    }
}
