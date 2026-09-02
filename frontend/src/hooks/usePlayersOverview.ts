import { useQuery } from "@tanstack/react-query";
import { fetchPlayersOverview } from "../api/statsApi";

const REFRESH_INTERVAL_MS = 60_000;

export function usePlayersOverview() {
  return useQuery({
    queryKey: ["playersOverview"],
    queryFn: fetchPlayersOverview,
    staleTime: 30_000,
    refetchInterval: REFRESH_INTERVAL_MS,
  });
}
