import axios from "axios";

// Empty string = same-origin relative requests. The k3s Ingress for mtracker.nikichxp.xyz routes
// the "/api" path prefix to mtracker-service and everything else to mtracker-ui (see
// infra-scripts/manifests/mtracker/30-ingress.yaml), so the UI and API share one host/domain and
// no cross-origin base URL is needed by default.
const baseURL = import.meta.env.VITE_API_BASE_URL ?? "";

/** Shared axios instance for all API calls. Once the backend adds authentication, the
 *  Authorization header attachment and 401 handling belong here as interceptors, so every
 *  module using `apiClient` (stats, players, ...) gets it for free. */
export const apiClient = axios.create({ baseURL, timeout: 15_000 });
