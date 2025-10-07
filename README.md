# Integral

> Verify that your players didn't modify their modpack you gave them

## Features

- When joining a server which has the mod installed for the first time,
  players see a client-side message informing them that the server collects client data.
- Clients with the mod send a list of all enabled mods and resource packs
  to the server
- The server will compare both lists to the original modpack and log differences
- It will also log whenever the mod isn't installed or if the player uses Geyser

### Drawbacks

This mod doesn't protect your server on its own; it's possible for hackers to
spoof their lists, which is why you should still use other anticheat measures.

That said, if you are fairly certain that your playerbase won't do this
(e.g. they pay for server access, you know them in real life) this mod should work on its own.

Note that Integral will never take any action (kick/ban), it will just report the mod lists.

## Config options

| Option                         | Type                                    | Default               | Description                                                                                                                                                                                       |
|--------------------------------|-----------------------------------------|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enableModInSingleplayer`      | boolean                                 | `false`               | Whether Integral should work in singleplayer.                                                                                                                                                     |
| `requestModsOnJoin`            | boolean                                 | `true`                | Whether a mod list should be requested from players with Integral when they join the server.                                                                                                      |
| `requestResourcePacksOnJoin`   | boolean                                 | `true`                | Same as `requestModsOnJoin` but for resource packs.                                                                                                                                               |
| `requestResourcePacksOnReload` | boolean                                 | `true`                | Players could spoof their resource pack list by enabling resource packs after joining. This option combats this by resending the resource pack list when the player reloads their resource packs. |
| `modProperties`                | list of [`"ID"`, `"NAME"`, `"VERSION"`] | `["NAME", "VERSION"]` | Controls which information should be requested in mod list requests.                                                                                                                              |
| `resourcePackProperties`       | list of [`"ID"`, `"NAME"`]              | `["NAME"]`            | Controls which information should be requested in resource pack list requests.                                                                                                                    |
| `logPlayersWithoutMod`         | boolean                                 | `true`                | Logs players who don't have Integral installed, since they can't respond to list requests. Players who are connected through Geyser aren't affected by this option.                               |
| `logGeyserPlayers`             | boolean                                 | `false`               | Include Geyser players in `logPlayersWithoutMod`.                                                                                                                                                 |
| `sendListsToDiscord`           | boolean                                 | `false`               | When [Discord Integration](https://modrinth.com/plugin/dcintegration) is installed, lists are logged to the command log channel (if configured).                                                  |