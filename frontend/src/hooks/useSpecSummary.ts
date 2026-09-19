import { useQuery } from "@tanstack/react-query";
import { fetchSpecSummary } from "../api/gearScopeApi";

export function useSpecSummary(specId: number | null, excludeRaid: boolean) {
  return useQuery({
    queryKey: ["gearScopeSpecSummary", specId, excludeRaid],
    queryFn: () => fetchSpecSummary(specId!, excludeRaid),
    enabled: specId !== null,
    staleTime: 60_000,
  });
}
