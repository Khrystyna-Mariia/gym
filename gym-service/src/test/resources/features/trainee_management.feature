Feature: Trainee registration and profile management

  @component @trainee @positive
  Scenario: Registering a trainee returns generated credentials
    When a trainee is registered with first name "Cuke" and last name "Register"
    Then the response status is 201
    And a username and password are returned

  @component @trainee @negative
  Scenario: Registering a trainee without a first name is rejected
    When a trainee registration is submitted with a blank first name
    Then the response status is 400

  @component @trainee @positive
  Scenario: An authenticated trainee can update their own profile
    Given a trainee is registered with first name "Cuke" and last name "Update"
    And they are logged in
    When they update their profile address to "Lviv"
    Then the response status is 200
    And their profile address is "Lviv"

  @component @trainee @negative
  Scenario: Looking up a non-existent trainee returns not found
    Given a trainee is registered with first name "Cuke" and last name "Lookup"
    And they are logged in
    When they request the profile of trainee "does.not.exist"
    Then the response status is 404

  @component @trainee @positive
  Scenario: Deactivating a trainee account succeeds
    Given a trainee is registered with first name "Cuke" and last name "Deactivate"
    And they are logged in
    When they deactivate their own account
    Then the response status is 200