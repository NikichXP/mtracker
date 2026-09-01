import { useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Select, { type SelectChangeEvent } from "@mui/material/Select";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import { useAvailableWeeks } from "../hooks/useAvailableWeeks";
import { useWeeklyStats } from "../hooks/useWeeklyStats";

/** "Weekly" tab: historical week-over-week stats for a selected week, populated from the
 *  list of available week keys returned by the backend. */
export function WeeklyTab() {
  const weeksQuery = useAvailableWeeks();
  const weeks = weeksQuery.data ?? [];
  const [selectedWeek, setSelectedWeek] = useState<string | null>(null);
  const week = selectedWeek && weeks.includes(selectedWeek) ? selectedWeek : (weeks[0] ?? null);

  const statsQuery = useWeeklyStats(week);
  const stats = useMemo(() => [...(statsQuery.data ?? [])].sort((a, b) => b.totalScore - a.totalScore), [statsQuery.data]);

  const error = weeksQuery.isError ? "Failed to load available weeks" : statsQuery.isError ? "Failed to load weekly stats" : null;

  return (
    <Box>
      <FormControl size="small" sx={{ mb: 2, minWidth: 200 }}>
        <InputLabel id="week-label">Week</InputLabel>
        <Select
          labelId="week-label"
          label="Week"
          value={week ?? ""}
          onChange={(e: SelectChangeEvent) => setSelectedWeek(e.target.value)}
          disabled={weeks.length === 0}
        >
          {weeks.map((w) => (
            <MenuItem key={w} value={w}>
              {w}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {(weeksQuery.isLoading || statsQuery.isLoading) && !error ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
          <CircularProgress />
        </Box>
      ) : (
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
                  <TableCell>Player</TableCell>
                  <TableCell align="right">Weekly Runs</TableCell>
                  <TableCell align="right">Weekly Best</TableCell>
                  <TableCell align="right">Total Score</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {stats.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4}>
                      <Typography color="text.secondary" sx={{ py: 3, textAlign: "center" }}>
                        No data for this week
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  stats.map((row) => (
                    <TableRow key={row.playerKey} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {row.displayName}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">{row.weeklyRunsCount}</TableCell>
                      <TableCell align="right">{row.weeklyHighestLevel > 0 ? `+${row.weeklyHighestLevel}` : "—"}</TableCell>
                      <TableCell align="right">
                        <Typography sx={{ fontWeight: 700 }}>{row.totalScore.toFixed(1)}</Typography>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}
    </Box>
  );
}
