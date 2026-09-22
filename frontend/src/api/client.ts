import axios from "axios";

// Empty string = same-origin relative requests. The k3s Ingress for mtracker.nikichxp.xyz routes
// the "/api" path prefix to mtracker-service and everything else to mtracker-ui (see
// infra-scripts/manifests/mtracker/30-ingress.yaml), so the UI and API share one host/domain and
// no cross-origin base URL is needed by default.
const envBaseURL = import.meta.env.VITE_API_BASE_URL ?? "";

const STORAGE_KEY = "mtracker.apiBaseUrl";

export const API_TARGETS = [
  { id: "local", label: "localhost", baseURL: envBaseURL },
  { id: "deployed", label: "mtracker.nikichxp.xyz", baseURL: "https://mtracker.nikichxp.xyz" },
] as const;

export type ApiTargetId = (typeof API_TARGETS)[number]["id"];

export function getApiTargetId(): ApiTargetId {
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored === "deployed" ? "deployed" : "local";
}

/** Shared axios instance for all API calls. Once the backend adds authentication, the
 *  Authorization header attachment and 401 handling belong here as interceptors, so every
 *  module using `apiClient` (stats, players, ...) gets it for free. */
export const apiClient = axios.create({ baseURL: envBaseURL, timeout: 15_000 });

export function setApiTarget(id: ApiTargetId) {
  localStorage.setItem(STORAGE_KEY, id);
  const target = API_TARGETS.find((t) => t.id === id) ?? API_TARGETS[0];
  apiClient.defaults.baseURL = target.baseURL;
}

// Restore the persisted target on startup (no-op when "local" is stored).
setApiTarget(getApiTargetId());
