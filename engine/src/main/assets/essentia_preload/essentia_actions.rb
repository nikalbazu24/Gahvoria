# essentia_actions.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Quick actions from Essentia (second-screen panel, controller shortcuts):
# turbo speed, screenshots, and for Pokémon Essentials games opening the team,
# the bag or the Pokédex, quick save and a few cheats.
#
# Essentia writes commands, one per line, to ESSENTIA_CMD_FILE; answers go to
# ESSENTIA_ACK_FILE. Menus only open while walking on the map with nothing else
# going on (no message, no event, not moving), exactly like the game's own menu.

module EssentiaActions
  CMD = ENV['ESSENTIA_CMD_FILE'].to_s
  ACK = ENV['ESSENTIA_ACK_FILE'].to_s
  SHOTS = ENV['ESSENTIA_SHOTS_DIR'].to_s
  MAP_COMMANDS = %w[party bag dex save heal money candy testpokemon].freeze

  @frame = 0
  @queue = []
  @speed = 1
  @base_rate = nil
  @through = false
  @no_encounters = false
  @rt0 = 0.0
  @vt0 = 0.0
  @hooked = {}

  class << self
    attr_reader :speed, :no_encounters

    # ------------------------------------------------------------ plumbing

    def tick
      @frame += 1
      install_hooks if @frame == 2 || (@frame % 120).zero?
      poll if (@frame % 6).zero?
    end

    def poll
      return if CMD.empty? || !File.exist?(CMD)
      work = CMD + '.work'
      File.rename(CMD, work)
      lines = File.read(work).split("\n")
      File.delete(work) rescue nil
      lines.each { |l| handle(l.strip) }
    rescue Exception
    end

    def ack(text)
      return if ACK.empty?
      File.open(ACK, 'ab') { |f| f.write(text.to_s.gsub(/[\r\n]/, ' ') + "\n") }
    rescue Exception
    end

    def handle(line)
      cmd, arg = line.split(' ', 2)
      if %w[through noenc heal money candy testpokemon].include?(cmd) && !backup_before_help
        return ack("#{cmd} fail")
      end
      case cmd
      when 'speed' then set_speed(arg.to_i)
      when 'shot' then screenshot
      when 'through' then toggle_through
      when 'noenc'
        @no_encounters = !@no_encounters
        ack("noenc #{@no_encounters ? 1 : 0}")
      when *MAP_COMMANDS
        @queue.delete(cmd)
        @queue << cmd
        ack("wait #{cmd}") unless map_safe?
      end
    end

    # Called from Scene_Map#update, before the game's own update.
    def run_queued
      return if @queue.empty? || !map_safe?
      cmd = @queue.shift
      ok = begin
        __send__(:"do_#{cmd}")
      rescue Exception => e
        STDERR.puts("[Essentia] #{cmd}: #{e.class}: #{e.message}") rescue nil
        :fail
      end
      result = ok == true ? 'ok' : (ok || 'fail').to_s
      # Opening a menu needs no answer unless it couldn't open
      ack("#{cmd} #{result}") unless result == 'ok' && %w[party bag dex].include?(cmd)
    end

    def map_safe?
      return false if companion_battle?
      t = $game_temp
      if t
        return false if t.respond_to?(:in_menu) && t.in_menu
        return false if t.respond_to?(:in_battle) && t.in_battle
        return false if t.respond_to?(:message_window_showing) && t.message_window_showing
        return false if t.respond_to?(:player_transferring) && t.player_transferring
      end
      return false if $game_player && $game_player.respond_to?(:moving?) && $game_player.moving?
      return false if respond_to?(:pbMapInterpreterRunning?, true) && pbMapInterpreterRunning?
      if $game_system && $game_system.respond_to?(:map_interpreter)
        return false if $game_system.map_interpreter.running?
      end
      true
    rescue Exception
      false
    end

    def trainer
      $player || $Trainer
    end

    # Read-only capability snapshot. Never open a menu just to probe support.
    def companion_battle?
      [$game_temp, $PokemonTemp].compact.any? do |temp|
        [:in_battle, :inBattle].any? { |flag| temp.respond_to?(flag) && temp.__send__(flag) }
      end
    rescue Exception
      false
    end

    def companion_context
      return 'battle' if companion_battle?
      return 'busy' unless map_safe?
      return 'map' if defined?(Scene_Map) && $scene.is_a?(Scene_Map)
      'busy'
    rescue Exception
      'unknown'
    end

    def companion_actions
      available = %w[shot speed]
      return available unless @hooked[:map]
      t = trainer
      if t && t.respond_to?(:party) &&
          ((defined?(PokemonParty_Scene) && defined?(PokemonPartyScreen)) ||
           (defined?(PokemonScreen_Scene) && defined?(PokemonScreen)))
        available << 'party'
      end
      available << 'bag' if ($bag || $PokemonBag) && defined?(PokemonBag_Scene) && defined?(PokemonBagScreen)
      dex_unlocked = t && (!t.respond_to?(:has_pokedex) || t.has_pokedex)
      if dex_unlocked && ((defined?(PokemonPokedex_Scene) && defined?(PokemonPokedexScreen)) ||
                         (defined?(PokemonPokedexScene) && defined?(PokemonPokedex)))
        available << 'dex'
      end
      save_disabled = $game_system && $game_system.respond_to?(:save_disabled) && $game_system.save_disabled
      available << 'save' if !save_disabled && ((defined?(Game) && Game.respond_to?(:save)) || respond_to?(:pbSave, true))
      available
    rescue Exception
      %w[shot speed]
    end

    # ------------------------------------------------------------ hooks

    def install_hooks
      if defined?(Scene_Map) && !@hooked[:map]
        Scene_Map.class_eval do
          alias_method :essentia_actions_update, :update
          def update(*args)
            EssentiaActions.run_queued
            essentia_actions_update(*args)
          end
        end
        @hooked[:map] = true
      end
      if ENV['ESSENTIA_DIAGONAL'] == '1' && defined?(Game_Player) && !@hooked[:diag]
        Game_Player.class_eval do
          alias_method :essentia_diag_update, :update
          def update(*args)
            EssentiaActions.diagonal_step(self)
            essentia_diag_update(*args)
          end
        end
        @hooked[:diag] = true
      end
      if defined?(PokemonEncounters) && !@hooked[:enc]
        [:encounter_triggered?, :pbCanEncounter?, :pbEncounterTriggered?].each do |m|
          next unless PokemonEncounters.method_defined?(m)
          raw = :"essentia_raw_#{m.to_s.delete('?')}"
          PokemonEncounters.send(:alias_method, raw, m)
          PokemonEncounters.send(:define_method, m) do |*args, &block|
            return false if EssentiaActions.no_encounters
            __send__(raw, *args, &block)
          end
        end
        @hooked[:enc] = true
      end
    rescue Exception
    end

    # ------------------------------------------------------------ diagonal

    # Two directions held: step diagonally (if the way is free; otherwise the
    # game moves straight as usual).
    def diagonal_step(player)
      return if player.moving?
      d = Input.dir8
      return unless d == 1 || d == 3 || d == 7 || d == 9
      return if player.respond_to?(:move_route_forcing) && player.move_route_forcing
      return unless map_safe?
      horz = (d == 1 || d == 7) ? 4 : 6
      vert = (d == 1 || d == 3) ? 2 : 8
      if player.respond_to?(:move_diagonal)
        player.move_diagonal(horz, vert)
      else
        case d
        when 1 then player.move_lower_left
        when 3 then player.move_lower_right
        when 7 then player.move_upper_left
        when 9 then player.move_upper_right
        end
      end
    rescue Exception
    end

    # ------------------------------------------------------------ turbo

    def set_speed(n)
      n = 1 if n < 1
      n = 5 if n > 5
      now = System.essentia_raw_uptime rescue nil
      if now
        @vt0 = virtual_time(now)
        @rt0 = now
      end
      @base_rate ||= Graphics.frame_rate
      @speed = n
      Graphics.essentia_raw_frame_rate = @base_rate * n
      ack("speed #{n}")
    rescue Exception
    end

    def base_rate=(v)
      @base_rate = v
    end

    def base_rate
      @base_rate
    end

    # Games that measure real time (Essentials v21) also run faster in turbo.
    def virtual_time(real)
      @vt0 + (real - @rt0) * @speed
    end

    # ------------------------------------------------------------ actions

    def screenshot
      return ack('shot fail') if SHOTS.empty?
      name = File.join(SHOTS, "Essentia_#{Time.now.strftime('%Y%m%d_%H%M%S')}.png")
      Graphics.screenshot(name)
      ack("shot #{name}")
    rescue Exception
      ack('shot fail')
    end

    def toggle_through
      return ack('through fail') unless $game_player
      @through = !@through
      if $game_player.respond_to?(:through=)
        $game_player.through = @through
      else
        $game_player.instance_variable_set(:@through, @through)
      end
      ack("through #{@through ? 1 : 0}")
    end

    def fade(&block)
      if respond_to?(:pbFadeOutIn, true)
        pbFadeOutIn(99999, &block)
      else
        block.call
      end
    end

    def do_party
      t = trainer
      return :unsupported unless t && t.respond_to?(:party)
      ret = nil
      if defined?(PokemonParty_Scene) && defined?(PokemonPartyScreen)
        fade { ret = PokemonPartyScreen.new(PokemonParty_Scene.new, t.party).pbPokemonScreen }
      elsif defined?(PokemonScreen_Scene) && defined?(PokemonScreen)
        fade { ret = PokemonScreen.new(PokemonScreen_Scene.new, t.party).pbPokemonScreen }
      else
        return :unsupported
      end
      # A field move chosen from the team (Cut, Surf...)
      if ret.is_a?(Array) && ret[0] && respond_to?(:pbUseHiddenMove, true)
        pbUseHiddenMove(ret[0], ret[1])
      end
      true
    end

    def do_bag
      bag = $bag || $PokemonBag
      return :unsupported unless bag && defined?(PokemonBag_Scene) && defined?(PokemonBagScreen)
      item = nil
      fade { item = PokemonBagScreen.new(PokemonBag_Scene.new, bag).pbStartScreen }
      if item && item != 0 && respond_to?(:pbUseKeyItemInField, true)
        pbUseKeyItemInField(item)
      end
      true
    end

    def do_dex
      t = trainer
      return :unsupported unless t
      has = if t.respond_to?(:has_pokedex) then t.has_pokedex
            elsif t.respond_to?(:pokedex) then t.pokedex
            else true
            end
      return :locked unless has
      dexes = (t.pokedex.accessible_dexes rescue nil) || ($PokemonGlobal.pokedexViable rescue nil)
      if dexes.is_a?(Array) && dexes.length > 1 && defined?(PokemonPokedexMenu_Scene) && defined?(PokemonPokedexMenuScreen)
        fade { PokemonPokedexMenuScreen.new(PokemonPokedexMenu_Scene.new).pbStartScreen }
        return true
      end
      if dexes.is_a?(Array) && dexes.length == 1 && $PokemonGlobal.respond_to?(:pokedexDex=)
        $PokemonGlobal.pokedexDex = dexes[0]
      end
      if defined?(PokemonPokedex_Scene) && defined?(PokemonPokedexScreen)
        fade { PokemonPokedexScreen.new(PokemonPokedex_Scene.new).pbStartScreen }
      elsif defined?(PokemonPokedexScene) && defined?(PokemonPokedex)
        fade { PokemonPokedex.new(PokemonPokedexScene.new).pbStartScreen }
      else
        return :unsupported
      end
      true
    end

    def do_save
      if $game_system && $game_system.respond_to?(:save_disabled) && $game_system.save_disabled
        return :disabled
      end
      if defined?(Game) && Game.respond_to?(:save)
        ok = Game.save
      elsif respond_to?(:pbSave, true)
        ok = pbSave
      else
        return :unsupported
      end
      ok == false ? :fail : true
    end

    def do_heal
      t = trainer
      return :unsupported unless t && t.respond_to?(:party)
      t.party.each { |pk| pk.heal if pk && pk.respond_to?(:heal) }
      true
    end

    # A new, complete directory is exposed to restoration only after every copy.
    def backup_before_help
      return true if @help_backup_done
      source = ENV['ESSENTIA_DATA_DIR'].to_s
      dest = ENV['ESSENTIA_HELP_BACKUP_DIR'].to_s
      return false if source.empty? || dest.empty? || !File.directory?(source)
      stage = dest + '.preparando'
      Dir.mkdir(stage) unless File.directory?(stage)
      total = 0
      Dir.entries(source).each do |name|
        low = name.downcase
        next unless low =~ /\.(rxdata|rvdata|rvdata2|sav)$/
        next if low =~ /^(scripts|map|tilesets|system\.|actors|classes|skills|items|weapons|armors|enemies|troops|states|animations|commonevents|pluginscripts)/
        file = File.join(source, name)
        next unless File.file?(file)
        return false if File.symlink?(file)
        total += File.size(file)
        return false if total > 20 * 1024 * 1024
        File.open(file, 'rb') do |input|
          File.open(File.join(stage, name), 'wb') do |output|
            while (bytes = input.read(65536))
              output.write(bytes)
            end
          end
        end
      end
      File.rename(stage, dest)
      @help_backup_done = true
      true
    rescue Exception
      false
    end

    def do_candy
      bag = $bag || $PokemonBag
      return :unsupported unless bag
      if bag.respond_to?(:add) && defined?(GameData::Item)
        item = GameData::Item.try_get(:RARECANDY) rescue nil
        return :unsupported unless item
        return bag.add(:RARECANDY, 5) ? true : :fail
      elsif bag.respond_to?(:pbStoreItem) && defined?(PBItems::RARECANDY)
        return bag.pbStoreItem(PBItems::RARECANDY, 5) ? true : :fail
      end
      :unsupported
    end

    def do_testpokemon
      return :unsupported unless trainer && respond_to?(:pbAddPokemon, true)
      species = nil
      if defined?(GameData::Species)
        species = :PIKACHU if (GameData::Species.try_get(:PIKACHU) rescue nil)
      elsif defined?(PBSpecies::PIKACHU)
        species = PBSpecies::PIKACHU
      end
      return :unsupported unless species
      pbAddPokemon(species, 5) ? true : :fail
    end

    def do_money
      t = trainer
      return :unsupported unless t && t.respond_to?(:money=)
      t.money += 10_000
      true
    end
  end
end

module Graphics
  class << self
    alias_method :essentia_raw_frame_rate, :frame_rate
    alias_method :essentia_raw_frame_rate=, :frame_rate=

    # The game sees its normal frame rate; turbo multiplies the real one.
    def frame_rate
      EssentiaActions.base_rate || essentia_raw_frame_rate
    end

    def frame_rate=(value)
      EssentiaActions.base_rate = value
      self.essentia_raw_frame_rate = value * EssentiaActions.speed
    end

    alias_method :essentia_actions_original_update, :update
    def update(*args)
      essentia_actions_original_update(*args)
      EssentiaActions.tick
    end
  end
end

module System
  class << self
    if method_defined?(:uptime)
      alias_method :essentia_raw_uptime, :uptime
      def uptime
        EssentiaActions.virtual_time(essentia_raw_uptime)
      end
    end
  end
end
