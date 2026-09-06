import { useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import InputAdornment from "@mui/material/InputAdornment";
import Paper from "@mui/material/Paper";
import SearchIcon from "@mui/icons-material/Search";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { usePlayersOverview } from "../hooks/usePlayersOverview";
import { PlayerRow } from "./PlayerRow";

/** "Gear" sub-tab: current/live gear & score stats for every tracked player, sorted by total score. */
export function GearTab() {
  const { data, isLoading, isError } = usePlayersOverview();
  const [search, setSearch] = useState("");

  const players = useMemo(() => {
    const all = data ?? [];
    const filtered = search.trim()
      ? all.filter((p) => p.displayName.toLowerCase().includes(search.trim().toLowerCase()))
      : all;
    return [...filtered].sort((a, b) => b.totalScore - a.totalScore);
  }, [data, search]);

  if (isLoading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }
  if (isError) {
    return <Alert severity="error">Failed to load overview stats</Alert>;
  }

  return (
    <Box>
      <TextField
        size="small"
        placeholder="Search player…"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        sx={{ mb: 2, minWidth: 260 }}
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" />
              </InputAdornment>
            ),
          },
        }}
      />

      <Paper
        elevation={0}
        sx={{
          bgcolor: "rgba(23,26,33,0.6)",
          backdropFilter: "blur(10px)",
          border: "1px solid rgba(255,255,255,0.08)",
          overflow: "hidden",
        }}
      >
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 40 }} />
                <TableCell>Player</TableCell>
                <TableCell align="right">Score</TableCell>
                <TableCell align="right">Weekly Runs</TableCell>
                <TableCell align="right">Weekly Best</TableCell>
                <TableCell align="right">Max iLvl</TableCell>
                <TableCell>Active Spec</TableCell>
                <TableCell align="right">Synced</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {players.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8}>
                    <Typography color="text.secondary" sx={{ py: 3, textAlign: "center" }}>
                      No players found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                players.map((player) => <PlayerRow key={player.playerKey} player={player} />)
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    </Box>
  );
}
