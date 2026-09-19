import { useQuery } from "@tanstack/react-query";
import { fetchSpecItems } from "../api/gearScopeApi";

export function useSpecItems(specId: number | null, excludeRaid: boolean) {
  return useQuery({
    queryKey: ["gearScopeSpecItems", specId, excludeRaid],
    queryFn: () => fetchSpecItems(specId!, excludeRaid),
    enabled: specId !== null,
    staleTime: 60_000,
  });
}
