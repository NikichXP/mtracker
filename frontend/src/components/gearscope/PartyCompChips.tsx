import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import Typography from "@mui/material/Typography";
import { roleVisual } from "../../utils/role";
import type { PartySpecUsageDto } from "../../api/types";

interface Props {
  partySpecs: PartySpecUsageDto[];
}

export function PartyCompChips({ partySpecs }: Props) {
  if (partySpecs.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No party composition data recorded.
      </Typography>
    );
  }

  return (
    <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.75 }}>
      {partySpecs.map((partySpec) => {
        const visual = roleVisual(partySpec.role);
        const Icon = visual.icon;
        return (
          <Chip
            key={partySpec.specId}
            size="small"
            icon={<Icon sx={{ color: `${visual.color} !important` }} fontSize="small" />}
            label={`${partySpec.className} ${partySpec.specName} · ${partySpec.count}`}
            variant="outlined"
            sx={{ borderColor: visual.color, color: visual.color }}
          />
        );
      })}
    </Box>
  );
}
