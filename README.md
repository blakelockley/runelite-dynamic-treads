# Dynamic Treads

![Dynamic Treads](https://raw.githubusercontent.com/blakelockley/runelite-dynamic-treads/main/screenshots/dynamic-treads.png)

![Dynamic Treads gif](https://raw.githubusercontent.com/blakelockley/runelite-dynamic-treads/main/screenshots/demo.gif)

Avernic Treads change their look to match how you're fighting. Wield a scimitar and they render as Primordial Boots; switch to a bow and they become Pegasian; equip a staff and they're Eternal. Purely visual — only you see the swap, your stats and the actual equipped item are untouched.

## How it works

The plugin watches the boots slot of your character. When it finds an Avernic Treads variant equipped, it inspects your main-hand weapon's combat stats to decide which style you're using (highest of stab/slash/crush → melee, vs. ranged, vs. magic) and overrides the local-only render to show the corresponding boot.

Each Avernic Treads variant only transmogs into styles it actually carries:

| Variant | ID | Transmogs into |
|---|---|---|
| (pr) | 31091 | Melee only |
| (pe) | 31092 | Ranged only |
| (et) | 31093 | Magic only |
| (pr)(pe) | 31094 | Melee, Ranged |
| (pr)(et) | 31095 | Melee, Magic |
| (pe)(et) | 31096 | Ranged, Magic |
| (max) | 31097 | All three |

When the weapon's style isn't covered by the variant (e.g. a bow in (pr)(et) treads), or you're unarmed, the configured **Default boot** is shown instead.

## Config

| Option | Choices | Default |
|---|---|---|
| **Default boot** | Avernic Treads (max), Avernic Treads, Spiked manacles | Avernic Treads (max) |
| **Melee boot** | Primordial boots, Dragon boots, Spiked manacles | Primordial boots |
| **Ranged boot** | Pegasian boots, Ranger boots | Pegasian boots |
| **Magic boot** | Eternal boots, Infinity boots | Eternal boots |

## Notes

- **Client-side only.** Other players, screenshots they take, and the game server all see the real Avernic Treads. Stats, set bonuses, and weight are unaffected.
- **Cleanup on disable.** Turning the plugin off restores the real boots model immediately.
- **Plain Avernic Treads (31088) is not a transmog source** — wearing it produces no swap. It's available as a Default-boot choice for users who want it as their non-style look.

## License

BSD 2-Clause. See [LICENSE](LICENSE).
