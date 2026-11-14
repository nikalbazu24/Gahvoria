"""Stops publication if the APK lost an engine, the compatibility scripts or the app itself."""
import json, sys, zipfile
from pathlib import Path

apk = Path(sys.argv[1])
manifest = json.loads(Path(sys.argv[2]).read_text())
with zipfile.ZipFile(apk) as archive:
    names = set(archive.namelist())
    required = {
        'AndroidManifest.xml', 'classes.dex',
        'lib/arm64-v8a/libmkxp-z.so',          # modern engine (Ruby 3.1)
        'lib/arm64-v8a/libmkxp-z-legacy.so',   # classic engine (Ruby 1.8)
        'assets/essentia_preload/essentia_games.rb',
        'assets/essentia_preload/essentia_compat.rb',
        'assets/licenses/montserrat-OFL.txt',
        'assets/ruby-runtime/lib/ruby/3.1.0/openssl.rb',
        'assets/ruby-runtime/lib/ruby/3.1.0/openssl/ssl.rb',
    }
    rbconfigs = [n for n in names if n.startswith('assets/ruby-runtime/lib/ruby/3.1.0/')
                 and n.endswith('/rbconfig.rb') and 'aarch64' in n]
    if len(rbconfigs) != 1 or archive.getinfo(rbconfigs[0]).file_size == 0:
        raise SystemExit('APK missing the Android ARM64 Ruby configuration')
    missing = required - names
    if missing:
        raise SystemExit('APK incomplete: ' + ', '.join(sorted(missing)))
    if any(n.startswith('lib/') and not n.startswith('lib/arm64-v8a/') for n in names):
        raise SystemExit('Unexpected ABI in the Android release')
    if archive.testzip() is not None:
        raise SystemExit('APK ZIP integrity check failed')
if manifest['apk'] != 'gahvoria.apk' or manifest['versionCode'] < 1:
    raise SystemExit('version.json does not match the published APK')
print('APK OK: modern + classic engines, compatibility scripts, Gahvoria app, arm64')
