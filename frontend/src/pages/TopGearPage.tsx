import { useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Container from "@mui/material/Container";
import Typography from "@mui/material/Typography";
import { AppHeader } from "../components/AppHeader";
import { SpecDetailPanel } from "../components/topgear/SpecDetailPanel";
import { SpecListPanel } from "../components/topgear/SpecListPanel";
import { useSpecOverview } from "../hooks/useSpecOverview";

export function TopGearPage() {
  const { data, isLoading, isError } = useSpecOverview();
  const specs = useMemo(() => [...(data ?? [])].sort((a, b) => b.parseCount - a.parseCount), [data]);
  const [selectedSpecId, setSelectedSpecId] = useState<number | null>(null);

  const selectedSpec = selectedSpecId !== null ? (specs.find((s) => s.specId === selectedSpecId) ?? null) : null;

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppHeader />

      <Container maxWidth={false} sx={{ py: { xs: 2, sm: 4 }, px: { xs: 2, sm: "6%", md: "10%" } }}>
        <Typography variant="h5" sx={{ mb: 0.5 }}>
          TopGear
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          What top-ranked EU players actually wear, per spec.
        </Typography>

        {isLoading ? (
          <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
            <CircularProgress />
          </Box>
        ) : isError ? (
          <Alert severity="error">Failed to load spec list</Alert>
        ) : (
          <Box sx={{ display: "flex", gap: 3, flexDirection: { xs: "column", md: "row" }, alignItems: "flex-start" }}>
            <Box sx={{ width: { xs: "100%", md: 320 }, flexShrink: 0 }}>
              <SpecListPanel specs={specs} selectedSpecId={selectedSpecId} onSelect={setSelectedSpecId} />
            </Box>
            <Box sx={{ flex: 1, minWidth: 0, width: "100%" }}>
              {selectedSpec ? (
                <SpecDetailPanel spec={selectedSpec} />
              ) : (
                <Alert severity="info">Select a spec to see its most-used gear.</Alert>
              )}
            </Box>
          </Box>
        )}
      </Container>
    </Box>
  );
}
