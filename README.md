# Integral

> Verify that your players didn't modify their modpack you gave them

## Features

- Operators can request mod and resource packs lists from players
- These lists are also sent automatically on join and resource reload
- The server will log the lists to the console and to Discord (see `sendListsToDiscord` below)
- It will also log whenever the mod isn't installed client-side and can differentiate between Java and Bedrock/Geyser
  players

### Drawbacks

**For server owners:** This mod doesn't protect your server on its own; it's possible for hackers to
spoof their lists, which is why you should still use other anticheat measures.
That said, if you are fairly certain that your playerbase won't do this
(e.g. they pay for server access, you know them in real life) this mod should work on its own.

**For players:** Currently this mod won't tell you if a server uses Integral,
meaning servers can see your mods and resource packs without you knowing. This is set to change in the future.
For the moment, you should disable the mod when playing on foreign servers.

## Commands

All commands start with `/integral` and require operator status (= permission level 4)

| Subcommand            | Description                                                                                                                                                                                         |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `set_modpack`         | Sends the mod and resource packs lists of the executing player´to the server. When `compareLists` is enabled (see below), all lists will indicate what mods players added/removed from the modpack. |
| `get <TYPE> <PLAYER>` | Requests a mod or resource pack list from the specified player and sends it to the executing player once received.                                                                                  | 
| `reload`              | Reloads the server-side configuration (both `integral.json` and `integral_modpack.json`)                                                                                                            | 

## Config options

| Option                         | Type    | Default | Description                                                                                                                                                                                       |
|--------------------------------|---------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enableModInSingleplayer`      | boolean | `false` | Whether Integral should work in singleplayer.                                                                                                                                                     |
| `compareLists`                 | boolean | `true`  | When this is enabled and `/integral set_modpack` has been run at least once, Integral will only log differences between player lists and the modpack.                                             |
| `includeOverlaps`              | boolean | `false` | When `compareLists` is true, this option will cause Integral to only print changes to the modpack.                                                                                                |
| `requestModsOnJoin`            | boolean | `true`  | Whether a mod list should be requested from players with Integral when they join the server.                                                                                                      |
| `requestResourcePacksOnJoin`   | boolean | `true`  | Same as `requestModsOnJoin` but for resource packs.                                                                                                                                               |
| `requestResourcePacksOnReload` | boolean | `true`  | Players could spoof their resource pack list by enabling resource packs after joining. This option combats this by resending the resource pack list when the player reloads their resource packs. |
| `reportConformingPlayers`      | boolean | `false` | When `compareLists` is enabled and no list changes are present, this causes the player to still be logged.                                                                                        |
| `reportPlayersWithoutMod`      | boolean | `true`  | Logs players who don't have Integral installed, since they can't respond to list requests. Players who are connected through Geyser aren't affected by this option.                               |
| `reportGeyserPlayers`          | boolean | `false` | Include Geyser players in `reportPlayersWithoutMod`.                                                                                                                                              |
| `sendListsToDiscord`           | boolean | `false` | When [Discord Integration](https://modrinth.com/plugin/dcintegration) is installed, lists are logged to the command log channel (if configured).                                                  |
