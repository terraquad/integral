# Integral

> Verify that your players didn't edit the modpack you gave them

## Features

- Operators can request mod and resource packs lists from players
- These lists are also sent automatically on join and resource reload
- The server will log the lists to the console and to Discord (see `sendListsToDiscord` below)
- It will also log whenever the mod isn't installed client-side and can differentiate between Java and Bedrock/Geyser
  players
- Players will be notified when servers send list requests for the first time

### Drawbacks

**For server owners:** This mod doesn't protect your server on its own; it's possible for hackers to
spoof their lists, which is why you should still use other anticheat measures.
Additionally, unlike other anticheat mods, Integral will never kick/ban players.

## Commands

All commands start with `/integral` and require operator status (= permission level 4)

| Subcommand            | Description                                                                                                        |
|-----------------------|--------------------------------------------------------------------------------------------------------------------|
| `set_modpack`         | Sends the mod and resource packs lists of the executing player to the server. See `compareLists` below.            |
| `get <PLAYER> <TYPE>` | Requests a mod or resource pack list from the specified player and sends it to the executing player once received. | 
| `reload`              | Reloads the server-side configuration (both `integral.json` and `integral_modpack.json`)                           | 

## Config options

| Option                         | Default | Description                                                                                                                                                                                       |
|--------------------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enableModInSingleplayer`      | `false` | Whether Integral should enable/disable itself when it detects that the client is in singleplayer.                                                                                                 |
| `compareLists`                 | `true`  | When this is enabled and `/integral set_modpack` has been run at least once, Integral will show which mods were added/removed from the client modpack.                                            |
| `excludeOverlaps`              | `false` | When `compareLists` is true, this option will cause Integral to only print changes to the modpack.                                                                                                |
| `requestModsOnJoin`            | `true`  | Whether a mod list should be requested from players with Integral when they join the server.                                                                                                      |
| `requestResourcePacksOnJoin`   | `true`  | Same as `requestModsOnJoin` but for resource packs.                                                                                                                                               |
| `requestResourcePacksOnReload` | `true`  | Players could spoof their resource pack list by enabling resource packs after joining. This option combats this by resending the resource pack list when the player reloads their resource packs. |
| `reportConformingPlayers`      | `false` | When `compareLists` is enabled and no list changes are present, this causes the player to still be logged.                                                                                        |
| `reportPlayersWithoutMod`      | `true`  | Logs players who don't have Integral installed, since they can't respond to list requests. Players who are connected through Geyser aren't affected by this option.                               |
| `reportGeyserPlayers`          | `false` | Include Geyser players in `reportPlayersWithoutMod`; they get a special log message.                                                                                                              |
| `sendListsToDiscord`           | `false` | When [Discord Integration](https://modrinth.com/plugin/dcintegration) is installed, lists are logged to the command log channel (if configured).                                                  |
