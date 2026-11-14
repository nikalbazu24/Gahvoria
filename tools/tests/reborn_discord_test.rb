require 'tmpdir'
require 'fileutils'

preload = File.expand_path('../../engine/src/main/assets/essentia_preload/essentia_compat.rb', __dir__)
Dir.mktmpdir('gahvoria-reborn-discord') do |game|
  %w[Reborn/Bootstrap.rb Online/DiscordRichPresence.rb].each do |relative|
    path = File.join(game, 'Scripts', relative)
    FileUtils.mkdir_p(File.dirname(path))
    File.write(path, '# layout fixture')
  end
  gems = File.join(game, 'gems')
  FileUtils.mkdir_p(gems)
  File.write(File.join(gems, 'discord.so'), 'desktop binary placeholder')
  $LOAD_PATH.unshift(gems)
  ENV.delete('GAHVORIA_RUBY_ARCHDIR')
  ENV['GAHVORIA_GAME_ROOT'] = game
  load preload

  raise 'First require must provision the optional API' unless require('discord')
  raise 'Repeat require must not load it again' if require(File.join(gems, 'discord.so'))
  raise 'Discord must remain disconnected' if Discord.connect('test') || Discord.connected?
  Discord.update
  Discord.update_activity({:details => 'test'})
  Discord.disconnect

  %w[other_extension other/discord.so].each do |feature|
    begin
      require feature
    rescue LoadError
      next
    end
    raise 'An unrelated library was swallowed'
  end
end
puts 'Reborn Discord compatibility check passed'
