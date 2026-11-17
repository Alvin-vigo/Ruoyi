#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Extract ```mermaid blocks from the detailed-design spec into numbered .mmd files,
and capture the nearest preceding heading / caption line for each."""
import re, os, sys

SRC = sys.argv[1]
OUTDIR = sys.argv[2]
os.makedirs(OUTDIR, exist_ok=True)

with open(SRC, encoding="utf-8") as f:
    lines = f.read().split("\n")

# Walk lines, detect fenced mermaid blocks
in_block = False
body = []
idx = 0
captions = []  # (idx, caption)
last_heading = ""
last_line = ""
for ln in lines:
    if not in_block:
        if ln.strip().startswith("```mermaid"):
            in_block = True
            body = []
            # caption = most recent heading, else previous non-empty line
            cap = last_heading or last_line
            continue
        # track headings and last non-empty line for caption
        if re.match(r"^#{1,6}\s+", ln):
            last_heading = ln.strip().lstrip("#").strip()
        elif ln.strip():
            last_line = ln.strip()
    else:
        if ln.strip().startswith("```"):
            in_block = False
            idx += 1
            mmd = os.path.join(OUTDIR, f"diagram_{idx:03d}.mmd")
            with open(mmd, "w", encoding="utf-8") as o:
                o.write("\n".join(body) + "\n")
            captions.append((idx, cap))
            continue
        body.append(ln)

# write caption map
with open(os.path.join(OUTDIR, "captions.txt"), "w", encoding="utf-8") as o:
    for i, c in captions:
        o.write(f"{i:03d}\t{c}\n")

print(f"Extracted {idx} mermaid diagrams to {OUTDIR}")
for i, c in captions:
    print(f"  {i:03d}: {c[:50]}")
