import { useState } from "react";
import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Tab from "@mui/material/Tab";
import Tabs from "@mui/material/Tabs";
import { AppHeader } from "../components/AppHeader";
import { OverviewTab } from "../components/OverviewTab";
import { WeeklyTab } from "../components/WeeklyTab";

type TabKey = "overview" | "weekly";

export function DashboardPage() {
  const [tab, setTab] = useState<TabKey>("overview");

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppHeader />

      <Container maxWidth="lg" sx={{ py: { xs: 2, sm: 4 }, px: { xs: 1.5, sm: 3 } }}>
        <Tabs
          value={tab}
          onChange={(_, v: TabKey) => setTab(v)}
          textColor="primary"
          indicatorColor="primary"
          sx={{ mb: 3 }}
        >
          <Tab value="overview" label="Overview" />
          <Tab value="weekly" label="Weekly" />
        </Tabs>

        {tab === "overview" ? <OverviewTab /> : <WeeklyTab />}
      </Container>
    </Box>
  );
}
