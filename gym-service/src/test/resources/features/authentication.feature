Feature: Authentication and access control

  @component @auth @positive
  Scenario: A newly registered trainee can log in with their generated password
    Given a trainee is registered with first name "Cuke" and last name "LoginOk"
    When they log in with their generated credentials
    Then the response status is 200
    And a JWT access token is returned

  @component @auth @negative
  Scenario: Logging in with the wrong password is rejected
    Given a trainee is registered with first name "Cuke" and last name "WrongPass"
    When they log in with the wrong password
    Then the response status is 401

  @component @auth @negative
  Scenario: Repeated failed logins lock the account
    Given a trainee is registered with first name "Cuke" and last name "Locked"
    When they attempt to log in with the wrong password 4 times
    Then the response status is 423

  @component @auth @permission @negative
  Scenario: Accessing a protected endpoint without a token is rejected
    When an unauthenticated request is made to get the profile of trainee "some.username"
    Then the response indicates the caller is not authorized

  @component @auth @permission @negative
  Scenario: Accessing a protected endpoint with a garbage token is rejected
    When a request with an invalid token is made to get the profile of trainee "some.username"
    Then the response indicates the caller is not authorized

  @component @auth @permission @positive
  Scenario: A logged-in trainee can access their own protected profile
    Given a trainee is registered with first name "Cuke" and last name "Authorized"
    And they are logged in
    When they request their own profile
    Then the response status is 200

  @component @auth @positive
  Scenario: Logging out blacklists the token so it can no longer be used
    Given a trainee is registered with first name "Cuke" and last name "Logout"
    And they are logged in
    When they log out
    And they request their own profile using that same, now blacklisted, token
    Then the response indicates the caller is not authorized