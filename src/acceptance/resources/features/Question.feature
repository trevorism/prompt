Feature: Question lifecycle
  A user can ask a question and have it answered

  Scenario: A question can be created and answered
    Given a plain question is created
    Then the created question is unanswered
    When the question is answered
    Then the question is marked answered
