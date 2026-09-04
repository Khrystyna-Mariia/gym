package org.gymcrm.workload.cucumber;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

public class Hooks {

    @Value("${local.server.port}")
    private int port;

    private final MongoTemplate mongoTemplate;

    public Hooks(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Before
    public void resetState() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        mongoTemplate.getCollection("trainer_workload").deleteMany(new Document());
    }
}