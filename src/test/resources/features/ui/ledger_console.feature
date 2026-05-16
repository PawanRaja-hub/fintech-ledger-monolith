Feature: Fintech ledger web console automation

  @ui
  Scenario: Admin funds a wallet from the web console
    Given I open the fintech ledger web console
    When I sign in as Platform Admin
    And I open the Admin workspace
    And I fund the first wallet with 10.00 from the web console
    Then I should see the wallet funded message
    When I open the Reconcile workspace
    Then I should see reconciliation rows on the page

  @ui
  Scenario: Customer can see transfer recipients
    Given I open the fintech ledger web console
    When I sign in as Alice Customer
    Then Bob should be available as a transfer recipient
