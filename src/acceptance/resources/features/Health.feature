Feature: Health and security
  The API must be alive and its secured endpoints must reject unauthenticated callers

  Scenario: Ping is publicly available
    When I GET "api/ping" anonymously
    Then the response body is "pong"

  Scenario: Creating a question requires authentication
    When I POST "api/question/" anonymously
    Then the request is rejected

  Scenario: The due date callback requires authentication
    When I POST "api/question/anything/duedate/callback" anonymously
    Then the request is rejected
