"""Stage the exact English→Spanish pack used by this build."""
import hashlib
import json
from pathlib import Path
import subprocess

here = Path(__file__).resolve().parent
manifest_path = here / "model-en-es.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
assets = here.parent / "src/main/assets/neural"
assets.mkdir(parents=True, exist_ok=True)

for entry in manifest["files"]:
    target = assets / (entry["name"] + ".zst")
    temporary = target.with_suffix(".part")
    try:
        subprocess.run([
            "curl", "--fail", "--silent", "--show-error", "--retry", "3",
            "--connect-timeout", "15", "--max-time", "150",
            "--proto", "=https", "--proto-redir", "=https", "--location",
            "--output", str(temporary), manifest["source"] + entry["attachment"],
        ], check=True)
        if temporary.stat().st_size != entry["size"] or hashlib.sha256(temporary.read_bytes()).hexdigest() != entry["sha256"]:
            raise SystemExit("Unexpected Mozilla model file: " + entry["name"])
        temporary.replace(target)
    finally:
        temporary.unlink(missing_ok=True)

(assets / "model-en-es.json").write_bytes(manifest_path.read_bytes())
