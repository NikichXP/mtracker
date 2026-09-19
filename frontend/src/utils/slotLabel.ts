const SLOT_LABELS: Record<string, string> = {
  head: "Head",
  neck: "Neck",
  shoulder: "Shoulder",
  back: "Back",
  chest: "Chest",
  shirt: "Shirt",
  tabard: "Tabard",
  wrist: "Wrist",
  hands: "Hands",
  waist: "Waist",
  legs: "Legs",
  feet: "Feet",
  finger1: "Ring 1",
  finger2: "Ring 2",
  trinket1: "Trinket 1",
  trinket2: "Trinket 2",
  mainhand: "Main Hand",
  offhand: "Off Hand",
};

export function slotLabel(slot: string): string {
  return SLOT_LABELS[slot] ?? slot.charAt(0).toUpperCase() + slot.slice(1);
}

const SLOT_ORDER = Object.keys(SLOT_LABELS);

/** Orders gear slots the way a character sheet would, falling back to alphabetical for unknown slots. */
export function sortSlots<T>(items: T[], getSlot: (item: T) => string): T[] {
  return [...items].sort((a, b) => {
    const indexA = SLOT_ORDER.indexOf(getSlot(a));
    const indexB = SLOT_ORDER.indexOf(getSlot(b));
    if (indexA === -1 && indexB === -1) return getSlot(a).localeCompare(getSlot(b));
    if (indexA === -1) return 1;
    if (indexB === -1) return -1;
    return indexA - indexB;
  });
}
