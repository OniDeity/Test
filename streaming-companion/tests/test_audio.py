import struct

from companion.capture.audio import rms


def _pcm16(samples):
    return struct.pack(f"<{len(samples)}h", *samples)


def test_rms_of_silence_is_zero():
    assert rms(_pcm16([0, 0, 0, 0])) == 0.0


def test_rms_of_empty_bytes_is_zero():
    assert rms(b"") == 0.0


def test_rms_of_constant_signal():
    assert rms(_pcm16([1000, -1000, 1000, -1000])) == 1000.0


def test_rms_ignores_trailing_odd_byte():
    data = _pcm16([500, 500]) + b"\x01"
    assert rms(data) == 500.0
