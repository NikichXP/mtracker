import { apiClient } from "./client";
import type { SpecGearReportDto, SpecOverviewDto, SpecSummaryDto } from "./types";

export async function fetchSpecOverview(): Promise<SpecOverviewDto[]> {
  const { data } = await apiClient.get<SpecOverviewDto[]>("/api/v1/gearscope/specs");
  return data;
}

export async function fetchSpecItems(specId: number, excludeRaid: boolean): Promise<SpecGearReportDto> {
  const { data } = await apiClient.get<SpecGearReportDto>(`/api/v1/gearscope/specs/${specId}/items`, {
    params: { excludeRaid },
  });
  return data;
}

export async function fetchSpecSummary(specId: number, excludeRaid: boolean): Promise<SpecSummaryDto> {
  const { data } = await apiClient.get<SpecSummaryDto>(`/api/v1/gearscope/specs/${specId}/summary`, {
    params: { excludeRaid },
  });
  return data;
}
