"""Check the files shipped by the Neural beta, independently of the general APK verifier."""
import hashlib
import json
from pathlib import Path
import sys
import zipfile

expected = json.loads(Path(__file__).with_name("model-en-es.json").read_bytes())
with zipfile.ZipFile(sys.argv[1]) as apk:
    if apk.getinfo("lib/arm64-v8a/libgahvoria_bergamot.so").file_size < 100_000:
        raise SystemExit("Missing ARM64 Bergamot engine")
    if json.loads(apk.read("assets/neural/model-en-es.json")) != expected:
        raise SystemExit("The APK includes a different model manifest")
    for file in expected["files"]:
        data = apk.read("assets/neural/" + file["name"] + ".zst")
        if len(data) != file["size"] or hashlib.sha256(data).hexdigest() != file["sha256"]:
            raise SystemExit("The APK includes a different model artifact: " + file["name"])
    for license_file in ("bergamot-mpl-2.0.txt", "bergamot-third-party.txt"):
        apk.getinfo("assets/licenses/" + license_file)
print("APK Neural OK: ARM64 engine, pinned en→es model, notices")
