import { useQuery } from "@tanstack/react-query";
import { fetchPlayerDetail } from "../api/statsApi";

/** Loads the per-character breakdown for a single player, only once their row is expanded. */
export function usePlayerDetail(playerKey: string | null) {
  return useQuery({
    queryKey: ["playerDetail", playerKey],
    queryFn: () => fetchPlayerDetail(playerKey!),
    enabled: playerKey !== null,
    staleTime: 30_000,
  });
}
