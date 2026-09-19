const ICON_BASE_URL = "https://wow.zamimg.com/images/wow/icons/medium";

export function wowIconUrl(icon: string | null): string {
  return `${ICON_BASE_URL}/${icon ?? "inv_misc_questionmark"}.jpg`;
}
