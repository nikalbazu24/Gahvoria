# encoding: UTF-8
# Simulated real formatter contracts: localization must precede insertion and drawing.
require 'tmpdir'

def check(expected, actual)
  raise "Expected #{expected.inspect}, got #{actual.inspect}" unless expected == actual
end
module Graphics
  def self.update; end
end
module MessageTypes
  Moves = 5; Items = 7; Abilities = 9; Types = 11; Natures = 25; Species = 1
  ScriptTexts = 22
  def self.getFromHash(_kind, key); key; end
  def self.getFromMapHash(_map, key); key; end
  def self.get(_kind, key); key; end
end
module GameData
  class Move
    attr_accessor :name
    def initialize(name); @name = name; end
  end
  class Item < Move; end
  class Ability < Move; end
  class Nature < Move; end
  class Type < Move; end
end
module PBMoves
  def self.getName(_id); 'ThunderPunch'; end
end

def _INTL(*args)
  text = MessageTypes.getFromHash(MessageTypes::ScriptTexts, args[0]).clone
  (1...args.size).each { |i| text.gsub!(/\{#{i}\}/) { args[i].to_s } }
  text
end

def _ISPRINTF(*args)
  text = MessageTypes.getFromHash(MessageTypes::ScriptTexts, args[0]).clone
  (1...args.size).each { |i| text.gsub!(/\{#{i}:([^}]+)\}/) { sprintf('%' + $1, args[i]) } }
  text
end

def _MAPINTL(map, *args)
  $last_map = map
  text = MessageTypes.getFromMapHash(map, args[0]).clone
  (1...args.size).each { |i| text.gsub!(/\{#{i}\}/) { args[i].to_s } }
  text
end

Dir.mktmpdir('gahvoria-templates') do |dir|
  ENV['ESSENTIA_TR_FILE'] = File.join(dir, 'answers')
  ENV['ESSENTIA_TR_REQ'] = File.join(dir, 'requests')
  ENV['ESSENTIA_TR_TARGET'] = 'es'
  ENV['ESSENTIA_TR_GAME'] = 'Pokemon Reborn'
  Dir.mkdir(ENV['ESSENTIA_TR_REQ'])
  root = File.expand_path('../../engine/src/main/assets/essentia_preload', File.dirname(__FILE__))
  load File.join(root, 'essentia_pokemon_terms.rb')
  load File.join(root, 'essentia_translate.rb')
  t = EssentiaTranslate
  rows = {'Hello, {1}!'=>'¡Hola, {1}!', 'You found {1:03d} coins.'=>'Encontraste {1:03d} monedas.',
          'The {1} belongs to {2}.'=>'El {1} es de {2}.'}
  File.open(ENV['ESSENTIA_TR_FILE'], 'wb') do |f|
    rows.each { |a,b| f.puts(t.esc(a) + "\t" + t.esc(b)) }
  end
  t.load_new
  ['Froakie', 'nico', 'Kai', 'Alex', 'Érika', 'A\\1'].each do |name|
    text = _INTL('Hello, {1}!', name)
    check('¡Hola, ' + name + '!', t.message_text(text, 0))
    check(true, text.instance_variable_get(:@gahvoria_translated))
  end
  check('Encontraste 007 monedas.', t.lookup(_ISPRINTF('You found {1:03d} coins.', 7)))
  check('El mapa es de Kai.', t.lookup(_MAPINTL(29, 'The {1} belongs to {2}.', 'mapa', 'Kai')))
  check(29, $last_map)
  check('¡Froakie usó Burbuja!', _INTL('{1} used {2}!', 'Froakie', 'Burbuja'))
  check('¡nico ganó $70 por el combate!', _INTL('{1} got ${2} for winning!', 'nico', 70))
  # Cold template: the complete sentence is queued once, not each expanded variant.
  cold = _INTL('Please follow {1}.', 'Mia')
  second = _INTL('Please follow {1}.', 'Kai')
  check('Please follow Mia.', t.message_text(cold, 0))
  t.flush
  queued = Dir.glob(File.join(ENV['ESSENTIA_TR_REQ'], '*.req')).map { |f| File.read(f) }.join
  check(true, queued.include?('Please follow {1}.'))
  check(false, queued.include?('Please follow Mia.'))
  File.open(ENV['ESSENTIA_TR_FILE'], 'ab') { |f| f.puts("Please follow {1}.\tPor favor, sigue a {1}.") }
  t.load_new
  check('Por favor, sigue a Mia.', t.message_text(cold.dup, 0))
  check('Por favor, sigue a Kai.', t.lookup(second))
  check('Por favor, sigue a Mia.', t.lookup(Marshal.load(Marshal.dump(cold))))
  check('Please follow Mia.', cold) # No mutation of caller-owned game text.

  # Every imported pair through the typed message API, even with empty neural memory.
  kinds = {:move=>MessageTypes::Moves, :item=>MessageTypes::Items, :ability=>MessageTypes::Abilities,
           :type=>MessageTypes::Types, :nature=>MessageTypes::Natures}
  count = 0
  GahvoriaPokemonTerms::NAMES.each do |kind, terms|
    terms.each do |source, spanish|
      check(spanish, MessageTypes.get(kinds[kind], source))
      count += 1
    end
  end
  check('Puño Trueno', PBMoves.getName(9))
  check('Lanzallamas', GameData::Move.new('Flamethrower').name)
  check('Pound', MessageTypes.get(MessageTypes::Species, 'Pound'))
  check('Fakemon Custom', MessageTypes.get(MessageTypes::Species, 'Fakemon Custom'))
  check('Burst of Starlight', GameData::Move.new('Burst of Starlight').name)
  custom = GameData::Move.new('Pound')
  check('Destructor', custom.name)
  check('Pound', custom.instance_variable_get(:@name))
  # Type/nature context comes from the whole menu, not an isolated character name.
  choices = ['Hardy', 'Brave', 'Bold', 'Cancel']
  check(['Fuerte', 'Audaz', 'Osada', 'Cancelar'], t.render_choices(choices))
  check(['Hardy', 'Brave', 'Bold', 'Cancel'], choices)
  check(['Planta', 'Lucha', 'Siniestro', 'Volver'], t.render_choices(['Grass', 'Fighting', 'Dark', 'Back']))
  check(nil, t.grouped_choice_terms(['hardy', 'elena', 'kai']))
  check(nil, t.grouped_choice_terms(['hardy', 'brave']))
  check('Hardy', MessageTypes.get(MessageTypes::Species, 'Hardy'))
  # Reinstallation must never stack wrappers or recurse.
  4.times { t.install }
  check('Lanzallamas', GameData::Move.new('Flamethrower').name)
  check('¡Hola, Mia!', _INTL('Hello, {1}!', 'Mia'))
  puts "Template reuse and #{count} typed glossary entries passed"
end
