import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { gearSourceVisual } from "../../utils/gearSource";
import { sortedStatEntries, statLabel } from "../../utils/statLabels";
import { wowIconUrl } from "../../utils/wowIcon";
import type { GearItemUsageDto } from "../../api/types";

interface Props {
  item: GearItemUsageDto;
}

export function ItemUsageCard({ item }: Props) {
  const source = gearSourceVisual(item.source);
  const stats = sortedStatEntries(item.stats);
  const gems = item.topGems.filter((v): v is string => Boolean(v));

  return (
    <Box
      sx={{
        display: "flex",
        gap: 1.25,
        p: 1.25,
        borderRadius: 1,
        border: "1px solid rgba(255,255,255,0.08)",
        bgcolor: "rgba(255,255,255,0.02)",
        minWidth: 0,
        maxWidth: "100%",
        boxSizing: "border-box",
      }}
    >
      <Box
        component="img"
        src={wowIconUrl(item.icon)}
        alt=""
        sx={{
          width: 40,
          height: 40,
          borderRadius: 0.5,
          flexShrink: 0,
          border: "1px solid rgba(255,255,255,0.15)",
        }}
        onError={(e) => {
          (e.currentTarget as HTMLImageElement).style.visibility = "hidden";
        }}
      />
      <Box sx={{ minWidth: 0, flex: 1 }}>
        <Typography variant="body2" noWrap sx={{ fontWeight: 600 }} title={item.itemName ?? undefined}>
          {item.itemName ?? `Item ${item.itemId}`}
        </Typography>
        <Box sx={{ display: "flex", alignItems: "center", gap: 0.75, flexWrap: "wrap", mt: 0.25, mb: 0.5 }}>
          <Chip
            size="small"
            label={source.label}
            variant="outlined"
            sx={{ borderColor: source.color, color: source.color, height: 20, fontSize: "0.7rem" }}
          />
          <Typography variant="caption" color="text.secondary">
            {(item.usageShare * 100).toFixed(0)}% · {item.usageCount} parses
            {item.avgItemLevel ? ` · ${item.avgItemLevel.toFixed(0)} iLvl` : ""}
          </Typography>
        </Box>
        {stats.length > 0 && (
          <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
            {stats.map(([stat, value]) => `${statLabel(stat)} ${value}`).join(" · ")}
          </Typography>
        )}
        {gems.length > 0 && (
          <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
            {gems.join(" · ")}
          </Typography>
        )}
        {item.topEnchant && (
          <Typography
            variant="caption"
            sx={{ display: "block", color: "#ce93d8", fontWeight: 600 }}
          >
            {item.topEnchant}
          </Typography>
        )}
        {item.sourceDetail && (
          <Tooltip title={item.sourceDetail}>
            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ display: "block", fontStyle: "italic" }}
              noWrap
            >
              {item.sourceDetail}
            </Typography>
          </Tooltip>
        )}
      </Box>
    </Box>
  );
}
