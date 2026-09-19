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

/** Mirrors `com.nikichxp.mtracker.domain.topgear.GearSource`. */
export type GearSource = "CRAFTED" | "RAID" | "KEYS" | "SET" | "VENDOR" | "QUEST" | "PVP" | "WORLD" | "UNKNOWN";

/** Mirrors `com.nikichxp.mtracker.domain.topgear.StatType`. */
export type StatType =
  | "STRENGTH"
  | "AGILITY"
  | "INTELLECT"
  | "STAMINA"
  | "CRITICAL_STRIKE"
  | "HASTE"
  | "MASTERY"
  | "VERSATILITY"
  | "SPEED"
  | "LEECH"
  | "AVOIDANCE"
  | "ARMOR";

/** Row of `/api/v1/gearscope/specs`: aggregate parse stats for one spec across all stored top-player gear snapshots. */
export interface SpecOverviewDto {
  specId: number;
  specName: string;
  specSlug: string;
  className: string;
  role: string;
  parseCount: number;
  characterCount: number;
  avgItemLevel: number | null;
}

export interface GearItemUsageDto {
  itemId: number;
  itemName: string | null;
  icon: string | null;
  source: GearSource;
  sourceDetail: string | null;
  usageCount: number;
  usageShare: number;
  avgItemLevel: number | null;
  stats: Partial<Record<StatType, number>>;
  topGems: string[];
  topEnchant: string | null;
}

export interface SlotReportDto {
  slot: string;
  topItems: GearItemUsageDto[];
}

/** Response of `/api/v1/gearscope/specs/{id}/items`: up to top-N items per slot. */
export interface SpecGearReportDto {
  specId: number;
  specName: string;
  specSlug: string;
  className: string;
  role: string;
  parseCount: number;
  characterCount: number;
  slots: SlotReportDto[];
}

export interface SlotPickDto {
  slot: string;
  item: GearItemUsageDto;
}

export interface TalentUsageDto {
  importString: string;
  usageCount: number;
}

export interface PartySpecUsageDto {
  specId: number;
  specName: string;
  className: string;
  role: string;
  count: number;
}

/** Response of `/api/v1/gearscope/specs/{id}/summary`: the "what people wear" best-in-slot overview. */
export interface SpecSummaryDto {
  specId: number;
  specName: string;
  className: string;
  role: string;
  parseCount: number;
  characterCount: number;
  slots: SlotPickDto[];
  sourceBreakdown: Partial<Record<GearSource, number>>;
  avgStats: Partial<Record<StatType, number>>;
  topTalentImportStrings: TalentUsageDto[];
  topPartySpecs: PartySpecUsageDto[];
}
