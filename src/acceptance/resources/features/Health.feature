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

  Scenario: The browser session is anonymous without cookies
    When I GET "api/auth/session" anonymously
    Then the response body reports nobody is signed in

  Scenario: Refreshing a session without a token is rejected
    When I POST "api/auth/refresh" as "application/x-www-form-urlencoded"
    Then the response status is 401

  Scenario: Logout accepts the content type a browser actually sends
    When I POST "api/auth/logout" as "application/x-www-form-urlencoded"
    Then the response status is 200

  Scenario: Signing in starts the one time code handoff
    When I GET "api/auth/login" without following redirects
    Then the response redirects to the login application with a callback on this host
