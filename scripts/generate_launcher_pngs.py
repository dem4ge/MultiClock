#!/usr/bin/env python3
"""Raster launcher icons matching vector ic_launcher_foreground (108dp layout)."""
from __future__ import annotations

import math
import os
import sys

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Pillow required: pip install pillow", file=sys.stderr)
    sys.exit(1)

BG = (18, 20, 22, 255)
DIALS = [
    ((181, 115, 106, 255), (46, 32, 28, 255), (30, 30), ((30, 19), (39, 34))),
    ((107, 140, 170, 255), (30, 40, 48, 255), (78, 30), ((87, 30), (74, 40))),
    ((122, 155, 122, 255), (31, 46, 31, 255), (30, 78), ((22, 78), (36, 69))),
    ((196, 181, 106, 255), (55, 50, 36, 255), (78, 78), ((78, 67), (68, 74))),
]
VIEWPORT = 108.0
RADIUS = 14.0


def draw_dial(
    draw: ImageDraw.ImageDraw,
    scale: float,
    dial_rgb: tuple[int, int, int, int],
    hand_rgb: tuple[int, int, int, int],
    center: tuple[float, float],
    hand_endpoints: tuple[tuple[float, float], tuple[float, float]],
) -> None:
    cx, cy = center[0] * scale, center[1] * scale
    r = RADIUS * scale
    bbox = [cx - r, cy - r, cx + r, cy + r]
    draw.ellipse(bbox, fill=dial_rgb[:3])
    hw_short = max(1, int(round(2.0 * scale)))
    hw_long = max(1, int(round(2.5 * scale)))
    h1, h2 = hand_endpoints
    draw.line(
        [(cx, cy), (h1[0] * scale, h1[1] * scale)],
        fill=hand_rgb[:3],
        width=hw_long,
    )
    draw.line(
        [(cx, cy), (h2[0] * scale, h2[1] * scale)],
        fill=hand_rgb[:3],
        width=hw_short,
    )


def render(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), BG)
    draw = ImageDraw.Draw(img)
    scale = size / VIEWPORT
    for dial_rgb, hand_rgb, c, ends in DIALS:
        draw_dial(draw, scale, dial_rgb, hand_rgb, c, ends)
    return img


def main() -> None:
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    res = os.path.join(root, "app", "src", "main", "res")
    nodpi = os.path.join(res, "drawable-nodpi")
    os.makedirs(nodpi, exist_ok=True)

    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, px in sizes.items():
        os.makedirs(os.path.join(res, folder), exist_ok=True)
        im = render(px)
        im.save(os.path.join(res, folder, "ic_launcher.png"), "PNG")
        im.save(os.path.join(res, folder, "ic_launcher_round.png"), "PNG")

    source = render(1024)
    source.save(os.path.join(nodpi, "ic_launcher_source.png"), "PNG")
    print("Wrote launcher PNGs + drawable-nodpi/ic_launcher_source.png (1024)")


if __name__ == "__main__":
    main()
