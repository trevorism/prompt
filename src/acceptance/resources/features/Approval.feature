Feature: Approval lifecycle
  A user can request approval from an approver. The approver can approve the request.

  Scenario: An approval can be requested and approved
    Given an approval request is created for a user
    Then the created question has kind "approval"
    When the approval is approved with a reason
    Then the question is marked answered
    And the decision is approved
