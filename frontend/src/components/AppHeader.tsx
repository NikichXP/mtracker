import { useState } from "react";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import CastleIcon from "@mui/icons-material/Castle";
import { useQueryClient } from "@tanstack/react-query";
import { Link, useLocation } from "react-router-dom";
import { API_TARGETS, getApiTargetId, setApiTarget, type ApiTargetId } from "../api/client";

const NAV_ITEMS = [
  { to: "/", label: "Dashboard" },
  { to: "/topgear", label: "TopGear" },
];

const IS_LOCALHOST = ["localhost", "127.0.0.1"].includes(window.location.hostname);

export function AppHeader() {
  const location = useLocation();
  const queryClient = useQueryClient();
  const [apiTarget, setApiTargetState] = useState<ApiTargetId>(getApiTargetId);

  const handleApiTargetChange = (id: ApiTargetId) => {
    setApiTarget(id);
    setApiTargetState(id);
    queryClient.invalidateQueries();
  };

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
        <Box sx={{ display: "flex", gap: 1, alignItems: "center" }}>
          {IS_LOCALHOST && (
            <Select
              size="small"
              value={apiTarget}
              onChange={(e) => handleApiTargetChange(e.target.value as ApiTargetId)}
              sx={{ mr: 1, fontSize: "0.8rem" }}
            >
              {API_TARGETS.map((t) => (
                <MenuItem key={t.id} value={t.id}>
                  API: {t.label}
                </MenuItem>
              ))}
            </Select>
          )}
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
