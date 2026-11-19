#!/usr/bin/env python3
"""Stonecutter-style prep: copy the right shim set into each versions/<mc>/src with its tokens filled in."""
import shutil, sys
from pathlib import Path
root = Path(__file__).resolve().parent
V = {
 "26.3":    ("mc26",  {"@MC@": "26.3",   "@SETSCREEN@": "mc.gui.setScreen"}),
 "26.2":    ("mc26",  {"@MC@": "26.2",   "@SETSCREEN@": "mc.gui.setScreen"}),
 "26.1.2":  ("mc26",  {"@MC@": "26.1.2", "@SETSCREEN@": "mc.setScreen"}),
 "1.21.11": ("mc1_21",{"@MC@": "1.21.11","@SETSCREEN@": "mc.setScreen", "@IDTYPE@": "Identifier", "@KEYGET@": "identifier"}),
 "1.21.10": ("mc1_21",{"@MC@": "1.21.10","@SETSCREEN@": "mc.setScreen", "@IDTYPE@": "ResourceLocation", "@KEYGET@": "location"}),
}
for mc, (shim, tok) in V.items():
    if len(sys.argv) > 1 and mc not in sys.argv[1:]: continue
    for side in ("main", "client"):
        dst = root / f"versions/{mc}/src/{side}/java"
        if dst.exists(): shutil.rmtree(dst)
        for f in (root / f"shims/{shim}/{side}").rglob("*.java"):
            s = f.read_text()
            for k, v in tok.items(): s = s.replace(k, v)
            assert "@" not in s.replace("@Override", ""), (mc, f, [l for l in s.splitlines() if "@" in l and "@Override" not in l])
            out = dst / f.relative_to(root / f"shims/{shim}/{side}")
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_text(s)
    print("prepared", mc, shim)
