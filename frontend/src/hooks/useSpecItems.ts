import { useQuery } from "@tanstack/react-query";
import { fetchSpecItems } from "../api/topGearApi";

export function useSpecItems(specId: number | null, excludeRaid: boolean) {
  return useQuery({
    queryKey: ["topGearSpecItems", specId, excludeRaid],
    queryFn: () => fetchSpecItems(specId!, excludeRaid),
    enabled: specId !== null,
    staleTime: 60_000,
  });
}
