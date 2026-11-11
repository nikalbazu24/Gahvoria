# essentia_quality.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Small comforts chosen in Essentia's settings (both Ruby engines):
#  * fast text: Pokémon Essentials' text speed at its fastest
#  * skip intros: no splash pictures before the title screen
#  * old TV lines: thin dark lines over the picture

module EssentiaQuality
  FAST = ENV['ESSENTIA_FAST_TEXT'] == '1'
  SKIP = ENV['ESSENTIA_SKIP_INTRO'] == '1'
  CRT = ENV['ESSENTIA_FILTER'] == 'crt'
  @frame = 0

  class << self
    # ------------------------------------------------------------ fast text

    def fastest_speed
      return @fastest if @fastest
      @fastest = 2
      if defined?(MessageConfig) && MessageConfig.respond_to?(:pbSettingToTextSpeed)
        # newer Essentials: 0..3, where 3 shows the text at once
        begin
          @fastest = 3 if MessageConfig.pbSettingToTextSpeed(3)
        rescue Exception
        end
      end
      @fastest
    end

    def fast_text
      return unless defined?($PokemonSystem) && $PokemonSystem && $PokemonSystem.respond_to?(:textspeed=)
      sp = fastest_speed
      $PokemonSystem.textspeed = sp if $PokemonSystem.textspeed != sp
    rescue Exception
    end

    # ------------------------------------------------------------ skip intros

    def skip_intro
      if defined?(IntroEventScene) && IntroEventScene.const_defined?(:SPLASH_IMAGES)
        v = IntroEventScene.const_get(:SPLASH_IMAGES)
        if v.is_a?(Array) && !v.empty?
          IntroEventScene.send(:remove_const, :SPLASH_IMAGES)
          IntroEventScene.const_set(:SPLASH_IMAGES, [])
        end
      end
    rescue Exception
    end

    # old Essentials: Scene_Intro.new(pictures, title) — no pictures
    def wrap_call_title
      return if @title_wrapped
      return unless Object.private_method_defined?(:pbCallTitle) || Object.method_defined?(:pbCallTitle)
      @title_wrapped = true
      Object.send(:alias_method, :essentia_q_raw_pbCallTitle, :pbCallTitle)
      Object.send(:define_method, :pbCallTitle) do |*args|
        EssentiaQuality.skip_intro
        scene = essentia_q_raw_pbCallTitle(*args)
        if scene.instance_variable_defined?(:@pics) && scene.instance_variable_get(:@pics).is_a?(Array)
          scene.instance_variable_set(:@pics, [])
        end
        scene
      end
      Object.send(:private, :pbCallTitle)
    rescue Exception
    end

    # ------------------------------------------------------------ TV lines

    def screen_size
      w = Graphics.respond_to?(:essentia_raw_width) ? Graphics.essentia_raw_width : Graphics.width
      h = Graphics.respond_to?(:essentia_raw_height) ? Graphics.essentia_raw_height : Graphics.height
      [w, h]
    end

    def crt
      size = screen_size
      return if @crt && !(@crt.disposed? rescue true) && @crt_size == size
      @crt.bitmap.dispose rescue nil
      @crt.dispose rescue nil
      @crt_vp.dispose rescue nil
      w, h = size
      if defined?(EssentiaWide)
        EssentiaWide.instance_variable_set(:@making_backdrop, true)
      end
      begin
        @crt_vp = Viewport.new(0, 0, w, h)
      ensure
        EssentiaWide.instance_variable_set(:@making_backdrop, false) if defined?(EssentiaWide)
      end
      @crt_vp.z = 0x3FFFFFFE
      bmp = Bitmap.new(w, h)
      line = Color.new(0, 0, 0, 70)
      y = 1
      while y < h
        bmp.fill_rect(0, y, w, 1, line)
        y += 2
      end
      @crt = Sprite.new(@crt_vp)
      @crt.bitmap = bmp
      @crt_size = size
    rescue Exception
    end

    def tick
      @frame += 1
      wrap_call_title if SKIP && @frame <= 1
      fast_text if FAST && (@frame % 60) == 1
      crt if CRT && (@frame == 2 || (@frame % 120).zero?)
    end
  end
end

# The title is decided before the first frame: hook it as soon as the game defines it
if EssentiaQuality::SKIP
  class << Object
    if method_defined?(:method_added) || private_method_defined?(:method_added)
      alias_method :essentia_q_method_added, :method_added
    end
    def method_added(name)
      essentia_q_method_added(name) if respond_to?(:essentia_q_method_added, true)
      EssentiaQuality.wrap_call_title if name.to_s == 'pbCallTitle'
    end
  end
end

module Graphics
  class << self
    alias_method :essentia_q_original_update, :update
    def update(*args)
      essentia_q_original_update(*args)
      EssentiaQuality.tick
    end
  end
end
