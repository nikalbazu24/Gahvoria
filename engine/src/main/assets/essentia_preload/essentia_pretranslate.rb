# encoding: UTF-8
# GPL-2.0. Extract display text before the game scripts run. Never evaluate source literals.
module GahvoriaPretranslate
  LITERAL = /(?:\b_INTL|\b_I|\b_ISPRINTF|\bpbMessage|\bpbConfirmMessage(?:Serious)?|\bpbMessageChooseNumber|\bpbEnterPlayerName)\s*\(\s*(?:"((?:\\.|[^"\\\r\n])*)"|'((?:\\.|[^'\\\r\n])*)')/
  SECOND_LITERAL = /(?:\b_MAPINTL|\b_MAPISPRINTF|\bpbMessageDisplay)\s*\(\s*(?:nil|[0-9]+|[A-Za-z_]\w*|\$game_map\.map_id)\s*,\s*(?:"((?:\\.|[^"\\\r\n])*)"|'((?:\\.|[^'\\\r\n])*)')/
  CHOICE_LIST = /(?:\bpbShowCommands\s*\(\s*(?:nil|[A-Za-z_]\w*)\s*,\s*|\bpbMessage\s*\(\s*(?:"(?:\\.|[^"\\\r\n])*"|'(?:\\.|[^'\\\r\n])*')\s*,\s*)\[([^\]\r\n]{1,2048})\]/
  CHOICE_ITEM = /\s*(?:"((?:\\.|[^"\\\r\n])*)"|'((?:\\.|[^'\\\r\n])*)')\s*(,|\z)/
  DISPLAY_LITERAL = /(?:\bgetLineBrokenChunks\s*\(\s*[A-Za-z_]\w*(?:\.contents)?\s*,|\bWindow_(?:Advanced|Unformatted)TextPokemon\.newWithSize\s*\()\s*(?:"((?:\\.|[^"\\\r\n])*)"|'((?:\\.|[^'\\\r\n])*)')/
  EXTENSIONS = ['rxdata', 'rvdata', 'rvdata2']
  MAX_SCRIPT_BYTES = 2 * 1024 * 1024
  MAX_TEXTS = 50_000
  MAX_SPOOL_BYTES = 24 * 1024 * 1024
  STARTUP_BUDGET_SECONDS = 12
  ANIMATION_EDITOR = /\A(?:anim|animation)[ _-]*editor\z/i

  class << self
    def scripts(source)
      return unless source.is_a?(String) && source.size <= MAX_SCRIPT_BYTES
      source = EssentiaTranslate.utf8(source.dup)
      return if source.respond_to?(:valid_encoding?) && !source.valid_encoding?
      literals = []
      [LITERAL, SECOND_LITERAL, DISPLAY_LITERAL].each do |pattern|
        source.scan(pattern) do |double, single|
          offset = $~.begin(0)
          text = literal_text(double, single)
          literals << [offset, text] if text
        end
      end
      literals.sort_by { |offset, _text| offset }.each { |_offset, text| yield text }
      source.scan(CHOICE_LIST) do |capture|
        choices = capture[0]
        labels = []
        until choices.strip.empty?
          item = CHOICE_ITEM.match(choices)
          break unless item && item.begin(0) == 0
          label = literal_text(item[1], item[2])
          break unless label
          labels << label
          choices = choices[item.end(0)..-1]
        end
        labels.each { |label| yield label } if choices.strip.empty?
      end
    end

    def literal_text(double, single)
      return nil if double && double =~ /#(?:\{|@|\$)/
      if double
        double.gsub(/\\([\\'"nrt])/) { {'n' => "\n", 'r' => "\r", 't' => "\t"}[$1] || $1 }
      else
        single.gsub(/\\([\\'])/) { $1 }
      end
    end

    def commands(list, cancelled)
      lines = []
      script = []
      (list || []).each do |command|
        return if cancelled.call
        code = command.code
        params = command.parameters
        unless code == 401 || code == 405
          emit_lines(lines) { |text| yield text }
          lines.clear
        end
        if code != 655 && !script.empty?
          scripts(script.join("\n")) { |text| yield text }
          script.clear
        end
        case code
        when 101
          # VX and VX Ace put the face graphic in the header; the text starts at 401.
          lines << params[0] if params.size == 1 && params[0].is_a?(String)
        when 401, 405
          lines << params[0] if params[0].is_a?(String)
        when 102
          params[0].each { |text| yield text } if params[0].is_a?(Array)
        when 355, 655
          script << params[0] if params[0].is_a?(String)
        when 111
          scripts(params[1]) { |text| yield text } if params[0] == 12
        end
      end
      emit_lines(lines) { |text| yield text }
      scripts(script.join("\n")) { |text| yield text } unless script.empty?
    end

    def emit_lines(lines)
      return if lines.empty?
      yield lines.join("\n")
      if lines.length > 1
        joined = lines.inject('') do |text, line|
          text + (text.empty? || text[-1, 1] == ' ' || line.empty? ? '' : ' ') + line
        end
        yield joined
      end
    end

    def messages(value, depth = 0, seen = {})
      return if depth > 20
      case value
      when String
        yield value
      when Array, Hash
        return if seen[value.object_id]
        seen[value.object_id] = true
        if value.is_a?(Array)
          value.each { |child| messages(child, depth + 1, seen) { |text| yield text } }
        else
          value.each do |key, child|
            yield key if key.is_a?(String)
            messages(child, depth + 1, seen) { |text| yield text }
          end
        end
      end
    end

    def read_data(path)
      load_data(path)
    rescue StandardError
      nil
    end

    def map_commands(path, cancelled)
      map = read_data(path)
      return unless map && map.respond_to?(:events) && map.events.is_a?(Hash)
      map.events.keys.select { |id| id.is_a?(Integer) }.sort.each do |id|
        event = map.events[id]
        return if cancelled.call
        (event.pages || []).each { |page| commands(page.list, cancelled) { |text| yield text } } if event
      end
    end

    def collect_scripts(root, data, cancelled, editor_labels)
      ($RGSS_SCRIPTS || []).each do |entry|
        return if cancelled.call
        next unless entry.is_a?(Array)
        if ANIMATION_EDITOR.match(entry[1].to_s.strip)
          scripts(entry[3]) { |text| editor_labels[text] = true }
        else
          scripts(entry[3]) { |text| yield text }
        end
      end
      [root, data].compact.each do |folder|
        Dir.glob(File.join(folder, '[Ss][Cc][Rr][Ii][Pp][Tt][Ss]', '**', '*.rb')).sort.each do |path|
          return if cancelled.call
          next unless File.file?(path) && File.size(path) <= MAX_SCRIPT_BYTES
          source = File.open(path, 'rb') { |file| file.read(MAX_SCRIPT_BYTES) }
          if ANIMATION_EDITOR.match(File.basename(path, '.rb'))
            scripts(source) { |text| editor_labels[text] = true }
          else
            scripts(source) { |text| yield text }
          end
        end
      end
    end

    def collect(root, cancelled)
      editor_labels = {}
      data = Dir.glob(File.join(root, '[Dd][Aa][Tt][Aa]')).find { |path| File.directory?(path) }
      folder = data ? File.basename(data) : 'Data'
      return if cancelled.call
      ext = EXTENSIONS.find do |suffix|
        (data && File.file?(File.join(data, "System.#{suffix}"))) || read_data("#{folder}/MapInfos.#{suffix}").is_a?(Hash)
      end
      if ext
        system = read_data("#{folder}/System.#{ext}")
        maps = read_data("#{folder}/MapInfos.#{ext}")
        files = data ? Dir.glob(File.join(data, "Map[0-9]*.#{ext}")).sort.map { |path| File.basename(path) } : []
        files |= maps.keys.select { |id| id.is_a?(Integer) }.sort.map { |id| "Map%03d.#{ext}" % id } if maps.is_a?(Hash)
        if system && system.respond_to?(:start_map_id) && system.start_map_id.is_a?(Integer)
          first = "Map%03d.#{ext}" % system.start_map_id
          if files.delete(first)
            map_commands("#{folder}/#{first}", cancelled) { |text| yield text }
          end
        end
        collect_scripts(root, data, cancelled, editor_labels) { |text| yield text }
        return if cancelled.call
        plugins = read_data("#{folder}/PluginScripts.#{ext}")
        if plugins.is_a?(Array) && defined?(Zlib::Inflate)
          plugins.each do |plugin|
            next unless plugin.is_a?(Array) && plugin[2].is_a?(Array)
            plugin[2].each do |entry|
              return if cancelled.call
              next unless entry.is_a?(Array) && entry[1].is_a?(String) && entry[1].size <= MAX_SCRIPT_BYTES
              inflater = Zlib::Inflate.new
              begin
                decoded = ''
                offset = 0
                while offset < entry[1].size && decoded.size <= MAX_SCRIPT_BYTES
                  decoded << inflater.inflate(entry[1][offset, 512])
                  offset += 512
                end
                scripts(decoded) { |text| yield text } if inflater.finished?
              rescue StandardError
                # A plugin may use a different archive layout.
              ensure
                inflater.close
              end
            end
          end
        end
        events = read_data("#{folder}/CommonEvents.#{ext}")
        (events.is_a?(Array) ? events : []).each do |event|
          return if cancelled.call
          commands(event.list, cancelled) { |text| yield text } if event && event.respond_to?(:list)
        end
        files.each do |name|
          return if cancelled.call
          map_commands("#{folder}/#{name}", cancelled) { |text| yield text }
        end
      end
      collect_scripts(root, data, cancelled, editor_labels) { |text| yield text } unless ext
      names = ['messages.dat', 'messages_core.dat', 'messages_game.dat']
      if data
        names |= Dir.glob(File.join(data, 'messages*.dat')).map { |path| File.basename(path) }
      end
      names.sort.each do |name|
        return if cancelled.call
        # The compiled catalog also contains copies of the editor's labels.
        # If an event or another script uses the same text, it already yielded it above.
        messages(read_data("#{folder}/#{name}")) { |text| yield text unless editor_labels[text] }
      end
    end

    def export_catalog(root, base)
      tmp = base + '.tmp'
      cancel = lambda { File.exist?(base + '.skip') }
      seen = {}
      truncated = false
      File.open(tmp, 'wb') do |file|
        collect(root, cancel) do |text|
          break if cancel.call
          next unless text.is_a?(String) && text.size <= 9000
          text = EssentiaTranslate.utf8(text.dup)
          next if text.respond_to?(:valid_encoding?) && !text.valid_encoding?
          next if text.strip.empty? || text !~ /[[:alpha:]]/ || seen[text]
          next if resolved_text?(text)
          row = EssentiaTranslate.esc(text) + "\n"
          if file.pos + (row.respond_to?(:bytesize) ? row.bytesize : row.size) > 32 * 1024 * 1024 || seen.size >= 120_000
            truncated = true
            break
          end
          seen[text] = true
          file.write(row)
        end
      end
      return if cancel.call
      File.open(base + '.limited', 'wb') {} if truncated
      File.rename(tmp, base + '.ready')
    rescue StandardError
      File.open(base + '.error', 'wb') {}
    ensure
      File.delete(tmp) if File.exist?(tmp)
    end

    def now
      return System.uptime if defined?(System) && System.respond_to?(:uptime)
      Time.now.to_f
    end

    def resolved_text?(text)
      EssentiaTranslate.display_format?(text) ||
        (EssentiaTranslate::SPANISH && (EssentiaTranslate::UI_SPANISH.has_key?(text) ||
          EssentiaTranslate.battle_label(text)))
    end

    def run(root, base, marker = '', budget = STARTUP_BUDGET_SECONDS)
      return if root.empty? || base.empty?
      ready = base + '.ready'
      done = base + '.done'
      skip = base + '.skip'
      spool = base + '.tmp'
      started = now
      cancelled = lambda { now - started >= budget || File.exist?(skip) || File.exist?(done) }
      scan_finished = lambda { cancelled.call || now - started >= [4, budget / 3.0].min }
      File.open(marker, 'wb') {} unless marker.empty?
      seen = {}
      File.open(spool, 'wb') do |file|
        collect(root, scan_finished) do |text|
          break if scan_finished.call
          next unless text.is_a?(String) && text.size <= 9000
          text = EssentiaTranslate.utf8(text.dup)
          next if text.respond_to?(:valid_encoding?) && !text.valid_encoding?
          next if text.strip.empty? || text !~ /[[:alpha:]]/ || seen[text]
          next if resolved_text?(text)
          row = EssentiaTranslate.esc(text) + "\n"
          break if seen.size >= MAX_TEXTS || file.pos + row.size > MAX_SPOOL_BYTES
          seen[text] = true
          file.write(row)
        end
      end
      if cancelled.call || seen.empty?
        File.open(skip, 'wb') {} unless File.exist?(done)
        return
      end
      seen.clear
      File.rename(spool, ready)
      until cancelled.call
        sleep(0.05)
      end
      File.open(skip, 'wb') {} unless File.exist?(done)
      EssentiaTranslate.load_prepared
    rescue StandardError => error
      warn("Gahvoria: no se pudo preparar la traducción: #{error.class}")
      begin
        File.open(skip, 'wb') {}
      rescue StandardError
      end
    ensure
      begin
        File.delete(marker) unless marker.empty? || !File.exist?(marker)
        File.delete(spool) if spool && File.exist?(spool)
      rescue StandardError
      end
    end
  end
end

catalog = ENV['ESSENTIA_TR_CATALOG'].to_s
if catalog.empty?
  GahvoriaPretranslate.run(ENV['ESSENTIA_TR_ROOT'].to_s, ENV['ESSENTIA_TR_PREPARE'].to_s,
                           ENV['ESSENTIA_TR_PREPARE_MARKER'].to_s)
else
  GahvoriaPretranslate.export_catalog(ENV['ESSENTIA_TR_ROOT'].to_s, catalog)
end
