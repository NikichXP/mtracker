Feature: Sync and Stats API
  As a player
  I want to view Mythic+ statistics of my guild and friends
  So that I can track character progress and weekly activity

  Background:
    Given the database is cleaned
    And an admin creates tracked guild with name "Bloodline" and realm "Gordunni"
    And an admin creates tracked player with displayName "Illidan" characterKeys "Illidan-Ravencrest,Illidari-Ravencrest" and isFriend true

  Scenario: Sync data from Raider.io stubs and verify overview stats
    When a sync is triggered
    Then the sync finishes successfully
    When a user gets player overview
    Then the response status should be 200
    And the overview contains player "Arthas-Gordunni" with totalScore 2850.5 weeklyRuns 2 maxItemLevel 635.5 role "TANK"
    And the overview contains player "Jaina-Gordunni" with totalScore 2720.0 weeklyRuns 1 maxItemLevel 630.0 role "DPS"
    And the overview contains player "Illidan-Ravencrest" with displayName "Illidan" totalScore 3050.0 weeklyRuns 2 maxItemLevel 639.0 characterCount 2

  Scenario: Verify player detail endpoint returns character runs from stubs
    Given a sync is triggered
    When a user gets player details for "Illidan-Ravencrest"
    Then the response status should be 200
    And the player detail has 2 characters
    And character "Illidan-Ravencrest" has spec "Havoc" role "DPS" score 3050.0 and 1 weekly runs
    And character "Illidari-Ravencrest" has spec "Vengeance" role "TANK" score 2200.0 and 1 weekly runs

  Scenario: Runs are stored with full roster and players are scheduled by score
    Given a sync is triggered
    Then the stored run 22345 in season "season-tww-1" has 5 roster players
    And the stored run 22346 in season "season-tww-1" has 5 roster players
    And the roster player "Arthas-Gordunni" of run 22345 in season "season-tww-1" is linked to a tracked player
    And the roster player "Jaina-Gordunni" of run 22345 in season "season-tww-1" is linked to a tracked player
    And the roster player "Pugheal-Draenor" of run 22345 in season "season-tww-1" is not linked to a tracked player
    And the roster player "Illidan-Ravencrest" of run 22346 in season "season-tww-1" is linked to a tracked player
    And player "Arthas-Gordunni" has rioScore 2850.5 and next update scheduled
    And player "Illidan-Ravencrest" has rioScore 3050.0 and next update scheduled

  Scenario: Recent runs endpoint returns stored runs with full roster
    Given a sync is triggered
    When a user gets recent runs for "Arthas-Gordunni"
    Then the response status should be 200
    And recent run 22345 in season "season-tww-1" has 5 roster members including a tracked player

  Scenario: Verify weekly stats and available weeks
    Given a sync is triggered
    When a user gets available weeks
    Then the response status should be 200
    And the list of weeks is not empty
    When a user gets weekly stats
    Then the response status should be 200
    And the weekly stats list contains player "Arthas-Gordunni" with 2 runs and totalScore 2850.5
    And the weekly stats list contains player "Illidan-Ravencrest" with 2 runs and totalScore 3050.0
