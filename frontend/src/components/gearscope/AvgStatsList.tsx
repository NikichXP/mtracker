import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import Typography from "@mui/material/Typography";
import { sortedStatEntries, statLabel } from "../../utils/statLabels";
import type { StatType } from "../../api/types";

interface Props {
  stats: Partial<Record<StatType, number>>;
}

export function AvgStatsList({ stats }: Props) {
  const entries = sortedStatEntries(stats);

  if (entries.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No stat data recorded.
      </Typography>
    );
  }

  return (
    <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1 }}>
      {entries.map(([stat, value]) => (
        <Chip key={stat} size="small" label={`${statLabel(stat)}: ${value}`} variant="outlined" />
      ))}
    </Box>
  );
}
