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
  collected = []
  GahvoriaPretranslate.collect(dir, lambda { false }) { |text| collected << text }
  check(expected, collected)

  base = File.join(dir, 'prepare')
  marker = File.join(dir, 'active')
  worker = Thread.new do
    until File.file?(base + '.ready')
      sleep(0.01)
    end
    check(true, File.file?(marker))
    lines = File.readlines(base + '.ready').map { |line| EssentiaTranslate.unesc(line.chomp) }
    check(expected, lines)
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
  check(expected.take(3) + ['Yes, please'] + (expected.drop(3) - ['Yes, please']), lines)
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
