import Box from "@mui/material/Box";
import LinearProgress from "@mui/material/LinearProgress";
import Typography from "@mui/material/Typography";
import { gearSourceVisual } from "../../utils/gearSource";
import type { GearSource } from "../../api/types";

interface Props {
  sourceBreakdown: Partial<Record<GearSource, number>>;
}

export function SourceBreakdown({ sourceBreakdown }: Props) {
  const entries = Object.entries(sourceBreakdown) as [GearSource, number][];
  const total = entries.reduce((sum, [, count]) => sum + count, 0);

  if (total === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No items recorded.
      </Typography>
    );
  }

  const sorted = [...entries].sort((a, b) => b[1] - a[1]);

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
      {sorted.map(([source, count]) => {
        const visual = gearSourceVisual(source);
        const share = (count / total) * 100;
        return (
          <Box key={source}>
            <Box sx={{ display: "flex", justifyContent: "space-between", mb: 0.25 }}>
              <Typography variant="body2">{visual.label}</Typography>
              <Typography variant="caption" color="text.secondary">
                {count} ({share.toFixed(0)}%)
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={share}
              sx={{
                height: 6,
                borderRadius: 3,
                bgcolor: "rgba(255,255,255,0.08)",
                "& .MuiLinearProgress-bar": { bgcolor: visual.color },
              }}
            />
          </Box>
        );
      })}
    </Box>
  );
}
