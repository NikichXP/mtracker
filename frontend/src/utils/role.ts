import type { ComponentType } from "react";
import type { SvgIconProps } from "@mui/material/SvgIcon";
import ShieldIcon from "@mui/icons-material/Shield";
import HealingIcon from "@mui/icons-material/Healing";
import SportsMartialArtsIcon from "@mui/icons-material/SportsMartialArts";
import HelpOutlineIcon from "@mui/icons-material/HelpOutlineOutlined";

interface RoleVisual {
  label: string;
  color: string;
  icon: ComponentType<SvgIconProps>;
}

const ROLE_VISUALS: Record<string, RoleVisual> = {
  TANK: { label: "Tank", color: "#7c9cff", icon: ShieldIcon },
  HEALER: { label: "Healer", color: "#4caf50", icon: HealingIcon },
  DPS: { label: "DPS", color: "#f44336", icon: SportsMartialArtsIcon },
};

/** Looks up display metadata (label/color/icon) for a role string from the API,
 *  falling back to a neutral placeholder for unknown/missing roles. */
export function roleVisual(role: string | null): RoleVisual {
  if (role && ROLE_VISUALS[role]) return ROLE_VISUALS[role];
  return { label: role ?? "Unknown", color: "#9e9e9e", icon: HelpOutlineIcon };
}
