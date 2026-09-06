import { useQuery } from "@tanstack/react-query";
import { fetchRecentRuns } from "../api/statsApi";

/** Loads the most recent stored keystone runs (with roster) for a player, only once expanded. */
export function useRecentRuns(playerKey: string | null) {
  return useQuery({
    queryKey: ["recentRuns", playerKey],
    queryFn: () => fetchRecentRuns(playerKey!),
    enabled: playerKey !== null,
    staleTime: 30_000,
  });
}
