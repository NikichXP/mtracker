import { useQuery } from "@tanstack/react-query";
import { fetchSpecOverview } from "../api/topGearApi";

export function useSpecOverview() {
  return useQuery({
    queryKey: ["topGearSpecs"],
    queryFn: fetchSpecOverview,
    staleTime: 5 * 60_000,
  });
}
