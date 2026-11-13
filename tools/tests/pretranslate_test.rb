# encoding: UTF-8
require 'tmpdir'
require 'fileutils'

root = File.expand_path('../../engine/src/main/assets/essentia_preload', File.dirname(__FILE__))
ENV.delete('ESSENTIA_TR_ROOT')
ENV.delete('ESSENTIA_TR_PREPARE')
require File.join(root, 'essentia_translate')
require File.join(root, 'essentia_pretranslate')

Command = Struct.new(:code, :parameters)
MapData = Struct.new(:events)
MapEvent = Struct.new(:pages)
Page = Struct.new(:list)
CommonEvent = Struct.new(:list)
SystemData = Struct.new(:start_map_id)

def load_data(path)
  $test_data.fetch(path) { raise IOError, path }
end

def check(expected, actual)
  raise "Expected #{expected.inspect}, got #{actual.inspect}" unless expected == actual
end

source = <<'RUBY'
  _INTL("Welcome, {1}!")
  pbMessageDisplay(msg, "Watch \\PN!\nGo!")
  pbMessage("Double #{dynamic}")
  _INTL('New adventure')
RUBY
$RGSS_SCRIPTS = [[0, 'Introduction', nil, source]]
$test_data = {
  'Data/System.rxdata' => SystemData.new(2),
  'Data/MapInfos.rxdata' => {1 => nil, 2 => nil},
  'Data/Map002.rxdata' => MapData.new({1 => MapEvent.new([Page.new([
    Command.new(101, ['Welcome to the academy.']), Command.new(401, ['Follow me.']),
    Command.new(102, [['Yes, please', 'Later']]),
    Command.new(355, ['pbMessage(']), Command.new(655, ['  "An event script!" )'])
  ])])}),
  'Data/Map001.rxdata' => MapData.new({2 => MapEvent.new([Page.new([
    Command.new(101, ['Another map.'])
  ])])}),
  'Data/CommonEvents.rxdata' => [nil, CommonEvent.new([Command.new(101, ['A common event.'])])],
  'Data/messages.dat' => [nil, {'Original catalog entry' => 'Visible catalog entry'}]
}

Dir.mktmpdir('gahvoria-pretranslation') do |dir|
  FileUtils.mkdir_p(File.join(dir, 'Data', 'Scripts'))
  File.write(File.join(dir, 'Data', 'Scripts', 'Intro.rb'), 'pbMessage("An extracted script.")')
  expected = [
    'Welcome, {1}!', "Watch \\PN!\nGo!", 'New adventure', 'An extracted script.',
    "Welcome to the academy.\nFollow me.", 'Welcome to the academy. Follow me.',
    'Yes, please', 'Later', 'An event script!', 'A common event.', 'Another map.',
    'Original catalog entry', 'Visible catalog entry'
  ]
  script_choices = []
  GahvoriaPretranslate.scripts(<<'CHOICES') { |text| script_choices << text }
pbMessage("Pick one", ["Go", 'Stay'])
pbShowCommands(nil, ["Red", "Blue",])
pbShowCommands(nil, ["Unsafe", dynamic()])
CHOICES
  check(['Pick one', 'Go', 'Stay', 'Red', 'Blue'], script_choices)

  extra_literals = []
  GahvoriaPretranslate.scripts(<<'EXTRA') { |text| extra_literals << text }
pbConfirmMessage("Open the door?")
_MAPINTL($game_map.map_id, "Welcome to {1}.")
pbEnterPlayerName("Your name?", 0, 8)
_ISPRINTF("Total {1:02d}", 2)
getLineBrokenChunks(bitmap, "An opening prompt.", 80)
Window_UnformattedTextPokemon.newWithSize("Pick an option.", 0, 0, 100, 30)
EXTRA
  check(['Open the door?', 'Welcome to {1}.', 'Your name?', 'Total {1:02d}', 'An opening prompt.', 'Pick an option.'], extra_literals)

  vx = []
  GahvoriaPretranslate.commands([
    Command.new(101, ['HeroFace', 0, 0, 2]), Command.new(401, ['A line from VX.']),
    Command.new(102, [['Open', 'Wait']])
  ], lambda { false }) { |text| vx << text }
  check(['A line from VX.', 'Open', 'Wait'], vx)

  collected = []
  GahvoriaPretranslate.collect(dir, lambda { false }) { |text| collected << text }
  opening_first = expected[4, 5] + expected[0, 4] + expected[9..-1]
  check(opening_first, collected)

  base = File.join(dir, 'prepare')
  marker = File.join(dir, 'active')
  worker = Thread.new do
    until File.file?(base + '.ready')
      sleep(0.01)
    end
    check(true, File.file?(marker))
    lines = File.readlines(base + '.ready').map { |line| EssentiaTranslate.unesc(line.chomp) }
    check(opening_first, lines)
    File.write(base + '.done', '')
  end
  GahvoriaPretranslate.run(dir, base, marker)
  worker.value
  check(false, File.exist?(marker))

  stalled = File.join(dir, 'stalled')
  started = GahvoriaPretranslate.now
  GahvoriaPretranslate.run(dir, stalled, marker, 0.25)
  check(true, File.file?(stalled + '.ready'))
  check(true, File.file?(stalled + '.skip'))
  check(false, File.exist?(marker))
  raise 'The launch deadline did not release the stalled translator' if GahvoriaPretranslate.now - started > 2
  check(false, File.exist?(base + '.tmp'))

  catalog = File.join(dir, 'catalog')
  $RGSS_SCRIPTS << [1, 'Duplicate', nil, 'pbMessage("Yes, please")']
  GahvoriaPretranslate.export_catalog(dir, catalog)
  lines = File.readlines(catalog + '.ready').map { |line| EssentiaTranslate.unesc(line.chomp) }
  check(opening_first, lines)
  check(false, File.exist?(catalog + '.tmp'))

  aborted = File.join(dir, 'aborted')
  File.write(aborted + '.skip', '')
  GahvoriaPretranslate.export_catalog(dir, aborted)
  check(false, File.exist?(aborted + '.ready'))
  check(false, File.exist?(aborted + '.tmp'))

  stopped = []
  GahvoriaPretranslate.collect(dir, lambda { true }) { |text| stopped << text }
  check([], stopped)
end
puts 'Prelaunch translation extraction checks passed'
