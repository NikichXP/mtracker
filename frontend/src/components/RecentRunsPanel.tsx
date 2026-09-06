import Box from "@mui/material/Box";
import Alert from "@mui/material/Alert";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { useRecentRuns } from "../hooks/useRecentRuns";
import { formatRelative } from "../utils/formatDate";
import type { RecentRunDto, RunRosterMemberDto } from "../api/types";

function RosterMemberChip({ member }: { member: RunRosterMemberDto }) {
  const label = `${member.name}${member.spec ? ` (${member.spec})` : ""}`;
  const tooltip = [
    `${member.name}-${member.realm}`,
    member.guildName ? `Guild: ${member.guildName}` : null,
    member.itemLevel ? `iLvl ${member.itemLevel.toFixed(0)}` : null,
    member.rioScore ? `Score ${member.rioScore.toFixed(1)}` : null,
  ]
    .filter(Boolean)
    .join(" · ");

  return (
    <Tooltip title={tooltip}>
      <Chip
        size="small"
        label={label}
        color={member.isTrackedPlayer ? "primary" : "default"}
        variant={member.isTrackedPlayer ? "filled" : "outlined"}
      />
    </Tooltip>
  );
}

function RunCard({ run }: { run: RecentRunDto }) {
  return (
    <Box
      sx={{
        p: 1.5,
        borderRadius: 1,
        border: "1px solid rgba(255,255,255,0.08)",
        bgcolor: "rgba(255,255,255,0.02)",
      }}
    >
      <Box sx={{ display: "flex", alignItems: "baseline", gap: 1, flexWrap: "wrap", mb: 1 }}>
        <Typography variant="body2" sx={{ fontWeight: 600 }}>
          {run.dungeonName} +{run.mythicLevel}
        </Typography>
        <Chip
          size="small"
          label={run.score?.toFixed(1) ?? "—"}
          color={run.timed ? "success" : "default"}
          variant={run.timed ? "filled" : "outlined"}
        />
        <Typography variant="caption" color="text.secondary" sx={{ ml: "auto" }}>
          {formatRelative(run.completedAt)}
        </Typography>
      </Box>
      <Stack direction="row" spacing={0.75} sx={{ flexWrap: "wrap", gap: 0.75 }}>
        {run.roster.map((member) => (
          <RosterMemberChip key={member.characterKey} member={member} />
        ))}
      </Stack>
    </Box>
  );
}

interface Props {
  playerKey: string;
}

/** Recent stored Mythic+ runs for a player's characters, each with its full 5-player roster
 *  (tracked players highlighted). Loaded lazily via `useRecentRuns` when the panel is shown. */
export function RecentRunsPanel({ playerKey }: Props) {
  const { data, isLoading, isError } = useRecentRuns(playerKey);

  if (isLoading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 2 }}>
        <CircularProgress size={20} />
      </Box>
    );
  }
  if (isError || !data) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        Failed to load recent runs
      </Alert>
    );
  }
  if (data.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ px: 2, py: 1 }}>
        No recent runs stored yet
      </Typography>
    );
  }

  return (
    <Stack spacing={1} sx={{ px: 2, pb: 2 }}>
      {data.map((run) => (
        <RunCard key={`${run.season}-${run.keystoneRunId}`} run={run} />
      ))}
    </Stack>
  );
}
