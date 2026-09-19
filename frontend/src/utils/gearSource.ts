import type { GearSource } from "../api/types";

interface GearSourceVisual {
  label: string;
  color: string;
}

const GEAR_SOURCE_VISUALS: Record<GearSource, GearSourceVisual> = {
  CRAFTED: { label: "Crafted", color: "#7c9cff" },
  RAID: { label: "Raid", color: "#f48fb1" },
  KEYS: { label: "Mythic+", color: "#4caf50" },
  SET: { label: "Tier Set", color: "#ffb74d" },
  VENDOR: { label: "Vendor", color: "#9e9e9e" },
  QUEST: { label: "Quest", color: "#4dd0e1" },
  PVP: { label: "PvP", color: "#f44336" },
  WORLD: { label: "World", color: "#a1887f" },
  UNKNOWN: { label: "Unknown", color: "#616161" },
};

export function gearSourceVisual(source: GearSource): GearSourceVisual {
  return GEAR_SOURCE_VISUALS[source] ?? { label: source, color: "#9e9e9e" };
}
