import { apiClient } from "./client";
import type { PlayerDetailDto, PlayerOverviewDto, RecentRunDto, WeeklyPlayerStatsDto } from "./types";

export async function fetchPlayersOverview(): Promise<PlayerOverviewDto[]> {
  const { data } = await apiClient.get<PlayerOverviewDto[]>("/api/v1/stats/overview");
  return data;
}

export async function fetchPlayerDetail(playerKey: string): Promise<PlayerDetailDto> {
  const { data } = await apiClient.get<PlayerDetailDto>(`/api/v1/stats/players/${encodeURIComponent(playerKey)}`);
  return data;
}

export async function fetchRecentRuns(playerKey: string): Promise<RecentRunDto[]> {
  const { data } = await apiClient.get<RecentRunDto[]>(
    `/api/v1/stats/players/${encodeURIComponent(playerKey)}/runs`,
  );
  return data;
}

export async function fetchWeeklyStats(week: string): Promise<WeeklyPlayerStatsDto[]> {
  const { data } = await apiClient.get<WeeklyPlayerStatsDto[]>("/api/v1/stats/weekly", { params: { week } });
  return data;
}

export async function fetchAvailableWeeks(): Promise<string[]> {
  const { data } = await apiClient.get<string[]>("/api/v1/stats/weeks");
  return data;
}
