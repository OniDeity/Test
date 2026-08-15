"""Drag-select tool for defining screen regions.

Usage: python -m companion.region_picker main_monitor
Drag a rectangle over the area you want the companion to see, release the mouse,
and it prints a YAML snippet you can paste into config/settings.yaml under `regions:`.
Press Escape to cancel.
"""

from __future__ import annotations

import sys
import tkinter as tk
from dataclasses import dataclass


@dataclass
class Rect:
    x1: int
    y1: int
    x2: int
    y2: int

    def normalized(self) -> tuple[int, int, int, int]:
        x = min(self.x1, self.x2)
        y = min(self.y1, self.y2)
        w = abs(self.x2 - self.x1)
        h = abs(self.y2 - self.y1)
        return x, y, w, h


def pick_region(name: str) -> tuple[int, int, int, int]:
    """Opens a full-screen transparent overlay; drag to select a region.

    Returns (x, y, width, height) in screen coordinates. Raises RuntimeError if
    the selection is cancelled with Escape.
    """
    result: dict[str, Rect] = {}

    root = tk.Tk()
    root.attributes("-fullscreen", True)
    root.attributes("-alpha", 0.3)
    root.attributes("-topmost", True)
    root.configure(bg="black")
    root.title(f"Drag to select region: {name} (Esc to cancel)")

    canvas = tk.Canvas(root, cursor="cross", bg="grey")
    canvas.pack(fill="both", expand=True)

    start: dict[str, int] = {}
    rect_id: dict[str, int] = {}

    def on_press(event: "tk.Event") -> None:
        start["x"], start["y"] = event.x_root, event.y_root
        rect_id["id"] = canvas.create_rectangle(event.x, event.y, event.x, event.y, outline="red", width=2)

    def on_drag(event: "tk.Event") -> None:
        if "id" in rect_id:
            x0 = start["x"] - root.winfo_rootx()
            y0 = start["y"] - root.winfo_rooty()
            canvas.coords(rect_id["id"], x0, y0, event.x, event.y)

    def on_release(event: "tk.Event") -> None:
        result["rect"] = Rect(start["x"], start["y"], event.x_root, event.y_root)
        root.destroy()

    def on_escape(_event: "tk.Event") -> None:
        root.destroy()

    canvas.bind("<ButtonPress-1>", on_press)
    canvas.bind("<B1-Motion>", on_drag)
    canvas.bind("<ButtonRelease-1>", on_release)
    root.bind("<Escape>", on_escape)

    root.mainloop()

    if "rect" not in result:
        raise RuntimeError("Region selection cancelled.")
    return result["rect"].normalized()


def main() -> None:
    name = sys.argv[1] if len(sys.argv) > 1 else "region"
    x, y, w, h = pick_region(name)
    print(f"- name: {name}")
    print(f"  x: {x}")
    print(f"  y: {y}")
    print(f"  width: {w}")
    print(f"  height: {h}")
    print('  description: ""')


if __name__ == "__main__":
    main()
