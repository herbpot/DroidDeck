# STOVE store preview (DroidDeck fork)

The STOVE chip in **Stores** is an **existing-client launcher**, not an unofficial STOVE catalog or API login. The client continues to manage its own Google sign-in, updates, downloads, and game data under the existing Proton prefix. No login tokens are stored by DroidDeck.

## Requirements

1. Apply `DroidDeck_STOVE_Unified_20261025.zip` **inside DroidDeck Linux desktop** using its documented `stove-manager.sh --doctor`, `--setup`, and `--verify` flow.
2. Confirm `/root/.local/bin/droiddeck-stove` exists in the DroidDeck Linux runtime. Do not create a dummy executable; it should be the real tested STOVE wrapper.
3. Open **Stores → STOVE → Launch STOVE**. If the desktop is not running, DroidDeck registers an LXQt autostart entry for **one launch** and opens the Linux desktop. This entry removes itself before invoking the launcher. The original STOVE installation is not changed.
4. When the desktop is already running, open the desktop and launch `droiddeck-stove` there; no delayed autostart is left behind.
5. If Google sign-in cannot return to the client, diagnose `sgup://` and `sgupgate8://` with the Linux bridge, not an Android WebView sign-in flow.

The one-shot entry is `/root/.config/autostart/droiddeck-stove-once.desktop` in the guest rootfs. Remove only that file to cancel a pending launch. Installing DroidDeck alone does not install STOVE/Proton/WebView2, and an APK build by itself cannot validate a third-party login round trip.

No STOVE game-library API, direct download, automatic retry, game imports, store auth state, or proprietary Windows binaries are implemented. Do not represent those as tested.
