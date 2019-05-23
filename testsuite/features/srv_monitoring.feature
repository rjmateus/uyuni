# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
#

Feature: Server monitoring

  Scenario: Check that server monitoring is disabled using the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    Then I wait until I do not see "Checking services..." text
    And I should see a list item with text "System" and bullet with class "text-danger"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with class "text-danger"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with class "text-danger"
    And I should see a list item with text "PostgreSQL database" and bullet with class "text-danger"
    And I should see a "Enable services" button

  Scenario: Enable server monitoring from the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    Then I wait until I do not see "Checking services..." text
    And I click on "Enable services"
    And I wait until I do not see "Enabling services..." text
    And I should see a "Monitoring enabled successfully." text
    And I should see a list item with text "System" and bullet with class "text-success"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with class "text-success"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with class "text-success"
    And I should see a list item with text "PostgreSQL database" and bullet with class "text-success"
    And I should see a "Disable services" button


  Scenario: Disable server monitoring from the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    Then I wait until I do not see "Checking services..." text
    And I click on "Disable services"
    And I wait until I do not see "Disabling services..." text
    And I should see a "Monitoring disabled successfully." text
    And I should see a list item with text "System" and bullet with class "text-danger"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with class "text-danger"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with class "text-danger"
    And I should see a list item with text "PostgreSQL database" and bullet with class "text-danger"
    And I should see a "Enable services" button