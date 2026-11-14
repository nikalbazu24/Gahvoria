# Exercises the same preloader used by both RPG Maker engines.
require 'tmpdir'

class Color
  attr_reader :red, :green, :blue, :alpha
  def initialize(red, green, blue, alpha = 255)
    @red, @green, @blue, @alpha = red, green, blue, alpha
  end
end

class Rect
  def initialize(*args); end
end

class Bitmap
  Font = Struct.new(:size, :bold, :color)
  attr_reader :width, :height, :font, :drawn
  def initialize(*args)
    @source = args[0] if args.size == 1
    @width, @height = @source ? (@source.end_with?('start.png') ? [512, 44] : [512, 384]) : args
    @width, @height = 260, 414 if @source == 'Graphics/Pictures/Battle/battleCommandButtons.png'
    @font = Font.new
    @drawn = []
  end
  def draw_text(*args)
    @drawn << (args.size <= 3 ? args[1] : args[4])
  end
  def text_size(value); Struct.new(:width).new(value.size); end
  def fill_rect(*args); end
  def clear_rect(*args); @drawn.clear; end
  def get_pixel(x, y)
    if @source == 'Graphics/Pictures/introTicket.png'
      return Color.new(255, 255, 255, 0) if x == 0 && y == 383
      return Color.new(78, 66, 66) if x == 112 && y == 182
      return Color.new(216, 216, 200)
    end
    centers = case @source
              when 'Graphics/Titles/splash1.png'; [180, 205]
              when 'Graphics/Titles/splash2.png'; [66, 120, 147, 175, 229, 283, 311]
              else; []
              end
    if @source == 'Graphics/Titles/press_enter.png'
      return (x >= 180 && x <= 330 && y >= 344 && y <= 364) ? Color.new(255, 255, 255) : Color.new(0, 0, 0, 0)
    end
    if x >= 48 && x < 464 && centers.any? { |center| (center - y).abs <= 8 }
      Color.new(255, 255, 255)
    else
      Color.new(0, 0, 0)
    end
  end
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
  reborn = ARGV.include?('--reborn')
  ENV['ESSENTIA_TR_GAME'] = reborn ? 'Pokémon Reborn' : 'Pokemon Solar Eclipse'
  opening = File.join(directory, 'opening-es.txt')
  File.open(opening, 'wb') { |file| file.write("Opening greeting\tSaludo inicial\nUnfinished\tIncompleto") }
  ENV['ESSENTIA_TR_OPENING'] = opening
  diagnostics = File.join(directory, 'gameplay-translation.tsv')
  ENV['ESSENTIA_TR_DIAGNOSTICS'] = diagnostics if ARGV.include?('--diagnostics')
  load script

  if ARGV.include?('--diagnostics')
    $game_map = Struct.new(:map_id).new(51)
    $scene = Object.new
    $Trainer = Struct.new(:name).new('Nico')
    $shown = []
    def pbMessage(text, choices = [])
      $shown << [text, choices]
    end
    pbMessage('Opening greeting', ['Save'])
    2.times { pbMessage("We've got your passes, Nico.") }
    pbMessage('A quiet afternoon.')
    15.times { Graphics.update }
    rows = File.readlines(diagnostics).map { |line| line.chomp.split("\t", -1) }
    assert_equal([
      ['handled', 'choice', '51', 'Object', ''],
      ['handled', 'dialogue', '51', 'Object', ''],
      ['english', 'dialogue', '51', 'Object', "We've got your passes, {jugador}."],
      ['unchanged', 'dialogue', '51', 'Object', 'A quiet afternoon.']
    ], rows)
    assert_equal(['Saludo inicial', ['Guardar']], $shown.first)
    puts 'Gameplay translation diagnostics checks passed'
    exit
  end

  if reborn
    module MessageTypes
      def self.getFromHash(_type, key); key; end
    end
    # Reborn defines _INTL before loading the table of berry names, before Graphics.update.
    def _INTL(key)
      MessageTypes.getFromHash(0, key)
    end
    assert_equal('Bayas Caoca', _INTL('Occa Berries'))
    assert_equal('Bayas Maranga', _INTL('Maranga Berries'))

    $game_map = Struct.new(:map_id).new(51)
    $Trainer = Struct.new(:name).new('nico')
    original = "Station master here.\\nWe've got your boarding passes."
    File.write(answers, "#{EssentiaTranslate.esc(original)}\tTexto mezclado\nMale\t¿Macho\n")
    EssentiaTranslate.load_new
    assert_equal(original, EssentiaTranslate.message_catalog_text(original))
    assert_equal("Soy el jefe de estación.\\nAquí tienes tus billetes.\1",
                 EssentiaTranslate.message_text(original + "\1", 0))
    assert_equal(['Masculino', 'Femenino', 'No binario'],
                 EssentiaTranslate.render_choices(['Male', 'Female', 'Non-binary']))

    def pbEnterPlayerName(heading, initial)
      [heading, initial]
    end
    class Window_UnformattedTextPokemon
      attr_reader :text
      def initialize(text = ''); @text = text; end
    end
    def pbDrawTextPositions(bitmap, rows)
      rows.each { |row| bitmap.draw_text(0, 0, 512, 32, row[0]) }
    end
    def pbTicketText(bitmap)
      pbDrawTextPositions(bitmap, [['Male'], ['Grandview Station'], ['8R750'], [$Trainer.name]])
    end
    EssentiaTranslate.install
    assert_equal(['¿Tu nombre?', 'nico'], pbEnterPlayerName('Your name?', 'nico'))
    help = "Enter text using the keyboard.  Press\nESC to cancel, or ENTER to confirm."
    assert_equal("Escribe con el teclado.\nEsc para cancelar o Enter para aceptar.",
                 Window_UnformattedTextPokemon.new(help).text)
    assert_equal('', Window_UnformattedTextPokemon.new.text)
    ticket = Bitmap.new('Graphics/Pictures/introTicket.png')
    assert_equal(['¡RUMBO A CIUDAD REBORN!', 'Nombre', 'Género', 'Tren', 'Asiento', 'Dest.', 'Adulto', 'Tipo'],
                 ticket.drawn)
    assert_equal([], Bitmap.new('Graphics/Pictures/other.png').drawn)
    overlay = Bitmap.new(512, 384)
    pbTicketText(overlay)
    assert_equal(['Masculino', 'Estación Grandview', '8R750', 'nico'], overlay.drawn)

    sentence = "???: Okay, thank you very much!\\nI'll see you when we get into the city."
    $game_map.map_id = 667
    assert_equal("???: ¡Muchas gracias!\\nNos vemos al llegar a la ciudad.",
                 EssentiaTranslate.message_text(sentence, 0))
    $game_map.map_id = 2
    assert_equal('Texto mezclado', EssentiaTranslate.message_text(original, 0))
    assert_equal("Texto mezclado\1", EssentiaTranslate.message_text(original + "\1", 0))
    $game_map.map_id = 37
    question = "Oh, pop quiz!\\nAre you going to take on the Reborn League?"
    assert_equal("¡Pregunta sorpresa!\\n¿Vas a desafiar a la Liga de Reborn?",
                 EssentiaTranslate.message_text(question, 0))
    assert_equal('Teclas predeterminadas', EssentiaTranslate.render_choices(['Keyboard Defaults']).first)
    assert_equal('Controles del mando', EssentiaTranslate.render_choices(['Gamepad Defaults']).first)

    def controlsDialogue
      raise 'Pause menu still visible' if $pause_test_scene.visible
      :controls
    end
    class PokemonMenu
      def initialize(scene); @scene = scene; end
      def pbStartPokemonMenu; controlsDialogue; end
    end
    $pause_test_scene = Struct.new(:visible).new(true)
    def $pause_test_scene.pbHideMenu; self.visible = false; end
    def $pause_test_scene.pbShowMenu; self.visible = true; end
    EssentiaTranslate.install
    assert_equal(:controls, PokemonMenu.new($pause_test_scene).pbStartPokemonMenu)
    assert_equal(true, $pause_test_scene.visible)
    assert_equal('{2} de {1}', MessageTypes.getFromHash(0, "{1}'s {2}"))
    assert_equal('Nv.35', EssentiaTranslate.lookup('Lv35'))
    assert_equal('Arraigo', EssentiaTranslate.lookup('Ingrain'))
    assert_equal('¿Qué hará {1}?', MessageTypes.getFromHash(0, "What will\n{1} do?"))
    assert_equal('¡{1} usó {2}!', MessageTypes.getFromHash(0, "{1} used\r\n{2}!"))
    assert_equal('¡{1} ganó 2 por el combate!', MessageTypes.getFromHash(0, "{1} got 2\r\nfor winning!"))
    assert_equal("PS máx.<r>+{1}\r\nAtaque<r>+{2}\r\nDef. Esp.<r>+{3}",
                 MessageTypes.getFromHash(0, "Max. HP<r>+{1}\r\nAttack<r>+{2}\r\nSp. Def<r>+{3}"))
    assert_equal("Bueno, Nico… ¿qué te parece un combate?\1",
                 EssentiaTranslate.message_text("Sooo, Nico... whaddya say to a battle?\1", 0))
    assert_equal('Litten', EssentiaTranslate.reborn_dialogue('AME: So, you want Litten, the Fire Cat Pokémon?')[/Litten/])
    class PokeBattle_Move
      attr_reader :name
      def initialize(move, name); @move, @name = move, name; end
      def getMoveUseName; @name; end
    end
    class PokemonDataBox
      def initialize(name); @battler = Struct.new(:name).new(name); end
      def refresh(bitmap)
        pbDrawTextPositions(bitmap, [[@battler.name], ['Lv5']])
      end
    end
    def pbGetMapNameFromId(id)
      id == 29 ? 'Opal Ward' : 'Other map'
    end
    EssentiaTranslate.install
    assert_equal('Burbuja', PokeBattle_Move.new(:BUBBLE, 'Bubble').getMoveUseName)
    assert_equal('Destructor', PokeBattle_Move.new(:POUND, 'Pound').name)
    assert_equal('Custom Pound', PokeBattle_Move.new(:POUND, 'Custom Pound').name)
    assert_equal('Barrio Ópalo', pbGetMapNameFromId(29))
    File.open(answers, 'ab') { |f| f.write("Froakie\t¿Froakie\n") }
    EssentiaTranslate.load_new
    battlebox = Bitmap.new(250, 96)
    PokemonDataBox.new('Froakie').refresh(battlebox)
    assert_equal(['Froakie', 'Nv.5'], battlebox.drawn)
    buttons = Bitmap.new('Graphics/Pictures/Battle/battleCommandButtons.png')
    assert_equal(['LUCHAR', 'LUCHAR', 'BOLSA', 'BOLSA', 'HUIR', 'HUIR'], buttons.drawn[0, 6])
    assert_equal('{1} - Pokémon {2}', MessageTypes.getFromHash(0, '{1} - {2} Pokémon'))
    {
      '{1:s} (M, Lv{2:d})' => 'Pikachu (M, Nv.12)',
      '{1:s} (F, Lv{2:d})' => 'Pikachu (F, Nv.12)',
      '{1:s} (Lv{2:d})' => 'Pikachu (Nv.12)'
    }.each do |source, expected|
      values = ['Pikachu', 12]
      template = MessageTypes.getFromHash(0, source)
      assert_equal(expected, template.gsub(/\{(\d+):(\w+)\}/) { sprintf('%' + $2, values[$1.to_i - 1]) })
    end
    formats = ['x{1: 2d}', 'x{1:03d}', 'PP: {1}/{2}', 'PP: 3/15', '{1: 2d}/{2: 2d}',
               "{1:s}\1", '{1:s}\1', ' ({1}) PP: {2}/{3}', 'Pokémon',
               'TH~ R0GUE DAa/ta cor$upt?@####']
    pending_before = Dir[File.join(requests, '*.req')].size
    formats.each { |format| assert_equal(format, EssentiaTranslate.lookup(format)) }
    EssentiaTranslate.flush
    assert_equal(pending_before, Dir[File.join(requests, '*.req')].size)
    puts 'Reborn intro translation checks passed'
    exit
  end

  assert_equal('Saludo inicial', EssentiaTranslate.lookup('Opening greeting'))
  assert_equal([], Dir[File.join(requests, '*.req')])

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
      bitmap.draw_text(0, 0, bitmap.text_size(row[0]).width, 20, row[0])
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
  assert_equal(['Un juego de', 'Peti & Gelato'], Bitmap.new('Graphics/Titles/splash1.png').drawn)
  assert_equal(true, Bitmap.new('Graphics/Titles/splash2.png').drawn.include?('Si pagaste por este juego, te estafaron.'))
  assert_equal(['Pulsa Enter'], Bitmap.new('Graphics/Titles/press_enter.png').drawn)
  menu.draw_text(0, 0, 100, 20, 'PRESS ENTER')
  assert_equal('Pulsa para empezar', menu.drawn.last)

  class Window_PokemonOption
    def refresh
      bitmap = Bitmap.new(100, 40)
      offset = bitmap.text_size('On').width
      pbDrawShadowText(bitmap, 0, 0, 100, 20, 'On')
      pbDrawShadowText(bitmap, offset, 0, 100, 20, 'Off')
    end
  end
  # Option drawing helpers are top-level in Essentials.
  def pbDrawShadowText(bitmap, x, y, width, height, value)
    $option_rectangles << [x, width, value]
    bitmap.draw_text(x, y, width, height, value)
  end
  $option_rectangles = []
  EssentiaTranslate.install
  Window_PokemonOption.new.refresh
  assert_equal([[0, 2, 'Sí'], [2, 100, 'No']], $option_rectangles)

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

  # A menu label takes precedence over old, literal translations in the session.
  File.open(answers, 'ab') do |f|
    f.write("Save\t¡Salve!\nCLOSE BAG\tBOLSA CERRAR\n{99999}, do your best!\t{99999}, ¡da lo mejor de ti!\n")
  end
  EssentiaTranslate.load_new
  menu.draw_text(0, 0, 100, 20, 'Save')
  menu.draw_text(0, 0, 100, 20, 'CLOSE BAG')
  assert_equal(['Guardar', 'Cerrar bolsa'], menu.drawn.last(2))
  assert_equal('Save', EssentiaTranslate.message_catalog_text('Save'))
  assert_equal('Proud', EssentiaTranslate.message_catalog_text('Proud'))
  assert_equal(['Relajado', 'Serio', 'Orgulloso', 'El mejor'],
               pbShowCommands(nil, ['Casual', 'Serious', 'Proud', 'The Best']))
  assert_equal(['Aspecto', 'Nombre', 'Dificultad', 'Extras', '¡Vamos!'],
               EssentiaTranslate.render_choices(['Appearance', 'Name', 'Difficulty', 'Additions', "Let’s go!!!"]))
  assert_equal('¿Eres más bien relajado?', EssentiaTranslate.message_text("You're more casual and laidback?", 0))

  $player = Struct.new(:name).new('Nico')
  assert_equal('Nico', EssentiaTranslate.rendering_text('Nico'))
  assert_equal('Nico, ¡da lo mejor de ti!', EssentiaTranslate.message_text('Nico, do your best!', 0))
  $player.name = 'Save'
  assert_equal('Save', EssentiaTranslate.rendering_text('Save'))
  $player = nil
  assert_equal('\\xn[Tía]...', EssentiaTranslate.message_text('\\xn[Aunt]...', 0))
  assert_equal('\\xn[Uncleared]...', EssentiaTranslate.message_text('\\xn[Uncleared]...', 0))

  def getFormattedText(_bitmap, _x, _y, _width, _height, text, *_options)
    text
  end

  def getLineBrokenChunks(_bitmap, text, _width)
    text.split(' ')
  end

  help = "Enter text using the keyboard. Press\nEnter to confirm, or Esc to cancel."
  appearance = 'Select your appearance.'
  difficulty = "I see… So you only want the toughest\nbattles?"
  assert_equal('<c2=000000>Escribe con el teclado. Enter para aceptar o Esc para cancelar.',
               getFormattedText(menu, 0, 0, 150, -1, '<c2=000000>' + help))
  assert_equal(['Elige', 'tu', 'aspecto.'], getLineBrokenChunks(menu, appearance, 160))
  assert_equal('Elige tu aspecto.', getFormattedText(menu, 0, 0, 150, -1, appearance))
  assert_equal('Saludo inicial', getFormattedText(menu, 0, 0, 150, -1, 'Opening greeting'))
  assert_equal('Nombre propio', getFormattedText(menu, 0, 0, 150, -1, 'Nombre propio'))
  assert_equal('Ya veo… ¿Solo quieres los combates más difíciles?', EssentiaTranslate.message_text(difficulty, 0))
  assert_equal('TONO', EssentiaTranslate.rendering_text('TONE'))
  assert_equal('No uses el arte de este juego con IA.',
               EssentiaTranslate.rendering_text("Do not feed this game’s art to AI."))
  assert_equal('Los personajes y sucesos de este juego son ficticios.',
               EssentiaTranslate.rendering_text("The characters and events depicted\nin this game are fictional."))

  class Window_TextEntry_Keyboard
    def refresh(bitmap)
      bitmap.draw_text(0, 0, 100, 20, 'Save')
    end
  end
  EssentiaTranslate.install
  Window_TextEntry_Keyboard.new.refresh(menu)
  assert_equal('Save', menu.drawn.last)

  def pbMessage(text, choices = nil)
    choices ? pbShowCommands(nil, choices) : text
  end
  assert_equal(['Guardar', 'Cerrar bolsa'], pbMessage('Pick one', ['Save', 'Close Bag']))
  assert_equal(['Derecha', 'Izquierda', 'De frente'],
               pbMessage('Which direction should we go?', ['Right', 'Left', 'Straight']))
  assert_equal(['Luchar', 'Huir', 'Bolsa'], pbMessage('Do you want to fight?', ['Fight', 'Run', 'Bag']))

  assert_equal(['Derecha', 'De frente'], pbMessage('Which way?', ['Right', 'Straight']))
  assert_equal('Correcto.', EssentiaTranslate.scene_label('Right.', 'Is that correct?'))
  assert_equal(nil, EssentiaTranslate.scene_label('Right.', 'Nice weather.'))
  interpreter = Struct.new(:event_id).new(2)
  $game_map = Struct.new(:map_id, :interpreter).new(8, interpreter)
  EssentiaTranslate.remember_scene_message('Is that correct?')
  assert_equal('Correcto.', pbMessage('Right.'))
  EssentiaTranslate.remember_scene_message('Is that correct?')
  interpreter.event_id = 3
  assert_equal(nil, EssentiaTranslate.scene_hint('Right.'))
  interpreter.event_id = 2
  $game_map.map_id = 9
  assert_equal(nil, EssentiaTranslate.scene_hint('Right.'))
  EssentiaTranslate.remember_scene_message('\\xn[Ada]Is that correct?')
  assert_equal(nil, EssentiaTranslate.scene_hint('\\xn[Ben]Right.'))
  $game_map = nil
end

puts 'Ruby translation bridge checks passed'
