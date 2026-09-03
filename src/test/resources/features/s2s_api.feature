Feature: S2S API for Telegram bot and external services
  As an external service (e.g. Telegram bot)
  I want to query overview and player stats via S2S API
  Using S2S token authentication

  Background:
    Given the database is cleaned
    And an admin creates tracked guild with name "Bloodline" and realm "Gordunni"
    And a sync is triggered

  Scenario: S2S endpoints reject unauthorized access
    When a user performs GET "/api/v1/s2s/stats/overview" without token
    Then the response status should be 401
    When a user performs GET "/api/v1/s2s/stats/players/Arthas-Gordunni" without token
    Then the response status should be 401

  Scenario: S2S endpoints accept valid S2S token
    When an S2S client gets overview stats
    Then the response status should be 200
    And the overview contains player "Arthas-Gordunni" with totalScore 2850.5 weeklyRuns 2 maxItemLevel 635.5 role "TANK"
    When an S2S client gets player details for "Arthas-Gordunni"
    Then the response status should be 200
    And the player detail has 1 characters
    And character "Arthas-Gordunni" has spec "Blood" role "TANK" score 2850.5 and 2 weekly runs
