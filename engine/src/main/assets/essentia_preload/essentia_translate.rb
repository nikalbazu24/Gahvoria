# essentia_translate.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Automatic translation of supported game dialogue (optional, per game).
# The game's texts go through Pokémon Essentials' message system; here they
# are looked up in a translations file kept by Essentia. Texts not translated
# yet are asked for (Essentia translates them in the background: official
# Pokémon names from a glossary, the rest with the translation service).
# Dialogue waits a moment for its translation; menus show the original until
# theirs arrives. Works with both Ruby engines (no newer Ruby syntax here).

module EssentiaTranslate
  FILE = ENV['ESSENTIA_TR_FILE'].to_s
  REQ = ENV['ESSENTIA_TR_REQ'].to_s
  @map = {}
  @done = {}
  @pending = {}
  @queue = []
  @sequence = 0
  @last_flush = 0
  @last_load = 0
  @pos = 0
  @frame = 0
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

    # New lines of the translations file (Essentia appends to it)
    def load_new
      return if FILE.empty? || !File.exist?(FILE)
      size = File.size(FILE)
      @pos = 0 if size < @pos
      return if size == @pos
      data = File.open(FILE, 'rb') { |f| f.seek(@pos); f.read }
      last = data.rindex("\n")
      return unless last
      @pos += last + 1
      data[0, last].split("\n").each do |line|
        a, b = line.split("\t", 2)
        next if b.nil? || a.nil? || a.empty?
        o = unesc(a)
        t = unesc(b)
        @pending.delete(o)
        @map[o] = t
        @done[t] = true
      end
    rescue Exception
    end

    def request(text)
      return false if REQ.empty? || (text.respond_to?(:bytesize) ? text.bytesize : text.size) > 9000
      now = Time.now.to_f
      return false if @pending[text] && now - @pending[text] < 18
      @pending.delete_if { |_key, stamp| now - stamp >= 18 } if @pending.size >= 512
      return false if @pending.size >= 512
      @pending[text] = now
      @queue << text
      flush if @queue.size >= 16 || now - @last_flush >= 0.25
      true
    rescue StandardError
      false
    end

    def flush
      return if @queue.empty? || REQ.empty?
      @sequence += 1
      name = "%s/%010d" % [REQ, @sequence]
      File.open(name + ".tmp", 'wb') { |f| @queue.each { |text| f.write(esc(text) + "\n") } }
      File.rename(name + ".tmp", name + ".req")
      @queue.clear
      @last_flush = Time.now.to_f
    rescue StandardError
      # Allow a later lookup to retry if the mailbox is temporarily unavailable.
      @queue.each { |text| @pending.delete(text) }
      @queue.clear
    end

    def load_if_due
      now = Time.now.to_f
      return if now - @last_load < 0.12
      @last_load = now
      load_new
    end

    def lookup(text, wait = 0)
      return text unless text.is_a?(String) && !text.empty?
      return text unless text =~ /[A-Za-z]/ || (text.respond_to?(:ascii_only?) ? !text.ascii_only? : text.unpack('C*').any? { |byte| byte >= 128 })
      load_if_due
      return @map[text] if @map.has_key?(text)
      return text if @done[text]
      if request(text) && wait > 0
        flush
        deadline = Time.now + wait
        while Time.now < deadline
          sleep(0.04)
          load_new
          return @map[text] if @map.has_key?(text)
        end
      end
      text
    rescue StandardError
      text
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
      if kind == :commands
        if args[argument].is_a?(Array)
          args[argument] = args[argument].map { |text| lookup(text) }
        end
      elsif args.length > argument
        args[argument] = lookup(args[argument], wait)
      end
      original.bind(receiver).call(*args, &block)
    end

    def wrap_top(name, argument, wait, kind)
      meta = class << Kernel; self; end
      [Object, Kernel, meta].each do |owner|
        next unless owner.method_defined?(name) || owner.private_method_defined?(name)
        current = owner.instance_method(name)
        next if current.respond_to?(:owner) && current.owner != owner
        next if @hooked[[owner, name]] == current
        private_method = owner.private_method_defined?(name)
        index = @originals.size
        @originals << [current, argument, wait, kind]
        # Ruby 1.8 accepts &block on a method definition, but not in define_method's block.
        owner.class_eval("def #{name}(*args, &block); EssentiaTranslate.invoke(#{index}, self, args, block); end")
        owner.send(:ruby2_keywords, name) if owner.respond_to?(:ruby2_keywords, true)
        owner.send(:private, name) if private_method
        @hooked[[owner, name]] = owner.instance_method(name)
      end
    rescue StandardError
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
        wrap_top(:pbMessage, 0, 2.0, :message)
        wrap_top(:pbMessageDisplay, 1, 2.0, :message)
        wrap_top(:pbShowCommands, 1, 0, :commands)
        wrap_messages
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
