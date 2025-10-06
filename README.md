# Integral

> Verify that your players didn't modify their modpack you gave them

## Features

- When joining a server which has the mod installed for the first time,
  players can choose which data they want to share.
- Clients with the mod send a list of all enabled mods and resource packs
  to the server
- The server will compare both lists to the original modpack and log differences
- It will also log whenever the mod isn't installed or if the player uses Geyser

### Drawbacks

This mod doesn't protect your server on its own; it's possible for hackers to
spoof their lists, which is why you should still use other anticheat measures.

## Config options

| Option(s)                                   | Possible values                          | Default      | Description                                                                                                                                                                                                                           |
|---------------------------------------------|------------------------------------------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `requestMods`, `requestResourcePacks`       | `"required"`, `"optional"`, `"disabled"` | `"optional"` | Determines whether clients should be asked to send mod/resource pack lists. When these options are set to `"required"` and a client refuses to send lists, they get kicked.                                                           |
| `requestResourcePacksOnReload`              | `true`, `false`                          | `true`       | When this is enabled, resource pack lists are resent whenever the client reloads resources. This prevents players from enabling other packs after joining to spoof their list.                                                        | 
| `logToFile`, `logToConsole`, `logToDiscord` | `"all"`, `"important"`, `"none"`         | `false`      | Where to log player diffs and when. `important` only logs when the mod isn't installed or when a change was detected. For `logToDiscord` to work, [Discord Integration](https://modrinth.com/plugin/dcintegration) must be installed. |
| `warnPlayersOnViolation`                    | `true`, `false`                          | `false`      | When an important log is generated (see `logTo*`), sends a private message to the offending player to inform them of the violation.                                                                                                   |
