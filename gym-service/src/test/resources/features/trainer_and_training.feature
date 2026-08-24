Feature: Trainer registration and training sessions

  Background:
    Given a "FITNESS" training type exists

  @component @trainer @positive
  Scenario: Registering a trainer returns generated credentials
    When a trainer is registered with first name "Cuke" and last name "Trainer" specializing in "FITNESS"
    Then the response status is 201
    And a username and password are returned

  @component @trainer @negative
  Scenario: Registering a trainer with an unknown specialization is rejected
    When a trainer is registered with first name "Cuke" and last name "BadSpec" specializing in unknown training type 9999
    Then the response status is 400

  @component @training @positive
  Scenario: Adding a training session for an existing trainee and trainer succeeds
    Given a trainee is registered with first name "Cuke" and last name "Trainee1"
    And a trainer is registered with first name "Cuke" and last name "Trainer1" specializing in "FITNESS"
    And the trainee is logged in
    When a training session of 60 minutes is added on "2026-09-15" between the trainee and the trainer
    Then the response status is 201

  @component @training @negative
  Scenario: Adding a training session for an unknown trainee is rejected
    Given a trainer is registered with first name "Cuke" and last name "Trainer2" specializing in "FITNESS"
    And the trainer is logged in
    When a training session is added for unknown trainee "ghost.trainee"
    Then the response status is 404