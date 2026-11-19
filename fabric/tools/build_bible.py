#!/usr/bin/env python3
"""Build the KJV resources for Jarry's Holy Bible (Java).

Source: King James Version (public domain), github.com/thiagobodruk/bible json/en_kjv.json
(shared with the Bedrock add-on). Writes assets/jarrys_holy_bible/kjv/index.json and one
gzipped JSON file per book (bNN.json.gz = [[verse, ...], ...]), read lazily by the client.
"""
import json, os, re, gzip, hashlib, io, sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
# The generated kjv assets are already in this repo. To regenerate them, point
# BIBLE_KJV_JSON at en_kjv.json from https://github.com/thiagobodruk/bible
# (King James Version, public domain).
src = Path(os.environ["BIBLE_KJV_JSON"]) if os.environ.get("BIBLE_KJV_JSON") else Path()
if not src.is_file():
    sys.exit("Set BIBLE_KJV_JSON to en_kjv.json from https://github.com/thiagobodruk/bible")
data = json.loads(src.read_text(encoding="utf-8-sig"))
OT = 39
assert len(data) == 66
total = sum(len(c) for b in data for c in b["chapters"])
assert total == 31102, total
fix = re.compile(r"\b(LORD|GOD|JEHOVAH|JAH) (?=[,.;:?!)'’])")
out = root / "common/src/main/resources/assets/jarrys_holy_bible/kjv"
out.mkdir(parents=True, exist_ok=True)
index = {"translation": "King James Version (public domain)", "totalVerses": total, "books": []}
for i, book in enumerate(data, start=1):
    chapters = [[fix.sub(r"\1", v.strip()) for v in ch] for ch in book["chapters"]]
    assert all(v for ch in chapters for v in ch)
    raw = json.dumps(chapters, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    buf = io.BytesIO()
    with gzip.GzipFile(filename="", mode="wb", fileobj=buf, mtime=0, compresslevel=9) as g:
        g.write(raw)
    (out / f"b{i:02d}.json.gz").write_bytes(buf.getvalue())
    index["books"].append({"file": f"b{i:02d}", "name": book["name"],
                           "testament": "old" if i <= OT else "new",
                           "verses": [len(ch) for ch in chapters]})
(out / "index.json").write_text(json.dumps(index, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
size = sum(p.stat().st_size for p in out.glob("*.gz"))
print("books", len(index["books"]), "verses", total, "gz bytes", size)
print("source sha256", hashlib.sha256(src.read_bytes()).hexdigest())
