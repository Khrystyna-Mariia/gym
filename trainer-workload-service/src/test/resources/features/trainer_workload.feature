Feature: Trainer workload tracking
  As the Gym CRM platform
  I want the Trainer Workload service to record and report each trainer's monthly training workload
  So that trainers' summaries stay accurate as training events occur

  @component @workload @positive
  Scenario: Recording a training event for a brand-new trainer creates a monthly summary
    Given trainer "cucumber.john" has no existing workload record
    When a training ADD event of 60 minutes on "2026-09-15" is submitted for trainer "cucumber.john" "Cucumber" "John" who is active
    Then the response status is 200
    And the workload summary for trainer "cucumber.john" shows 60 minutes for year 2026 month 9

  @component @workload @positive
  Scenario: Recording a second training event in the same month accumulates duration
    Given trainer "cucumber.jane" "Cucumber" "Jane" already has 30 minutes recorded for year 2026 month 9
    When a training ADD event of 45 minutes on "2026-09-20" is submitted for trainer "cucumber.jane" "Cucumber" "Jane" who is active
    Then the response status is 200
    And the workload summary for trainer "cucumber.jane" shows 75 minutes for year 2026 month 9

  @component @workload @positive
  Scenario: Deleting a training event reduces the recorded duration
    Given trainer "cucumber.mark" "Cucumber" "Mark" already has 90 minutes recorded for year 2026 month 9
    When a training DELETE event of 30 minutes on "2026-09-05" is submitted for trainer "cucumber.mark" "Cucumber" "Mark" who is active
    Then the response status is 200
    And the workload summary for trainer "cucumber.mark" shows 60 minutes for year 2026 month 9

  @component @workload @negative
  Scenario: Deleting more minutes than were recorded never goes negative
    Given trainer "cucumber.zoe" "Cucumber" "Zoe" already has 20 minutes recorded for year 2026 month 9
    When a training DELETE event of 50 minutes on "2026-09-05" is submitted for trainer "cucumber.zoe" "Cucumber" "Zoe" who is active
    Then the response status is 200
    And the workload summary for trainer "cucumber.zoe" shows 0 minutes for year 2026 month 9

  @component @workload @negative
  Scenario: A workload event with a blank trainer username is rejected
    When a training ADD event of 60 minutes on "2026-09-15" is submitted for trainer "" "Cucumber" "Ghost" who is active
    Then the response status is 400

  @component @workload @negative
  Scenario: A workload event with a non-positive duration is rejected
    When a training ADD event of -15 minutes on "2026-09-15" is submitted for trainer "cucumber.neg" "Cucumber" "Neg" who is active
    Then the response status is 400

  @component @workload @positive
  Scenario: Searching by first and last name finds a recorded trainer
    Given trainer "cucumber.searchable" "Cucumber" "Searchable" already has 40 minutes recorded for year 2026 month 6
    When trainer summaries are searched by first name "Cucumber" and last name "Searchable"
    Then the response status is 200
    And the search results include trainer "cucumber.searchable"

  @component @workload @negative
  Scenario: Requesting a summary for an unknown trainer returns not found
    When the workload summary for trainer "cucumber.unknown.xyz" is requested
    Then the response status is 404

  @component @auth @negative
  Scenario: Calling the workload API without a service token is rejected
    When an unauthenticated training ADD event of 60 minutes on "2026-09-15" is submitted for trainer "cucumber.noauth" "Cucumber" "NoAuth" who is active
    Then the response status is 401

  @component @auth @negative
  Scenario: Calling the workload API with an invalid service token is rejected
    When a training ADD event of 60 minutes on "2026-09-15" with an invalid service token is submitted for trainer "cucumber.badtoken" "Cucumber" "BadToken" who is active
    Then the response status is 401
