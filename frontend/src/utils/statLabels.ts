import type { StatType } from "../api/types";

const STAT_LABELS: Record<StatType, string> = {
  STRENGTH: "Strength",
  AGILITY: "Agility",
  INTELLECT: "Intellect",
  STAMINA: "Stamina",
  CRITICAL_STRIKE: "Crit",
  HASTE: "Haste",
  MASTERY: "Mastery",
  VERSATILITY: "Versatility",
  SPEED: "Speed",
  LEECH: "Leech",
  AVOIDANCE: "Avoidance",
  ARMOR: "Armor",
};

export function statLabel(stat: StatType): string {
  return STAT_LABELS[stat] ?? stat;
}

const STAT_GROUP_ORDER: StatType[] = ["STRENGTH", "AGILITY", "INTELLECT", "STAMINA"];

/** Sorts primary stats first (in their canonical order), then secondaries by descending value. */
export function sortedStatEntries(stats: Partial<Record<StatType, number>>): [StatType, number][] {
  const entries = Object.entries(stats) as [StatType, number][];
  return entries.sort(([statA, valueA], [statB, valueB]) => {
    const groupIndexA = STAT_GROUP_ORDER.indexOf(statA);
    const groupIndexB = STAT_GROUP_ORDER.indexOf(statB);
    if (groupIndexA !== -1 || groupIndexB !== -1) {
      if (groupIndexA === -1) return 1;
      if (groupIndexB === -1) return -1;
      return groupIndexA - groupIndexB;
    }
    return valueB - valueA;
  });
}
