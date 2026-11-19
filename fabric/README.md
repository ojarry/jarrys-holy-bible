# Jarry's Holy Bible (Java / Fabric) 1.0.0

Fabric mod `jarrys_holy_bible` for Minecraft 26.3, 26.2, 26.1.2, 1.21.11 and 1.21.10.

- `common/`: shared code (item, loot, gift, KJV data, reader UI), resources and unit tests
- `shims/mc26`, `shims/mc1_21`: version-specific bridges (Identifier vs ResourceLocation, CreativeModeTabEvents vs ItemGroupEvents, GuiGraphicsExtractor vs GuiGraphics, gui.setScreen vs setScreen)
- `versions/<mc>/`: one Loom build per Minecraft version (`prepare.py` copies the right shim in)
- `tools/build_bible.py`: regenerates `assets/jarrys_holy_bible/kjv/*.json.gz` from the Bedrock add-on's en_kjv.json
- `build_all.sh [mc...]`: build, unit tests and dev dedicated-server self-test
- `client_smoke.sh <mc>`: dev client self-test on a virtual display (screenshots in shots/)
- `prodtest/`: production jar plus Fabric API on a real Fabric server, per version
- `dist/`: the five jars. `modrinth/`: listing text and icon (not published). `SAFETY_REVIEW.md`
