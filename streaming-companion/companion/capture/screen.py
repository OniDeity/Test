"""Captures configured screen regions and composites them into one labeled JPEG frame.

Gemini Live's video input is a single stream of frames, so instead of juggling
multiple video tracks we grab every configured region on each tick and stack
them into one labeled collage - the model sees the main monitor, the Discord
call window, etc. all at once, every frame.
"""

from __future__ import annotations

import asyncio
import io
from typing import AsyncIterator

from ..config import ScreenRegion

MAX_TILE_WIDTH = 640
LABEL_HEIGHT = 20


def _capture_and_composite(regions: list[ScreenRegion]) -> bytes:
    import mss
    from PIL import Image, ImageDraw

    tiles: list[tuple[str, "Image.Image"]] = []
    with mss.mss() as sct:
        for region in regions:
            monitor = {
                "left": region.x,
                "top": region.y,
                "width": region.width,
                "height": region.height,
            }
            shot = sct.grab(monitor)
            image = Image.frombytes("RGB", shot.size, shot.bgra, "raw", "BGRX")
            if image.width > MAX_TILE_WIDTH:
                ratio = MAX_TILE_WIDTH / image.width
                image = image.resize((MAX_TILE_WIDTH, max(1, int(image.height * ratio))))
            tiles.append((region.name, image))

    if not tiles:
        raise ValueError("No regions to capture")

    tile_width = max(image.width for _, image in tiles)
    total_height = sum(image.height + LABEL_HEIGHT for _, image in tiles)
    canvas = Image.new("RGB", (tile_width, total_height), "black")
    draw = ImageDraw.Draw(canvas)

    y = 0
    for name, image in tiles:
        draw.rectangle([0, y, tile_width, y + LABEL_HEIGHT], fill="black")
        draw.text((4, y + 2), name, fill="white")
        y += LABEL_HEIGHT
        canvas.paste(image, (0, y))
        y += image.height

    buffer = io.BytesIO()
    canvas.save(buffer, format="JPEG", quality=70)
    return buffer.getvalue()


async def stream_regions(regions: list[ScreenRegion], fps: float) -> AsyncIterator[bytes]:
    """Yields JPEG-encoded composite frames of the given screen regions at ~fps."""
    interval = 1.0 / fps if fps > 0 else 1.0
    loop = asyncio.get_event_loop()
    while True:
        start = loop.time()
        frame = await loop.run_in_executor(None, _capture_and_composite, regions)
        yield frame
        elapsed = loop.time() - start
        await asyncio.sleep(max(0.0, interval - elapsed))
