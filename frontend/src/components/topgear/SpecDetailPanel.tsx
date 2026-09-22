import { useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import FormControlLabel from "@mui/material/FormControlLabel";
import Paper from "@mui/material/Paper";
import Switch from "@mui/material/Switch";
import Typography from "@mui/material/Typography";
import { useSpecItems } from "../../hooks/useSpecItems";
import { useSpecSummary } from "../../hooks/useSpecSummary";
import { RoleBadge } from "../RoleBadge";
import { AvgStatsList } from "./AvgStatsList";
import { BestInSlotGrid } from "./BestInSlotGrid";
import { PartyCompChips } from "./PartyCompChips";
import { SlotItemsSection } from "./SlotItemsSection";
import { SourceBreakdown } from "./SourceBreakdown";
import { TalentBuildsList } from "./TalentBuildsList";
import type { SpecOverviewDto } from "../../api/types";

interface Props {
  spec: SpecOverviewDto;
}

const panelSx = {
  bgcolor: "rgba(23,26,33,0.6)",
  backdropFilter: "blur(10px)",
  border: "1px solid rgba(255,255,255,0.08)",
  p: 2,
};

export function SpecDetailPanel({ spec }: Props) {
  const [excludeRaid, setExcludeRaid] = useState(false);
  const summaryQuery = useSpecSummary(spec.specId, excludeRaid);
  const itemsQuery = useSpecItems(spec.specId, excludeRaid);

  const isLoading = summaryQuery.isLoading || itemsQuery.isLoading;
  const isError = summaryQuery.isError || itemsQuery.isError;

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
      <Paper elevation={0} sx={panelSx}>
        <Box
          sx={{
            display: "flex",
            alignItems: "flex-start",
            justifyContent: "space-between",
            flexWrap: "wrap",
            gap: 2,
          }}
        >
          <Box>
            <Typography variant="h6">
              {spec.className} — {spec.specName}
            </Typography>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mt: 0.5 }}>
              <RoleBadge role={spec.role} />
              <Typography variant="body2" color="text.secondary">
                {spec.parseCount} parses · {spec.characterCount} characters
                {spec.avgItemLevel ? ` · avg ${spec.avgItemLevel.toFixed(0)} iLvl` : ""}
              </Typography>
            </Box>
          </Box>
          <FormControlLabel
            control={<Switch checked={excludeRaid} onChange={(e) => setExcludeRaid(e.target.checked)} />}
            label="Hide raid & tier gear"
          />
        </Box>
      </Paper>

      {isLoading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
          <CircularProgress />
        </Box>
      ) : isError || !summaryQuery.data || !itemsQuery.data ? (
        <Alert severity="error">Failed to load spec details</Alert>
      ) : (
        <>
          <Paper elevation={0} sx={panelSx}>
            <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
              What people wear (best in slot)
            </Typography>
            <BestInSlotGrid slots={summaryQuery.data.slots} />
          </Paper>

          <Box sx={{ display: "flex", gap: 2, flexDirection: { xs: "column", md: "row" } }}>
            <Paper elevation={0} sx={{ ...panelSx, flex: 1 }}>
              <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
                Gear source breakdown
              </Typography>
              <SourceBreakdown sourceBreakdown={summaryQuery.data.sourceBreakdown} />
            </Paper>
            <Paper elevation={0} sx={{ ...panelSx, flex: 1 }}>
              <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
                Average stats
              </Typography>
              <AvgStatsList stats={summaryQuery.data.avgStats} />
            </Paper>
          </Box>

          <Box sx={{ display: "flex", gap: 2, flexDirection: { xs: "column", md: "row" } }}>
            <Paper elevation={0} sx={{ ...panelSx, flex: 1 }}>
              <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
                Top talent builds
              </Typography>
              <TalentBuildsList builds={summaryQuery.data.topTalentImportStrings} />
            </Paper>
            <Paper elevation={0} sx={{ ...panelSx, flex: 1 }}>
              <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
                Common party comps
              </Typography>
              <PartyCompChips partySpecs={summaryQuery.data.topPartySpecs} />
            </Paper>
          </Box>

          <Paper elevation={0} sx={panelSx}>
            <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
              Top items per slot
            </Typography>
            <SlotItemsSection slots={itemsQuery.data.slots} />
          </Paper>
        </>
      )}
    </Box>
  );
}
