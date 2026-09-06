import { useState } from "react";
import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import Collapse from "@mui/material/Collapse";
import IconButton from "@mui/material/IconButton";
import TableCell from "@mui/material/TableCell";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import GroupIcon from "@mui/icons-material/Group";
import PersonIcon from "@mui/icons-material/Person";
import { RoleBadge } from "./RoleBadge";
import { CharacterBreakdown } from "./CharacterBreakdown";
import { formatRelative } from "../utils/formatDate";
import type { PlayerOverviewDto } from "../api/types";

interface Props {
  player: PlayerOverviewDto;
  /** 1-based leaderboard position, rendered as a leading column when set (e.g. for the Buddy ranking). */
  rank?: number;
  /** Shows the buddy-score column (share of this player's run rosters made up of other tracked players). */
  showBuddyScore?: boolean;
}

/** One row of the overview table; expands via `Collapse` to a per-character breakdown,
 *  loaded lazily by `CharacterBreakdown` only once the row has been opened at least once. */
export function PlayerRow({ player, rank, showBuddyScore = false }: Props) {
  const [open, setOpen] = useState(false);
  const [everOpened, setEverOpened] = useState(false);

  const handleToggle = () => {
    setOpen((prev) => !prev);
    setEverOpened(true);
  };

  const colSpan = 8 + (rank !== undefined ? 1 : 0) + (showBuddyScore ? 1 : 0);

  return (
    <>
      <TableRow hover sx={{ cursor: "pointer", "& > *": { borderBottom: "unset" } }} onClick={handleToggle}>
        {rank !== undefined && (
          <TableCell align="right" sx={{ width: 40 }}>
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
              #{rank}
            </Typography>
          </TableCell>
        )}
        <TableCell sx={{ width: 40 }}>
          <IconButton size="small" onClick={(e) => { e.stopPropagation(); handleToggle(); }}>
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell>
          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {player.displayName}
            </Typography>
            {player.isGuildMember && (
              <Chip size="small" icon={<GroupIcon fontSize="small" />} label="Guild" color="primary" variant="outlined" />
            )}
            {player.isFriend && (
              <Chip size="small" icon={<PersonIcon fontSize="small" />} label="Friend" color="secondary" variant="outlined" />
            )}
          </Box>
          <Typography variant="caption" color="text.secondary">
            {player.characterCount} character{player.characterCount === 1 ? "" : "s"}
          </Typography>
        </TableCell>
        {showBuddyScore && (
          <TableCell align="right">
            <Typography sx={{ fontWeight: 700 }}>
              {player.buddyScore !== null ? `${(player.buddyScore * 100).toFixed(0)}%` : "—"}
            </Typography>
          </TableCell>
        )}
        <TableCell align="right">
          <Typography sx={{ fontWeight: 700 }}>{player.totalScore.toFixed(1)}</Typography>
        </TableCell>
        <TableCell align="right">{player.weeklyRunsCount}</TableCell>
        <TableCell align="right">{player.weeklyHighestLevel > 0 ? `+${player.weeklyHighestLevel}` : "—"}</TableCell>
        <TableCell align="right">{player.maxItemLevel.toFixed(0)}</TableCell>
        <TableCell>
          <RoleBadge role={player.activeSpecRole} specName={player.activeSpecName} />
        </TableCell>
        <TableCell align="right">
          <Typography variant="caption" color="text.secondary">
            {formatRelative(player.lastSyncedAt)}
          </Typography>
        </TableCell>
      </TableRow>
      <TableRow>
        <TableCell colSpan={colSpan} sx={{ p: 0, borderBottom: open ? undefined : "unset" }}>
          <Collapse in={open} timeout="auto" unmountOnExit={false}>
            {everOpened && <CharacterBreakdown playerKey={player.playerKey} />}
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}
