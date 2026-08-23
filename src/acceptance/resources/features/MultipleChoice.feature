Feature: Multiple choice questions
  A user can ask a question that offers a fixed set of choices instead of a free text answer.

  Scenario: A single select question can be created and answered by picking one choice
    Given a multiple choice question is created with choices "Red,Green,Blue"
    Then the created question offers 3 choices
    And the created question does not allow multiple answers
    When the choice "green" is selected
    Then the answer text is "Green"
    And the question is marked answered

  Scenario: A multi select question accepts more than one choice
    Given a multi select question is created with choices "Red,Green,Blue"
    Then the created question allows multiple answers
    When the choices "red,blue" are selected
    Then the answer text is "Red, Blue"
    And the question is marked answered

  Scenario: A choice outside the offered set is rejected
    Given a multiple choice question is created with choices "Red,Green,Blue"
    When the choice "purple" is attempted
    Then the request is rejected
    And the question is not marked answered

  Scenario: A second choice is rejected on a single select question
    Given a multiple choice question is created with choices "Red,Green,Blue"
    When the choices "red,green" are attempted
    Then the request is rejected

  Scenario: A question offering fewer than two choices is rejected
    When a multiple choice question is attempted with choices "Only one"
    Then the request is rejected
