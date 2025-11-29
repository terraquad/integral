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

This mod doesn't protect your server on its own; it's possible for hackers to
spoof their lists, which is why you should still use other anticheat measures.
Additionally, unlike other anticheat mods, Integral will never kick/ban players.

**Recommendation:** Install a mod which increases the chat line limit
(e.g. [More Chat History](https://modrinth.com/mod/morechathistory)), since `/integral get` results may send more than
100 lines.

## Commands

All commands start with `/integral` and require operator status (= permission level 4)

| Subcommand                                 | Description                                                                                                                                                                                                                                                                    |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `set_modpack`                              | Sends the mod and resource packs lists of the executing player to the server. See `compareLists` below.                                                                                                                                                                        |
| `get <PLAYER> <TYPE> [SUMMARY] [OVERLAPS]` | Requests a mod or resource pack list from the specified player and sends it to the executing player once received. `[SUMMARY]` returns a list summary instead of the actual list and `[OVERLAPS]` toggles mods present on both client and server (does nothing with summaries) | 
| `reload`                                   | Reloads the server-side configuration (both `integral.json` and `integral_modpack.json`)                                                                                                                                                                                       | 
| `config <KEY> [VALUE]`                     | Retrieves/changes the config value at `KEY`.                                                                                                                                                                                                                                   |

## Config options

| Option                         | Default | Description                                                                                                                                                                                        |
|--------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enableModInSingleplayer`      | `false` | Whether Integral should enable/disable itself when it detects that the client is in singleplayer.                                                                                                  |
| `compareLists`                 | `true`  | When this is enabled and `/integral set_modpack` has been run at least once, Integral will show which mods were added/removed from the client modpack.                                             |
| `includeOverlaps`              | `true`  | When `compareLists` is true, this option will cause Integral to show mods existing on both sides. This includes version differences.                                                               |
| `requestModsOnJoin`            | `true`  | Whether a mod list should be requested from players with Integral when they join the server.                                                                                                       |
| `requestResourcePacksOnJoin`   | `true`  | Same as `requestModsOnJoin` but for resource packs.                                                                                                                                                |
| `requestResourcePacksOnReload` | `true`  | Players could spoof their resource pack list by enabling resource packs after joining. This option combats this by requesting the resource pack list when the player reloads their resource packs. |
| `reportConformingPlayers`      | `false` | When `compareLists` is enabled and no list changes are present, affected players will get logged anyways with this option.                                                                         |
| `reportPlayersWithoutMod`      | `true`  | Logs players who don't have Integral installed, since they can't respond to list requests. Players who are connected through Geyser aren't affected by this option.                                |
| `reportGeyserPlayers`          | `true`  | Include Geyser players in `reportPlayersWithoutMod`; they get a special log message.                                                                                                               |
| `sendListsToDiscord`           | `false` | When [Discord Integration](https://modrinth.com/plugin/dcintegration) is installed, lists are logged to the command log channel (if configured).                                                   |
| `summarizeToOperators`         | `false` | When a list request is collected, Integral will send a summary of the results to all operators.                                                                                                    |
| `summarizeEverywhere`          | `false` | Like `summarizeToOperators`, but replacing every occasion where list contents would be logged with a summary.                                                                                      |