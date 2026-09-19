import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import { slotLabel, sortSlots } from "../../utils/slotLabel";
import { ItemUsageCard } from "./ItemUsageCard";
import type { SlotPickDto } from "../../api/types";

interface Props {
  slots: SlotPickDto[];
}

export function BestInSlotGrid({ slots }: Props) {
  if (slots.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No gear data yet for this spec.
      </Typography>
    );
  }

  const ordered = sortSlots(slots, (pick) => pick.slot);

  return (
    <Box sx={{ display: "grid", gap: 1.5, gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr", md: "1fr 1fr 1fr" } }}>
      {ordered.map((pick) => (
        <Box key={pick.slot}>
          <Typography
            variant="caption"
            color="text.secondary"
            sx={{ display: "block", mb: 0.5, textTransform: "uppercase", letterSpacing: 0.5 }}
          >
            {slotLabel(pick.slot)}
          </Typography>
          <ItemUsageCard item={pick.item} />
        </Box>
      ))}
    </Box>
  );
}
