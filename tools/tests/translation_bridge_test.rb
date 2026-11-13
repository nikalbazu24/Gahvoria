# Exercises the same preloader used by both RPG Maker engines.
require 'tmpdir'

module Graphics
  def self.update
  end
end

def assert_equal(expected, actual)
  raise "Expected #{expected.inspect}, got #{actual.inspect}" unless expected == actual
end

script = File.expand_path('../../engine/src/main/assets/essentia_preload/essentia_translate.rb', File.dirname(__FILE__))
if ARGV.include?('--disabled')
  ENV['ESSENTIA_TR_FILE'] = ''
  ENV['ESSENTIA_TR_REQ'] = ''
  load script
  assert_equal(false, Graphics.respond_to?(:essentia_tr_original_update))
  puts 'Disabled bridge check passed'
  exit
end

Dir.mktmpdir('gahvoria-translation') do |directory|
  answers = File.join(directory, 'answers.tsv')
  requests = File.join(directory, 'queue')
  Dir.mkdir(requests)
  ENV['ESSENTIA_TR_FILE'] = answers
  ENV['ESSENTIA_TR_REQ'] = requests
  load script

  def pbMessage(text)
    pbMessageDisplay(nil, text)
  end

  def pbMessageDisplay(_window, text)
    text
  end

  def pbShowCommands(_window, commands)
    commands
  end

  text = "Hello \\PN!\nNext\tline"
  translated = "Hola \\PN!\nSiguiente\tlínea"
  assert_equal([text], pbShowCommands(nil, [text]))
  request = Dir[File.join(requests, '*.req')]
  assert_equal(1, request.size)
  assert_equal(EssentiaTranslate.esc(text) + "\n", File.open(request.first, 'rb') { |f| f.read })

  partial = EssentiaTranslate.esc(text) + "\t" + EssentiaTranslate.esc(translated)
  File.open(answers, 'wb') { |f| f.write(partial) }
  EssentiaTranslate.load_new
  assert_equal(text, EssentiaTranslate.lookup(text))
  File.open(answers, 'ab') { |f| f.write("\n") }
  EssentiaTranslate.load_new
  assert_equal(translated, pbMessage(text))
  assert_equal(translated, pbMessage(EssentiaTranslate.lookup(text)))
  assert_equal(1, Dir[File.join(requests, '*.req')].size)

  # A new original can equal a previous translated string.
  assert_equal(translated, EssentiaTranslate.lookup(translated))
  EssentiaTranslate.flush
  assert_equal(2, Dir[File.join(requests, '*.req')].size)

  # Existing asynchronous requests can still be resolved when a dialogue opens.
  pending = 'Open the door'
  assert_equal(pending, EssentiaTranslate.lookup(pending))
  EssentiaTranslate.flush
  Thread.new do
    sleep(0.04)
    File.open(answers, 'ab') { |f| f.write("Open the door\tAbre la puerta\n") }
  end
  assert_equal('Abre la puerta', pbMessage(pending))
end

puts 'Ruby translation bridge checks passed'
