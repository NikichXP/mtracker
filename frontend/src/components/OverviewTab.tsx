import { useState } from "react";
import Box from "@mui/material/Box";
import Tab from "@mui/material/Tab";
import Tabs from "@mui/material/Tabs";
import { GearTab } from "./GearTab";
import { BuddyTab } from "./BuddyTab";

type OverviewSubTab = "gear" | "buddy";

/** "Overview" tab: current/live stats for every tracked player, split into a "Gear" sub-tab
 *  (score/item level, sorted by score) and a "Buddy" sub-tab (ranked by how often a player
 *  groups with other tracked players rather than pugging). */
export function OverviewTab() {
  const [subTab, setSubTab] = useState<OverviewSubTab>("gear");

  return (
    <Box>
      <Tabs
        value={subTab}
        onChange={(_, v: OverviewSubTab) => setSubTab(v)}
        textColor="primary"
        indicatorColor="primary"
        sx={{ mb: 2, minHeight: 36 }}
      >
        <Tab value="gear" label="Gear" sx={{ minHeight: 36 }} />
        <Tab value="buddy" label="Buddy" sx={{ minHeight: 36 }} />
      </Tabs>

      {subTab === "gear" ? <GearTab /> : <BuddyTab />}
    </Box>
  );
}
