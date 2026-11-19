# Jarry's Holy Bible (Java) 1.0.0 - state-safety review

**Gift flag.** `jarrys_holy_bible:gifted` is a Fabric data attachment: persistent (Codec.BOOL), `copyOnDeath`, not synced.
It lives in the player's NBT (`fabric:attachments`). This was confirmed in a dev save: the second join gave no second Bible.
- The flag is set *before* the item is handed over, so a crash mid-gift can never produce two. Worst case is zero, and a Bible can be crafted.
- Death, respawn, End exit, dimension change and relog all keep the flag. Death uses copyOnDeath, and Fabric copies attachments on non-death respawn.
- Existing players in an existing world have no flag, so each gets one Bible on their first join after the mod is installed. That's intended and matches the Bedrock add-on.
- The JOIN event runs on the server thread. Inventory changes and entity spawns happen on that thread too.
- Full inventory: the item is dropped exactly at the feet. It has no pickup delay and unlimited lifetime, so it doesn't despawn after 5 minutes.

**Loot.** `LootTableEvents.MODIFY` gets a fresh builder on every data-pack reload, so `/reload` never stacks pools.
It matches the 16 `minecraft:chests/village/*` keys by id, so a data pack or mod that replaces those tables still gets the pool. It never touches other tables.
The pool has 1 roll, a `random_chance` of 0.001 and 1 Bible. Measured: about 0.1% over 320,000 rolls per version.

**Client/server split.** Screen code lives only in the client source set. `BibleItem` holds a `Runnable` that's a no-op on a dedicated server, so no client class is loaded there.
This was checked by loading the production jars on real Fabric dedicated servers for all 5 versions.
- The reader is client-only state: the last-read position is kept in memory for the session and is never written to disk or sent to the server.
- Text cache: at most 4 books are decoded at once (synchronized). The index is about 6 KB. A book is read from the jar only when it's opened.

**Compatibility.** The mod is required on both sides, because Fabric registry sync rejects a client missing the item. It has no mixins and no access wideners.
If the mod is removed, the Bible items disappear (unknown registry entry). The leftover attachment tag is ignored.

**Dev-only code.** `SelfTest`/`ClientSelfTest` run only when `-Djarrys_holy_bible.selftest=true` or `clientselftest=true` is set; that's never in normal play.
