# essentia_translate.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Automatic translation of supported game dialogue (optional, per game).
# The game's texts go through Pokémon Essentials' message system; here they
# are sent to Gahvoria's background worker, which checks the durable memory
# before consulting a provider. Ruby reads validated answers for this session.
# Dialogue has a short wait budget; menus always return immediately.

module EssentiaTranslate
  FILE = ENV['ESSENTIA_TR_FILE'].to_s
  REQ = ENV['ESSENTIA_TR_REQ'].to_s
  OPENING = ENV['ESSENTIA_TR_OPENING'].to_s
  DIAGNOSTICS = ENV['ESSENTIA_TR_DIAGNOSTICS'].to_s
  DIAGNOSTIC_LIMIT = 2500
  ENGLISH_IN_DISPLAY = /\b(?:we['’](?:ve|ll)|you['’](?:re|ll)|they['’]re|jammies|gonna|cuz)\b/i
  @map = {}
  @displaying = []
  @pending = {}
  @queue = []
  @sequence = 0
  @last_flush = 0
  @last_load = 0
  @pos = 0
  @frame = 0
  MAX_BATCH = 8
  MAX_SESSION_ENTRIES = 4096
  MAX_PREPARED_ENTRIES = 50_000
  @session_limit = MAX_SESSION_ENTRIES
  MAX_REQUEST_FILES = 128
  RETRY_SECONDS = 18
  DIALOGUE_WAIT = 0.65
  SPANISH = ENV['ESSENTIA_TR_TARGET'] == 'es'
  SOLAR_ECLIPSE = ENV['ESSENTIA_TR_GAME'].to_s.downcase.gsub(/[^a-z]/, '').include?('solareclipse')
  TRANSLATION_ROOT = ENV['ESSENTIA_TR_ROOT'].to_s
  REBORN = (defined?(Essentia) && Essentia.respond_to?(:reborn_game?) && Essentia.reborn_game?) ||
    (!TRANSLATION_ROOT.empty? &&
      File.file?(File.join(TRANSLATION_ROOT, 'Scripts/Reborn/Bootstrap.rb')) &&
      File.file?(File.join(TRANSLATION_ROOT, 'Scripts/Online/DiscordRichPresence.rb'))) ||
    ENV['ESSENTIA_TR_GAME'].to_s.gsub('é', 'e').gsub('É', 'E').downcase.gsub(/[^a-z0-9]/, '') =~ /\A(?:pokemon)?reborn\d*\z/
  REBORN_INTRO_SPANISH = {
    "station master here. we've got your boarding passes." =>
      "Soy el jefe de estación.\\nAquí tienes tus billetes.",
    "then, just to be sure, for 'gender' i should put..." =>
      "Y para confirmar,\\n¿qué pongo en «Género»?",
    "just between you, me, and the ol' battered fence post, i hear reborn city's kind of a dump." =>
      "Que quede entre nosotros: dicen que Ciudad Reborn está hecha un desastre.",
    'there, all set.' => 'Listo, ya está.',
    "???: okay, thank you very much! i'll see you when we get into the city." =>
      "???: ¡Muchas gracias!\\nNos vemos al llegar a la ciudad.",
    "my name's ame. i'm the manager of the reborn league." =>
      "Soy Ame.\\nDirijo la Liga de Reborn.",
    'could i see your boarding ticket, please?' => '¿Me enseñas tu billete, por favor?',
    "shouldn't we be decelerating already?" => '¿No deberíamos estar frenando ya?',
    'oh no-- move!' => '¡Oh, no! ¡Apártate!',
    'oh, pop quiz! are you going to take on the reborn league?' =>
      "¡Pregunta sorpresa!\\n¿Vas a desafiar a la Liga de Reborn?",
    "cuz, you know, if things are exploding, i gotta be there to see it!!!" =>
      '¡Es que, si algo está explotando, tengo que ir a verlo!',
    "then i'll be seeing you very soon, because i'm also the electric gym leader!" =>
      'Entonces nos veremos muy pronto. ¡También soy la líder del Gimnasio de tipo Eléctrico!'
  }
  REBORN_GENDER_SPANISH = {
    'male' => 'Masculino', 'female' => 'Femenino', 'non-binary' => 'No binario'
  }
  REBORN_TICKET_TEXT = {
    'grandview station' => 'Estación Grandview', 'one' => 'UNO'
  }
  REBORN_INTRO_MAPS = [37, 51, 667]
  REBORN_GLITCH_TEXT = 'TH~ R0GUE DAa/ta cor$upt?@####'
  SPANISH_GAMEPLAY_TEXT = {
    'Ingrain' => 'Arraigo', 'Lv{1}' => 'Nv.{1}',
    '{1} - {2} Pokémon' => '{1} - Pokémon {2}',
    "{1}'s {2}" => '{2} de {1}',
    '{1:s} (M, Lv{2:d})' => '{1:s} (M, Nv.{2:d})',
    '{1:s} (F, Lv{2:d})' => '{1:s} (F, Nv.{2:d})',
    '{1:s} (Lv{2:d})' => '{1:s} (Nv.{2:d})',
    'Occa Berries' => 'Bayas Caoca',
    'Maranga Berries' => 'Bayas Maranga'
  }
  # Some catalog entries are display formats, with no words to translate.
  QUANTITY_FORMAT = /\Ax?\{\d+:[ 0-9]*d\}(?:\/\{\d+:[ 0-9]*d\})?\z/
  FORMATTED_NUMBER = /\Ax? *\d+(?:\/ *\d+)?\z/
  LEVEL_NUMBER = /\ALv *(\d+)\z/
  PP_FORMAT = /\A(?: \(\{\d+\}\) )?PP: ?(?:\d+|\{\d+\})\/(?:\d+|\{\d+\})\z/
  REBORN_TICKET_REGIONS = [
    [108, 28, 294, 24, '¡RUMBO A CIUDAD REBORN!'],
    [110, 179, 75, 20, 'Nombre'], [280, 179, 108, 20, 'Género'],
    [112, 228, 54, 19, 'Tren'], [280, 228, 68, 19, 'Asiento'],
    [110, 252, 60, 20, 'Dest.'],
    [110, 275, 85, 21, 'Adulto'], [280, 276, 72, 22, 'Tipo']
  ]
  SOLAR_SPLASHES = [
    [[180, 205], ['Un juego de', 'Peti & Gelato']],
    [[66, 120, 147, 175, 229, 283, 311],
     ['Este es un juego sin fines de lucro hecho por fans.', '',
      'Este juego no está afiliado a Nintendo,',
      'The Pokémon Company, Creatures Inc. ni Game Freak.', '',
      'Apoya los lanzamientos oficiales.', '',
      'Si pagaste por este juego, te estafaron.']]
  ]
  UI_SPANISH = {
    'Save' => 'Guardar',
    'Close Bag' => 'Cerrar bolsa',
    'Trainer Card' => 'Ficha de entrenador',
    "Trainer's Card" => 'Ficha de entrenador',
    'ID No.' => 'N.º de ID',
    'Phone' => 'Teléfono',
    'Music' => 'Música',
    'Tasks' => 'Tareas',
    'TMs' => 'MT',
    'Appearance' => 'Aspecto',
    'Select your appearance.' => 'Elige tu aspecto.',
    'Select your appearance' => 'Elige tu aspecto',
    'Your name?' => '¿Tu nombre?',
    'Enter text using the keyboard. Press Enter to confirm, or Esc to cancel.' =>
      'Escribe con el teclado. Enter para aceptar o Esc para cancelar.',
    'Enter text using the keyboard. Press ESC to cancel, or ENTER to confirm.' =>
      "Escribe con el teclado.\nEsc para cancelar o Enter para aceptar.",
    'Enter text using the keyboard. Press ENTER to confirm.' =>
      "Escribe con el teclado.\nPulsa Enter para aceptar.",
    'Difficulty' => 'Dificultad',
    'Info' => 'Info',
    'Press Start' => 'Pulsa START',
    'Press Enter' => 'Pulsa Enter',
    'A game by' => 'Un juego de',
    'Press any key' => 'Pulsa cualquier tecla',
    'Continue' => 'Continuar',
    'New Game' => 'Nueva partida',
    'Options' => 'Opciones',
    'Settings' => 'Ajustes',
    'Configure (F1)' => 'Configurar (F1)',
    'Keyboard Defaults' => 'Teclas predeterminadas',
    'Gamepad Defaults' => 'Controles del mando',
    'What controls do you want to see?' => '¿Qué controles quieres ver?',
    'Quit Game' => 'Salir del juego',
    'Exit Game' => 'Salir del juego',
    'Save Game' => 'Guardar partida',
    'Load Game' => 'Cargar partida',
    'Close' => 'Cerrar',
    'Cancel' => 'Cancelar',
    'Back' => 'Atrás',
    'Yes' => 'Sí',
    'No' => 'No',
    'Badges:' => 'Medallas:',
    'Time:' => 'Tiempo:',
    'Text Speed' => 'Velocidad de texto',
    'Battle Effects' => 'Efectos de combate',
    'Battle Style' => 'Estilo de combate',
    'Screen Size' => 'Tamaño de pantalla',
    'On' => 'Sí',
    'Off' => 'No'
  }
  UI_SPANISH_FOLDED = {}
  UI_SPANISH.each { |key, value| UI_SPANISH_FOLDED[key.downcase] = value }

  TRAINER_STYLE_SPANISH = {
    'casual' => 'Relajado', 'serious' => 'Serio', 'proud' => 'Orgulloso', 'the best' => 'El mejor'
  }
  SETUP_SPANISH = {
    'appearance' => 'Aspecto', 'name' => 'Nombre', 'difficulty' => 'Dificultad',
    'additions' => 'Extras', "let's go!!!" => '¡Vamos!'
  }
  SOLAR_TEXT_SPANISH = {
    'tone' => 'TONO',
    'basic reading ability is needed to fully enjoy this game.' =>
      'Para disfrutar plenamente del juego, necesitas saber leer.',
    "do not feed this game's art to ai." => 'No uses el arte de este juego con IA.',
    'the characters and events depicted in this game are fictional.' =>
      'Los personajes y sucesos de este juego son ficticios.',
    "don't take it too seriously." => 'No te lo tomes demasiado en serio.',
    "you're more casual and laidback?" => '¿Eres más bien relajado?',
    "you're more casual and laid-back?" => '¿Eres más bien relajado?',
    'i see... so you only want the toughest battles?' => 'Ya veo… ¿Solo quieres los combates más difíciles?'
  }
  SPEAKER_SPANISH = { 'aunt' => 'Tía', 'uncle' => 'Tío', 'mom' => 'Mamá', 'dad' => 'Papá' }
  NAME_BOX = /\\xn\[([^\[\]\\\r\n]+)\]/i
  SCENE_HINTS = [
    [/\b(?:go|turn|head|move|look|walk)\b.{0,45}\b(?:left|right|direction|which way)\b|\bwhich (?:way|direction)\b/i,
     { 'right' => 'Derecha', 'left' => 'Izquierda', 'straight' => 'De frente', 'back' => 'Atrás' }],
    [/\b(?:fight|battle|opponent|wild pok[eé]mon|flee)\b/i,
     { 'run' => 'Huir', 'fight' => 'Luchar', 'bag' => 'Bolsa' }],
    [/\b(?:is that correct|did i get that right|did i understand correctly)\b/i,
     { 'right' => 'Correcto', 'right.' => 'Correcto.' }]
  ]
  @paint_stack = []
  @paint_waiting = []
  @verbatim = 0
  @reborn_ticket = false
  @hooked = {}
  @originals = []
  @diagnostic_seen = {}
  @diagnostic_rows = []

  class << self
    def utf8(s)
      s.respond_to?(:force_encoding) ? s.force_encoding('UTF-8') : s
    end

    def unesc(s)
      utf8(s.gsub(/\\(.)/) do
        case $1
        when 'n' then "\n"
        when 't' then "\t"
        when 'r' then "\r"
        else $1
        end
      end)
    end

    def esc(s)
      s.gsub('\\') { '\\\\' }.gsub("\n", '\\n').gsub("\t", '\\t').gsub("\r", '\\r')
    end

    def diagnostic_text(text)
      value = utf8(text.dup)
      return nil if value.respond_to?(:valid_encoding?) && !value.valid_encoding?
      name = player_name
      if name.is_a?(String) && !name.empty? && (!name.respond_to?(:valid_encoding?) || name.valid_encoding?)
        value = value.gsub(/(^|[^[:alnum:]_])#{Regexp.escape(name)}(?=$|[^[:alnum:]_])/) { $1 + '{jugador}' }
      end
      value.gsub(/[[:cntrl:]]/, ' ').gsub(/\s+/, ' ').strip[0, 320]
    end

    def record_display(kind, source, displayed)
      return if DIAGNOSTICS.empty? || @diagnostic_seen.size >= DIAGNOSTIC_LIMIT || @verbatim > 0
      return unless source.is_a?(String) && displayed.is_a?(String) && source.size <= 2000 &&
                    source =~ /[A-Za-z]{2}/ && !player_name?(source) && !display_format?(source)
      return if kind == 'draw' && (!@displaying.empty? ||
        (@paint_stack.empty? && source !~ /[A-Za-z]{2}.*\s+[A-Za-z]{2}/))
      map = defined?($game_map) && $game_map
      map_id = map.map_id if map && map.respond_to?(:map_id)
      scene = defined?($scene) && $scene
      scene_name = scene ? scene.class.to_s : ''
      status = if ENGLISH_IN_DISPLAY.match(displayed)
                 'english'
               elsif source == displayed && !displayed.instance_variable_get(:@gahvoria_translated)
                 'unchanged'
               else
                 'handled'
               end
      key = [kind, map_id, scene_name, source.dup, status]
      return if @diagnostic_seen[key]
      visible = status == 'handled' ? '' : diagnostic_text(displayed)
      return unless visible
      @diagnostic_seen[key] = true
      @diagnostic_rows << [status, kind, map_id || 0, scene_name, visible].map { |field| esc(field.to_s) }.join("\t") + "\n"
      flush_diagnostics if @diagnostic_rows.size >= 64
    rescue StandardError
      # Diagnostics must never interfere with rendering.
    end

    def flush_diagnostics
      return if @diagnostic_rows.empty?
      File.open(DIAGNOSTICS, 'ab') { |file| file.write(@diagnostic_rows.join) }
    rescue StandardError
      # Keep the game playable if the diagnostics directory becomes unavailable.
    ensure
      @diagnostic_rows.clear
    end

    # Read only complete replies. Java appends to this session file.
    def load_new
      return if FILE.empty? || !File.exist?(FILE)
      size = File.size(FILE)
      if size < @pos
        @pos = 0
        @map.clear
      end
      return if size == @pos
      data = File.open(FILE, 'rb') { |f| f.seek(@pos); f.read(512 * 1024) }
      return if data.nil?
      last = data.rindex("\n")
      return unless last
      @pos += last + 1
      load_rows(data[0, last])
    rescue StandardError
    end

    def load_opening
      return if OPENING.empty?
      data = File.open(OPENING, 'rb') { |f| f.read(512 * 1024) }
      last = data && data.rindex("\n")
      load_rows(data[0, last]) if last
    rescue StandardError
    end

    def load_rows(data)
      data.split("\n").each do |line|
        a, b = line.split("\t", 2)
        next if b.nil? || a.nil? || a.empty? || b.empty?
        o = unesc(a)
        t = unesc(b)
        next if t.respond_to?(:valid_encoding?) && (!o.valid_encoding? || !t.valid_encoding?)
        @pending.delete(o)
        @map.delete(o)
        @map.shift if @map.size >= @session_limit
        @map[o] = t
      end
    rescue StandardError
    end

    def load_prepared
      @session_limit = MAX_PREPARED_ENTRIES + MAX_SESSION_ENTRIES
      loop do
        previous = @pos
        load_new
        break if @pos == previous
      end
    end

    def request(text)
      return false if REQ.empty? || (text.respond_to?(:bytesize) ? text.bytesize : text.size) > 9000
      now = Time.now.to_f
      return false if @pending[text] && now - @pending[text] < RETRY_SECONDS
      @pending.delete_if { |_key, stamp| now - stamp >= RETRY_SECONDS } if @pending.size >= 512
      return false if @pending.size >= 512
      @pending[text] = now
      @queue << text
      return flush if @queue.size >= MAX_BATCH || now - @last_flush >= 0.25
      true
    rescue StandardError
      false
    end

    def flush
      return true if @queue.empty?
      return false if REQ.empty?
      if Dir.glob(File.join(REQ, '*.req')).size >= MAX_REQUEST_FILES
        @queue.each { |text| @pending.delete(text) }
        @queue.clear
        return false
      end
      @sequence += 1
      name = "%s/%010d" % [REQ, @sequence]
      File.open(name + ".tmp", 'wb') { |f| @queue.each { |text| f.write(esc(text) + "\n") } }
      File.rename(name + ".tmp", name + ".req")
      @queue.clear
      @last_flush = Time.now.to_f
      true
    rescue StandardError
      @queue.each { |text| @pending.delete(text) }
      @queue.clear
      false
    end

    def load_if_due
      now = Time.now.to_f
      return if !@paint_stack.empty? || now - @last_load < 0.12
      @last_load = now
      load_new
      refresh_ready_windows
    end

    def lookup(text, wait = 0)
      return text unless text.is_a?(String) && !text.empty?
      return text if text.instance_variable_get(:@gahvoria_translated) || player_name?(text)
      return text if display_format?(text)
      label = gameplay_label(text)
      return marked(label) if label
      return text unless text =~ /[A-Za-z]/ || (text.respond_to?(:ascii_only?) ? !text.ascii_only? : text.unpack('C*').any? { |byte| byte >= 128 })
      load_if_due
      return translated_copy(text) if @map.has_key?(text)
      return text if REQ.empty? || FILE.empty?
      request(text)
      @paint_stack.last[1][text.dup] = true unless @paint_stack.empty?
      if @pending[text] && wait > 0 && flush
        deadline = Time.now + wait
        while (remaining = deadline - Time.now) > 0
          sleep([0.04, remaining].min)
          load_new
          return translated_copy(text) if @map.has_key?(text)
        end
      end
      text
    rescue StandardError
      text
    end

    # Preserve provenance when the same String flows through _INTL and a display hook.
    def translated_copy(text)
      result = @map[text].dup
      result.instance_variable_set(:@gahvoria_translated, true)
      result
    end

    # Kinds of names that stay as they are (Pokémon, trainers)
    def keep_type?(type)
      return false unless defined?(MessageTypes)
      @keep ||= begin
        list = []
        %w[Species SPECIES_NAMES TrainerNames TRAINER_NAMES SpeciesForms FORM_NAMES].each do |c|
          list << MessageTypes.const_get(c) if MessageTypes.const_defined?(c)
        end
        list
      end
      @keep.include?(type)
    rescue Exception
      false
    end

    # Repaint only known windows that drew an original while inference was pending.
    # Limit retained windows and the number repainted per frame.
    def paint(window)
      load_if_due
      stable_widths = window.class.ancestors.any? { |type| type.to_s =~ /Option|Settings/ }
      state = [window, {}, nil, stable_widths ? {} : nil]
      @paint_stack << state
      begin
        yield
      ensure
        @paint_stack.pop
        @paint_waiting.delete_if { |item| item[0].equal?(window) }
        if !state[1].empty?
          @paint_waiting.shift if @paint_waiting.size >= 32
          @paint_waiting << state
        end
      end
    end

    def refresh_ready_windows
      return unless @paint_stack.empty?
      ready = []
      @paint_waiting.delete_if do |item|
        window, texts = item
        if (window.respond_to?(:disposed?) && window.disposed?) || (item[2] && window.text != item[2])
          true
        elsif ready.size < 2 && (!window.respond_to?(:visible) || window.visible) &&
              texts.keys.any? { |text| @map.has_key?(text) }
          ready << item
          true
        else
          false
        end
      end
      ready.each do |item|
        window = item[0]
        begin
          if item[2]
            window.text = message_text(item[2], 0)
          elsif window.respond_to?(:pbRefresh)
            window.pbRefresh
          else
            window.refresh
          end
        rescue StandardError
          # A game can dispose or replace its window during a scene transition.
        end
      end
    end

    def marked(text)
      result = text.dup
      result.instance_variable_set(:@gahvoria_translated, true)
      result
    end

    def display_format?(text)
      return false unless text.size <= 100
      text == 'Pokémon' || text == "{1:s}\1" || text == '{1:s}\1' ||
        (REBORN && text == REBORN_GLITCH_TEXT) ||
        QUANTITY_FORMAT.match(text) || PP_FORMAT.match(text) ||
        FORMATTED_NUMBER.match(text) || /\ANv\. ?\d+\z/.match(text)
    end

    def gameplay_label(text)
      return nil unless SPANISH
      label = SPANISH_GAMEPLAY_TEXT[text]
      label ||= 'Nv.' + $1 if LEVEL_NUMBER.match(text)
      label
    end

    def ui_label(text)
      label = UI_SPANISH[text]
      return label if label
      normalized = text.gsub("’", "'").gsub(/\s+/, ' ').strip
      return 'Pulsa para empezar' if text =~ /\Apress\s+(?:the\s+)?(?:enter|start)(?:\s+key)?[!.]?\z/i
      UI_SPANISH_FOLDED[normalized.downcase]
    end

    def visible_label(text)
      return nil unless SPANISH && text.is_a?(String) && !player_name?(text)
      return nil if text.instance_variable_get(:@gahvoria_translated)
      # The formatter can add a color prefix, and the game can supply manual line breaks.
      return nil if text.size > 512
      match = /\A((?:<[^<>\r\n]{1,80}>)*)([^<>]*)((?:<\/[A-Za-z0-9]+>)*)\z/.match(text)
      return nil unless match
      body = match[2].gsub("’", "'").gsub("…", "...").gsub(/\s+/, ' ').strip
      label = ui_label(body)
      label ||= gameplay_label(body)
      label ||= SOLAR_TEXT_SPANISH[body.downcase] if SOLAR_ECLIPSE
      marked(match[1] + label + match[3]) if label
    end

    def player_name
      player = defined?($player) && $player
      player ||= defined?($Trainer) && $Trainer
      player.name if player && player.respond_to?(:name)
    end

    def player_name?(text)
      player_name == text
    end

    def reborn_intro_map?
      map = defined?($game_map) && $game_map
      REBORN && map && map.respond_to?(:map_id) && REBORN_INTRO_MAPS.include?(map.map_id)
    end

    def reborn_intro_label(text)
      return nil unless text.is_a?(String) && text.size <= 512 && reborn_intro_map?
      body = text.sub(/\x01\z/, '').gsub(/\\n/i, ' ')
      body = body.gsub("’", "'").gsub(/\s+/, ' ').strip.downcase
      label = REBORN_INTRO_SPANISH[body]
      marked(label + (text.end_with?("\1") ? "\1" : '')) if label
    end

    def scene_label(text, hint)
      return nil unless SPANISH && text.is_a?(String) && hint.is_a?(String) && hint.size <= 512
      return nil if text.instance_variable_get(:@gahvoria_translated) || player_name?(text)
      key = text.strip.downcase
      SCENE_HINTS.each do |pattern, terms|
        return marked(terms[key]) if terms.has_key?(key) && hint =~ pattern
      end
      nil
    end

    def scene_id
      map = defined?($game_map) && $game_map
      return nil unless map && map.respond_to?(:map_id)
      interpreter = map.interpreter if map.respond_to?(:interpreter)
      system = defined?($game_system) && $game_system
      interpreter ||= system.map_interpreter if system && system.respond_to?(:map_interpreter)
      event = interpreter.event_id if interpreter && interpreter.respond_to?(:event_id)
      [map.map_id, event] if event.is_a?(Integer) && event > 0
    end

    def scene_hint(source)
      return nil unless @scene_message && source.is_a?(String)
      id, stamp, previous = @scene_message
      elapsed = Time.now.to_f - stamp
      return nil unless id && id == scene_id && elapsed >= 0 && elapsed <= 12
      current_speaker = NAME_BOX.match(source)
      previous_speaker = NAME_BOX.match(previous)
      return nil if current_speaker && previous_speaker && current_speaker[1] != previous_speaker[1]
      previous
    end

    def remember_scene_message(source)
      return unless SPANISH
      @scene_message = nil
      return unless source.is_a?(String) && source.size <= 512
      id = scene_id
      @scene_message = [id, Time.now.to_f, source] if id
    end

    def render_choices(choices, question = nil)
      return choices unless choices.is_a?(Array)
      normalized = choices.map { |value| value.is_a?(String) ? value.gsub("’", "'").strip.downcase : nil }
      glossary = nil
      if SPANISH
        if REBORN && REBORN_GENDER_SPANISH.keys.all? { |key| normalized.include?(key) }
          glossary = REBORN_GENDER_SPANISH
        elsif normalized.include?('casual') && normalized.include?('proud') && normalized.include?('the best')
          glossary = TRAINER_STYLE_SPANISH
        elsif normalized.include?('appearance') && normalized.include?('name') && normalized.include?('difficulty')
          glossary = SETUP_SPANISH
        elsif SOLAR_ECLIPSE && question.is_a?(String) && question =~ /how would you describe yourself as a trainer/i
          glossary = TRAINER_STYLE_SPANISH
        end
      end
      if SPANISH && normalized.include?('left') && normalized.include?('right')
        question = 'Which way?'
      end
      choices.each_with_index.map do |text, index|
        label = glossary && glossary[normalized[index]]
        rendered = label ? marked(label) : scene_label(text, question) || rendering_text(text, true)
        record_display('choice', text, rendered) if @displaying.empty?
        rendered
      end
    end

    PLAYER_NAME_TOKEN = '{99999}'

    def message_key(source)
      name = player_name
      token = PLAYER_NAME_TOKEN
      if name.is_a?(String) && name.size >= 2 && name =~ /\A[A-Za-z0-9_\- ]+\z/ && !source.include?(token) &&
         !source.include?('\\') && !source.include?('<')
        return source.gsub(/(^|[^A-Za-z0-9_])#{Regexp.escape(name)}(?=$|[^A-Za-z0-9_])/) { $1 + token }
      end
      source
    end

    def message_text(source, wait, hint = nil)
      return lookup(source, wait) unless SPANISH && source.is_a?(String)
      return source if source.instance_variable_get(:@gahvoria_translated)
      label = reborn_intro_label(source) || scene_label(source, hint) || visible_label(source)
      return label if label
      pause = source.end_with?("\1")
      dialogue = pause ? source[0...-1] : source
      key = message_key(dialogue)
      token = PLAYER_NAME_TOKEN
      translated = lookup(key, wait)
      if translated.equal?(key)
        translated = source
      else
        translated = marked(translated.gsub(token) { player_name.to_s }) if key != dialogue
        translated = marked(translated + "\1") if pause
      end
      # Translate roles on nameplates. The body and the exact speaker identifiers stay independent.
      text = translated.gsub(NAME_BOX) do |code|
        name = $1
        role = SPEAKER_SPANISH[name.downcase]
        role && !player_name?(name) ? code.sub('[' + name + ']', '[' + role + ']') : code
      end
      text != translated ? marked(text) : translated
    end

    def rendering_text(text, choice = false)
      if @reborn_ticket && @verbatim == 0 && text.is_a?(String) && !player_name?(text)
        key = text.downcase
        return REBORN_GENDER_SPANISH[key] || REBORN_TICKET_TEXT[key] || text
      end
      return text if @verbatim > 0 || (!choice && !@displaying.empty?)
      if SPANISH && text.is_a?(String) && !text.instance_variable_get(:@gahvoria_translated)
        return text if player_name?(text)
        label = visible_label(text)
        return label if label
      end
      lookup(text)
    end

    def verbatim
      @verbatim += 1
      begin
        yield
      ensure
        @verbatim -= 1
      end
    end

    # Solar Eclipse's two intro slides have white text on a black 4:3 canvas.
    # Recognize their distinct line positions and margins before drawing Spanish.
    def solar_splash(bitmap)
      width, height = bitmap.width, bitmap.height
      return nil unless width >= 320 && height >= 240 && width <= 1280 && (width * 3 - height * 4).abs <= 8
      dark_margins = [[12, 12], [500, 12], [12, 370], [500, 370], [256, 26]].all? do |point|
        color = bitmap.get_pixel(width * point[0] / 512, height * point[1] / 384)
        color.red < 28 && color.green < 28 && color.blue < 28 && color.alpha > 200
      end
      return nil unless dark_margins
      centers = []
      first = nil
      0.step(380, 3) do |y|
        hits = 0
        32.step(480, 3) do |x|
          color = bitmap.get_pixel(width * x / 512, height * y / 384)
          hits += 1 if color.red > 145 && color.green > 145 && color.blue > 145 && color.alpha > 180
        end
        if hits >= 3
          first = y if first.nil?
        elsif first
          centers << (first + y - 3) / 2 if y - first >= 6
          first = nil
        end
      end
      SOLAR_SPLASHES.each do |signature, lines|
        if centers.size == signature.size && centers.zip(signature).all? { |pair| (pair[0] - pair[1]).abs <= 9 }
          return lines
        end
      end
      nil
    end

    def title_prompt_rect(bitmap)
      width, height = bitmap.width, bitmap.height
      return [0, 0, width, height] if width >= 48 && height >= 10 && height <= 160 && width >= height * 2
      return nil unless SOLAR_ECLIPSE && width >= 320 && height >= 240 && width <= 1280
      # Some title prompts are transparent canvases the size of the whole screen.
      # Find only their lower, compact band of visible pixels.
      min_x = width
      max_x = 0
      min_y = height
      max_y = 0
      return nil if bitmap.get_pixel(width / 2, height / 3).alpha >= 48
      step = [width / 160, 2].max
      0.step(height - 1, step) do |y|
        0.step(width - 1, step) do |x|
          next if bitmap.get_pixel(x, y).alpha < 48
          min_x = x if x < min_x
          max_x = x if x > max_x
          min_y = y if y < min_y
          max_y = y if y > max_y
        end
      end
      return nil unless min_y >= height * 2 / 3 && max_y - min_y <= height / 6 &&
                        max_x - min_x >= width / 10 && max_x - min_x <= width * 3 / 4 &&
                        (min_x + max_x - width).abs <= width / 5
      left = [min_x - 8, 0].max
      top = [min_y - 4, 0].max
      right = [max_x + 9, width].min
      bottom = [max_y + 5, height].min
      [left, top, right - left, bottom - top]
    end

    def replace_title_text(bitmap, rect, lines, opaque)
      x, y, width, height = rect
      replacement = Bitmap.new(width, height)
      begin
        replacement.fill_rect(0, 0, width, height, Color.new(0, 0, 0)) if opaque
        row_height = opaque ? height * 7 / 100 : height
        font = replacement.font
        font.size = [[row_height - 2, 32].min, 9].max
        font.bold = !opaque
        font.color = Color.new(255, 255, 255)
        font.outline = !opaque if font.respond_to?(:outline=)
        top = (height - row_height * lines.size) / 2
        verbatim do
          lines.each_with_index do |line, index|
            replacement.draw_text(4, top + index * row_height, width - 8, row_height, line, 1)
          end
        end
        if opaque
          bitmap.clear
        else
          bitmap.clear_rect(x, y, width, height)
        end
        bitmap.blt(x, y, replacement, Rect.new(0, 0, width, height))
      ensure
        replacement.dispose
      end
    end

    def localize_title_prompt(bitmap, path)
      return unless SPANISH && path.is_a?(String)
      normalized = path.tr('\\', '/').sub(/\A\.\/+/, '')
      if REBORN && normalized =~ /\AGraphics\/Pictures\/introTicket(?:\.png)?\z/i
        localize_reborn_ticket(bitmap)
        return
      end
      title_folder = normalized =~ /\AGraphics\/(?:Titles|Title|UI\/(?:Titles|Title|Title Screen))\//i
      filename = normalized.split('/').last.to_s.sub(/\.(?:png|bmp|gif|webp)\z/i, '').downcase.gsub(/[ _-]/, '')
      solar_intro = SOLAR_ECLIPSE && normalized =~ /\AGraphics\/(?:Pictures|UI|Titles?)\//i &&
                    (title_folder || normalized =~ /\/(?:intro(?:duction)?|splashes|splash|title(?:[ _-]?screen)?s?)\//i ||
                     filename =~ /\A(?:splash|intro|credit|disclaimer|notice|warning|press)/)
      return unless title_folder || solar_intro

      if filename =~ /\A(?:start|pressstart|pressenter|presstostart|startprompt|titlepressstart|titlepressenter|titlestart|enter|press)\z/
        rect = title_prompt_rect(bitmap)
        if rect
          label = filename.include?('enter') ? 'Pulsa Enter' : 'Pulsa para empezar'
          replace_title_text(bitmap, rect, [label], false)
        end
      elsif solar_intro
        lines = solar_splash(bitmap)
        if lines
          replace_title_text(bitmap, [0, 0, bitmap.width, bitmap.height], lines, true)
        elsif bitmap.height >= 240 && (title_folder || filename =~ /(?:title|start|enter|prompt)/)
          rect = title_prompt_rect(bitmap)
          replace_title_text(bitmap, rect, ['Pulsa Enter'], false) if rect
        end
      end
    rescue StandardError
      # Leave unrecognized artwork on the normal rendering path.
    end

    def localize_reborn_ticket(bitmap)
      return unless bitmap.width == 512 && bitmap.height == 384
      ink = bitmap.get_pixel(112, 182)
      paper = bitmap.get_pixel(104, 30)
      return unless ink.alpha == 255 && ink.red < 110 && paper.red > 185 &&
                    bitmap.get_pixel(0, 383).alpha == 0
      font = bitmap.font
      font.color = Color.new(78, 66, 66)
      font.bold = true
      font.outline = false if font.respond_to?(:outline=)
      font.shadow = false if font.respond_to?(:shadow=)
      verbatim do
        REBORN_TICKET_REGIONS.each_with_index do |region, index|
          x, y, width, height, text = region
          bitmap.fill_rect(x, y, width, height, bitmap.get_pixel(x + width, y + height / 2))
          font.size = index == 0 ? 20 : 16
          bitmap.draw_text(x + 2, y, width - 4, height, text, index == 0 ? 1 : 0)
        end
      end
    end

    # ------------------------------------------------------------ hooks

    def invoke(index, receiver, args, block)
      original, argument, wait, kind = @originals[index]
      if kind == :paint
        return paint(receiver) { original.bind(receiver).call(*args, &block) }
      elsif kind == :pause_menu
        previous_menu = @pause_menu
        begin
          @pause_menu = receiver.instance_variable_get(:@scene)
          return original.bind(receiver).call(*args, &block)
        ensure
          @pause_menu = previous_menu
        end
      elsif kind == :controls_dialogue && @pause_menu
        menu = @pause_menu
        menu.pbHideMenu
        begin
          return original.bind(receiver).call(*args, &block)
        ensure
          menu.pbShowMenu
        end
      elsif kind == :command_size
        if args[0].is_a?(Array)
          args[0] = render_choices(args[0], @choice_question)
        end
      elsif kind == :verbatim
        return verbatim { original.bind(receiver).call(*args, &block) }
      elsif kind == :reborn_ticket
        previous_ticket = @reborn_ticket
        begin
          @reborn_ticket = true
          return original.bind(receiver).call(*args, &block)
        ensure
          @reborn_ticket = previous_ticket
        end
      elsif kind == :help_text
        if args[argument].is_a?(String)
          source = args[argument]
          args[argument] = visible_label(source) || lookup(source)
          record_display('help', source, args[argument])
        end
      elsif kind == :layout
        if @verbatim == 0 && @displaying.empty?
          text = args[argument]
          label = visible_label(text)
          if label.nil? && text.is_a?(String) && !text.instance_variable_get(:@gahvoria_translated) && !player_name?(text)
            load_if_due
            label = translated_copy(text) if @map.has_key?(text)
            label ||= lookup(text) unless @paint_stack.empty?
          end
        end
        args[argument] = label if label
        return verbatim { original.bind(receiver).call(*args, &block) }
      elsif kind == :bitmap_init
        result = original.bind(receiver).call(*args, &block)
        localize_title_prompt(receiver, args[0]) if args.size == 1
        return result
      elsif kind == :bitmap_text
        # Bitmap#draw_text accepts (rect, text, align) and (x, y, w, h, text, align).
        slot = args.size <= 3 ? 1 : 4
        text = args[slot]
        if text.is_a?(String) && text =~ /[A-Za-z]{2}/
          args[slot] = rendering_text(text)
          record_display('draw', text, args[slot])
          widths = @paint_stack.empty? ? nil : @paint_stack.last[3]
          if slot == 4 && widths && text != args[slot] && widths[text] && widths[text] > 0 && args[2].is_a?(Integer)
            args[2] = args[2] > 0 ? [args[2], widths[text]].min : widths[text]
          end
        end
      elsif kind == :measure
        unless @paint_stack.empty? || @verbatim > 0
          widths = @paint_stack.last[3]
          if widths && args[0].is_a?(String)
            result = original.bind(receiver).call(*args, &block)
            widths[args[0]] = result.width if result.respond_to?(:width)
            return result
          end
          args[0] = rendering_text(args[0])
        end
      elsif kind == :text_positions
        if args[argument].is_a?(Array) && @verbatim == 0
          args[argument] = args[argument].map do |row|
            if row.is_a?(Array) && row[0].is_a?(String)
              copy = row.dup
              copy[0] = rendering_text(copy[0])
              record_display('draw', row[0], copy[0])
              copy
            else
              row
            end
          end
        end
        return verbatim { original.bind(receiver).call(*args, &block) }
      elsif kind == :draw
        source = args[argument]
        args[argument] = rendering_text(source)
        record_display('draw', source, args[argument])
        widths = @paint_stack.empty? ? nil : @paint_stack.last[3]
        # Option rows often advance x using a separate measurement of each value.
        # Keep that slot fixed and let RGSS fit the translated glyphs inside it.
        if widths && argument == 5 && args[3].is_a?(Integer) &&
           source != args[argument] && widths[source] && widths[source] > 0
          args[3] = args[3] > 0 ? [args[3], widths[source]].min : widths[source]
        end
        return verbatim { original.bind(receiver).call(*args, &block) }
      elsif kind == :window
        # pbMessageDisplay already intercepts complete text before it expands controls.
        # Static help boxes can be passed directly to the same setter.
        if @verbatim == 0 && @displaying.empty? && (!receiver.respond_to?(:text) || receiver.text != args[argument])
          slow = receiver.respond_to?(:letterbyletter) && receiver.letterbyletter
          source = args[argument]
          args[argument] = message_text(source, slow ? wait : 0)
          record_display('window', source, args[argument])
          key = SPANISH && source.is_a?(String) ? message_key(source) : source
          if !slow && source.is_a?(String) && args[argument].equal?(source) && @pending[key]
            @paint_waiting.delete_if { |item| item[0].equal?(receiver) }
            @paint_waiting.shift if @paint_waiting.size >= 32
            @paint_waiting << [receiver, {key.dup => true}, source.dup]
          end
        end
      elsif kind == :commands
        if args[argument].is_a?(Array)
          args[argument] = render_choices(args[argument], @choice_question)
        end
      elsif args.length > argument && !@displaying.include?(args[argument])
        if kind == :message_with_choices && args[1].is_a?(Array)
          args[1] = render_choices(args[1], args[argument])
        end
        source = args[argument]
        outer = @displaying.empty?
        args[argument] = message_text(source, wait, outer ? scene_hint(source) : nil)
        record_display('dialogue', source, args[argument]) if outer
        remember_scene_message(source) if outer
        previous_question = @choice_question
        @choice_question = source if outer
        @displaying << args[argument]
        begin
          return original.bind(receiver).call(*args, &block)
        ensure
          @displaying.pop
          @choice_question = previous_question
        end
      end
      original.bind(receiver).call(*args, &block)
    end

    def wrap_method(owner, name, argument, wait, kind)
      return unless owner.instance_methods(false).any? { |method| method.to_s == name.to_s } ||
                    owner.private_instance_methods(false).any? { |method| method.to_s == name.to_s }
      current = owner.instance_method(name)
      return if @hooked[[owner, name]] == current
      private_method = owner.private_method_defined?(name)
      protected_method = owner.protected_method_defined?(name)
      index = @originals.size
      @originals << [current, argument, wait, kind]
      # Ruby 1.8 accepts &block on a method definition, but not in define_method's block.
      if kind == :window
        owner.class_eval("def text=(value); EssentiaTranslate.invoke(#{index}, self, [value], nil); end")
      else
        owner.class_eval("def #{name}(*args, &block); EssentiaTranslate.invoke(#{index}, self, args, block); end")
        owner.send(:ruby2_keywords, name) if owner.respond_to?(:ruby2_keywords, true)
      end
      owner.send(:private, name) if private_method
      owner.send(:protected, name) if protected_method
      @hooked[[owner, name]] = owner.instance_method(name)
    rescue StandardError
    end

    def wrap_top(name, argument, wait, kind)
      meta = class << Kernel; self; end
      [Object, Kernel, meta].each { |owner| wrap_method(owner, name, argument, wait, kind) }
    end

    def wrap_dialogue_window
      if defined?(Window_AdvancedTextPokemon)
        wrap_method(Window_AdvancedTextPokemon, :text=, 0, DIALOGUE_WAIT, :window)
      end
      wrap_method(Window_UnformattedTextPokemon, :text=, 0, 0, :window) if defined?(Window_UnformattedTextPokemon)
    end

    def wrap_ui
      if defined?(Bitmap)
        wrap_method(Bitmap, :initialize, 0, 0, :bitmap_init)
        wrap_method(Bitmap, :draw_text, 0, 0, :bitmap_text)
        wrap_method(Bitmap, :text_size, 0, 0, :measure)
      end
      wrap_top(:getLineBrokenChunks, 1, 0, :layout)
      wrap_top(:getFormattedText, 5, 0, :layout)
      wrap_top(:getFormattedTextForDims, 5, 0, :layout)
      wrap_top(:pbDrawTextPositions, 1, 0, :text_positions)
      [:pbDrawPlainText, :pbDrawShadowText, :pbDrawOutlineText].each { |name| wrap_top(name, 5, 0, :draw) }
      wrap_top(:drawFormattedTextEx, 4, 0, :draw)
      wrap_top(:drawTextEx, 5, 0, :draw)
      wrap_top(:pbEnterPlayerName, 0, 0, :help_text)
      wrap_top(:pbTicketText, 0, 0, :reborn_ticket) if SPANISH && REBORN
      if REBORN && defined?(PokemonMenu)
        wrap_method(PokemonMenu, :pbStartPokemonMenu, 0, 0, :pause_menu)
        wrap_top(:controlsDialogue, 0, 0, :controls_dialogue)
      end
      [:drawFormattedChars, :drawSingleFormattedChar, :renderLineBrokenChunks,
       :renderLineBrokenChunksWithShadow].each { |name| wrap_top(name, 0, 0, :verbatim) }
      if defined?(Window_DrawableCommand)
        wrap_method(Window_DrawableCommand, :refresh, 0, 0, :paint)
        wrap_method(Window_DrawableCommand, :getAutoDims, 0, 0, :command_size)
      end
      if defined?(Window_TextEntry)
        wrap_method(Window_TextEntry, :refresh, 0, 0, :verbatim)
      end
      if defined?(Window_TextEntry_Keyboard)
        wrap_method(Window_TextEntry_Keyboard, :refresh, 0, 0, :verbatim)
      end
      if defined?(Window_UnformattedTextPokemon)
        wrap_method(Window_UnformattedTextPokemon, :initialize, 0, 0, :help_text)
      end
      wrap_method(PokemonLoadPanel, :refresh, 0, 0, :paint) if defined?(PokemonLoadPanel)
      wrap_method(Window_PokemonOption, :refresh, 0, 0, :paint) if defined?(Window_PokemonOption)
    end

    def message_catalog_text(text)
      if text.is_a?(String) && !player_name?(text)
        label = gameplay_label(text)
        return marked(label) if label
      end
      # Keep UI originals available for the selector or drawing hook to resolve in context.
      return text if SPANISH && text.is_a?(String) && (ui_label(text) ||
        (REBORN && (REBORN_GENDER_SPANISH.has_key?(text.downcase) || reborn_intro_label(text))) ||
        TRAINER_STYLE_SPANISH.has_key?(text.downcase) || SETUP_SPANISH.has_key?(text.downcase) ||
        SCENE_HINTS.any? { |_pattern, terms| terms.has_key?(text.downcase) })
      lookup(text, 0)
    end

    def wrap_messages
      return unless defined?(MessageTypes)
      meta = class << MessageTypes; self; end
      %w[getFromHash getFromMapHash get].each do |m|
        name = m.to_sym
        next unless meta.method_defined?(name)
        current = meta.instance_method(name)
        next if @hooked[[meta, name]] == current
        if m == 'getFromMapHash'
          meta.send(:define_method, name) do |*args|
            EssentiaTranslate.message_catalog_text(current.bind(self).call(*args))
          end
        else
          meta.send(:define_method, name) do |*args|
            r = current.bind(self).call(*args)
            EssentiaTranslate.keep_type?(args[0]) ? r : EssentiaTranslate.message_catalog_text(r)
          end
        end
        @hooked[[meta, name]] = meta.instance_method(name)
      end
    rescue Exception
    end

    def install
      return if @installing
      @installing = true
      begin
        wrap_top(:pbMessage, 0, DIALOGUE_WAIT, :message_with_choices)
        wrap_top(:pbMessageDisplay, 1, DIALOGUE_WAIT, :message)
        wrap_top(:pbShowCommands, 1, 0, :commands)
        wrap_messages
        wrap_dialogue_window
        wrap_ui
      ensure
        @installing = false
      end
    end

    def tick
      @frame += 1
      install if @frame == 1 || (@frame % 120).zero?
      if (@frame % 15).zero?
        load_if_due
        flush
        flush_diagnostics unless DIAGNOSTICS.empty?
      end
    end
  end
end

unless EssentiaTranslate::FILE.empty? || EssentiaTranslate::REQ.empty?
  EssentiaTranslate.load_opening
  EssentiaTranslate.load_new
  EssentiaTranslate.install

  class << Object
    alias_method :essentia_translation_method_added, :method_added
    def method_added(name)
      essentia_translation_method_added(name)
      if name.to_s =~ /\A(?:_INTL|pbMessage|pbMessageDisplay|pbShowCommands|getLineBrokenChunks|getFormattedText(?:ForDims)?|pbDrawTextPositions|pbDrawPlainText|pbDrawShadowText|pbDrawOutlineText|drawTextEx|drawFormattedTextEx|drawFormattedChars|drawSingleFormattedChar|renderLineBrokenChunks|renderLineBrokenChunksWithShadow)\z/
        EssentiaTranslate.install
      end
    end
  end

  module Graphics
    class << self
      alias_method :essentia_tr_original_update, :update
      def update(*args)
        essentia_tr_original_update(*args)
        EssentiaTranslate.tick
      end
    end
  end
end
