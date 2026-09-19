import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import CastleIcon from "@mui/icons-material/Castle";
import { Link, useLocation } from "react-router-dom";

const NAV_ITEMS = [
  { to: "/", label: "Dashboard" },
  { to: "/gearscope", label: "GearScope" },
];

/** Sticky top bar shared by every page, with navigation between the dashboard and GearScope. */
export function AppHeader() {
  const location = useLocation();

  return (
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
        <Box sx={{ display: "flex", gap: 1 }}>
          {NAV_ITEMS.map((item) => {
            const active = location.pathname === item.to;
            return (
              <Button
                key={item.to}
                component={Link}
                to={item.to}
                size="small"
                color={active ? "primary" : "inherit"}
                variant={active ? "outlined" : "text"}
              >
                {item.label}
              </Button>
            );
          })}
        </Box>
      </Toolbar>
    </AppBar>
  );
}
