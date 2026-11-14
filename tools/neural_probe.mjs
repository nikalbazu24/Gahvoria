// Development probe only. Run with the locally staged, pinned npm worker and model files.
import {readFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {pathToFileURL} from 'node:url';
import {createInterface} from 'node:readline';
const [runtime, models, mode] = process.argv.slice(2);
const {BatchTranslator, TranslatorBacking} = await import(pathToFileURL(resolve(runtime, 'translator.js')));
const bytes = name => Uint8Array.from(readFileSync(resolve(models, name))).buffer;
class LocalBacking extends TranslatorBacking {
  async loadModelRegistery() { return [{from: 'en', to: 'es'}]; }
  async loadTranslationModel() {
    return {model: bytes('model.enes.intgemm.alphas.bin'), shortlist: bytes('lex.50.50.enes.s2t.bin'),
      vocabs: [bytes('vocab.enes.spm')], config: {'beam-size': 1, 'workspace': 64,
        'mini-batch-words': 256, 'max-length-break': 128, 'ssplit-mode': 'paragraph'}};
  }
}
const translator = new BatchTranslator({workers: 1}, new LocalBacking());
try {
  for await (const line of createInterface({input: process.stdin})) {
    const text = Buffer.from(line, 'base64').toString('utf8');
    const result = await translator.translate({from: 'en', to: 'es', text, html: mode === 'html'});
    process.stdout.write(Buffer.from(result.target.text).toString('base64') + '\n');
  }
} finally { await translator.delete(); }
