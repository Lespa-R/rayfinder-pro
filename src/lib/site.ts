// Informations partagées par le site et l'app Android (même éditeur, même backend).
export const APP_NAME = "RayFinder Pro";
export const ANDROID_PACKAGE = "me.datagrow.rayfinderpro";
export const PLAY_STORE_URL = `https://play.google.com/store/apps/details?id=${ANDROID_PACKAGE}`;
// Passer à true une fois l'app publiée en production sur Google Play.
export const PLAY_STORE_LIVE = false;
export const CONTACT_EMAIL = "lespa+rayfinder@datagrow.me";

export const PUBLISHER = {
  name: "SASU DataGrow",
  siret: "938 744 802 00027",
  address: "22 chemin du Cap Bernard, 97400 Saint-Denis, La Réunion, France",
  director: "Robin Lespagnol",
  website: "https://datagrow.me",
} as const;

export const LEGAL_UPDATED = "8 octobre 2026";
