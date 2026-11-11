# essentia_games.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Fixes for things many RPG Maker / Pokémon Essentials games do that only work
# on a Windows PC. Loaded for every game, with both Ruby engines.
#
#  * Old Pokémon Essentials (up to v16, e.g. Pokémon Z) resizes its window with
#    Windows calls ("Sprite_Resizer") and shows its 512x384 picture inside the
#    640x480 RPG Maker XP screen. Here the screen simply takes the game's size,
#    and the window-size options of those games do nothing (the phone decides).
#  * Those games also take screenshots (for battle transitions) with a Windows
#    DLL; the engine's own screenshot is used instead.

module EssentiaGames
  @frame = 0

  class << self
    def graphics_meta
      class << Graphics; self; end
    end

    # The size the game is made for (what its own Graphics.width says)
    def game_size
      w = h = 0
      begin
        w = Graphics.send(:class_variable_get, :@@width).to_i
        h = Graphics.send(:class_variable_get, :@@height).to_i
      rescue Exception
      end
      if w <= 0 || h <= 0
        w = Object.const_defined?(:DEFAULTSCREENWIDTH) ? Object.const_get(:DEFAULTSCREENWIDTH).to_i : 0
        h = Object.const_defined?(:DEFAULTSCREENHEIGHT) ? Object.const_get(:DEFAULTSCREENHEIGHT).to_i : 0
      end
      return nil if w <= 0 || h <= 0
      [w, h]
    rescue Exception
      nil
    end

    def old_resizer?
      Object.private_method_defined?(:pbSetResizeFactor2) || Object.method_defined?(:pbSetResizeFactor2)
    end

    # Screen at the game's own size, no zoom, no offsets
    def normal_size
      return if @resizing
      @resizing = true
      $ResizeFactor = 1.0
      $ResizeFactorMul = 100
      $ResizeOffsetX = 0
      $ResizeOffsetY = 0
      $ResizeFactorSet = true
      size = game_size
      set_size(size[0], size[1]) if size
    rescue Exception
    ensure
      @resizing = false
    end

    def set_size(w, h)
      if defined?(EssentiaWide) && EssentiaWide.respond_to?(:base=)
        return if EssentiaWide.base == [w, h] && Graphics.essentia_raw_width == EssentiaWide.wide_w
        EssentiaWide.base = [w, h]
        Graphics.essentia_raw_resize_screen(*EssentiaWide.wide_size)
      else
        return if Graphics.essentia_native_width == w && Graphics.essentia_native_height == h
        Graphics.essentia_native_resize_screen(w, h)
      end
    rescue Exception
    end

    # The old window-size functions are replaced (once the game has defined them)
    def tame_resizer
      return if @taming || !old_resizer?
      current = Object.instance_method(:pbSetResizeFactor2) rescue nil
      return if current && @mine && current == @mine
      @taming = true
      Object.class_eval do
        def pbSetResizeFactor(*args); EssentiaGames.normal_size; end
        def pbSetResizeFactor2(*args); EssentiaGames.normal_size; end
        def pbConfigureFullScreen(*args); EssentiaGames.normal_size; end
        def pbConfigureWindowedScreen(*args); EssentiaGames.normal_size; end
        private :pbSetResizeFactor, :pbSetResizeFactor2, :pbConfigureFullScreen, :pbConfigureWindowedScreen
      end
      @mine = Object.instance_method(:pbSetResizeFactor2)
      @taming = false
      normal_size
    rescue Exception
    ensure
      @taming = false
    end

    # The engine's screenshot stays (the game may replace it with a DLL one)
    def keep_snapshot
      meta = graphics_meta
      return unless meta.method_defined?(:essentia_native_snap)
      current = meta.instance_method(:snap_to_bitmap) rescue nil
      return if current && @snap && current == @snap
      meta.send(:define_method, :snap_to_bitmap) { |*a| essentia_native_snap(*a) }
      @snap = meta.instance_method(:snap_to_bitmap)
    rescue Exception
    end

    # Some games synchronously check the internet before showing their load menu.
    # Leave automatic version checks to Essentia; manual online features stay intact.
    def tame_version_check
      return if @version_wrapping
      current = Object.instance_method(:pbCheckVersionOnLoad) rescue nil
      return unless current
      return if @version_check && current == @version_check
      @version_wrapping = true
      Object.send(:define_method, :pbCheckVersionOnLoad) do |*args|
        ack = ENV['ESSENTIA_ACK_FILE'].to_s
        File.open(ack, 'ab') { |f| f.write("updater 1\n") } unless ack.empty?
        nil
      end
      Object.send(:private, :pbCheckVersionOnLoad)
      @version_check = Object.instance_method(:pbCheckVersionOnLoad)
    rescue Exception
    ensure
      @version_wrapping = false
    end

    def tick
      @frame += 1
      return unless @frame <= 2 || (@frame % 60).zero?
      tame_resizer
      keep_snapshot
      tame_version_check
    end
  end
end

module Graphics
  class << self
    alias_method :essentia_native_width, :width
    alias_method :essentia_native_height, :height
    alias_method :essentia_native_resize_screen, :resize_screen if method_defined?(:resize_screen)
    alias_method :essentia_native_snap, :snap_to_bitmap if method_defined?(:snap_to_bitmap)

    alias_method :essentia_games_original_update, :update
    def update(*args)
      essentia_games_original_update(*args)
      EssentiaGames.tick
    end
  end
end

# The game's scripts define the old resizer while loading, before the first
# frame: replace it right when it appears.
class << Object
  if method_defined?(:method_added) || private_method_defined?(:method_added)
    alias_method :essentia_games_method_added, :method_added
  end
  def method_added(name)
    essentia_games_method_added(name) if respond_to?(:essentia_games_method_added, true)
    EssentiaGames.tame_resizer if name.to_s == 'pbConfigureWindowedScreen'
    EssentiaGames.tame_version_check if name.to_s == 'pbCheckVersionOnLoad'
  end
end

# RPG Maker XP's Ruby (1.8.1) let a script define a class again with another
# parent class: the new class simply replaced the old one. Newer Ruby stops
# with "superclass mismatch" (Pokémon Cristal: BerryPlantSprite, GS Vitro:
# DarknessSprite). Every "class X < Y" gets a tiny check in front, on the same
# line, that does what the old Ruby did.
module Essentia
  RECLASS = /^([ \t]*)class[ \t]+([A-Z]\w*)[ \t]*<[ \t]*([A-Z][\w:]*)(?=[ \t]*(?:#|;|\r?$))/n
  MISMATCH = /superclass mismatch for class ([A-Z]\w*)/

  def self.prepare_script(src)
    return src unless src.is_a?(String) && src.index('class ')
    # bytes, not characters: a script may have text that isn't valid UTF-8
    # (old comments in Latin-1), and modern Ruby refuses to search those
    enc = src.respond_to?(:encoding) ? src.encoding : nil
    bin = enc ? src.dup.force_encoding('BINARY') : src
    out = bin.gsub(RECLASS) { "#{$1}Essentia.reclass(self, :#{$2}, #{$3}); class #{$2} < #{$3}" }
    enc ? out.force_encoding(enc) : out
  rescue Exception
    src
  end

  # scope: where the class is being defined (a module or class body, or the
  # top level)
  def self.reclass(scope, name, parent = nil)
    scope, name, parent = Object, scope, name if parent.nil?
    owner = scope.is_a?(Module) ? scope : Object
    here = RUBY_VERSION < '1.9' ? owner.const_defined?(name.to_s) : owner.const_defined?(name, false)
    return unless here
    k = owner.const_get(name)
    return unless k.is_a?(Class) && parent.is_a?(Class)
    owner.send(:remove_const, name) if k.superclass != parent
  rescue Exception
  end

  # Called by the engine when a script section stops with an error: returns
  # the part of the script still to run (same line numbers) when the error is
  # one the old Ruby didn't have, or nil. A class defined again with another
  # parent ("superclass mismatch") that the check above couldn't see (written
  # inside other code, or with an unusual layout) ends up here.
  def self.retry_after_error(src, exc)
    return nil unless src.is_a?(String) && exc.is_a?(TypeError)
    return nil unless exc.message.to_s =~ MISMATCH
    name = $1
    where = (exc.backtrace || []).find { |l| l.to_s =~ /:\d+/ }.to_s
    return nil unless where =~ /:(\d+)(?::in |\z)/
    line = $1.to_i
    return nil if line < 1
    enc = src.respond_to?(:encoding) ? src.encoding : nil
    bin = enc ? src.dup.force_encoding('BINARY') : src
    lines = bin.split(/\n/, -1)
    return nil if line > lines.length
    # the error may point a line or two below "class X <" (parent on its own line)
    start = line
    (0..3).each do |back|
      l = line - back
      break if l < 1
      if lines[l - 1] =~ /^[ \t]*class[ \t]+#{name}\b/n
        start = l
        break
      end
    end
    line = start
    rest = ("\n" * (line - 1)) + lines[line - 1..-1].join("\n")
    rest.force_encoding(enc) if enc
    # only when what's left is a complete piece of code (the class was at the
    # top level of the script)
    ok = catch(:essentia_parsed) do
      begin
        eval("throw :essentia_parsed, true\n" + rest, TOPLEVEL_BINDING)
      rescue SyntaxError
        false
      end
    end
    return nil unless ok == true
    Object.send(:remove_const, name) if Object.const_defined?(name)
    rest
  rescue Exception
    nil
  end
end
