package org.gymcrm.workload.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadApiSteps {

    private final TestServiceTokenFactory tokenFactory;
    private Response lastResponse;

    public WorkloadApiSteps(TestServiceTokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    @Given("trainer {string} has no existing workload record")
    public void trainerHasNoRecord(String username) {
    }

    @Given("trainer {string} {string} {string} already has {int} minutes recorded for year {int} month {int}")
    public void trainerHasExistingMinutes(String username, String firstName, String lastName,
                                          int minutes, int year, int month) {
        submitEvent(username, firstName, lastName, "2026-" + pad(month) + "-01", minutes, "ADD",
                tokenFactory.validToken());
        assertThat(lastResponse.statusCode()).isEqualTo(200);
    }

    @When("a training {word} event of {int} minutes on {string} is submitted for trainer {string} {string} {string} who is active")
    public void submitTrainingEvent(String action, int duration, String date, String username,
                                    String firstName, String lastName) {
        submitEvent(username, firstName, lastName, date, duration, action, tokenFactory.validToken());
    }

    @When("an unauthenticated training {word} event of {int} minutes on {string} is submitted for trainer {string} {string} {string} who is active")
    public void submitUnauthenticatedEvent(String action, int duration, String date, String username,
                                           String firstName, String lastName) {
        submitEvent(username, firstName, lastName, date, duration, action, null);
    }

    @When("a training {word} event of {int} minutes on {string} with an invalid service token is submitted for trainer {string} {string} {string} who is active")
    public void submitEventWithInvalidToken(String action, int duration, String date, String username,
                                            String firstName, String lastName) {
        submitEvent(username, firstName, lastName, date, duration, action, tokenFactory.invalidToken());
    }

    @When("trainer summaries are searched by first name {string} and last name {string}")
    public void searchByName(String firstName, String lastName) {
        lastResponse = authenticated()
                .queryParam("firstName", firstName)
                .queryParam("lastName", lastName)
                .when()
                .get("/api/v1/workload/search");
    }

    @When("the workload summary for trainer {string} is requested")
    public void requestSummary(String username) {
        lastResponse = authenticated()
                .when()
                .get("/api/v1/workload/{username}", username);
    }

    @Then("the response status is {int}")
    public void assertStatus(int status) {
        assertThat(lastResponse.statusCode()).isEqualTo(status);
    }

    @Then("the workload summary for trainer {string} shows {int} minutes for year {int} month {int}")
    public void assertSummary(String username, int expectedMinutes, int year, int month) {
        Response response = authenticated()
                .when()
                .get("/api/v1/workload/{username}", username);

        assertThat(response.statusCode()).isEqualTo(200);
        Integer actual = response.jsonPath().getInt(
                "years.find { it.year == " + year + " }.months.find { it.month == " + month + " }.summaryDuration");
        assertThat(actual).as("summary duration for %s / %d-%d", username, year, month).isEqualTo(expectedMinutes);
    }

    @Then("the search results include trainer {string}")
    public void assertSearchIncludes(String username) {
        List<String> usernames = lastResponse.jsonPath().getList("trainerUsername", String.class);
        assertThat(usernames).contains(username);
    }

    private void submitEvent(String username, String firstName, String lastName,
                             String date, int duration, String action, String token) {
        RequestSpecification request = given().contentType(ContentType.JSON);
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }

        String body = """
                {
                  "trainerUsername": "%s",
                  "trainerFirstName": "%s",
                  "trainerLastName": "%s",
                  "isActive": true,
                  "trainingDate": "%s",
                  "trainingDuration": %d,
                  "actionType": "%s"
                }
                """.formatted(username, firstName, lastName, date, duration, action);

        lastResponse = request.body(body).when().post("/api/v1/workload");
    }

    private RequestSpecification authenticated() {
        return given().header("Authorization", "Bearer " + tokenFactory.validToken());
    }

    private static String pad(int month) {
        return month < 10 ? "0" + month : String.valueOf(month);
    }
}