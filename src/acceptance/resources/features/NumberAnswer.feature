Feature: Number answers
  A question can expect a number, optionally with a unit and a range

  Scenario: A number question accepts only numbers in its range
    Given a number question in "lb" from 100 to 300 is created
    Then the question expects a number in "lb"
    When I try to answer it with "about 180"
    Then the request is rejected
    When I try to answer it with "301"
    Then the request is rejected
    When I answer it with "182.5"
    Then the stored answer has the value 182.5 and the text "182.5 lb"
