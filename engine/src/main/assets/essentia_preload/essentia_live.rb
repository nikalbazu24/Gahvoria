# essentia_live.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Live map for the second screen: about twice a second, writes where the player
# is (map, position and, for Pokémon Essentials games, the spot on the town map)
# to a small file that Essentia's game panel reads. It never changes the game.

module Essentia
  LIVE_FILE = ENV['ESSENTIA_LIVE_FILE']
  @live_frame = 0
  @live_last = nil
  @live_time = 0.0
  @progress_last = nil
  PROGRESS_FILE = ENV['ESSENTIA_PROGRESS_FILE'].to_s

  def self.live_update
    return if LIVE_FILE.to_s.empty? && PROGRESS_FILE.empty?
    @live_frame += 1
    return unless (@live_frame % 15).zero?
    now = Time.now.to_f
    return if now - @live_time < 0.5
    @live_time = now
    return unless defined?($game_map) && $game_map && defined?($game_player) && $game_player

    map_id = $game_map.map_id
    name = live_map_name(map_id)
    region, mx, my = live_town_map_spot(map_id)
    image = live_town_map_image(region)
    progress = live_progress
    if !PROGRESS_FILE.empty? && progress != @progress_last
      tmp_progress = PROGRESS_FILE + '.tmp'
      File.open(tmp_progress, 'wb') { |f| f.write(progress.map { |v| v.to_s.gsub(/[\t\r\n]/, ' ') }.join("\t")) }
      File.rename(tmp_progress, PROGRESS_FILE)
      @progress_last = progress
    end
    return if LIVE_FILE.to_s.empty?
    fields = [map_id, $game_player.x, $game_player.y, $game_map.width, $game_map.height,
              region, mx, my, image, name] + progress
    if defined?(EssentiaActions) && EssentiaActions.respond_to?(:companion_actions)
      fields += [EssentiaActions.companion_actions.join(','), EssentiaActions.companion_context]
    else
      fields += ['', 'unknown']
    end
    line = fields.map { |v| v.to_s.gsub(/[\t\r\n]/, ' ') }.join("\t")
    return if line == @live_last
    @live_last = line
    tmp = LIVE_FILE + '.tmp'
    File.open(tmp, 'wb') { |f| f.write(line) }
    File.rename(tmp, LIVE_FILE)
  rescue Exception
  end

  # Optional reads only: never load, deserialize or modify a saved game.
  def self.live_progress
    trainer = (defined?($player) && $player) || (defined?($Trainer) && $Trainer)
    return ['', '', '', ''] unless trainer
    party = (trainer.party rescue []) || []
    team = party.compact[0, 6].map do |pk|
      name = (pk.name rescue '?').to_s
      level = (pk.level rescue '?')
      hp = (pk.hp rescue '?')
      total = (pk.totalhp rescue '?')
      "#{name} Nv.#{level} #{hp}/#{total} PS"
    end.join(' · ')
    badges = (trainer.badge_count rescue nil)
    if badges.nil?
      raw = (trainer.badges rescue nil)
      badges = raw.select { |v| v == true }.length if raw.is_a?(Array)
    end
    dex = (trainer.pokedex rescue nil)
    owned = (dex.owned_count rescue nil)
    seen = (dex.seen_count rescue nil)
    if owned.nil?
      raw = (trainer.owned rescue nil)
      owned = raw.select { |v| v == true }.length if raw.is_a?(Array)
    end
    if seen.nil?
      raw = (trainer.seen rescue nil)
      seen = raw.select { |v| v == true }.length if raw.is_a?(Array)
    end
    [team, badges, owned, seen].map { |v| v.nil? ? '' : v }
  rescue Exception
    ['', '', '', '']
  end

  def self.live_map_name(map_id)
    (pbGetMapNameFromId(map_id) rescue nil) || ($game_map.name rescue nil) || ''
  rescue Exception
    ''
  end

  # [region, square x, square y] on the town map, adjusted for maps that span
  # several squares (Pokémon Essentials v16 and newer).
  def self.live_town_map_spot(map_id)
    pos = nil
    size = nil
    if defined?(GameData::MapMetadata)
      md = GameData::MapMetadata.try_get(map_id) rescue nil
      if md
        pos = md.town_map_position rescue nil
        size = md.town_map_size rescue nil
      end
    end
    if pos.nil? && defined?(pbGetMetadata) && defined?(MetadataMapPosition)
      pos = pbGetMetadata(map_id, MetadataMapPosition) rescue nil
      size = pbGetMetadata(map_id, MetadataMapSize) rescue nil if defined?(MetadataMapSize)
    end
    return [nil, nil, nil] unless pos
    region, x, y = pos[0], pos[1].to_i, pos[2].to_i
    if size && size[0].to_i > 0
      sw = size[0].to_i
      sh = (size[1].to_s.length.to_f / sw).ceil
      sh = 1 if sh < 1
      x += ($game_player.x * sw / [$game_map.width, 1].max)
      y += ($game_player.y * sh / [$game_map.height, 1].max)
    end
    [region, x, y]
  rescue Exception
    [nil, nil, nil]
  end

  def self.live_town_map_image(region)
    return nil if region.nil?
    if defined?(GameData::TownMap)
      tm = GameData::TownMap.try_get(region) rescue nil
      return "Graphics/UI/Town Map/#{tm.filename}" if tm && tm.respond_to?(:filename)
    end
    if defined?(pbLoadTownMapData)
      data = pbLoadTownMapData rescue nil
      return "Graphics/Pictures/#{data[region][1]}" if data && data[region]
    end
    "Graphics/Pictures/mapRegion#{region}"
  rescue Exception
    nil
  end
end

module Graphics
  class << self
    alias_method :essentia_live_original_update, :update
    def update(*args)
      essentia_live_original_update(*args)
      Essentia.live_update
    end
  end
end
