import { useQuery } from "@tanstack/react-query";
import { fetchSpecOverview } from "../api/gearScopeApi";

export function useSpecOverview() {
  return useQuery({
    queryKey: ["gearScopeSpecs"],
    queryFn: fetchSpecOverview,
    staleTime: 5 * 60_000,
  });
}
