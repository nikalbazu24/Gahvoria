"""Stage a pinned WASM development worker plus the APK's verified model in a scratch folder."""
import hashlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tarfile
import zstandard

root = Path(__file__).resolve().parents[1]
out = Path(sys.argv[1]).resolve()
out.mkdir(parents=True, exist_ok=True)

def fetch(url, sha):
    response = subprocess.check_output(['curl', '--fail', '--silent', '--show-error', '--location',
        '--retry', '2', '--connect-timeout', '15', '--max-time', '120', '--proto', '=https', url])
    if hashlib.sha256(response).hexdigest() != sha:
        raise SystemExit('Probe download checksum mismatch')
    return response

package = fetch('https://registry.npmjs.org/@browsermt/bergamot-translator/-/bergamot-translator-0.4.9.tgz',
                '9011be93222d839d7448ffdf00549d53ce8f541fd782ffc79779d1756397c41f')
with tarfile.open(fileobj=io.BytesIO(package), mode='r:gz') as archive:
    # Read only these explicit files; never extract arbitrary archive paths or links.
    for name in ('package.json', 'translator.js', 'worker/translator-worker.js',
                 'worker/bergamot-translator-worker.js', 'worker/bergamot-translator-worker.wasm'):
        path = out / 'package' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(archive.extractfile('package/' + name).read())
loader = out / 'package/translator.js'
loader.write_text(loader.read_text().replace('./worker/translator-worker.js', './worker/translator-worker.cjs'))
worker = out / 'package/worker/translator-worker.js'
worker.with_suffix('.cjs').write_text(worker.read_text().replace('mini-batch-words: 1024', 'mini-batch-words: 256')
                                    .replace('workspace: 128', 'workspace: 64'))
manifest = json.loads((root / 'engine/neural/model-en-es.json').read_text())
for file in manifest['files']:
    compressed = fetch(manifest['source'] + file['attachment'], file['sha256'])
    raw = zstandard.ZstdDecompressor().decompress(compressed, max_output_size=file['rawSize'])
    if len(raw) != file['rawSize'] or hashlib.sha256(raw).hexdigest() != file['rawSha256']:
        raise SystemExit('Probe model checksum mismatch')
    (out / file['name']).write_bytes(raw)
print('Staged verified model and WASM 0.4.9 probe (not an Android performance benchmark)')
