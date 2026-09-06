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
  buddyScore: number | null;
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

/** One of the 5 roster members of a `RecentRunDto`, snapshotted at the time the run was fetched. */
export interface RunRosterMemberDto {
  characterKey: string;
  name: string;
  realm: string;
  characterClass: string | null;
  spec: string | null;
  role: string | null;
  guildName: string | null;
  itemLevel: number | null;
  rioScore: number | null;
  isTrackedPlayer: boolean;
  playerKey: string | null;
}

/** Row of `/api/v1/stats/players/{playerKey}/runs`: a stored keystone run with its full roster. */
export interface RecentRunDto {
  season: string;
  keystoneRunId: number;
  dungeonName: string;
  dungeonShortName: string | null;
  mythicLevel: number;
  score: number | null;
  timed: boolean;
  numKeystoneUpgrades: number;
  completedAt: string | null;
  url: string | null;
  roster: RunRosterMemberDto[];
}
