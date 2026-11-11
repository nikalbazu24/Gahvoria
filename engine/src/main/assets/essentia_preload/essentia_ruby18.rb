# essentia_ruby18.rb — Essentia (GPLv2, like the rest of Essentia)
#
# Only for the classic engine (Ruby 1.8.7). The bundled helper scripts were
# written for modern Ruby; these few methods let them run on Ruby 1.8 too.
# Nothing here changes how the game's own Ruby 1.8 code behaves.

class String
  def getbyte(i); self[i]; end unless method_defined?(:getbyte)
  unless method_defined?(:setbyte)
    def setbyte(i, b)
      self[i] = b
      b
    end
  end
  def ord; self[0]; end unless method_defined?(:ord)
  def force_encoding(*args); self; end unless method_defined?(:force_encoding)
  def b; self; end unless method_defined?(:b)
  def unpack1(fmt); unpack(fmt)[0]; end unless method_defined?(:unpack1)
end

class Array
  def sample; choice; end unless method_defined?(:sample)
end

class Symbol
  def to_proc
    sym = self
    Proc.new { |obj, *args| obj.__send__(sym, *args) }
  end unless method_defined?(:to_proc)
end

# Windows programs (.exe...) can't run on Android: answer like a program that
# printed nothing, and tell Essentia if it was the game's updater.
module EssentiaClassic
  WINDOWS_PROGRAM = /\.(exe|bat|cmd|com|vbs|ps1|msi)\b/i

  def self.windows_program?(args)
    text = args.flatten.map { |a| a.is_a?(Hash) ? '' : a.to_s }.join(' ')
    return false unless text =~ WINDOWS_PROGRAM
    if text =~ /updater/i && !ENV['ESSENTIA_ACK_FILE'].to_s.empty?
      File.open(ENV['ESSENTIA_ACK_FILE'], 'ab') { |f| f.write("updater 1\n") } rescue nil
    end
    true
  end
end

class << IO
  alias_method :essentia_original_popen, :popen
  def popen(*args, &block)
    if EssentiaClassic.windows_program?(args)
      io = File.open('/dev/null', 'r')
      return block ? block.call(io) : io
    end
    essentia_original_popen(*args, &block)
  end
end

module Kernel
  alias_method :essentia_original_system, :system
  def system(*args)
    return false if EssentiaClassic.windows_program?(args)
    essentia_original_system(*args)
  end
end
