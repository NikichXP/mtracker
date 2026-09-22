import Accordion from "@mui/material/Accordion";
import AccordionDetails from "@mui/material/AccordionDetails";
import AccordionSummary from "@mui/material/AccordionSummary";
import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import Typography from "@mui/material/Typography";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { slotLabel, sortSlots } from "../../utils/slotLabel";
import { ItemUsageCard } from "./ItemUsageCard";
import type { SlotReportDto } from "../../api/types";

interface Props {
  slots: SlotReportDto[];
}

export function SlotItemsSection({ slots }: Props) {
  if (slots.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No gear data yet for this spec.
      </Typography>
    );
  }

  const ordered = sortSlots(slots, (report) => report.slot);

  return (
    <Box>
      {ordered.map((report) => (
        <Accordion
          key={report.slot}
          disableGutters
          elevation={0}
          sx={{ bgcolor: "transparent", "&:before": { display: "none" } }}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <Typography sx={{ fontWeight: 600 }}>{slotLabel(report.slot)}</Typography>
              <Chip
                size="small"
                label={`${report.topItems.length} item${report.topItems.length === 1 ? "" : "s"}`}
                variant="outlined"
              />
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
              {report.topItems.map((item) => (
                <ItemUsageCard key={item.itemId} item={item} />
              ))}
            </Box>
          </AccordionDetails>
        </Accordion>
      ))}
    </Box>
  );
}
