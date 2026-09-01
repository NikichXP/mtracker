import { useQuery } from "@tanstack/react-query";
import { fetchAvailableWeeks } from "../api/statsApi";

export function useAvailableWeeks() {
  return useQuery({
    queryKey: ["availableWeeks"],
    queryFn: fetchAvailableWeeks,
    staleTime: 5 * 60_000,
  });
}
