package org.gymcrm.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class IntegrationSteps {

    private static final String SHARED_SERVICE_JWT_SECRET = "1fZxu+Aap4l/FQda9j+AyFPZ4rhWEpSpsHshfliipgU=";

    private String gymServiceBaseUrl;
    private String workloadServiceBaseUrl;

    private String traineeUsername;
    private String trainerUsername;
    private String traineeToken;
    private Response lastWorkloadResponse;

    @Before("@integration")
    public void configureBaseUrls() {
        gymServiceBaseUrl = System.getProperty("gym-service.base-url", "http://localhost:8080");
        workloadServiceBaseUrl = System.getProperty("workload-service.base-url", "http://localhost:8082");
    }

    @Given("a trainee and a trainer are registered in gym-service")
    public void registerTraineeAndTrainer() {
        String suffix = String.valueOf(System.nanoTime());

        Response traineeResponse = given()
                .baseUri(gymServiceBaseUrl)
                .contentType(ContentType.JSON)
                .body("""
                        { "firstName": "Integration", "lastName": "Trainee%s" }
                        """.formatted(suffix))
                .when()
                .post("/api/v1/trainees");
        assertThat(traineeResponse.statusCode()).isEqualTo(201);
        traineeUsername = traineeResponse.jsonPath().getString("username");
        String traineePassword = traineeResponse.jsonPath().getString("password");

        Response loginResponse = given()
                .baseUri(gymServiceBaseUrl)
                .contentType(ContentType.JSON)
                .body("""
                        { "username": "%s", "password": "%s" }
                        """.formatted(traineeUsername, traineePassword))
                .when()
                .post("/api/v1/auth/login");
        assertThat(loginResponse.statusCode()).isEqualTo(200);
        traineeToken = loginResponse.jsonPath().getString("token");

        long specializationId = given()
                .baseUri(gymServiceBaseUrl)
                .header("Authorization", "Bearer " + traineeToken)
                .when()
                .get("/api/v1/training-types")
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].trainingTypeId");

        Response trainerResponse = given()
                .baseUri(gymServiceBaseUrl)
                .contentType(ContentType.JSON)
                .body("""
                        { "firstName": "Integration", "lastName": "Trainer%s", "specializationId": %d }
                        """.formatted(suffix, specializationId))
                .when()
                .post("/api/v1/trainers");
        assertThat(trainerResponse.statusCode()).isEqualTo(201);
        trainerUsername = trainerResponse.jsonPath().getString("username");
    }

    @When("a training session of {int} minutes on {string} is added between them in gym-service")
    public void addTrainingSession(int duration, String date) {
        Response response = given()
                .baseUri(gymServiceBaseUrl)
                .header("Authorization", "Bearer " + traineeToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "traineeUsername": "%s",
                          "trainerUsername": "%s",
                          "trainingName": "Integration Training",
                          "trainingDate": "%s",
                          "trainingDuration": %d
                        }
                        """.formatted(traineeUsername, trainerUsername, date, duration))
                .when()
                .post("/api/v1/trainings");

        assertThat(response.statusCode()).isEqualTo(201);
    }

    @Then("trainer-workload-service eventually reports {int} minutes for that trainer in year {int} month {int}")
    public void assertWorkloadEventuallyReflectsTraining(int expectedMinutes, int year, int month) {
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(workloadServiceBaseUrl)
                            .header("Authorization", "Bearer " + mintServiceToken())
                            .when()
                            .get("/api/v1/workload/{username}", trainerUsername);

                    assertThat(response.statusCode()).isEqualTo(200);
                    Integer actual = response.jsonPath().getInt("years.find { it.year == "
                            + year + " }.months.find { it.month == " + month + " }.summaryDuration");
                    assertThat(actual).isEqualTo(expectedMinutes);
                });
    }

    @When("trainer-workload-service is queried directly without a service token")
    public void queryWorkloadServiceWithoutToken() {
        lastWorkloadResponse = given()
                .baseUri(workloadServiceBaseUrl)
                .when()
                .get("/api/v1/workload/anyone");
    }

    @Then("trainer-workload-service responds with an unauthorized status")
    public void assertWorkloadServiceUnauthorized() {
        assertThat(lastWorkloadResponse.statusCode()).isEqualTo(401);
    }

    private static String mintServiceToken() {
        SecretKey key = Keys.hmacShaKeyFor(SHARED_SERVICE_JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject("gym-service")
                .claim("type", "service")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(key)
                .compact();
    }
}