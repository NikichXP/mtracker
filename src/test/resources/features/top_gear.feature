Feature: Top Gear scan and GearScope API
  As a user
  I want top-player gear snapshots collected from raider.io rankings
  So that I can see what the best players of each spec wear

  Background:
    Given the database is cleaned

  Scenario: Producer creates one scan task per character and spec
    When the top gear producer runs
    Then 8 top gear scan tasks exist
    And the character "Holyone-gordunni" has 2 scan tasks for different specs
    And a scan task exists for "Clickzww-tarren-mill" spec 269

  Scenario: Scan consumes a task and stores gear snapshots
    Given a top gear scan task exists for "Scanchi" realm "gordunni" spec 269 named "Windwalker" scoring 4000.0
    When the top gear scan runs
    Then 2 gear snapshots exist
    And the gear snapshot for "Scanchi-gordunni" run 9001 has 4 items, 4 party specs and sources "SET,RAID,CRAFTED,KEYS"
    And the gear snapshot for "Scanchi-gordunni" run 9001 uses talent import "PROFILE_TALENT_WW"
    And the gear snapshot for "Scanchi-gordunni" run 9002 has 4 items, 4 party specs and sources "SET,RAID,CRAFTED,KEYS"
    And no scan task exists for "Scanchi-gordunni"

  Scenario: A snapshot is also stored for a roster member with a pending task
    Given a top gear scan task exists for "Scanchi" realm "gordunni" spec 269 named "Windwalker" scoring 4000.0
    And a top gear scan task exists for "Coheals" realm "draenor" spec 257 named "Holy" scoring 3000.0
    When the top gear scan runs
    Then 4 gear snapshots exist
    And the gear snapshot for "Coheals-draenor" run 9001 has 4 items, 4 party specs and sources "SET,RAID,CRAFTED,KEYS"

  Scenario: Claim exclusivity respects the claim timeout
    Given a top gear scan task exists for "Scanchi" realm "gordunni" spec 269 named "Windwalker" scoring 4000.0
    And the scan task for "Scanchi" spec 269 is claimed 5 minutes ago
    When the top gear scan runs
    Then 0 gear snapshots exist
    And the character "Scanchi-gordunni" has 1 scan tasks for different specs
    Given the scan task for "Scanchi" spec 269 is claimed 20 minutes ago
    When the top gear scan runs
    Then 2 gear snapshots exist
    And no scan task exists for "Scanchi-gordunni"

  Scenario: Cleanup removes old snapshots but keeps caches
    Given a gear snapshot exists for "Oldmonk-gordunni" captured 100 hours ago
    And a gear snapshot exists for "Freshmonk-gordunni" captured 1 hours ago
    And a gear item catalog entry exists for item 240949
    When the top gear cleanup runs
    Then 1 gear snapshots exist
    And 1 gear item catalog entries exist

  Scenario: GearScope API reports spec overviews and item usage
    Given a top gear scan task exists for "Scanchi" realm "gordunni" spec 269 named "Windwalker" scoring 4000.0
    When the top gear scan runs
    Then 2 gear snapshots exist
    When a user gets gearscope specs
    Then the gearscope specs response contains spec 269 with parseCount 2 and characterCount 1
    When a user gets gearscope items for spec 269
    Then the gearscope items response has item 268215 in slot "mainhand" with source "RAID"
    And the gearscope items response has item 240949 in slot "finger1" with source "CRAFTED"
    When a user gets gearscope items for spec 269 excluding raid
    Then the gearscope items response contains no RAID or SET items
