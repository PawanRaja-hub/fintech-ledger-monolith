Feature: Fintech ledger API automation

  Scenario: Admin funds Alice and Alice transfers money to Bob
    Given the fintech ledger API is available
    When the admin signs in through the API
    And Alice signs in through the API
    And the admin loads the wallet directory
    And the admin funds Alice with 250.00
    And Alice transfers 25.50 to Bob through the API
    Then the API transfer should be completed
    And replaying the same API transfer should return the same payment reference
    And the reconciliation API should show every wallet matched
