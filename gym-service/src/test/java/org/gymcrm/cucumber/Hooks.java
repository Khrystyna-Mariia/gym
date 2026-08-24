package org.gymcrm.cucumber;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Value;

public class Hooks {

    @Value("${local.server.port}")
    private int port;

    @Before("@component")
    public void resetState() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}