Feature: Admin API Management
  As an administrator
  I want to manage tracked guilds and players
  So that mtracker knows what roster to sync

  Scenario: Admin endpoints require authorization
    When a user performs GET "/api/v1/admin/guilds" without token
    Then the response status should be 401
    When a user performs GET "/api/v1/admin/tracked-players" without token
    Then the response status should be 401

  Scenario: Admin can create, read, update, and delete tracked guilds
    When an admin creates tracked guild with name "Bloodline" and realm "Gordunni"
    Then the response status should be 201
    And the response json contains name "Bloodline" and realm "Gordunni"
    When an admin gets all tracked guilds
    Then the response status should be 200
    And the guild list contains "Bloodline-Gordunni"
    When an admin deletes the created guild
    Then the response status should be 204
    When an admin gets all tracked guilds
    Then the guild list should not contain "Bloodline-Gordunni"

  Scenario: Admin can create, read, update, and delete tracked players
    When an admin creates tracked player with displayName "Illidan" characterKeys "Illidan-Ravencrest,Illidari-Ravencrest" and isFriend true
    Then the response status should be 201
    And the response json contains displayName "Illidan"
    When an admin gets all tracked players
    Then the response status should be 200
    And the player list contains "Illidan"
    When an admin deletes the created player
    Then the response status should be 204
