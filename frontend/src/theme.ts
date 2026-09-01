import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "dark",
    primary: { main: "#7c9cff" },
    secondary: { main: "#f48fb1" },
    success: { main: "#4caf50" },
    warning: { main: "#ffb74d" },
    error: { main: "#f44336" },
    background: {
      default: "#0f1115",
      paper: "#171a21",
    },
  },
  shape: {
    borderRadius: 12,
  },
  typography: {
    fontFamily: ['"Roboto"', '"Helvetica"', '"Arial"', "sans-serif"].join(","),
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: "none",
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: "1px solid rgba(255,255,255,0.08)",
        },
      },
    },
  },
});
