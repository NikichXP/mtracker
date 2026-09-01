import Chip from "@mui/material/Chip";
import { roleVisual } from "../utils/role";

interface Props {
  role: string | null;
  specName?: string | null;
}

/** Small colored chip with a role icon (tank/healer/dps), optionally labeled with the spec name. */
export function RoleBadge({ role, specName }: Props) {
  const visual = roleVisual(role);
  const Icon = visual.icon;
  const label = specName ? `${specName} ${visual.label}` : visual.label;
  return (
    <Chip
      size="small"
      icon={<Icon sx={{ color: `${visual.color} !important` }} fontSize="small" />}
      label={label}
      variant="outlined"
      sx={{
        borderColor: visual.color,
        color: visual.color,
        fontWeight: 600,
      }}
    />
  );
}
