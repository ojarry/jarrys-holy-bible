#!/usr/bin/env python3
"""Copy the right shim set into each versions/<mc>/src with its tokens filled in."""
import shutil, sys
from pathlib import Path
root = Path(__file__).resolve().parent
V = {
 "1.21.11": ("mc1_21_11", {"@MC@": "1.21.11", "@SETSCREEN@": "mc.setScreen", "@IDTYPE@": "Identifier", "@KEYGET@": "identifier"}),
 "1.21.10": ("mc1_21_10", {"@MC@": "1.21.10", "@SETSCREEN@": "mc.setScreen", "@IDTYPE@": "ResourceLocation", "@KEYGET@": "location"}),
}
for mc, (shim, tok) in V.items():
    if len(sys.argv) > 1 and mc not in sys.argv[1:]: continue
    ver = root / f"versions/{mc}"
    for side in ("main", "client"):
        dst = ver / f"src/{side}/java"
        if dst.exists(): shutil.rmtree(dst)
        src = root / f"shims/{shim}/{side}"
        if not src.exists(): continue
        for f in src.rglob("*.java"):
            s = f.read_text()
            for k, v in tok.items(): s = s.replace(k, v)
            leftovers = [l for l in s.splitlines() if "@" in l and "@Override" not in l and "@ApiStatus" not in l and "@NotNull" not in l and "{@" not in l]
            assert not leftovers, (mc, f, leftovers)
            out = dst / f.relative_to(src)
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_text(s)
    res_dst = ver / "src/main/resources"
    if res_dst.exists(): shutil.rmtree(res_dst)
    res_src = root / f"shims/{shim}/resources"
    if res_src.exists():
        shutil.copytree(res_src, res_dst)
    print("prepared", mc, shim)
