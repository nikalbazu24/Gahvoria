# Opt-in local frame measurements. Never uploads anything or inspects save data.
module EssentiaDiagnostics
  FILE = ENV['ESSENTIA_DIAGNOSTICS_FILE'].to_s
  @frames = 0
  @last = nil
  @start = nil
  @peak = 0.0
  def self.now
    return System.essentia_raw_uptime if defined?(System) && System.respond_to?(:essentia_raw_uptime)
    return System.uptime if defined?(System) && System.respond_to?(:uptime)
    Time.now.to_f
  end
  def self.tick
    return if FILE.empty?
    time = now
    if @last.nil?
      @start = @last = time
      return
    end
    gap = time - @last
    @last = time
    return if gap < 0
    @peak = gap if gap > @peak
    @frames += 1
    elapsed = time - @start
    return if elapsed < 1.0
    line = [sprintf('%.1f', @frames / elapsed), sprintf('%.1f', @peak * 1000), @frames].join("\t")
    File.open(FILE + '.tmp', 'wb') { |f| f.write(line) }
    File.rename(FILE + '.tmp', FILE)
    @start = time
    @frames = 0
    @peak = 0.0
  rescue Exception
  end
end
module Graphics
  class << self
    alias_method :essentia_diagnostics_update, :update
    def update(*args)
      essentia_diagnostics_update(*args)
      EssentiaDiagnostics.tick
    end
  end
end
