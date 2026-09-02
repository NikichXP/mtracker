import { useState } from "react";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Tab from "@mui/material/Tab";
import Tabs from "@mui/material/Tabs";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import CastleIcon from "@mui/icons-material/Castle";
import { OverviewTab } from "../components/OverviewTab";
import { WeeklyTab } from "../components/WeeklyTab";

type TabKey = "overview" | "weekly";

export function DashboardPage() {
  const [tab, setTab] = useState<TabKey>("overview");

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar
        position="sticky"
        color="transparent"
        elevation={0}
        sx={{ borderBottom: "1px solid rgba(255,255,255,0.08)", backdropFilter: "blur(6px)" }}
      >
        <Toolbar sx={{ gap: 1 }}>
          <CastleIcon sx={{ mr: { xs: 0.5, sm: 1.5 } }} color="primary" />
          <Typography variant="h6" component="div" noWrap sx={{ flexGrow: 1, minWidth: 0 }}>
            Mtracker — M+ Tracker
          </Typography>
        </Toolbar>
      </AppBar>

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
