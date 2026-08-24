Feature: Cross-service training workload integration

  @integration @positive
  Scenario: Adding a training session updates the trainer's workload summary in the other service
    Given a trainee and a trainer are registered in gym-service
    When a training session of 50 minutes on "2026-09-10" is added between them in gym-service
    Then trainer-workload-service eventually reports 50 minutes for that trainer in year 2026 month 9

  @integration @positive
  Scenario: Two training sessions in the same month accumulate in trainer-workload-service
    Given a trainee and a trainer are registered in gym-service
    When a training session of 30 minutes on "2026-09-11" is added between them in gym-service
    And a training session of 20 minutes on "2026-09-12" is added between them in gym-service
    Then trainer-workload-service eventually reports 50 minutes for that trainer in year 2026 month 9

  @integration @negative
  Scenario: Trainer-workload-service rejects direct calls without a service token
    When trainer-workload-service is queried directly without a service token
    Then trainer-workload-service responds with an unauthorized status
