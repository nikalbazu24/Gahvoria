require 'tmpdir'
require 'fileutils'

module Input
  A = 11

  class << self
    attr_accessor :held, :triggered, :text_input

    def press?(button); button == A; end
    def trigger?(button); button == A; end
    def time?(button); button == A ? 250_000 : 0; end
    def pressex?(key); held.include?(key); end
    def triggerex?(key); triggered.include?(key); end
  end
end

module Graphics
end

module Audio
  def self.se_play(*args); args; end
end

def check(condition, message)
  raise message unless condition
end

preload = File.expand_path('../../engine/src/main/assets/essentia_preload/essentia_compat.rb', __dir__)
Dir.mktmpdir('gahvoria-reborn-engine') do |game|
  other_game = ARGV.include?('--other-game')
  FileUtils.mkdir_p(File.join(game, 'Scripts/Reborn'))
  File.write(File.join(game, 'Scripts/Reborn/Bootstrap.rb'), '# layout fixture')
  unless other_game
    FileUtils.mkdir_p(File.join(game, 'Scripts/Online'))
    File.write(File.join(game, 'Scripts/Online/DiscordRichPresence.rb'), '# layout fixture')
  end
  ENV['GAHVORIA_GAME_ROOT'] = game
  load preload

  if other_game
    check(!Input.const_defined?(:E, false), 'Unrelated game got a Reborn binding')
    check(Input.time?(Input::A) == 250_000, 'Unrelated game changed input units')
    check(!Graphics.respond_to?(:start_playing), 'Unrelated game got a Reborn clock')
    check(!Audio.respond_to?(:se_play_position), 'Unrelated game got a Reborn sound adapter')
    next
  end

  def shortcuts
    [
      !Input.text_input && Input.trigger?(Input::E),
      !Input.text_input && Input.trigger?(Input::F),
      Input.press?(Input::D)
    ]
  end

  Input.held = []
  Input.triggered = []
  check(shortcuts == [false, false, false], 'Idle input must not raise or trigger a shortcut')
  Input.held = [:RSHIFT]
  [:TAB, :LALT, :M].each do |key|
    Input.triggered = [key, :F7]
    check(shortcuts == [true, true, true], "Reborn shortcuts failed for #{key}")
  end
  Input.text_input = true
  check(shortcuts == [false, false, true], 'Turbo and mute must stay off while entering text')
  check(Input.trigger?(Input::A) && Input.press?(Input::A), 'Normal buttons must reach the engine')
  check(Input.time?(Input::A) == 0.25, 'Hold duration must be in seconds')

  now = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  check(Graphics.start_playing <= now && Graphics.time_passed == 0, 'Invalid initial playtime')
  Graphics.time_passed = 40 * 60
  Graphics.start_playing = now - 3
  check(Graphics.time_passed / 40 + (now - Graphics.start_playing).to_i == 63, 'Loaded playtime lost')
  check(Audio.se_play_position('cursor', 80, 90, -1, 0, 0) == ['cursor', 80, 90], 'Sound effect lost')
end
puts 'Reborn engine compatibility check passed'
