package org.gymcrm.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class GymServiceApiSteps {

    private final SessionFactory sessionFactory;

    private Response lastResponse;
    private String currentUsername;
    private String currentPassword;
    private String currentToken;
    private String secondUsername;
    private String secondPassword;
    private Long fitnessTrainingTypeId;

    public GymServiceApiSteps(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Given("a {string} training type exists")
    public void ensureTrainingTypeExists(String typeName) {
        TrainingTypeEnum typeEnum = TrainingTypeEnum.valueOf(typeName);
        try (Session session = sessionFactory.openSession()) {
            TrainingType existing = session.createQuery(
                            "from TrainingType where trainingTypeName = :name", TrainingType.class)
                    .setParameter("name", typeEnum)
                    .uniqueResult();
            if (existing != null) {
                fitnessTrainingTypeId = existing.getId();
                return;
            }

            Transaction tx = session.beginTransaction();
            TrainingType type = new TrainingType();
            type.setTrainingTypeName(typeEnum);
            session.persist(type);
            tx.commit();
            fitnessTrainingTypeId = type.getId();
        }
    }

    @Given("a trainee is registered with first name {string} and last name {string}")
    public void registerTrainee(String firstName, String lastName) {
        Response response = postRegistration("/api/v1/trainees", traineeBody(firstName, lastName, null, null));
        assertThat(response.statusCode()).isEqualTo(201);
        currentUsername = response.jsonPath().getString("username");
        currentPassword = response.jsonPath().getString("password");
        lastResponse = response;
    }

    @When("a trainee registration is submitted with a blank first name")
    public void registerTraineeWithBlankFirstName() {
        lastResponse = postRegistration("/api/v1/trainees", traineeBody("", "Blank", null, null));
    }

    @Given("a trainer is registered with first name {string} and last name {string} specializing in {string}")
    public void registerTrainer(String firstName, String lastName, String specialization) {
        lastResponse = postRegistration("/api/v1/trainers", trainerBody(firstName, lastName, fitnessTrainingTypeId));
        assertThat(lastResponse.statusCode()).isEqualTo(201);
        secondUsername = lastResponse.jsonPath().getString("username");
        secondPassword = lastResponse.jsonPath().getString("password");
        if (currentUsername == null) {
            currentUsername = secondUsername;
            currentPassword = secondPassword;
        }
    }

    @When("a trainer is registered with first name {string} and last name {string} specializing in unknown training type {long}")
    public void registerTrainerWithUnknownSpecialization(String firstName, String lastName, long unknownId) {
        lastResponse = postRegistration("/api/v1/trainers", trainerBody(firstName, lastName, unknownId));
    }

    @Given("they are logged in")
    @Given("the trainee is logged in")
    @Given("the trainer is logged in")
    public void logIn() {
        Response response = postLogin(currentUsername, currentPassword);
        assertThat(response.statusCode()).isEqualTo(200);
        currentToken = response.jsonPath().getString("token");
    }

    @When("they log in with their generated credentials")
    public void logInWithGeneratedCredentials() {
        lastResponse = postLogin(currentUsername, currentPassword);
        if (lastResponse.statusCode() == 200) {
            currentToken = lastResponse.jsonPath().getString("token");
        }
    }

    @When("they log in with the wrong password")
    public void logInWithWrongPassword() {
        lastResponse = postLogin(currentUsername, currentPassword + "-wrong");
    }

    @When("they attempt to log in with the wrong password {int} times")
    public void attemptLoginMultipleTimes(int attempts) {
        for (int i = 0; i < attempts; i++) {
            lastResponse = postLogin(currentUsername, currentPassword + "-wrong");
        }
    }

    @When("they log out")
    public void logOut() {
        lastResponse = given()
                .header("Authorization", "Bearer " + currentToken)
                .when()
                .post("/api/v1/auth/logout");
    }

    @When("they request their own profile")
    public void requestOwnProfile() {
        lastResponse = authenticated(currentToken)
                .when()
                .get("/api/v1/trainees/{username}", currentUsername);
    }

    @When("they request their own profile using that same, now blacklisted, token")
    public void requestOwnProfileWithBlacklistedToken() {
        requestOwnProfile();
    }

    @When("they request the profile of trainee {string}")
    public void requestProfileOf(String username) {
        lastResponse = authenticated(currentToken)
                .when()
                .get("/api/v1/trainees/{username}", username);
    }

    @When("an unauthenticated request is made to get the profile of trainee {string}")
    public void requestProfileUnauthenticated(String username) {
        lastResponse = given()
                .when()
                .get("/api/v1/trainees/{username}", username);
    }

    @When("a request with an invalid token is made to get the profile of trainee {string}")
    public void requestProfileWithInvalidToken(String username) {
        lastResponse = authenticated("this.is.not-a-valid-jwt")
                .when()
                .get("/api/v1/trainees/{username}", username);
    }

    @When("they update their profile address to {string}")
    public void updateAddress(String newAddress) {
        String body = """
                {
                  "username": "%s",
                  "firstName": "Cuke",
                  "lastName": "Update",
                  "address": "%s",
                  "isActive": true
                }
                """.formatted(currentUsername, newAddress);

        lastResponse = authenticated(currentToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/v1/trainees/{username}", currentUsername);
    }

    @When("they deactivate their own account")
    public void deactivateOwnAccount() {
        String body = """
                {
                  "username": "%s",
                  "isActive": false
                }
                """.formatted(currentUsername);

        lastResponse = authenticated(currentToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch("/api/v1/trainees/{username}/status", currentUsername);
    }

    @When("a training session of {int} minutes is added on {string} between the trainee and the trainer")
    public void addTrainingSession(int duration, String date) {
        lastResponse = authenticated(currentToken)
                .contentType(ContentType.JSON)
                .body(trainingBody(currentUsername, secondUsername, date, duration))
                .when()
                .post("/api/v1/trainings");
    }

    @When("a training session is added for unknown trainee {string}")
    public void addTrainingSessionForUnknownTrainee(String unknownTraineeUsername) {
        lastResponse = authenticated(currentToken)
                .contentType(ContentType.JSON)
                .body(trainingBody(unknownTraineeUsername, secondUsername, "2026-09-15", 45))
                .when()
                .post("/api/v1/trainings");
    }

    @Then("the response status is {int}")
    public void assertStatus(int status) {
        assertThat(lastResponse.statusCode()).isEqualTo(status);
    }

    @Then("the response indicates the caller is not authorized")
    public void assertNotAuthorized() {
        assertThat(lastResponse.statusCode()).isIn(401, 403);
    }

    @Then("a JWT access token is returned")
    public void assertTokenReturned() {
        String token = lastResponse.jsonPath().getString("token");
        assertThat(token).isNotBlank();
    }

    @Then("a username and password are returned")
    public void assertCredentialsReturned() {
        assertThat(lastResponse.jsonPath().getString("username")).isNotBlank();
        assertThat(lastResponse.jsonPath().getString("password")).isNotBlank();
    }

    @Then("their profile address is {string}")
    public void assertProfileAddress(String expectedAddress) {
        Response response = authenticated(currentToken)
                .when()
                .get("/api/v1/trainees/{username}", currentUsername);
        assertThat(response.jsonPath().getString("address")).isEqualTo(expectedAddress);
    }

    private Response postRegistration(String path, String body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }

    private Response postLogin(String username, String password) {
        String body = """
                { "username": "%s", "password": "%s" }
                """.formatted(username, password);
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v1/auth/login");
    }

    private RequestSpecification authenticated(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

    private String traineeBody(String firstName, String lastName, String dateOfBirth, String address) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s"
                }
                """.formatted(firstName, lastName);
    }

    private String trainerBody(String firstName, String lastName, Long specializationId) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "specializationId": %d
                }
                """.formatted(firstName, lastName, specializationId);
    }

    private String trainingBody(String traineeUsername, String trainerUsername, String date, int duration) {
        return """
                {
                  "traineeUsername": "%s",
                  "trainerUsername": "%s",
                  "trainingName": "Cucumber Training",
                  "trainingDate": "%s",
                  "trainingDuration": %d
                }
                """.formatted(traineeUsername, trainerUsername, date, duration);
    }
}