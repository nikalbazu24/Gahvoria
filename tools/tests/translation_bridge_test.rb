# Exercises the same preloader used by both RPG Maker engines.
require 'tmpdir'

class Color
  def initialize(*args); end
end

class Rect
  def initialize(*args); end
end

class Bitmap
  Font = Struct.new(:size, :bold, :color)
  attr_reader :width, :height, :font, :drawn
  def initialize(*args)
    @width, @height = args.size == 1 ? [512, 44] : args
    @font = Font.new
    @drawn = []
  end
  def draw_text(*args)
    @drawn << (args.size <= 3 ? args[1] : args[4])
  end
  def text_size(value); value.size; end
  def clear; @drawn.clear; end
  def blt(_x, _y, source, _rect); @drawn.concat(source.drawn); end
  def dispose; end
end

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
  ENV['ESSENTIA_TR_TARGET'] = 'es'
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

  # Some Essentials cutscenes assign the text directly to the dialogue window.
  class Window_AdvancedTextPokemon
    attr_accessor :letterbyletter
    attr_reader :text
    def text=(value)
      @text = value
    end
  end

  Graphics.update
  window = Window_AdvancedTextPokemon.new
  intro = 'Take on the Gym Challenge!'
  spanish = '¡Enfréntate al desafío de los gimnasios!'
  window.text = ''
  assert_equal('', window.text)
  window.letterbyletter = true

  before = Dir[File.join(requests, '*.req')].size
  Thread.new do
    sleep(0.05)
    File.open(answers, 'ab') do |f|
      f.write(EssentiaTranslate.esc(intro) + "\t" + EssentiaTranslate.esc(spanish) + "\n")
    end
  end
  window.text = intro
  assert_equal(spanish, window.text)
  assert_equal(before + 1, Dir[File.join(requests, '*.req')].size)

  window.text = window.text
  EssentiaTranslate.flush
  assert_equal(before + 1, Dir[File.join(requests, '*.req')].size)

  # A display wrapper can write transformed text to the same window without sending it twice.
  def pbMessageDisplay(window, text)
    window.text = "<b>" + text + "</b>"
  end
  pbMessageDisplay(window, intro)
  assert_equal("<b>" + spanish + "</b>", window.text)
  EssentiaTranslate.flush
  assert_equal(before + 1, Dir[File.join(requests, '*.req')].size)
  def pbDrawTextPositions(bitmap, rows)
    rows.each do |row|
      bitmap.draw_text(0, 0, bitmap.text_size(row[0]), 20, row[0])
    end
  end

  menu = Bitmap.new(200, 100)
  rows = [['New Game', 20], ['Options', 40]]
  pbDrawTextPositions(menu, rows)
  menu.draw_text(Rect.new(0, 0, 180, 20), 'Press Start', 1)
  assert_equal(['Nueva partida', 'Opciones', 'Pulsa START'], menu.drawn)
  assert_equal('New Game', rows[0][0])
  assert_equal(['Pulsa para empezar'], Bitmap.new('Graphics/Titles/start.png').drawn)
  assert_equal([], Bitmap.new('Graphics/Titles/title.png').drawn)
  assert_equal([], Bitmap.new('Graphics/Pictures/start.png').drawn)

  class Window_DrawableCommand
    attr_reader :bitmap, :selected
    def initialize
      @bitmap = Bitmap.new(100, 40)
      @selected = 2
    end
    def disposed?; false; end
    def visible; true; end
    def refresh
      @bitmap.clear
      @bitmap.draw_text(0, 0, 100, 20, 'Select a challenge')
    end
  end
  EssentiaTranslate.install
  options = Window_DrawableCommand.new
  options.refresh
  assert_equal(['Select a challenge'], options.bitmap.drawn)
  window.letterbyletter = false
  window.text = 'Choose your preferred difficulty'
  File.open(answers, 'ab') do |f|
    f.write("Select a challenge\tElige un desafío\n")
    f.write("Choose your preferred difficulty\tElige la dificultad que prefieras\n")
  end
  EssentiaTranslate.load_new
  EssentiaTranslate.refresh_ready_windows
  assert_equal(['Elige un desafío'], options.bitmap.drawn)
  assert_equal(2, options.selected)
  assert_equal('Elige la dificultad que prefieras', window.text)
end

puts 'Ruby translation bridge checks passed'
