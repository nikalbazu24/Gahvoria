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
  MAX_REQUEST_FILES = 128
  RETRY_SECONDS = 18
  DIALOGUE_WAIT = 0.65
  @hooked = {}
  @originals = []

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
      data[0, last].split("\n").each do |line|
        a, b = line.split("\t", 2)
        next if b.nil? || a.nil? || a.empty? || b.empty?
        o = unesc(a)
        t = unesc(b)
        next if t.respond_to?(:valid_encoding?) && (!o.valid_encoding? || !t.valid_encoding?)
        @pending.delete(o)
        @map.delete(o)
        @map.shift if @map.size >= MAX_SESSION_ENTRIES
        @map[o] = t
      end
    rescue StandardError
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
      return if now - @last_load < 0.12
      @last_load = now
      load_new
    end

    def lookup(text, wait = 0)
      return text unless text.is_a?(String) && !text.empty?
      return text if text.instance_variable_get(:@gahvoria_translated)
      return text unless text =~ /[A-Za-z]/ || (text.respond_to?(:ascii_only?) ? !text.ascii_only? : text.unpack('C*').any? { |byte| byte >= 128 })
      load_if_due
      return translated_copy(text) if @map.has_key?(text)
      return text if REQ.empty? || FILE.empty?
      request(text)
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

    # ------------------------------------------------------------ hooks

    def invoke(index, receiver, args, block)
      original, argument, wait, kind = @originals[index]
      if kind == :window
        # pbMessageDisplay already intercepts complete text before it expands controls.
        # Direct scene writers bypass it and give the letter-by-letter window the original.
        if @displaying.empty? && receiver.respond_to?(:letterbyletter) && receiver.letterbyletter &&
           (!receiver.respond_to?(:text) || receiver.text != args[argument])
          args[argument] = lookup(args[argument], wait)
        end
      elsif kind == :commands
        if args[argument].is_a?(Array)
          args[argument] = args[argument].map { |text| lookup(text) }
        end
      elsif args.length > argument && !@displaying.include?(args[argument])
        args[argument] = lookup(args[argument], wait)
        @displaying << args[argument]
        begin
          return original.bind(receiver).call(*args, &block)
        ensure
          @displaying.pop
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
      return unless defined?(Window_AdvancedTextPokemon)
      wrap_method(Window_AdvancedTextPokemon, :text=, 0, DIALOGUE_WAIT, :window)
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
            EssentiaTranslate.lookup(current.bind(self).call(*args), 0)
          end
        else
          meta.send(:define_method, name) do |*args|
            r = current.bind(self).call(*args)
            EssentiaTranslate.keep_type?(args[0]) ? r : EssentiaTranslate.lookup(r, 0)
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
        wrap_top(:pbMessage, 0, DIALOGUE_WAIT, :message)
        wrap_top(:pbMessageDisplay, 1, DIALOGUE_WAIT, :message)
        wrap_top(:pbShowCommands, 1, 0, :commands)
        wrap_messages
        wrap_dialogue_window
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
      end
    end
  end
end

unless EssentiaTranslate::FILE.empty? || EssentiaTranslate::REQ.empty?
  EssentiaTranslate.load_new
  EssentiaTranslate.install

  class << Object
    alias_method :essentia_translation_method_added, :method_added
    def method_added(name)
      essentia_translation_method_added(name)
      case name.to_s
      when 'pbMessage', 'pbMessageDisplay', 'pbShowCommands'
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
