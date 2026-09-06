import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import Divider from "@mui/material/Divider";
import { usePlayerDetail } from "../hooks/usePlayerDetail";
import { RoleBadge } from "./RoleBadge";
import { RecentRunsPanel } from "./RecentRunsPanel";
import { formatRelative } from "../utils/formatDate";
import type { DungeonRunDto } from "../api/types";

function RunChips({ runs }: { runs: DungeonRunDto[] }) {
  if (runs.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No runs this week
      </Typography>
    );
  }
  return (
    <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.75 }}>
      {runs.map((run, idx) => (
        <Chip
          key={`${run.dungeonName}-${idx}`}
          size="small"
          label={`${run.dungeonName} +${run.mythicLevel} (${run.score.toFixed(0)})`}
          color={run.timed ? "success" : "default"}
          variant={run.timed ? "filled" : "outlined"}
        />
      ))}
    </Box>
  );
}

interface Props {
  playerKey: string;
}

/** Table of a player's individual characters (main + alts) with weekly run chips, loaded
 *  lazily via `usePlayerDetail` when the parent row is expanded. */
export function CharacterBreakdown({ playerKey }: Props) {
  const { data, isLoading, isError } = usePlayerDetail(playerKey);

  if (isLoading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 3 }}>
        <CircularProgress size={24} />
      </Box>
    );
  }
  if (isError || !data) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        Failed to load character breakdown
      </Alert>
    );
  }

  return (
    <Box sx={{ p: 2, bgcolor: "rgba(255,255,255,0.02)" }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Character</TableCell>
            <TableCell>Class</TableCell>
            <TableCell>Role / Spec</TableCell>
            <TableCell align="right">Item Level</TableCell>
            <TableCell align="right">Score</TableCell>
            <TableCell>Weekly Runs</TableCell>
            <TableCell align="right">Synced</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {data.characters.map((character) => (
            <TableRow key={character.characterKey} hover>
              <TableCell>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {character.name}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {character.realm}
                </Typography>
              </TableCell>
              <TableCell>{character.characterClass ?? "—"}</TableCell>
              <TableCell>
                <RoleBadge role={character.activeSpecRole} specName={character.activeSpecName} />
              </TableCell>
              <TableCell align="right">{character.itemLevelEquipped?.toFixed(0) ?? "—"}</TableCell>
              <TableCell align="right">{character.mythicPlusScore?.toFixed(1) ?? "—"}</TableCell>
              <TableCell>
                <RunChips runs={character.weeklyRuns} />
              </TableCell>
              <TableCell align="right">
                <Typography variant="caption" color="text.secondary">
                  {formatRelative(character.lastSyncedAt)}
                </Typography>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Divider sx={{ my: 2 }} />
      <Typography variant="subtitle2" sx={{ px: 2, mb: 1 }}>
        Recent Runs
      </Typography>
      <RecentRunsPanel playerKey={playerKey} />
    </Box>
  );
}
