/** Mirrors `com.nikichxp.mtracker` stats DTOs from the mtracker backend. */

export type SpecRole = "TANK" | "HEALER" | "DPS";

/** Row of `/api/v1/stats/overview`: one real person (main + rolled-up alts). */
export interface PlayerOverviewDto {
  playerKey: string;
  displayName: string;
  isFriend: boolean;
  isGuildMember: boolean;
  totalScore: number;
  weeklyRunsCount: number;
  weeklyHighestLevel: number;
  maxItemLevel: number;
  activeSpecName: string | null;
  activeSpecRole: string | null;
  characterCount: number;
  lastSyncedAt: string | null;
}

export interface DungeonRunDto {
  dungeonName: string;
  mythicLevel: number;
  score: number;
  timed: boolean;
  completedAt: string | null;
}

export interface CharacterDto {
  characterKey: string;
  name: string;
  realm: string;
  characterClass: string | null;
  activeSpecName: string | null;
  activeSpecRole: string | null;
  itemLevelEquipped: number | null;
  mythicPlusScore: number | null;
  weeklyRuns: DungeonRunDto[];
  lastSyncedAt: string | null;
}

export interface PlayerDetailDto {
  playerKey: string;
  displayName: string;
  characters: CharacterDto[];
}

/** Row of `/api/v1/stats/weekly?week=...`: historical week-over-week snapshot. */
export interface WeeklyPlayerStatsDto {
  weekKey: string;
  playerKey: string;
  displayName: string;
  weeklyRunsCount: number;
  weeklyHighestLevel: number;
  totalScore: number;
}
