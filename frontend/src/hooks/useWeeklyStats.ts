import { useQuery } from "@tanstack/react-query";
import { fetchWeeklyStats } from "../api/statsApi";

export function useWeeklyStats(week: string | null) {
  return useQuery({
    queryKey: ["weeklyStats", week],
    queryFn: () => fetchWeeklyStats(week!),
    enabled: week !== null && week.length > 0,
    staleTime: 60_000,
  });
}
