import { useState } from "react";
import Box from "@mui/material/Box";
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
import { RoleBadge } from "../RoleBadge";
import type { SpecOverviewDto } from "../../api/types";

interface Props {
  specs: SpecOverviewDto[];
  selectedSpecId: number | null;
  onSelect: (specId: number) => void;
}

export function SpecListPanel({ specs, selectedSpecId, onSelect }: Props) {
  const [search, setSearch] = useState("");

  const filtered = search.trim()
    ? specs.filter((spec) => `${spec.className} ${spec.specName}`.toLowerCase().includes(search.trim().toLowerCase()))
    : specs;

  return (
    <Paper
      elevation={0}
      sx={{
        bgcolor: "rgba(23,26,33,0.6)",
        backdropFilter: "blur(10px)",
        border: "1px solid rgba(255,255,255,0.08)",
        overflow: "hidden",
      }}
    >
      <Box sx={{ p: 1.5, pb: 1 }}>
        <TextField
          size="small"
          fullWidth
          placeholder="Search spec…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
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
      </Box>
      <TableContainer sx={{ maxHeight: 640 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>Spec</TableCell>
              <TableCell align="right">Parses</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={2}>
                  <Typography color="text.secondary" sx={{ py: 3, textAlign: "center" }}>
                    No specs found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((spec) => (
                <TableRow
                  key={spec.specId}
                  hover
                  selected={spec.specId === selectedSpecId}
                  onClick={() => onSelect(spec.specId)}
                  sx={{ cursor: "pointer" }}
                >
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {spec.className} — {spec.specName}
                    </Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <RoleBadge role={spec.role} />
                    </Box>
                  </TableCell>
                  <TableCell align="right">
                    <Typography sx={{ fontWeight: 700 }}>{spec.parseCount}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {spec.characterCount} char{spec.characterCount === 1 ? "" : "s"}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
