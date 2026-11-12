# essentia_compat.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Compatibility helpers loaded before every game:
#  * Windows programs (.exe, .bat...) can't run on Android. Games that call them
#    (for example update checkers) get an empty answer instead of a crash.
#  * Essentia.fix_legacy_syntax: rewrites old Ruby 1.8 syntax that modern Ruby
#    rejects. The engine uses it to retry a game script that fails to parse.

begin
  require 'stringio'
rescue LoadError
end

module Essentia
  WINDOWS_PROGRAM = /\.(exe|bat|cmd|com|vbs|ps1|msi)\b/i

  def self.windows_program?(args)
    text = args.flatten.map { |a| a.is_a?(Hash) ? '' : a.to_s }.join(' ')
    return false unless text =~ WINDOWS_PROGRAM
    note_program(text)
    true
  end

  # The game wanted to run its updater (PokéUpdater): tell Essentia, which can
  # do the update from the library.
  def self.note_program(text)
    return unless text =~ /updater/i
    ack = ENV['ESSENTIA_ACK_FILE'].to_s
    File.open(ack, 'ab') { |f| f.write("updater 1\n") } unless ack.empty?
  rescue Exception
  end

  def self.empty_io
    defined?(StringIO) ? StringIO.new('') : File.open(File::NULL, 'r')
  end

  WHEN_CHUNK = %q{(?:[^:#'"\n?]|::|'[^'\n]*'|"[^"\n]*")+?}

  # Ruby 1.8:  when 0: foo     /   when CONST:       (Ruby 1.9+: "then")
  WHEN_EOL    = /^(\s*when\s+#{WHEN_CHUNK})\s*:(?!:)(?=\s*(?:#.*)?$)/
  WHEN_INLINE = /^(\s*when\s+#{WHEN_CHUNK})\s*:(?!:)\s+(?=\S)/

  # Rewrites old Ruby syntax. With the parser's error message, also fixes the exact
  # lines it complains about (the engine calls this again after each failed try).
  def self.fix_legacy_syntax(source, message = nil)
    s = source.dup
    s.force_encoding('UTF-8')
    s = s.scrub('?') unless s.valid_encoding?
    s = s.gsub(WHEN_EOL) { "#{$1} then" }
    s = s.gsub(WHEN_INLINE) { "#{$1} then " }
    s = fix_reported_lines(s, message.to_s) if message
    s
  end

  def self.fix_reported_lines(source, message)
    lines = source.split("\n", -1)
    message.scan(/:(\d+): (Invalid retry|Invalid redo|Invalid next|Invalid break|invalid multibyte escape: (\/.*\/))/) do |num, kind, regex|
      i = num.to_i - 1
      next if i < 0 || i >= lines.size
      line = lines[i]
      case kind
      when 'Invalid retry'
        # Ruby 1.8 allowed "retry" inside loops to start over; the closest
        # modern equivalent is "redo" (repeat the current step).
        lines[i] = line.sub(/\bretry\b/, 'redo')
      when 'Invalid redo'
        lines[i] = line.sub(/\bredo\b/, 'nil')
      when 'Invalid next', 'Invalid break'
        # Ruby 1.8 accepted "next"/"break" directly in a method: it left it
        lines[i] = line.sub(/\b(next|break)\b/, 'return')
      else
        # "\x80"-style bytes in a regexp: in UTF-8 they mean characters U+0080...
        if regex && line.include?(regex)
          lines[i] = line.sub(regex) { regex.gsub(/\\x([89a-fA-F][0-9a-fA-F])/) { "\\u00#{$1}" } }
        end
      end
    end
    lines.join("\n")
  end
end

class << IO
  alias_method :essentia_original_popen, :popen
  def popen(*args, &block)
    if Essentia.windows_program?(args)
      io = Essentia.empty_io
      return block ? block.call(io) : io
    end
    essentia_original_popen(*args, &block)
  end
end

module Kernel
  alias_method :essentia_original_system, :system
  def system(*args)
    return false if Essentia.windows_program?(args)
    essentia_original_system(*args)
  end

  alias_method :essentia_original_backtick, :`
  def `(cmd)
    return '' if Essentia.windows_program?([cmd])
    essentia_original_backtick(cmd)
  end
end

# ---------------------------------------------------------------------------
# Ruby 1.8 methods that modern Ruby removed (old community games still call them).

class Thread
  class << self
    # Ruby 1.8 used Thread.critical to pause other threads; not needed anymore.
    def critical; false; end unless method_defined?(:critical)
    def critical=(value); value; end unless method_defined?(:critical=)
  end
end

class String
  # Only answers when a game really calls it, so modern games are unaffected.
  # - each: Ruby 1.8 strings could be walked line by line.
  # - number methods: in Ruby 1.8, text[i] gave a number (the byte), now it gives
  #   a one-letter string. When an old game does text[i].times, text[i] & 0xFF...
  #   the one-letter string answers as that number.
  # (String#to_a is left out: Array(text) and *text would change for every game.)
  alias_method :essentia_method_missing, :method_missing
  # Old games sometimes bring their own String#getbyte/bytesize (written for
  # Ruby 1.8); unpack is used instead so they can't make this loop forever.
  def method_missing(name, *args, &block)
    if name == :each
      each_line(*args, &block)
    elsif !Thread.current[:essentia_byte_call] && Integer.method_defined?(name) &&
          (byte = essentia_single_byte)
      begin
        Thread.current[:essentia_byte_call] = true
        byte.__send__(name, *args, &block)
      ensure
        Thread.current[:essentia_byte_call] = false
      end
    else
      essentia_method_missing(name, *args, &block)
    end
  end
  private :method_missing

  def essentia_single_byte
    b = unpack('C*')
    b.length == 1 ? b[0] : nil
  end
  private :essentia_single_byte

  # 5 + text[i] (Ruby 1.8 style): the one-letter string acts as its byte.
  unless method_defined?(:coerce)
    def coerce(other)
      byte = essentia_single_byte
      if byte && other.is_a?(Numeric)
        [other, byte]
      else
        raise TypeError, "String can't be coerced into #{other.class}"
      end
    end
  end
end

class Array
  def nitems; count { |x| !x.nil? }; end unless method_defined?(:nitems)
  def choice; sample; end unless method_defined?(:choice)
  def indexes(*i); values_at(*i); end unless method_defined?(:indexes)
  alias_method :indices, :indexes unless method_defined?(:indices)
end

class Hash
  def index(value); key(value); end unless method_defined?(:index)
  def indexes(*k); values_at(*k); end unless method_defined?(:indexes)
  alias_method :indices, :indexes unless method_defined?(:indices)
end

module Kernel
  # Libraries from Ruby 1.8 that no longer exist; old scripts only "require" them.
  ESSENTIA_GONE_LIBS = %w[jcode dl dl/import Win32API win32api].freeze

  alias_method :essentia_original_require, :require
  def require(name)
    return true if ESSENTIA_GONE_LIBS.include?(name.to_s.sub(/\.(rb|so)\z/, ''))
    essentia_original_require(name)
  end
end

# Ruby 1.8: proc { ... } worked like a lambda, so "return" (or "break") inside
# it just left the proc. Old Pokémon Essentials event handlers rely on that
#   Events.onWildBattleOverride += proc { |sender, e| return if ... }
# (Pokémon Z: "unexpected return"). Modern Ruby raises an error instead; here
# the proc simply ends, as it did back then.
class Proc
  alias_method :essentia_raw_call, :call

  def call(*args, &block)
    essentia_raw_call(*args, &block)
  rescue LocalJumpError => e
    raise if lambda? || (e.reason != :return && e.reason != :break)
    e.exit_value
  end
  ruby2_keywords(:call) if respond_to?(:ruby2_keywords, true)

  alias_method :[], :call
  alias_method :===, :call
  alias_method :yield, :call
end

# ---------------------------------------------------------------------------
# Some games replace File.open with their own version (for example to read
# packed or encrypted files) that only knows how to read. Writing a small file
# then fails with "not opened for writing" (Pokémon Z: LastSave.dat in
# pbStoredLastPlayed). When a game defines its own File.open, it is wrapped:
# if, for a writing mode, the game's version gives back something that can't be
# written, Ruby's own File.open is used for that file. Games whose version writes
# fine, and every read, keep going through the game's own code. A plain method
# (not "prepend") so games that "alias" File.open keep working.
if RUBY_VERSION >= '2.0'
  module EssentiaWritableOpen
    ORIGINAL = IO.singleton_class.instance_method(:open)

    def self.writing?(mode)
      case mode
      when String then mode =~ /[wa+]/ ? true : false
      when Integer then (mode & (File::WRONLY | File::RDWR | File::APPEND)) != 0
      else false
      end
    end

    def self.call(klass, inner, args, block)
      mode = args[1]
      mode = args.last[:mode] if mode.nil? && args.last.is_a?(Hash)
      return klass.send(inner, *args, &block) unless writing?(mode)
      f = klass.send(inner, *args)
      begin
        # an empty write that still checks: IO#write('') skips the check, syswrite doesn't
        f.is_a?(IO) ? f.syswrite('') : f.write('')
      rescue IOError, NoMethodError
        f.close rescue nil
        f = ORIGINAL.bind(klass).call(*args)
      end
      return f unless block
      begin
        block.call(f)
      ensure
        f.close unless (f.closed? rescue true)
      end
    end
  end

  def File.singleton_method_added(name)
    return unless name == :open
    return if Thread.current[:essentia_wrapping_open]
    Thread.current[:essentia_wrapping_open] = true
    begin
      @essentia_open_n = (@essentia_open_n || 0) + 1
      inner = :"essentia_game_open_#{@essentia_open_n}"
      singleton_class.send(:alias_method, inner, :open)
      define_singleton_method(:open) { |*args, &block| EssentiaWritableOpen.call(self, inner, args, block) }
    ensure
      Thread.current[:essentia_wrapping_open] = false
    end
  end
end
