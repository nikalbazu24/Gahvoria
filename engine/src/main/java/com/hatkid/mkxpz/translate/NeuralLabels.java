package com.hatkid.mkxpz.translate;

// Generated from the pinned PokéAPI names by tools/build_neural_lexicon.py.
// BSD-3-Clause: engine/src/main/assets/licenses/pokeapi.txt.
final class NeuralLabels {
    static final java.util.Map<String,String> LABELS = new java.util.HashMap<>();
    static final java.util.List<String[]> POSSESSED = new java.util.ArrayList<>();
    static {
        labels0();
        labels1();
        labels2();
        labels3();
        labels4();
        labels5();
        labels6();
        labels7();
        labels8();
        labels9();
        labels10();
        labels11();
        labels12();
        labels13();
        labels14();
        labels15();
        labels16();
        labels17();
        labels18();
        labels19();
        labels20();
        labels21();
        labels22();
        labels23();
        labels24();
        labels25();
        labels26();
        labels27();
        labels28();
        labels29();
        labels30();
        labels31();
        labels32();
        labels33();
        labels34();
        labels35();
        labels36();
        labels37();
        for (String source : new String[]{
            "10,000,000 Volt Thunderbolt",
            "Menacing Moonraze Maelstrom",
            "Soul-Stealing 7-Star Strike",
            "Light That Burns the Sky",
            "Never-Ending Nightmare",
            "Splintered Stormshards",
            "Let’s Snuggle Forever",
            "Searing Sunraze Smash",
            "Clangorous Soulblaze",
            "Supersonic Skystrike",
            "Malicious Moonsault",
            "Pulverizing Pancake",
            "Sinister Arrow Raid",
            "Black Hole Eclipse",
            "Stoked Sparksurfer",
            "All-Out Pummeling",
            "Continental Crush",
            "Devastating Drake",
            "Genesis Supernova",
            "Guardian of Alola",
            "Inferno Overdrive",
            "Burning Jealousy",
            "Chilly Reception",
            "Collision Course",
            "Curious Medicine",
            "Dauntless Shield",
            "Double Iron Bash",
            "Electric Terrain",
            "Electromorphosis",
            "Extreme Evoboost",
            "First Impression",
            "Nature’s Madness",
            "Neutralizing Gas",
            "Oceanic Operetta",
            "Orichalcum Pulse",
            "Parabolic Charge",
            "Poison Puppeteer",
            "Power of Alchemy",
            "Precipice Blades",
            "Revelation Dance",
            "Revival Blessing",
            "Shattered Psyche",
            "Springtide Storm",
            "Stomping Tantrum",
            "Supersweet Syrup",
            "Supreme Overlord",
            "Thermal Exchange",
            "Wandering Spirit",
            "Water Compaction",
            "Bleakwind Storm",
            "Breakneck Blitz",
            "Burning Bulwark",
            "Clanging Scales",
            "Clangorous Soul",
            "Corkscrew Crash",
            "Disarming Voice",
            "Expanding Force",
            "False Surrender",
            "Full Metal Body",
            "Gorilla Tactics",
            "High Horsepower",
            "Hyperspace Fury",
            "Hyperspace Hole",
            "Infernal Parade",
            "Lingering Aroma",
            "Malignant Chain",
            "Misty Explosion",
            "Population Bomb",
            "Power Construct",
            "Prismatic Laser",
            "Psychic Terrain",
            "Queenly Majesty",
            "Savage Spin-Out",
            "Scorching Sands",
            "Subzero Slammer",
            "Sunsteel Strike",
            "Surging Strikes",
            "Tablets of Ruin",
            "Thousand Arrows",
            "Thunderous Kick",
            "Well-Baked Body",
            "Alluring Voice",
            "Astral Barrage",
            "Baby-Doll Eyes",
            "Baneful Bunker",
            "Behemoth Blade",
            "Blazing Torque",
            "Breaking Swipe",
            "Ceaseless Edge",
            "Chilling Neigh",
            "Chilling Water",
            "Darkest Lariat",
            "Dazzling Gleam",
            "Dynamax Cannon",
            "Electric Surge",
            "Emergency Exit",
            "Floral Healing",
            "Forest’s Curse",
            "Freezing Glare",
            "Gigaton Hammer",
            "Gigavolt Havoc",
            "Grassy Terrain",
            "High Jump Kick",
            "Intrepid Sword",
            "Jungle Healing",
            "Lunar Blessing",
            "Magical Torque",
            "Max Overgrowth",
            "Max Steelspike",
            "Meteor Assault",
            "Moongeist Beam",
            "Mycelium Might",
            "Mystical Power",
            "Noxious Torque",
            "Petal Blizzard",
            "Piercing Drill",
            "Power-Up Punch",
            "Primordial Sea",
            "Propeller Tail",
            "Protosynthesis",
            "Psyshield Bash",
            "Purifying Salt",
            "Rising Voltage",
            "Sandsear Storm",
            "Screen Cleaner",
            "Shell Side Arm",
            "Smelling Salts",
            "Sparkling Aria",
            "Spectral Thief",
            "Spirit Shackle",
            "Splishy Splash",
            "Steam Eruption",
            "Supercell Slam",
            "Tachyon Cutter",
            "Tera Starstorm",
            "Thousand Waves",
            "Trick-or-Treat",
            "Twinkle Tackle",
            "Vessel of Ruin",
            "Water Shuriken",
            "Wildbolt Storm",
            "Acid Downpour",
            "Ancient Power",
            "Aromatic Mist",
            "Beads of Ruin",
            "Behemoth Bash",
            "Bitter Malice",
            "Bouncy Bubble",
            "Combat Torque",
            "Compound Eyes",
            "Core Enforcer",
            "Corrosive Gas",
            "Crafty Shield",
            "Desolate Land",
            "Diamond Storm",
            "Dragon Ascent",
            "Dragon Breath",
            "Dragon Energy",
            "Dragon Hammer",
            "Draining Kiss",
            "Dual Wingbeat",
            "Dynamic Punch",
            "Eerie Impulse",
            "Electro Drift",
            "Embody Aspect",
            "Extreme Speed",
            "Feather Dance",
            "Fishious Rend",
            "Flower Shield",
            "Glacial Lance",
            "Grass Whistle",
            "Hadron Engine",
            "Headlong Rush",
            "Hunger Switch",
            "King’s Shield",
            "Kowtow Cleave",
            "Last Respects",
            "Light of Ruin",
            "Lightning Rod",
            "Magnetic Flux",
            "Matcha Gotcha",
            "Max Airstream",
            "Max Flutterby",
            "Max Hailstorm",
            "Max Lightning",
            "Max Mindstorm",
            "Mega Launcher",
            "Mighty Cleave",
            "Misty Terrain",
            "Mountain Gale",
            "Mystical Fire",
            "Oblivion Wing",
            "Parental Bond",
            "Phantom Force",
            "Photon Geyser",
            "Poison Powder",
            "Psychic Fangs",
            "Psychic Noise",
            "Psychic Surge",
            "Rocky Payload",
            "Self-Destruct",
            "Shadow Shield",
            "Skitter Smack",
            "Sparkly Swirl",
            "Spicy Extract",
            "Stance Change",
            "Steely Spirit",
            "Strange Steam",
            "Sword of Ruin",
            "Tangling Hair",
            "Tectonic Rage",
            "Teraform Zero",
            "Terrain Pulse",
            "Thunder Punch",
            "Thunder Shock",
            "Triple Arrows",
            "Veevee Volley",
            "Victory Dance",
            "Wicked Torque",
            "Adaptability",
            "Anticipation",
            "Armor Cannon",
            "Aromatherapy",
            "Attack Order",
            "Barb Barrage",
            "Battle Armor",
            "Bitter Blade",
            "Brutal Swing",
            "Bullet Punch",
            "Catastropika",
            "Circle Throw",
            "Close Combat",
            "Color Change",
            "Conversion 2",
            "Cosmic Power",
            "Cotton Guard",
            "Cotton Spore",
            "Court Change",
            "Cross Poison",
            "Defend Order",
            "Defense Curl",
            "Delta Stream",
            "Destiny Bond",
            "Double Shock",
            "Draco Meteor",
            "Dragon Cheer",
            "Dragon Dance",
            "Dragon Darts",
            "Dragon Pulse",
            "Dragon’s Maw",
            "Drum Beating",
            "Echoed Voice",
            "Effect Spore",
            "Electro Ball",
            "Electro Shot",
            "Extrasensory",
            "Feint Attack",
            "Fell Stinger",
            "Final Gambit",
            "Flame Charge",
            "Flamethrower",
            "Flash Cannon",
            "Fleur Cannon",
            "Flower Trick",
            "Flying Press",
            "Focus Energy",
            "Freeze Shock",
            "Freezy Frost",
            "Frenzy Plant",
            "Friend Guard",
            "Frost Breath",
            "Fusion Flare",
            "Future Sight",
            "Good as Gold",
            "Grass Pledge",
            "Grassy Glide",
            "Grassy Surge",
            "Gulp Missile",
            "Healing Wish",
            "Helping Hand",
            "Hidden Power",
            "Honey Gather",
            "Hydro Cannon",
            "Hydro Vortex",
            "Hyper Cutter",
            "Icicle Crash",
            "Icicle Spear",
            "Iron Defense",
            "Land’s Wrath",
            "Leaf Tornado",
            "Light Screen",
            "Liquid Voice",
            "Lumina Crash",
            "Luster Purge",
            "Magic Bounce",
            "Magic Powder",
            "Magical Leaf",
            "Make It Rain",
            "Marvel Scale",
            "Max Darkness",
            "Max Phantasm",
            "Max Rockfall",
            "Max Starfall",
            "Max Wyrmwind",
            "Mirror Armor",
            "Mold Breaker",
            "Multi-Attack",
            "Natural Cure",
            "Natural Gift",
            "Nature Power",
            "Ominous Wind",
            "Origin Pulse",
            "Parting Shot",
            "Plasma Fists",
            "Poison Point",
            "Poison Sting",
            "Poison Touch",
            "Psycho Boost",
            "Psycho Shift",
            "Quick Attack",
            "Quiver Dance",
            "Reflect Type",
            "Roar of Time",
            "Rock Wrecker",
            "Rolling Kick",
            "Sacred Sword",
            "Searing Shot",
            "Secret Power",
            "Secret Sword",
            "Seismic Toss",
            "Serene Grace",
            "Shadow Blast",
            "Shadow Blitz",
            "Shadow Break",
            "Shadow Chill",
            "Shadow Force",
            "Shadow Panic",
            "Shadow Punch",
            "Shadow Sneak",
            "Shadow Storm",
            "Shields Down",
            "Sizzly Slide",
            "Sky Uppercut",
            "Sleep Powder",
            "Smart Strike",
            "Snow Warning",
            "Spacial Rend",
            "Spike Cannon",
            "Spiky Shield",
            "Spirit Break",
            "Stealth Rock",
            "Steam Engine",
            "Steel Roller",
            "Stored Power",
            "Strength Sap",
            "Struggle Bug",
            "Stuff Cheeks",
            "Sucker Punch",
            "Suction Cups",
            "Surge Surfer",
            "Swords Dance",
            "Synchronoise",
            "Tangled Feet",
            "Tearful Look",
            "Techno Blast",
            "Teeter Dance",
            "Temper Flare",
            "Thunder Cage",
            "Thunder Fang",
            "Thunder Wave",
            "Toxic Debris",
            "Toxic Spikes",
            "Toxic Thread",
            "Venom Drench",
            "Victory Star",
            "Vital Spirit",
            "Wake-Up Slap",
            "Water Absorb",
            "Water Bubble",
            "Water Pledge",
            "Weather Ball",
            "Wonder Guard",
            "Zen Headbutt",
            "Zero to Hero",
            "Acupressure",
            "Ally Switch",
            "Anchor Shot",
            "Anger Point",
            "Anger Shell",
            "Aqua Cutter",
            "Aura Sphere",
            "Aurora Beam",
            "Aurora Veil",
            "Battle Bond",
            "Beast Boost",
            "Bolt Strike",
            "Branch Poke",
            "Brick Break",
            "Bubble Beam",
            "Bullet Seed",
            "Bulletproof",
            "Charge Beam",
            "Cheek Pouch",
            "Chloroblast",
            "Chlorophyll",
            "Comet Punch",
            "Comeuppance",
            "Competitive",
            "Confuse Ray",
            "Cotton Down",
            "Cursed Body",
            "Dizzy Punch",
            "Doom Desire",
            "Double Kick",
            "Double Slap",
            "Double Team",
            "Double-Edge",
            "Dragon Claw",
            "Dragon Rage",
            "Dragon Rush",
            "Dragon Tail",
            "Drain Punch",
            "Dream Eater",
            "Earth Eater",
            "Earth Power",
            "Eerie Spell",
            "Energy Ball",
            "Entrainment",
            "False Swipe",
            "Fickle Beam",
            "Fiery Dance",
            "Fiery Wrath",
            "Fillet Away",
            "Fire Pledge",
            "Flame Burst",
            "Flame Wheel",
            "Flare Blitz",
            "Flare Boost",
            "Floaty Fall",
            "Flower Gift",
            "Flower Veil",
            "Focus Blast",
            "Focus Punch",
            "Frustration",
            "Fury Attack",
            "Fury Cutter",
            "Fury Swipes",
            "Fusion Bolt",
            "Gastro Acid",
            "Giga Impact",
            "Glaive Rush",
            "Glitzy Glow",
            "Guard Split",
            "Head Charge",
            "Heart Stamp",
            "Heavy Metal",
            "Horn Attack",
            "Hospitality",
            "Hydro Steam",
            "Hyper Drill",
            "Hyper Voice",
            "Ice Spinner",
            "Infestation",
            "Infiltrator",
            "Innards Out",
            "Inner Focus",
            "Karate Chop",
            "Laser Focus",
            "Last Resort",
            "Light Metal",
            "Liquid Ooze",
            "Liquidation",
            "Lovely Kiss",
            "Lucky Chant",
            "Lunar Dance",
            "Magic Guard",
            "Magma Armor",
            "Magma Storm",
            "Magnet Bomb",
            "Magnet Pull",
            "Magnet Rise",
            "Max Knuckle",
            "Metal Burst",
            "Metal Sound",
            "Meteor Beam",
            "Meteor Mash",
            "Mind Reader",
            "Miracle Eye",
            "Mirror Coat",
            "Mirror Move",
            "Mirror Shot",
            "Misty Surge",
            "Morning Sun",
            "Mortal Spin",
            "Motor Drive",
            "Muddy Water",
            "Night Shade",
            "Night Slash",
            "Odor Sleuth",
            "Opportunist",
            "Pastel Veil",
            "Perish Body",
            "Perish Song",
            "Petal Dance",
            "Pin Missile",
            "Poison Fang",
            "Poison Heal",
            "Poison Tail",
            "Pollen Puff",
            "Poltergeist",
            "Powder Snow",
            "Power Shift",
            "Power Split",
            "Power Trick",
            "Prism Armor",
            "Quark Drive",
            "Quick Guard",
            "Rage Powder",
            "Raging Bull",
            "Raging Fury",
            "Razor Shell",
            "Refrigerate",
            "Regenerator",
            "Rock Polish",
            "Sacred Fire",
            "Sand Attack",
            "Sand Stream",
            "Shadow Ball",
            "Shadow Bolt",
            "Shadow Bone",
            "Shadow Claw",
            "Shadow Down",
            "Shadow Fire",
            "Shadow Half",
            "Shadow Hold",
            "Shadow Mist",
            "Shadow Rave",
            "Shadow Rush",
            "Shadow Shed",
            "Shadow Wave",
            "Sheer Force",
            "Shell Armor",
            "Shell Smash",
            "Shield Dust",
            "Signal Beam",
            "Silver Wind",
            "Simple Beam",
            "Sludge Bomb",
            "Sludge Wave",
            "Smokescreen",
            "Soft-Boiled",
            "Solar Blade",
            "Solar Power",
            "Speed Boost",
            "Spicy Spray",
            "Steamroller",
            "Steelworker",
            "Sticky Hold",
            "Storm Drain",
            "Storm Throw",
            "String Shot",
            "Sweet Scent",
            "Synchronize",
            "Telekinesis",
            "Throat Chop",
            "Thunderbolt",
            "Thunderclap",
            "Tinted Lens",
            "Topsy-Turvy",
            "Tough Claws",
            "Toxic Boost",
            "Toxic Chain",
            "Triple Axel",
            "Triple Dive",
            "Triple Kick",
            "Unseen Fist",
            "Vacuum Wave",
            "Vital Throw",
            "Volt Absorb",
            "Volt Switch",
            "Volt Tackle",
            "Water Pulse",
            "Water Sport",
            "Water Spout",
            "White Smoke",
            "Wicked Blow",
            "Wild Charge",
            "Will-O-Wisp",
            "Wing Attack",
            "Wonder Room",
            "Wonder Skin",
            "Wood Hammer",
            "Accelerock",
            "Acid Armor",
            "Acid Spray",
            "Acrobatics",
            "Aerial Ace",
            "Air Cutter",
            "Apple Acid",
            "Arena Trap",
            "Arm Thrust",
            "Armor Tail",
            "Aroma Veil",
            "Aura Break",
            "Aura Wheel",
            "Autotomize",
            "Bad Dreams",
            "Ball Fetch",
            "Baton Pass",
            "Beak Blast",
            "Belly Drum",
            "Blast Burn",
            "Blaze Kick",
            "Blood Moon",
            "Bloom Doom",
            "Blue Flare",
            "Body Press",
            "Bonemerang",
            "Brave Bird",
            "Buzzy Buzz",
            "Camouflage",
            "Clear Body",
            "Clear Smog",
            "Cloud Nine",
            "Conversion",
            "Crabhammer",
            "Cross Chop",
            "Crush Claw",
            "Crush Grip",
            "Cute Charm",
            "Dark Pulse",
            "Double Hit",
            "Drill Peck",
            "Early Bird",
            "Earthquake",
            "Electroweb",
            "Esper Wing",
            "Eternabeam",
            "Fairy Aura",
            "Fairy Lock",
            "Fairy Wind",
            "Fake Tears",
            "Fire Blast",
            "Fire Punch",
            "Flame Body",
            "Flash Fire",
            "Force Palm",
            "Freeze-Dry",
            "Gale Wings",
            "Gear Grind",
            "Giga Drain",
            "Grass Knot",
            "Grass Pelt",
            "Grav Apple",
            "Grim Neigh",
            "Guard Swap",
            "Guillotine",
            "Hammer Arm",
            "Happy Hour",
            "Hard Press",
            "Head Smash",
            "Heal Block",
            "Heal Order",
            "Heal Pulse",
            "Heart Swap",
            "Heat Crash",
            "Heavy Slam",
            "Hold Hands",
            "Hone Claws",
            "Horn Drill",
            "Horn Leech",
            "Huge Power",
            "Hydro Pump",
            "Hyper Beam",
            "Hyper Fang",
            "Ice Hammer",
            "Ice Scales",
            "Illuminate",
            "Incinerate",
            "Intimidate",
            "Ion Deluge",
            "Iron Barbs",
            "Ivy Cudgel",
            "Lava Plume",
            "Leaf Blade",
            "Leaf Guard",
            "Leaf Storm",
            "Leech Life",
            "Leech Seed",
            "Long Reach",
            "Mach Punch",
            "Magic Coat",
            "Magic Room",
            "Max Geyser",
            "Max Strike",
            "Mega Drain",
            "Mega Punch",
            "Metal Claw",
            "Milk Drink",
            "Mind Blown",
            "Mind’s Eye",
            "Multiscale",
            "Nasty Plot",
            "Needle Arm",
            "Neuroforce",
            "Night Daze",
            "No Retreat",
            "Noble Roar",
            "Pain Split",
            "Pickpocket",
            "Pika Papow",
            "Play Rough",
            "Poison Gas",
            "Poison Jab",
            "Power Spot",
            "Power Swap",
            "Power Trip",
            "Power Whip",
            "Psycho Cut",
            "Punishment",
            "Pure Power",
            "Quick Draw",
            "Quick Feet",
            "RKS System",
            "Rain Dance",
            "Rapid Spin",
            "Razor Leaf",
            "Razor Wind",
            "Relic Song",
            "Rock Blast",
            "Rock Climb",
            "Rock Slide",
            "Rock Smash",
            "Rock Throw",
            "Rototiller",
            "Rough Skin",
            "Sand Force",
            "Sap Sipper",
            "Sappy Seed",
            "Scale Shot",
            "Scary Face",
            "Seed Flare",
            "Seed Sower",
            "Shadow End",
            "Shadow Sky",
            "Shadow Tag",
            "Sheer Cold",
            "Shell Trap",
            "Shift Gear",
            "Shock Wave",
            "Skill Link",
            "Skill Swap",
            "Skull Bash",
            "Sky Attack",
            "Sleep Talk",
            "Slow Start",
            "Slush Rush",
            "Smack Down",
            "Snipe Shot",
            "Snow Cloak",
            "Solar Beam",
            "Solid Rock",
            "Sonic Boom",
            "Soul-Heart",
            "Soundproof",
            "Speed Swap",
            "Spider Web",
            "Steel Beam",
            "Steel Wing",
            "Sticky Web",
            "Stone Edge",
            "Strong Jaw",
            "Stun Spore",
            "Submission",
            "Substitute",
            "Super Fang",
            "Super Luck",
            "Superpower",
            "Supersonic",
            "Sweet Kiss",
            "Sweet Veil",
            "Swift Swim",
            "Switcheroo",
            "Syrup Bomb",
            "Take Heart",
            "Technician",
            "Tera Blast",
            "Tera Shell",
            "Tera Shift",
            "Torch Song",
            "Trailblaze",
            "Transistor",
            "Tri Attack",
            "Trick Room",
            "Trump Card",
            "Turboblaze",
            "Upper Hand",
            "Water Veil",
            "Wave Crash",
            "Weak Armor",
            "Wide Guard",
            "Wind Power",
            "Wind Rider",
            "Worry Seed",
            "Zap Cannon",
            "Aeroblast",
            "After You",
            "Aftermath",
            "Air Slash",
            "Aqua Ring",
            "Aqua Step",
            "Aqua Tail",
            "Assurance",
            "Avalanche",
            "Baddy Bad",
            "Big Pecks",
            "Body Slam",
            "Bolt Beak",
            "Bone Club",
            "Bone Rush",
            "Boomburst",
            "Calm Mind",
            "Captivate",
            "Celebrate",
            "Chip Away",
            "Commander",
            "Confusion",
            "Constrict",
            "Corrosion",
            "Dark Aura",
            "Dark Void",
            "Defeatist",
            "Dire Claw",
            "Discharge",
            "Dragonize",
            "Drill Run",
            "Dual Chop",
            "Electrify",
            "Explosion",
            "Fire Fang",
            "Fire Lash",
            "Fire Spin",
            "Flip Turn",
            "Follow Me",
            "Foresight",
            "Foul Play",
            "Galvanize",
            "Guard Dog",
            "Gunk Shot",
            "Gyro Ball",
            "Heal Bell",
            "Heat Wave",
            "Heatproof",
            "Hold Back",
            "Hurricane",
            "Hydration",
            "Ice Punch",
            "Ice Shard",
            "Iron Fist",
            "Iron Head",
            "Iron Tail",
            "Jet Punch",
            "Jump Kick",
            "Justified",
            "Knock Off",
            "Low Sweep",
            "Magnitude",
            "Mat Block",
            "Max Flare",
            "Max Guard",
            "Max Quake",
            "Mean Look",
            "Mega Kick",
            "Merciless",
            "Metronome",
            "Mist Ball",
            "Moonblast",
            "Moonlight",
            "Mud Sport",
            "Multitype",
            "Nightmare",
            "Normalize",
            "Oblivious",
            "Octazooka",
            "Overdrive",
            "Own Tempo",
            "Play Nice",
            "Power Gem",
            "Prankster",
            "Psystrike",
            "Punk Rock",
            "Pyro Ball",
            "Rage Fist",
            "Rain Dish",
            "Retaliate",
            "Rock Head",
            "Rock Tomb",
            "Role Play",
            "Ruination",
            "Safeguard",
            "Salt Cure",
            "Sand Rush",
            "Sand Spit",
            "Sand Tomb",
            "Sand Veil",
            "Sandstorm",
            "Schooling",
            "Seed Bomb",
            "Sharpness",
            "Shed Skin",
            "Shed Tail",
            "Silk Trap",
            "Slack Off",
            "Snap Trap",
            "Snowscape",
            "Spotlight",
            "Steadfast",
            "Stockpile",
            "Stone Axe",
            "Sunny Day",
            "Symbiosis",
            "Synthesis",
            "Tail Glow",
            "Tail Slap",
            "Tail Whip",
            "Take Down",
            "Telepathy",
            "Thick Fat",
            "Transform",
            "Trop Kick",
            "Twin Beam",
            "Twineedle",
            "Venoshock",
            "Vine Whip",
            "Vise Grip",
            "Water Gun",
            "Waterfall",
            "Whirlpool",
            "Whirlwind",
            "Wring Out",
            "X-Scissor",
            "Zippy Zap",
            "Aerilate",
            "Air Lock",
            "Analytic",
            "Aqua Jet",
            "Astonish",
            "Axe Kick",
            "Blizzard",
            "Bug Bite",
            "Bug Buzz",
            "Bulldoze",
            "Coaching",
            "Comatose",
            "Contrary",
            "Cud Chew",
            "Dazzling",
            "Decorate",
            "Disguise",
            "Download",
            "Dry Skin",
            "Egg Bomb",
            "Endeavor",
            "Eruption",
            "Fake Out",
            "Forecast",
            "Forewarn",
            "Fur Coat",
            "Geomancy",
            "Glaciate",
            "Gluttony",
            "Headbutt",
            "Hypnosis",
            "Ice Ball",
            "Ice Beam",
            "Ice Body",
            "Ice Burn",
            "Ice Face",
            "Ice Fang",
            "Icy Wind",
            "Illusion",
            "Immunity",
            "Imposter",
            "Imprison",
            "Insomnia",
            "Instruct",
            "Jaw Lock",
            "Judgment",
            "Keen Eye",
            "Lash Out",
            "Levitate",
            "Life Dew",
            "Low Kick",
            "Magician",
            "Max Ooze",
            "Me First",
            "Meditate",
            "Mega Sol",
            "Megahorn",
            "Minimize",
            "Mud Bomb",
            "Mud Shot",
            "Mud-Slap",
            "No Guard",
            "Obstruct",
            "Octolock",
            "Order Up",
            "Overcoat",
            "Overgrow",
            "Overheat",
            "Pixilate",
            "Pressure",
            "Psyblade",
            "Psych Up",
            "Psyshock",
            "Receiver",
            "Reckless",
            "Reversal",
            "Run Away",
            "Shore Up",
            "Sky Drop",
            "Spin Out",
            "Stakeout",
            "Stalwart",
            "Strength",
            "Struggle",
            "Tailwind",
            "Tar Shot",
            "Teleport",
            "Teravolt",
            "Unburden",
            "V-create",
            "Wimp Out",
            "Withdraw",
            "Zen Mode",
            "Zing Zap",
            "Agility",
            "Amnesia",
            "Attract",
            "Barrage",
            "Barrier",
            "Battery",
            "Beat Up",
            "Berserk",
            "Bulk Up",
            "Burn Up",
            "Chatter",
            "Confide",
            "Copycat",
            "Counter",
            "Defiant",
            "Disable",
            "Drizzle",
            "Drought",
            "Embargo",
            "Fissure",
            "Flatter",
            "Gear Up",
            "Gravity",
            "Harvest",
            "Inferno",
            "Ingrain",
            "Kinesis",
            "Leafage",
            "Lock-On",
            "Memento",
            "Mimicry",
            "Outrage",
            "Pay Day",
            "Payback",
            "Present",
            "Protean",
            "Protect",
            "Psybeam",
            "Psychic",
            "Psywave",
            "Pursuit",
            "Rattled",
            "Recover",
            "Recycle",
            "Reflect",
            "Refresh",
            "Revenge",
            "Rivalry",
            "Rollout",
            "Scrappy",
            "Scratch",
            "Screech",
            "Sharpen",
            "Shelter",
            "Spit Up",
            "Stamina",
            "Swagger",
            "Swallow",
            "Teatime",
            "Thunder",
            "Tidy Up",
            "Torment",
            "Torrent",
            "Twister",
            "Unaware",
            "Unnerve",
            "Work Up",
            "Absorb",
            "As One",
            "Assist",
            "Bestow",
            "Bounce",
            "Bubble",
            "Charge",
            "Costar",
            "Crunch",
            "Dancer",
            "Detect",
            "Doodle",
            "Encore",
            "Endure",
            "Facade",
            "Filter",
            "Fluffy",
            "Growth",
            "Grudge",
            "Harden",
            "Healer",
            "Hustle",
            "Libero",
            "Limber",
            "Nuzzle",
            "Pickup",
            "Pounce",
            "Powder",
            "Purify",
            "Return",
            "Simple",
            "Sketch",
            "Sludge",
            "Snatch",
            "Sniper",
            "Spikes",
            "Splash",
            "Static",
            "Stench",
            "Sturdy",
            "Tackle",
            "Thrash",
            "Tickle",
            "Triage",
            "Truant",
            "U-turn",
            "Uproar",
            "Belch",
            "Blaze",
            "Block",
            "Brine",
            "Charm",
            "Clamp",
            "Covet",
            "Curse",
            "Defog",
            "Ember",
            "Feint",
            "Flail",
            "Flash",
            "Fling",
            "Frisk",
            "Glare",
            "Gooey",
            "Growl",
            "Klutz",
            "Lunge",
            "Mimic",
            "Minus",
            "Moody",
            "Moxie",
            "Mummy",
            "Pluck",
            "Pound",
            "Quash",
            "Ripen",
            "Roost",
            "Round",
            "Scald",
            "Slash",
            "Snarl",
            "Snore",
            "Spark",
            "Spite",
            "Spore",
            "Stall",
            "Stomp",
            "Swarm",
            "Swift",
            "Taunt",
            "Thief",
            "Toxic",
            "Trace",
            "Trick",
            "Acid",
            "Bide",
            "Bind",
            "Bite",
            "Coil",
            "Damp",
            "Dive",
            "Gust",
            "Guts",
            "Hail",
            "Haze",
            "Howl",
            "Leer",
            "Lick",
            "Mist",
            "Peck",
            "Plus",
            "Rage",
            "Rest",
            "Roar",
            "Sing",
            "Slam",
            "Smog",
            "Soak",
            "Surf",
            "Wish",
            "Wrap",
            "Yawn",
            "Cut",
            "Dig",
            "Fly",
            "Hex",
        }) POSSESSED.add(new String[]{source, LABELS.get(source)});
    }
    private static void labels0() {
        LABELS.put("10,000,000 Volt Thunderbolt", "Gigarrayo Fulminante");
        LABELS.put("???", "???");
        LABELS.put("Ability Capsule", "Cáps. Habilidad");
        LABELS.put("Ability Patch", "Parche Habilidad");
        LABELS.put("Ability Shield", "Escudo Habilidad");
        LABELS.put("Ability Urge", "Habilitador");
        LABELS.put("Abomasite", "Abomasnowita");
        LABELS.put("Abra Candy", "Caramelo Abra");
        LABELS.put("Absolite", "Absolita");
        LABELS.put("Absolite Z", "Absolita Z");
        LABELS.put("Absorb", "Absorber");
        LABELS.put("Absorb Bulb", "Tubérculo");
        LABELS.put("Academy Ball", "Pelota Academia");
        LABELS.put("Academy Bottle", "Termo Academia");
        LABELS.put("Academy Cup", "Taza Academia");
        LABELS.put("Academy Tablecloth", "Mantel Academia");
        LABELS.put("Accelerock", "Roca Veloz");
        LABELS.put("Acid", "Ácido");
        LABELS.put("Acid Armor", "Armadura Ácida");
        LABELS.put("Acid Downpour", "Diluvio Corrosivo");
        LABELS.put("Acid Spray", "Bomba Ácida");
        LABELS.put("Acro Bike", "Bici Acrobática");
        LABELS.put("Acrobatics", "Acróbata");
        LABELS.put("Acupressure", "Acupresión");
        LABELS.put("Adamant", "Firme");
        LABELS.put("Adamant Crystal", "Gran Diamansfera");
        LABELS.put("Adamant Mint", "Menta Firme");
        LABELS.put("Adamant Orb", "Diamansfera");
        LABELS.put("Adaptability", "Adaptable");
        LABELS.put("Adrenaline Orb", "Nerviosfera");
        LABELS.put("Adventure Guide", "Guía de Aventura");
        LABELS.put("Aerial Ace", "Golpe Aéreo");
        LABELS.put("Aerilate", "Piel Celeste");
        LABELS.put("Aeroblast", "Aerochorro");
        LABELS.put("Aerodactyl Candy", "Caramelo Aerodactyl");
        LABELS.put("Aerodactylite", "Aerodactylita");
        LABELS.put("After You", "Cede Paso");
        LABELS.put("Aftermath", "Detonación");
        LABELS.put("Aggronite", "Aggronita");
        LABELS.put("Agility", "Agilidad");
        LABELS.put("Aguav Berry", "Baya Guaya");
        LABELS.put("Aipom Hair", "Pelo de Aipom");
        LABELS.put("Air Balloon", "Globo Helio");
        LABELS.put("Air Cutter", "Aire Afilado");
        LABELS.put("Air Lock", "Bucle Aire");
        LABELS.put("Air Mail", "Carta Aérea");
        LABELS.put("Air Slash", "Tajo Aéreo");
        LABELS.put("Alakazite", "Alakazamita");
        LABELS.put("All-Out Pummeling", "Ráfaga Demoledora");
        LABELS.put("Alluring Voice", "Canto Encantador");
        LABELS.put("Ally Switch", "Cambio de Banda");
        LABELS.put("Alomomola Mucus", "Secreción de Alomomola");
        LABELS.put("Aloraichium Z", "Alo-Raistal Z");
        LABELS.put("Altarianite", "Altarianita");
        LABELS.put("Amaze Mulch", "Abono Insólito");
        LABELS.put("Amnesia", "Amnesia");
        LABELS.put("Ampharosite", "Ampharosita");
        LABELS.put("Amulet Coin", "Moneda Amuleto");
        LABELS.put("Analytic", "Cálculo Final");
        LABELS.put("Anchor Shot", "Anclaje");
        LABELS.put("Ancient Power", "Poder Pasado");
        LABELS.put("Anger Point", "Irascible");
        LABELS.put("Anger Shell", "Coraza Ira");
        LABELS.put("Anticipation", "Anticipación");
        LABELS.put("Antidote", "Antídoto");
        LABELS.put("Apicot Berry", "Baya Aricoc");
        LABELS.put("Apple", "Manzana");
        LABELS.put("Apple Acid", "Ácido Málico");
        LABELS.put("Applin Juice", "Zumo de Applin");
        LABELS.put("Apricorn Box", "Caja Bonguri");
        LABELS.put("Aqua Cutter", "Tajo Acuático");
        LABELS.put("Aqua Jet", "Acua Jet");
        LABELS.put("Aqua Ring", "Acua Aro");
        LABELS.put("Aqua Step", "Danza Acuática");
        LABELS.put("Aqua Suit", "Traje Aqua");
        LABELS.put("Aqua Tail", "Acua Cola");
        LABELS.put("Arena Trap", "Trampa Arena");
        LABELS.put("Arm Thrust", "Empujón");
        LABELS.put("Armor Cannon", "Cañón Armadura");
        LABELS.put("Armor Fossil", "Fósil Coraza");
        LABELS.put("Armor Pass", "Pase Armadura");
        LABELS.put("Armor Tail", "Cola Armadura");
        LABELS.put("Armorite Ore", "Duralium");
        LABELS.put("Aroma Veil", "Velo Aroma");
        LABELS.put("Aromatherapy", "Aromaterapia");
        LABELS.put("Aromatic Mist", "Niebla Aromática");
        LABELS.put("Arrokuda Scales", "Escama de Arrokuda");
        LABELS.put("Articuno Candy", "Caramelo Articuno");
        LABELS.put("As One", "Unidad Ecuestre");
        LABELS.put("Aspear Berry", "Baya Perasi");
    }
    private static void labels1() {
        LABELS.put("Assault Vest", "Chaleco Asalto");
        LABELS.put("Assist", "Ayuda");
        LABELS.put("Assurance", "Buena Baza");
        LABELS.put("Astonish", "Impresionar");
        LABELS.put("Astral Barrage", "Orbes Espectro");
        LABELS.put("Attack Order", "Al Ataque");
        LABELS.put("Attract", "Atracción");
        LABELS.put("Audinite", "Audinita");
        LABELS.put("Aura Break", "Rompeaura");
        LABELS.put("Aura Sphere", "Esfera Aural");
        LABELS.put("Aura Wheel", "Rueda Aural");
        LABELS.put("Aurora Beam", "Rayo Aurora");
        LABELS.put("Aurora Veil", "Velo Aurora");
        LABELS.put("AuroraTicket", "Ori-Ticket");
        LABELS.put("Auspicious Armor", "Armadura Auspiciosa");
        LABELS.put("Autograph", "Autógrafo");
        LABELS.put("Autotomize", "Aligerar");
        LABELS.put("Avalanche", "Alud");
        LABELS.put("Avocado", "Aguacate");
        LABELS.put("Awakening", "Despertar");
        LABELS.put("Axe Kick", "Patada Hacha");
        LABELS.put("Axew Scales", "Escama de Axew");
        LABELS.put("Azure Flute", "Flauta Azur");
        LABELS.put("Azurill Fur", "Pelo de Azurill");
        LABELS.put("B&W Grass Tablecloth", "Mantel Hierba Alta");
        LABELS.put("Babiri Berry", "Baya Baribá");
        LABELS.put("Baby-Doll Eyes", "Ojitos Tiernos");
        LABELS.put("Bach’s Food Tin", "Lata de Bach");
        LABELS.put("Bacon", "Panceta");
        LABELS.put("Bad Dreams", "Mal Sueño");
        LABELS.put("Baddy Bad", "Umbreozona");
        LABELS.put("Bagon Scales", "Escama de Bagon");
        LABELS.put("Baguette", "Barra de Pan");
        LABELS.put("Ball Fetch", "Recogebolas");
        LABELS.put("Balm Mushroom", "Seta Aroma");
        LABELS.put("Banana", "Plátano");
        LABELS.put("Band Autograph", "Autógrafo Grupo");
        LABELS.put("Baneful Bunker", "Búnker");
        LABELS.put("Banettite", "Banettita");
        LABELS.put("Barb Barrage", "Mil Púas Tóxicas");
        LABELS.put("Barbaracite", "Barbaraclita");
        LABELS.put("Barboach Slime", "Secreción de Barboach");
        LABELS.put("Barrage", "Bombardeo");
        LABELS.put("Barred Cup", "Taza Raya Horizontal");
        LABELS.put("Barrier", "Barrera");
        LABELS.put("Basculin Fang", "Colmillo de Basculin");
        LABELS.put("Bashful", "Tímida");
        LABELS.put("Basil", "Albahaca");
        LABELS.put("Baton Pass", "Relevo");
        LABELS.put("Battery", "Batería");
        LABELS.put("Battle Armor", "Armadura Batalla");
        LABELS.put("Battle Bond", "Fuerte Afecto");
        LABELS.put("Battle Pocket", "Bolsillo Combate");
        LABELS.put("Battle Tablecloth", "Mantel Combate");
        LABELS.put("Baxcalibrite", "Baxcaliburita");
        LABELS.put("Beach Glass", "Cristal Marino");
        LABELS.put("Bead Mail", "Carta Imagen");
        LABELS.put("Beads of Ruin", "Abalorio Debacle");
        LABELS.put("Beak Blast", "Pico Cañón");
        LABELS.put("Beast Ball", "Ente Ball");
        LABELS.put("Beast Boost", "Ultraimpulso");
        LABELS.put("Beat Up", "Paliza");
        LABELS.put("Beedrillite", "Beedrillita");
        LABELS.put("Behemoth Bash", "Embate Supremo");
        LABELS.put("Behemoth Blade", "Tajo Supremo");
        LABELS.put("Belch", "Eructo");
        LABELS.put("Bellsprout Candy", "Caramelo Bellsprout");
        LABELS.put("Bellsprout Vine", "Liana de Bellsprout");
        LABELS.put("Belly Drum", "Tambor");
        LABELS.put("Belue Berry", "Baya Andano");
        LABELS.put("Bergmite Ice", "Hielo de Bergmite");
        LABELS.put("Berry Juice", "Zumo de Baya");
        LABELS.put("Berry Pots", "Plantabayas");
        LABELS.put("Berry Pouch", "Saco Bayas");
        LABELS.put("Berry Sweet", "Confite Fruto");
        LABELS.put("Berserk", "Cólera");
        LABELS.put("Bestow", "Ofrenda");
        LABELS.put("Bicycle", "Bici");
        LABELS.put("Bide", "Venganza");
        LABELS.put("Big Bamboo Shoot", "Bambú Grande");
        LABELS.put("Big Malasada", "Malasada Maxi");
        LABELS.put("Big Mushroom", "Seta Grande");
        LABELS.put("Big Nugget", "Maxipepita");
        LABELS.put("Big Pearl", "Perla Grande");
        LABELS.put("Big Pecks", "Sacapecho");
        LABELS.put("Big Root", "Raíz Grande");
        LABELS.put("Bike", "Bici");
        LABELS.put("Bike Voucher", "Bono Bici");
        LABELS.put("Bind", "Atadura");
        LABELS.put("Binding Band", "Banda Atadura");
    }
    private static void labels2() {
        LABELS.put("Bite", "Mordisco");
        LABELS.put("Bitter Blade", "Espada Lamento");
        LABELS.put("Bitter Herba Mystica", "Especia Oculta Amarga");
        LABELS.put("Bitter Malice", "Rencor Reprimido");
        LABELS.put("Black Apricorn", "Bonguri Negro");
        LABELS.put("Black Augurite", "Mineral Negro");
        LABELS.put("Black Belt", "Cinturón Negro");
        LABELS.put("Black Flute", "Flauta Negra");
        LABELS.put("Black Glasses", "Gafas de Sol");
        LABELS.put("Black Hole Eclipse", "Agujero Negro Aniquilador");
        LABELS.put("Black Mane Hair", "Crin Negra");
        LABELS.put("Black Sludge", "Lodo Negro");
        LABELS.put("Blank Plate", "Tabla Neutra");
        LABELS.put("Blast Burn", "Anillo Ígneo");
        LABELS.put("Blastoisinite", "Blastoisita");
        LABELS.put("Blaze", "Mar Llamas");
        LABELS.put("Blaze Kick", "Patada Ígnea");
        LABELS.put("Blazikenite", "Blazikenita");
        LABELS.put("Blazing Torque", "Pirochoque");
        LABELS.put("Bleakwind Storm", "Vendaval Gélido");
        LABELS.put("Blizzard", "Ventisca");
        LABELS.put("Block", "Bloqueo");
        LABELS.put("Blood Moon", "Luna Roja");
        LABELS.put("Bloom Doom", "Megatón Floral");
        LABELS.put("Bloom Mail", "Carta Flores");
        LABELS.put("Blue Apricorn", "Bonguri Azul");
        LABELS.put("Blue Bottle", "Termo Azul");
        LABELS.put("Blue Card", "Tarjeta Azul");
        LABELS.put("Blue Cup", "Taza Azul");
        LABELS.put("Blue Dish", "Plato Azul");
        LABELS.put("Blue Flare", "Llama Azul");
        LABELS.put("Blue Flute", "Flauta Azul");
        LABELS.put("Blue Orb", "Prisma Azul");
        LABELS.put("Blue Petal", "Pétalo Azul");
        LABELS.put("Blue Poké Ball Pick", "Palillo Ball Azul");
        LABELS.put("Blue Scarf", "Pañuelo Azul");
        LABELS.put("Blue Shard", "Parte Azul");
        LABELS.put("Blue Tablecloth", "Mantel Azul");
        LABELS.put("Blue-Flag Pick", "Palillo Bandera Azul");
        LABELS.put("Blue-Sky Flower Pick", "Palillo Flor Azul");
        LABELS.put("Bluk Berry", "Baya Oram");
        LABELS.put("Blunder Policy", "Seguro Fallo");
        LABELS.put("Bob’s Food Tin", "Lata de Darren");
        LABELS.put("Body Press", "Plancha Corporal");
        LABELS.put("Body Slam", "Golpe Cuerpo");
        LABELS.put("Boiled Egg", "Huevo Duro");
        LABELS.put("Bold", "Osada");
        LABELS.put("Bold Mint", "Menta Osada");
        LABELS.put("Bolt Beak", "Electropico");
        LABELS.put("Bolt Strike", "Ataque Fulgor");
        LABELS.put("Bombirdier Feather", "Pluma de Bombirdier");
        LABELS.put("Bone Club", "Hueso Palo");
        LABELS.put("Bone Rush", "Ataque Óseo");
        LABELS.put("Bonemerang", "Huesomerang");
        LABELS.put("Bonsly Tears", "Lágrima de Bonsly");
        LABELS.put("Boomburst", "Estruendo");
        LABELS.put("Boost Mulch", "Abono Fructífero");
        LABELS.put("Booster Energy", "Energía Potenciadora");
        LABELS.put("Bottle Cap", "Chapa Plateada");
        LABELS.put("Bounce", "Bote");
        LABELS.put("Bouncy Bubble", "Vapodrenaje");
        LABELS.put("Bounsweet Sweat", "Sudor de Bounsweet");
        LABELS.put("Bramblin Twig", "Rama de Bramblin");
        LABELS.put("Branch Poke", "Punzada Rama");
        LABELS.put("Brave", "Audaz");
        LABELS.put("Brave Bird", "Pájaro Osado");
        LABELS.put("Brave Mint", "Menta Audaz");
        LABELS.put("Bread", "Pan de Molde");
        LABELS.put("Breaking Swipe", "Vasto Impacto");
        LABELS.put("Breakneck Blitz", "Carrera Arrolladora");
        LABELS.put("Brick Break", "Demolición");
        LABELS.put("Brick Mail", "Carta Pared");
        LABELS.put("Bridge Mail D", "Carta Puente F");
        LABELS.put("Bridge Mail M", "Carta Puente P");
        LABELS.put("Bridge Mail S", "Carta Puente S");
        LABELS.put("Bridge Mail T", "Carta Puente A");
        LABELS.put("Bridge Mail V", "Carta Puente V");
        LABELS.put("Bright Powder", "Polvo Brillo");
        LABELS.put("Brine", "Salmuera");
        LABELS.put("Brittle Bones", "Huesos Finos");
        LABELS.put("Bronze Bottle", "Termo Bronceado");
        LABELS.put("Bronze Cup", "Taza Bronceada");
        LABELS.put("Bronzor Fragment", "Fragmento de Bronzor");
        LABELS.put("Brutal Swing", "Giro Vil");
        LABELS.put("Bruxish Tooth", "Diente de Bruxish");
        LABELS.put("Bubble", "Burbuja");
        LABELS.put("Bubble Beam", "Rayo Burbuja");
        LABELS.put("Bubble Mail", "Carta Pompas");
        LABELS.put("Bug", "Bicho");
        LABELS.put("Bug Bite", "Picadura");
    }
    private static void labels3() {
        LABELS.put("Bug Buzz", "Zumbido");
        LABELS.put("Bug Gem", "Gema Bicho");
        LABELS.put("Bug Memory", "Disco Bicho");
        LABELS.put("Bug Tera Shard", "Teralito Bicho");
        LABELS.put("Buginium Z", "Insectostal Z");
        LABELS.put("Buizel Fur", "Pelo de Buizel");
        LABELS.put("Bulbasaur Candy", "Caramelo Bulbasaur");
        LABELS.put("Bulk Up", "Corpulencia");
        LABELS.put("Bulldoze", "Terratemblor");
        LABELS.put("Bullet Punch", "Puño Bala");
        LABELS.put("Bullet Seed", "Semilladora");
        LABELS.put("Bulletproof", "Antibalas");
        LABELS.put("Burn Drive", "PiroROM");
        LABELS.put("Burn Heal", "Antiquemar");
        LABELS.put("Burn Up", "Llama Final");
        LABELS.put("Burning Bulwark", "Llama Protectora");
        LABELS.put("Burning Jealousy", "Envidia Ardiente");
        LABELS.put("Butter", "Mantequilla");
        LABELS.put("Buzzy Buzz", "Joltioparálisis");
        LABELS.put("Cacnea Needle", "Espina de Cacnea");
        LABELS.put("Calcium", "Calcio");
        LABELS.put("Calm", "Serena");
        LABELS.put("Calm Mind", "Paz Mental");
        LABELS.put("Calm Mint", "Menta Serena");
        LABELS.put("Cameruptite", "Cameruptita");
        LABELS.put("Camouflage", "Camuflaje");
        LABELS.put("Camping Gear", "Kit de Acampada");
        LABELS.put("Candy Jar", "Tarro de Caramelos");
        LABELS.put("Capsakid Seed", "Semilla de Capsakid");
        LABELS.put("Captivate", "Seducción");
        LABELS.put("Carbink Jewel", "Gema de Carbink");
        LABELS.put("Carbos", "Carburante");
        LABELS.put("Card Key", "Llave Magnética");
        LABELS.put("Careful", "Cauta");
        LABELS.put("Careful Mint", "Menta Cauta");
        LABELS.put("Carrot Seeds", "Sem. Zanahoria");
        LABELS.put("Casteliacone", "Porcehelado");
        LABELS.put("Catastropika", "Pikavoltio Letal");
        LABELS.put("Catching Charm", "Amuleto Captura");
        LABELS.put("Catching Pocket", "Bolsillo Captura");
        LABELS.put("Caterpie Candy", "Caramelo Caterpie");
        LABELS.put("Ceaseless Edge", "Tajo Metralla");
        LABELS.put("Celebrate", "Celebración");
        LABELS.put("Cell Battery", "Pila");
        LABELS.put("Cetoddle Grease", "Grasa de Cetoddle");
        LABELS.put("Chalky Stone", "Tiza");
        LABELS.put("Chandelurite", "Chandelurita");
        LABELS.put("Chansey Candy", "Caramelo Chansey");
        LABELS.put("Charcadet Soot", "Hollín de Charcadet");
        LABELS.put("Charcoal", "Carbón");
        LABELS.put("Charge", "Carga");
        LABELS.put("Charge Beam", "Rayo Carga");
        LABELS.put("Charizardite X", "Charizardita X");
        LABELS.put("Charizardite Y", "Charizardita Y");
        LABELS.put("Charm", "Encanto");
        LABELS.put("Charmander Candy", "Caramelo Charmander");
        LABELS.put("Charti Berry", "Baya Alcho");
        LABELS.put("Chatter", "Cháchara");
        LABELS.put("Cheek Pouch", "Carrillo");
        LABELS.put("Cheese", "Queso");
        LABELS.put("Cheri Berry", "Baya Zreza");
        LABELS.put("Cherish Ball", "Gloria Ball");
        LABELS.put("Cherry Tomatoes", "Tomate Cherri");
        LABELS.put("Chesnaughtite", "Chesnaughtita");
        LABELS.put("Chesto Berry", "Baya Atania");
        LABELS.put("Chewtle Claw", "Garra de Chewtle");
        LABELS.put("Chilan Berry", "Baya Chilan");
        LABELS.put("Chili Sauce", "Salsa Picante");
        LABELS.put("Chill Drive", "CrioROM");
        LABELS.put("Chilling Neigh", "Relincho Blanco");
        LABELS.put("Chilling Water", "Agua Fría");
        LABELS.put("Chilly Reception", "Fría Acogida");
        LABELS.put("Chimechite", "Chimechita");
        LABELS.put("Chingling Fragment", "Fragmento de Chingling");
        LABELS.put("Chip Away", "Guardia Baja");
        LABELS.put("Chipped Pot", "Tetera Rota");
        LABELS.put("Chloroblast", "Clorofiláser");
        LABELS.put("Chlorophyll", "Clorofila");
        LABELS.put("Choice Band", "Cinta Elección");
        LABELS.put("Choice Scarf", "Pañuelo Elección");
        LABELS.put("Choice Specs", "Gafas Elección");
        LABELS.put("Chople Berry", "Baya Pomaro");
        LABELS.put("Chorizo", "Chorizo");
        LABELS.put("Circle Throw", "Llave Giro");
        LABELS.put("Clamp", "Tenaza");
        LABELS.put("Clanging Scales", "Fragor Escamas");
        LABELS.put("Clangorous Soul", "Estruendo Escama");
        LABELS.put("Clangorous Soulblaze", "Estruendo Implacable");
        LABELS.put("Clauncher Claw", "Pinza de Clauncher");
        LABELS.put("Claw Fossil", "Fósil Garra");
    }
    private static void labels4() {
        LABELS.put("Cleanse Tag", "Amuleto");
        LABELS.put("Clear Amulet", "Amuleto Puro");
        LABELS.put("Clear Bell", "Cascabel Claro");
        LABELS.put("Clear Body", "Cuerpo Puro");
        LABELS.put("Clear Smog", "Niebla Clara");
        LABELS.put("Clefablite", "Clefablita");
        LABELS.put("Clefairy Candy", "Caramelo Clefairy");
        LABELS.put("Cleffa Fur", "Pelo de Cleffa");
        LABELS.put("Clever Feather", "Pluma Mente");
        LABELS.put("Clever Mochi", "Mochi Mente");
        LABELS.put("Close Combat", "A Bocajarro");
        LABELS.put("Clothing Trunk", "Maleta");
        LABELS.put("Cloud Nine", "Aclimatación");
        LABELS.put("Clover Sweet", "Confite Trébol");
        LABELS.put("Coaching", "Motivación");
        LABELS.put("Coba Berry", "Baya Kouba");
        LABELS.put("Coconut Milk", "Leche de Coco");
        LABELS.put("Coil", "Enrosque");
        LABELS.put("Coin Case", "Monedero");
        LABELS.put("Colbur Berry", "Baya Dillo");
        LABELS.put("Collision Course", "Nitrochoque");
        LABELS.put("Color Change", "Cambio Color");
        LABELS.put("Colress Machine", "Acromáquina");
        LABELS.put("Comatose", "Letargo Perenne");
        LABELS.put("Combat Torque", "Pugnachoque");
        LABELS.put("Combee Honey", "Miel de Combee");
        LABELS.put("Comet Punch", "Puño Cometa");
        LABELS.put("Comet Shard", "Fragmento Cometa");
        LABELS.put("Comeuppance", "Resarcimiento");
        LABELS.put("Commander", "Comandar");
        LABELS.put("Common Stone", "Piedra Común");
        LABELS.put("Competitive", "Tenacidad");
        LABELS.put("Compound Eyes", "Ojo Compuesto");
        LABELS.put("Confide", "Confidencia");
        LABELS.put("Confuse Ray", "Rayo Confuso");
        LABELS.put("Confusion", "Confusión");
        LABELS.put("Constrict", "Restricción");
        LABELS.put("Contest Pass", "Pase Concurso");
        LABELS.put("Continental Crush", "Aplastamiento Gigalítico");
        LABELS.put("Contrary", "Respondón");
        LABELS.put("Conversion", "Conversión");
        LABELS.put("Conversion 2", "Conversión 2");
        LABELS.put("Copycat", "Copión");
        LABELS.put("Core Enforcer", "Núcleo Castigo");
        LABELS.put("Corkscrew Crash", "Hélice Trepanadora");
        LABELS.put("Cornerstone Mask", "Máscara Cimiento");
        LABELS.put("Cornn Berry", "Baya Mais");
        LABELS.put("Corphish Shell", "Cáscara de Corphish");
        LABELS.put("Corrosion", "Corrosión");
        LABELS.put("Corrosive Gas", "Gas Corrosivo");
        LABELS.put("Cosmic Power", "Masa Cósmica");
        LABELS.put("Costar", "Unísono");
        LABELS.put("Cotton Down", "Pelusa");
        LABELS.put("Cotton Guard", "Rizo Algodón");
        LABELS.put("Cotton Spore", "Esporagodón");
        LABELS.put("Counter", "Contraataque");
        LABELS.put("Coupon 1", "Cupón 1");
        LABELS.put("Coupon 2", "Cupón 2");
        LABELS.put("Coupon 3", "Cupón 3");
        LABELS.put("Courage Candy", "Caramelo Mente");
        LABELS.put("Courage Candy L", "Caramelo Mente +");
        LABELS.put("Courage Candy XL", "Caramelo Mente ++");
        LABELS.put("Court Change", "Cambio de Cancha");
        LABELS.put("Cover Fossil", "Fósil Tapa");
        LABELS.put("Covert Cloak", "Capa Furtiva");
        LABELS.put("Covet", "Antojo");
        LABELS.put("Crabhammer", "Martillazo");
        LABELS.put("Crabominite", "Crabominablita");
        LABELS.put("Crabrawler Shell", "Cáscara de Crabrawler");
        LABELS.put("Cracked Pot", "Tetera Agrietada");
        LABELS.put("Crafty Shield", "Truco Defensa");
        LABELS.put("Cramorant Down", "Plumón de Cramorant");
        LABELS.put("Cream Cheese", "Queso Crema");
        LABELS.put("Croagunk Poison", "Toxinas de Croagunk");
        LABELS.put("Cross Chop", "Tajo Cruzado");
        LABELS.put("Cross Poison", "Veneno X");
        LABELS.put("Crown Pass", "Pase Corona");
        LABELS.put("Crunch", "Triturar");
        LABELS.put("Crush Claw", "Garra Brutal");
        LABELS.put("Crush Grip", "Agarrón");
        LABELS.put("Cryogonal Ice", "Hielo de Cryogonal");
        LABELS.put("Crystal Cluster", "Fragmento de Cristal");
        LABELS.put("Cubchoo Fur", "Pelo de Cubchoo");
        LABELS.put("Cubone Candy", "Caramelo Cubone");
        LABELS.put("Cucumber", "Pepino");
        LABELS.put("Cud Chew", "Rumia");
        LABELS.put("Cufant Tarnish", "Óxido de Cufant");
        LABELS.put("Curious Medicine", "Medicina Extraña");
        LABELS.put("Curry Powder", "Curri en Polvo");
        LABELS.put("Curse", "Maldición");
    }
    private static void labels5() {
        LABELS.put("Cursed Body", "Cuerpo Maldito");
        LABELS.put("Custap Berry", "Baya Chiri");
        LABELS.put("Cut", "Corte");
        LABELS.put("Cute Charm", "Gran Encanto");
        LABELS.put("Cutiefly Powder", "Polvo de Cutiefly");
        LABELS.put("Cyber Ball", "Pelota Cibernética");
        LABELS.put("Cyclizar Scales", "Escama de Cyclizar");
        LABELS.put("DNA Splicers", "Punta ADN");
        LABELS.put("Damp", "Humedad");
        LABELS.put("Damp Mulch", "Abono Lento");
        LABELS.put("Damp Rock", "Roca Lluvia");
        LABELS.put("Dancer", "Pareja de Baile");
        LABELS.put("Dark", "Siniestro");
        LABELS.put("Dark Aura", "Aura Oscura");
        LABELS.put("Dark Gem", "Gema Siniestra");
        LABELS.put("Dark Memory", "Disco Siniestro");
        LABELS.put("Dark Pulse", "Pulso Umbrío");
        LABELS.put("Dark Stone", "Orbe Oscuro");
        LABELS.put("Dark Tera Shard", "Teralito Siniestro");
        LABELS.put("Dark Void", "Brecha Negra");
        LABELS.put("Darkest Lariat", "Lariat Oscuro");
        LABELS.put("Darkinium Z", "Nictostal Z");
        LABELS.put("Darkranite", "Darkrainita");
        LABELS.put("Data Card 01", "Tarjeta Datos 01");
        LABELS.put("Data Card 02", "Tarjeta Datos 02");
        LABELS.put("Data Card 03", "Tarjeta Datos 03");
        LABELS.put("Data Card 04", "Tarjeta Datos 04");
        LABELS.put("Data Card 05", "Tarjeta Datos 05");
        LABELS.put("Data Card 06", "Tarjeta Datos 06");
        LABELS.put("Data Card 07", "Tarjeta Datos 07");
        LABELS.put("Data Card 08", "Tarjeta Datos 08");
        LABELS.put("Data Card 09", "Tarjeta Datos 09");
        LABELS.put("Data Card 10", "Tarjeta Datos 10");
        LABELS.put("Data Card 11", "Tarjeta Datos 11");
        LABELS.put("Data Card 12", "Tarjeta Datos 12");
        LABELS.put("Data Card 13", "Tarjeta Datos 13");
        LABELS.put("Data Card 14", "Tarjeta Datos 14");
        LABELS.put("Data Card 15", "Tarjeta Datos 15");
        LABELS.put("Data Card 16", "Tarjeta Datos 16");
        LABELS.put("Data Card 17", "Tarjeta Datos 17");
        LABELS.put("Data Card 18", "Tarjeta Datos 18");
        LABELS.put("Data Card 19", "Tarjeta Datos 19");
        LABELS.put("Data Card 20", "Tarjeta Datos 20");
        LABELS.put("Data Card 21", "Tarjeta Datos 21");
        LABELS.put("Data Card 22", "Tarjeta Datos 22");
        LABELS.put("Data Card 23", "Tarjeta Datos 23");
        LABELS.put("Data Card 24", "Tarjeta Datos 24");
        LABELS.put("Data Card 25", "Tarjeta Datos 25");
        LABELS.put("Data Card 26", "Tarjeta Datos 26");
        LABELS.put("Data Card 27", "Tarjeta Datos 27");
        LABELS.put("Dauntless Shield", "Escudo Recio");
        LABELS.put("Dawn Stone", "Piedra Alba");
        LABELS.put("Dazzling", "Cuerpo Vívido");
        LABELS.put("Dazzling Gleam", "Brillo Mágico");
        LABELS.put("Decidium Z", "Dueyestal Z");
        LABELS.put("Decorate", "Decoración");
        LABELS.put("Dedenne Fur", "Pelo de Dedenne");
        LABELS.put("Deep Sea Scale", "Escama Marina");
        LABELS.put("Deep Sea Tooth", "Diente Marino");
        LABELS.put("Deerling Hair", "Pelo de Deerling");
        LABELS.put("Defeatist", "Flaqueza");
        LABELS.put("Defend Order", "A Defender");
        LABELS.put("Defense Curl", "Rizo Defensa");
        LABELS.put("Defiant", "Competitivo");
        LABELS.put("Defog", "Despejar");
        LABELS.put("Deino Scales", "Escama de Deino");
        LABELS.put("Delibird Parcel", "Paquete de Delibird");
        LABELS.put("Delphoxite", "Delphoxita");
        LABELS.put("Delta Stream", "Ráfaga Delta");
        LABELS.put("Desolate Land", "Tierra del Ocaso");
        LABELS.put("Destiny Bond", "Mismo Destino");
        LABELS.put("Destiny Knot", "Lazo Destino");
        LABELS.put("Detect", "Detección");
        LABELS.put("Devastating Drake", "Dracoaliento Devastador");
        LABELS.put("Devon Goods", "Piezas Devon");
        LABELS.put("Devon Parts", "Piezas Devon");
        LABELS.put("Devon Scope", "Detector Devon");
        LABELS.put("Devon Scuba Gear", "Bombona Devon");
        LABELS.put("Diamond Bottle", "Termo Rombos");
        LABELS.put("Diamond Pattern Cup", "Taza Rombos");
        LABELS.put("Diamond Storm", "Tormenta de Diamantes");
        LABELS.put("Diamond Tablecloth", "Mantel Rombos");
        LABELS.put("Diancite", "Diancita");
        LABELS.put("Dig", "Excavar");
        LABELS.put("Diglett Candy", "Caramelo Diglett");
        LABELS.put("Diglett Dirt", "Tierra de Diglett");
        LABELS.put("Dire Claw", "Garra Nociva");
        LABELS.put("Dire Hit", "Crítico X");
        LABELS.put("Dire Hit 2", "Crítico X 2");
        LABELS.put("Dire Hit 3", "Crítico X 3");
    }
    private static void labels6() {
        LABELS.put("Disable", "Anulación");
        LABELS.put("Disarming Voice", "Voz Cautivadora");
        LABELS.put("Discharge", "Chispazo");
        LABELS.put("Discount Coupon", "Vale Descuento");
        LABELS.put("Disguise", "Disfraz");
        LABELS.put("Ditto Candy", "Caramelo Ditto");
        LABELS.put("Ditto Goo", "Pegote de Ditto");
        LABELS.put("Dive", "Buceo");
        LABELS.put("Dive Ball", "Buceo Ball");
        LABELS.put("Dizzy Punch", "Puño Mareo");
        LABELS.put("Docile", "Dócil");
        LABELS.put("Doduo Candy", "Caramelo Doduo");
        LABELS.put("Dome Fossil", "Fósil Domo");
        LABELS.put("Dondozo Whisker", "Bigote de Dondozo");
        LABELS.put("Doodle", "Decalcomanía");
        LABELS.put("Doom Desire", "Deseo Oculto");
        LABELS.put("Double Hit", "Doble Golpe");
        LABELS.put("Double Iron Bash", "Ferropuño Doble");
        LABELS.put("Double Kick", "Doble Patada");
        LABELS.put("Double Shock", "Electropalmas");
        LABELS.put("Double Slap", "Doble Bofetón");
        LABELS.put("Double Team", "Doble Equipo");
        LABELS.put("Double-Edge", "Doble Filo");
        LABELS.put("Douse Drive", "HidroROM");
        LABELS.put("Download", "Descarga");
        LABELS.put("Dowsing Machine", "Zahorí");
        LABELS.put("Draco Meteor", "Cometa Draco");
        LABELS.put("Draco Plate", "Tabla Draco");
        LABELS.put("Dragalgite", "Dragalgita");
        LABELS.put("Dragon", "Dragón");
        LABELS.put("Dragon Ascent", "Ascenso Draco");
        LABELS.put("Dragon Breath", "Dragoaliento");
        LABELS.put("Dragon Cheer", "Bramido Dragón");
        LABELS.put("Dragon Claw", "Garra Dragón");
        LABELS.put("Dragon Dance", "Danza Dragón");
        LABELS.put("Dragon Darts", "Dracoflechas");
        LABELS.put("Dragon Energy", "Dracoenergía");
        LABELS.put("Dragon Fang", "Colmillo de Dragón");
        LABELS.put("Dragon Gem", "Gema Dragón");
        LABELS.put("Dragon Hammer", "Martillo Dragón");
        LABELS.put("Dragon Memory", "Disco Dragón");
        LABELS.put("Dragon Pulse", "Pulso Dragón");
        LABELS.put("Dragon Rage", "Furia Dragón");
        LABELS.put("Dragon Rush", "Carga Dragón");
        LABELS.put("Dragon Scale", "Escama Dragón");
        LABELS.put("Dragon Skull", "Cráneo Dragón");
        LABELS.put("Dragon Tail", "Cola Dragón");
        LABELS.put("Dragon Tera Shard", "Teralito Dragón");
        LABELS.put("Dragoninite", "Dragonitita");
        LABELS.put("Dragonium Z", "Dracostal Z");
        LABELS.put("Dragonize", "Piel Dragontina");
        LABELS.put("Dragon’s Maw", "Mandíbula Dragón");
        LABELS.put("Drain Punch", "Puño Drenaje");
        LABELS.put("Draining Kiss", "Beso Drenaje");
        LABELS.put("Drampanite", "Drampanita");
        LABELS.put("Dratini Candy", "Caramelo Dratini");
        LABELS.put("Dratini Scales", "Escama de Dratini");
        LABELS.put("Dread Plate", "Tabla Oscura");
        LABELS.put("Dream Ball", "Ensueño Ball");
        LABELS.put("Dream Eater", "Comesueños");
        LABELS.put("Dream Mail", "Carta Sueño");
        LABELS.put("Dreepy Powder", "Polvo de Dreepy");
        LABELS.put("Drifloon Gas", "Gas de Drifloon");
        LABELS.put("Drill Peck", "Pico Taladro");
        LABELS.put("Drill Run", "Taladradora");
        LABELS.put("Drizzle", "Llovizna");
        LABELS.put("Dropped Item", "Objeto Perdido");
        LABELS.put("Drought", "Sequía");
        LABELS.put("Drowzee Candy", "Caramelo Drowzee");
        LABELS.put("Drowzee Fur", "Pelo de Drowzee");
        LABELS.put("Drum Beating", "Batería Asalto");
        LABELS.put("Dry Skin", "Piel Seca");
        LABELS.put("Dual Chop", "Golpe Bis");
        LABELS.put("Dual Wingbeat", "Ala Bis");
        LABELS.put("Dubious Disc", "Disco Extraño");
        LABELS.put("Ducklett Feather", "Pluma de Ducklett");
        LABELS.put("Dunsparce Scales", "Escama de Dunsparce");
        LABELS.put("Durin Berry", "Baya Rudion");
        LABELS.put("Dusk Ball", "Ocaso Ball");
        LABELS.put("Dusk Stone", "Piedra Noche");
        LABELS.put("Duskull Fragment", "Fragmento de Duskull");
        LABELS.put("Dynamax Band", "Maximuñequera");
        LABELS.put("Dynamax Candy", "Caramelo Dinamax");
        LABELS.put("Dynamax Cannon", "Cañón Dinamax");
        LABELS.put("Dynamic Punch", "Puño Dinámico");
        LABELS.put("Dynite Ore", "Maxinium");
        LABELS.put("Early Bird", "Madrugar");
        LABELS.put("Earth Eater", "Geofagia");
        LABELS.put("Earth Plate", "Tabla Terrax");
        LABELS.put("Earth Power", "Tierra Viva");
    }
    private static void labels7() {
        LABELS.put("Earthquake", "Terremoto");
        LABELS.put("Echoed Voice", "Eco Voz");
        LABELS.put("Eelektrossite", "Eelektrossita");
        LABELS.put("Eerie Impulse", "Onda Anómala");
        LABELS.put("Eerie Spell", "Conjuro Funesto");
        LABELS.put("Eevee Candy", "Caramelo Eevee");
        LABELS.put("Eevee Cup", "Taza Eevee");
        LABELS.put("Eevee Fur", "Pelo de Eevee");
        LABELS.put("Eevium Z", "Eeveestal Z");
        LABELS.put("Effect Spore", "Efecto Espora");
        LABELS.put("Egg", "Huevo Cocido");
        LABELS.put("Egg Bomb", "Bomba Huevo");
        LABELS.put("Eiscue Down", "Plumón de Eiscue");
        LABELS.put("Eject Button", "Botón Escape");
        LABELS.put("Eject Pack", "Mochila Escape");
        LABELS.put("Ekans Candy", "Caramelo Ekans");
        LABELS.put("Ekans Fang", "Colmillo de Ekans");
        LABELS.put("Electabuzz Candy", "Caramelo Electabuzz");
        LABELS.put("Electirizer", "Electrizador");
        LABELS.put("Electric", "Eléctrico");
        LABELS.put("Electric Gem", "Gema Eléctrica");
        LABELS.put("Electric Memory", "Disco Eléctrico");
        LABELS.put("Electric Seed", "Semilla Electro");
        LABELS.put("Electric Surge", "Electrogénesis");
        LABELS.put("Electric Tera Shard", "Teralito Eléctrico");
        LABELS.put("Electric Terrain", "Campo Eléctrico");
        LABELS.put("Electrify", "Electrificación");
        LABELS.put("Electrium Z", "Electrostal Z");
        LABELS.put("Electro Ball", "Bola Voltio");
        LABELS.put("Electro Drift", "Electroderrape");
        LABELS.put("Electro Shot", "Electrorrayo");
        LABELS.put("Electromorphosis", "Dinamo");
        LABELS.put("Electroweb", "Electrotela");
        LABELS.put("Elevator Key", "Llave Ascensor");
        LABELS.put("Elixir", "Elixir");
        LABELS.put("Embargo", "Embargo");
        LABELS.put("Ember", "Ascuas");
        LABELS.put("Emboarite", "Emboarita");
        LABELS.put("Embody Aspect", "Evocarrecuerdos");
        LABELS.put("Emergency Exit", "Retirada");
        LABELS.put("Encore", "Otra Vez");
        LABELS.put("Endeavor", "Esfuerzo");
        LABELS.put("Endorsement", "Recomendación");
        LABELS.put("Endure", "Aguante");
        LABELS.put("Energy Ball", "Energibola");
        LABELS.put("Energy Powder", "Polvo Energía");
        LABELS.put("Energy Root", "Raíz Energía");
        LABELS.put("Enigma Berry", "Baya Enigma");
        LABELS.put("Enigma Stone", "Misticristal");
        LABELS.put("Enigmatic Card", "Nota Intrigante");
        LABELS.put("Entrainment", "Danza Amiga");
        LABELS.put("Eon Flute", "Flauta Eón");
        LABELS.put("Eon Ticket", "Ticket Eón");
        LABELS.put("Eruption", "Estallido");
        LABELS.put("Escape Rope", "Cuerda Huida");
        LABELS.put("Esper Wing", "Ala Aural");
        LABELS.put("Eternabeam", "Rayo Infinito");
        LABELS.put("Ether", "Éter");
        LABELS.put("Everstone", "Piedra Eterna");
        LABELS.put("Eviolite", "Mineral Evol");
        LABELS.put("Excadrite", "Excadrillita");
        LABELS.put("Exeggcute Candy", "Caramelo Exeggcute");
        LABELS.put("Exercise Ball", "Pelota Pilates");
        LABELS.put("Exp. Candy L", "Caramelo Exp. L");
        LABELS.put("Exp. Candy M", "Caramelo Exp. M");
        LABELS.put("Exp. Candy S", "Caramelo Exp. S");
        LABELS.put("Exp. Candy XL", "Caramelo Exp. XL");
        LABELS.put("Exp. Candy XS", "Caramelo Exp. XS");
        LABELS.put("Exp. Charm", "Amuleto Exp");
        LABELS.put("Exp. Share", "Repartir Exp");
        LABELS.put("Expanding Force", "Vasta Fuerza");
        LABELS.put("Expert Belt", "Cinta Experto");
        LABELS.put("Explorer Kit", "Kit Explorador");
        LABELS.put("Explosion", "Explosión");
        LABELS.put("Extrasensory", "Paranormal");
        LABELS.put("Extreme Evoboost", "Novena Potencia");
        LABELS.put("Extreme Speed", "Velocidad Extrema");
        LABELS.put("Fab Mail", "Carta Fab.");
        LABELS.put("Facade", "Fachada");
        LABELS.put("Fairium Z", "Feeristal Z");
        LABELS.put("Fairy", "Hada");
        LABELS.put("Fairy Aura", "Aura Feérica");
        LABELS.put("Fairy Feather", "Pluma Feérica");
        LABELS.put("Fairy Gem", "Gema Hada");
        LABELS.put("Fairy Lock", "Cerrojo Feérico");
        LABELS.put("Fairy Memory", "Disco Hada");
        LABELS.put("Fairy Tera Shard", "Teralito Hada");
        LABELS.put("Fairy Wind", "Viento Feérico");
        LABELS.put("Fake Out", "Sorpresa");
        LABELS.put("Fake Tears", "Llanto Falso");
    }
    private static void labels8() {
        LABELS.put("Falinks Sweat", "Sudor de Falinks");
        LABELS.put("Falinksite", "Falinksita");
        LABELS.put("False Surrender", "Irreverencia");
        LABELS.put("False Swipe", "Falso Tortazo");
        LABELS.put("Fame Checker", "Memorín");
        LABELS.put("Fancy Apple", "Manzana Selecta");
        LABELS.put("Farfetch’d Candy", "Caramelo Farfetch’d");
        LABELS.put("Fashion Case", "Caja Corazón");
        LABELS.put("Fast Ball", "Rapid Ball");
        LABELS.put("Favored Mail", "Carta Favoritos");
        LABELS.put("Feather Ball", "Pluma Ball");
        LABELS.put("Feather Dance", "Danza Pluma");
        LABELS.put("Feebas Scales", "Escama de Feebas");
        LABELS.put("Feint", "Amago");
        LABELS.put("Feint Attack", "Finta");
        LABELS.put("Fell Stinger", "Aguijón Letal");
        LABELS.put("Feraligite", "Feraligatrita");
        LABELS.put("Festival Ticket", "Festicupón");
        LABELS.put("Fickle Beam", "Láser Veleidoso");
        LABELS.put("Fidough Fur", "Pelo de Fidough");
        LABELS.put("Fiery Dance", "Danza Llama");
        LABELS.put("Fiery Wrath", "Furia Candente");
        LABELS.put("Fighting", "Lucha");
        LABELS.put("Fighting Gem", "Gema Lucha");
        LABELS.put("Fighting Memory", "Disco Lucha");
        LABELS.put("Fighting Tera Shard", "Teralito Lucha");
        LABELS.put("Fightinium Z", "Lizastal Z");
        LABELS.put("Figy Berry", "Baya Higog");
        LABELS.put("Fillet Away", "Deslome");
        LABELS.put("Filter", "Filtro");
        LABELS.put("Final Gambit", "Sacrificio");
        LABELS.put("Finizen Mucus", "Secreción de Finizen");
        LABELS.put("Finneon Scales", "Escama de Finneon");
        LABELS.put("Fire", "Fuego");
        LABELS.put("Fire Blast", "Llamarada");
        LABELS.put("Fire Fang", "Colmillo Ígneo");
        LABELS.put("Fire Gem", "Gema Fuego");
        LABELS.put("Fire Lash", "Látigo Ígneo");
        LABELS.put("Fire Memory", "Disco Fuego");
        LABELS.put("Fire Pattern Cup", "Taza Llamas");
        LABELS.put("Fire Pledge", "Voto Fuego");
        LABELS.put("Fire Punch", "Puño Fuego");
        LABELS.put("Fire Spin", "Giro Fuego");
        LABELS.put("Fire Stone", "Piedra Fuego");
        LABELS.put("Fire Tera Shard", "Teralito Fuego");
        LABELS.put("Firium Z", "Pirostal Z");
        LABELS.put("First Impression", "Escaramuza");
        LABELS.put("Fishing Rod", "Caña");
        LABELS.put("Fishious Rend", "Branquibocado");
        LABELS.put("Fissure", "Fisura");
        LABELS.put("Fist Plate", "Tabla Fuerte");
        LABELS.put("Flabébé Pollen", "Polen de Flabébé");
        LABELS.put("Flail", "Azote");
        LABELS.put("Flame Body", "Cuerpo Llama");
        LABELS.put("Flame Burst", "Pirotecnia");
        LABELS.put("Flame Charge", "Nitrocarga");
        LABELS.put("Flame Mail", "Carta Fuego");
        LABELS.put("Flame Orb", "Llamasfera");
        LABELS.put("Flame Plate", "Tabla Llama");
        LABELS.put("Flame Wheel", "Rueda Fuego");
        LABELS.put("Flamethrower", "Lanzallamas");
        LABELS.put("Flamigo Down", "Plumón de Flamigo");
        LABELS.put("Flare Blitz", "Envite Ígneo");
        LABELS.put("Flare Boost", "Ímpetu Ardiente");
        LABELS.put("Flash", "Destello");
        LABELS.put("Flash Cannon", "Cañón Resplandor");
        LABELS.put("Flash Fire", "Absorbe Fuego");
        LABELS.put("Flatter", "Camelo");
        LABELS.put("Fletchling Feather", "Pluma de Fletchling");
        LABELS.put("Fleur Cannon", "Cañón Floral");
        LABELS.put("Fling", "Lanzamiento");
        LABELS.put("Flip Turn", "Viraje");
        LABELS.put("Flittle Down", "Plumón de Flittle");
        LABELS.put("Float Stone", "Piedra Pómez");
        LABELS.put("Floaty Fall", "Pikapicado");
        LABELS.put("Floettite", "Floettita");
        LABELS.put("Floral Healing", "Cura Floral");
        LABELS.put("Flower Gift", "Don Floral");
        LABELS.put("Flower Pattern Cup", "Taza Flores");
        LABELS.put("Flower Shield", "Defensa Floral");
        LABELS.put("Flower Sweet", "Confite Flor");
        LABELS.put("Flower Trick", "Truco Floral");
        LABELS.put("Flower Veil", "Velo Flor");
        LABELS.put("Fluffy", "Peluche");
        LABELS.put("Fluffy Tail", "Cola Skitty");
        LABELS.put("Fly", "Vuelo");
        LABELS.put("Flying", "Volador");
        LABELS.put("Flying Gem", "Gema Voladora");
        LABELS.put("Flying Memory", "Disco Volador");
        LABELS.put("Flying Press", "Plancha Voladora");
    }
    private static void labels9() {
        LABELS.put("Flying Tera Shard", "Teralito Volador");
        LABELS.put("Flyinium Z", "Aerostal Z");
        LABELS.put("Focus Band", "Cinta Aguante");
        LABELS.put("Focus Blast", "Onda Certera");
        LABELS.put("Focus Energy", "Foco Energía");
        LABELS.put("Focus Punch", "Puño Certero");
        LABELS.put("Focus Sash", "Banda Aguante");
        LABELS.put("Follow Me", "Señuelo");
        LABELS.put("Fomantis Leaf", "Hoja de Fomantis");
        LABELS.put("Foongus Spores", "Esporas de Foongus");
        LABELS.put("Forage Bag", "Zurrón");
        LABELS.put("Force Palm", "Palmeo");
        LABELS.put("Forecast", "Predicción");
        LABELS.put("Foresight", "Profecía");
        LABELS.put("Forest’s Curse", "Condena Silvana");
        LABELS.put("Forewarn", "Alerta");
        LABELS.put("Fossilized Bird", "Ornitofósil");
        LABELS.put("Fossilized Dino", "Plesiofósil");
        LABELS.put("Fossilized Drake", "Dracofósil");
        LABELS.put("Fossilized Fish", "Ictiofósil");
        LABELS.put("Foul Play", "Juego Sucio");
        LABELS.put("Freeze Shock", "Rayo Gélido");
        LABELS.put("Freeze-Dry", "Liofilización");
        LABELS.put("Freezing Glare", "Mirada Heladora");
        LABELS.put("Freezy Frost", "Glaceoprisma");
        LABELS.put("Frenzy Plant", "Planta Feroz");
        LABELS.put("Fresh Cream", "Nata Fresca");
        LABELS.put("Fresh Water", "Agua Fresca");
        LABELS.put("Fried Fillet", "Filete Frito");
        LABELS.put("Fried Food", "Frituras");
        LABELS.put("Friend Ball", "Amigo Ball");
        LABELS.put("Friend Guard", "Compiescolta");
        LABELS.put("Frigibax Scales", "Escama de Frigibax");
        LABELS.put("Frisk", "Cacheo");
        LABELS.put("Froslassite", "Froslassita");
        LABELS.put("Frost Breath", "Vaho Gélido");
        LABELS.put("Fruit Bunch", "Fruta Tropical");
        LABELS.put("Frustration", "Frustración");
        LABELS.put("Full Heal", "Cura Total");
        LABELS.put("Full Incense", "Incienso Lento");
        LABELS.put("Full Metal Body", "Guardia Metálica");
        LABELS.put("Full Restore", "Restaurar Todo");
        LABELS.put("Fur Coat", "Pelaje Recio");
        LABELS.put("Fury Attack", "Ataque Furia");
        LABELS.put("Fury Cutter", "Corte Furia");
        LABELS.put("Fury Swipes", "Golpes Furia");
        LABELS.put("Fusion Bolt", "Rayo Fusión");
        LABELS.put("Fusion Flare", "Llama Fusión");
        LABELS.put("Future Sight", "Premonición");
        LABELS.put("GB Sounds", "Lector GB");
        LABELS.put("Galactic Key", "Llave Galaxia");
        LABELS.put("Galarica Cuff", "Brazal Galanuez");
        LABELS.put("Galarica Twig", "Rama de Galanuez");
        LABELS.put("Galarica Wreath", "Corona Galanuez");
        LABELS.put("Gale Wings", "Alas Vendaval");
        LABELS.put("Galladite", "Galladita");
        LABELS.put("Galvanize", "Piel Eléctrica");
        LABELS.put("Ganlon Berry", "Baya Gonlan");
        LABELS.put("Garchompite", "Garchompita");
        LABELS.put("Garchompite Z", "Garchompita Z");
        LABELS.put("Gardevoirite", "Gardevoirita");
        LABELS.put("Gastly Candy", "Caramelo Gastly");
        LABELS.put("Gastly Gas", "Gas de Gastly");
        LABELS.put("Gastro Acid", "Bilis");
        LABELS.put("Gear Grind", "Rueda Doble");
        LABELS.put("Gear Up", "Piñón Auxiliar");
        LABELS.put("Genesis Supernova", "Supernova Original");
        LABELS.put("Gengarite", "Gengarita");
        LABELS.put("Genius Feather", "Pluma Intelecto");
        LABELS.put("Genius Mochi", "Mochi Intelecto");
        LABELS.put("Gentle", "Amable");
        LABELS.put("Gentle Mint", "Menta Amable");
        LABELS.put("Geodude Candy", "Caramelo Geodude");
        LABELS.put("Geodude Fragment", "Fragmento de Geodude");
        LABELS.put("Geomancy", "Geocontrol");
        LABELS.put("Ghost", "Fantasma");
        LABELS.put("Ghost Gem", "Gema Fantasma");
        LABELS.put("Ghost Memory", "Disco Fantasma");
        LABELS.put("Ghost Tera Shard", "Teralito Fantasma");
        LABELS.put("Ghostium Z", "Espectrostal Z");
        LABELS.put("Gible Scales", "Escama de Gible");
        LABELS.put("Giga Drain", "Gigadrenado");
        LABELS.put("Giga Impact", "Gigaimpacto");
        LABELS.put("Gigantamix", "Especias Gigamax");
        LABELS.put("Gigaton Ball", "Quintal Ball");
        LABELS.put("Gigaton Hammer", "Martillo Colosal");
        LABELS.put("Gigavolt Havoc", "Gigavoltio Destructor");
        LABELS.put("Gimmighoul Coin", "Moneda de Gimmighoul");
        LABELS.put("Girafarig Fur", "Pelo de Girafarig");
        LABELS.put("Glacial Lance", "Lanza Glacial");
    }
    private static void labels10() {
        LABELS.put("Glaciate", "Mundo Gélido");
        LABELS.put("Glaive Rush", "Asalto Espadón");
        LABELS.put("Glalitite", "Glalita");
        LABELS.put("Glare", "Deslumbrar");
        LABELS.put("Gligar Fang", "Colmillo de Gligar");
        LABELS.put("Glimmering Charm", "Amuleto Cristalino");
        LABELS.put("Glimmet Crystal", "Cristal de Glimmet");
        LABELS.put("Glimmoranite", "Glimmoranita");
        LABELS.put("Glitter Mail", "Carta Brillo");
        LABELS.put("Glitzy Glow", "Espeaura");
        LABELS.put("Gluttony", "Gula");
        LABELS.put("Go-Goggles", "Gafas Aislantes");
        LABELS.put("Gold Bottle", "Termo Dorado");
        LABELS.put("Gold Bottle Cap", "Chapa Dorada");
        LABELS.put("Gold Cup", "Taza Dorada");
        LABELS.put("Gold Leaf", "Hoja de Oro");
        LABELS.put("Gold Pick", "Palillo Dorado");
        LABELS.put("Gold Teeth", "Dentadura de Oro");
        LABELS.put("Goldeen Candy", "Caramelo Goldeen");
        LABELS.put("Golden Nanab Berry", "Baya Latano Dorada");
        LABELS.put("Golden Pinap Berry", "Baya Pinia Dorada");
        LABELS.put("Golden Razz Berry", "Baya Frambu Dorada");
        LABELS.put("Golisopite", "Golisopodita");
        LABELS.put("Golurkite", "Golurkita");
        LABELS.put("Good Rod", "Caña Buena");
        LABELS.put("Good as Gold", "Cuerpo Áureo");
        LABELS.put("Gooey", "Baba");
        LABELS.put("Gooey Mulch", "Abono Brote");
        LABELS.put("Goomy Goo", "Secreción de Goomy");
        LABELS.put("Gorilla Tactics", "Monotema");
        LABELS.put("Gothita Eyelash", "Pestaña de Gothita");
        LABELS.put("Gracidea", "Gracídea");
        LABELS.put("Gram 1", "Envío 1");
        LABELS.put("Gram 2", "Envío 2");
        LABELS.put("Gram 3", "Envío 3");
        LABELS.put("Grass", "Planta");
        LABELS.put("Grass Gem", "Gema Planta");
        LABELS.put("Grass Knot", "Hierba Lazo");
        LABELS.put("Grass Mail", "Carta Hierba");
        LABELS.put("Grass Memory", "Disco Planta");
        LABELS.put("Grass Pelt", "Manto Frondoso");
        LABELS.put("Grass Pledge", "Voto Planta");
        LABELS.put("Grass Tera Shard", "Teralito Planta");
        LABELS.put("Grass Whistle", "Silbato");
        LABELS.put("Grassium Z", "Fitostal Z");
        LABELS.put("Grassy Glide", "Fitoimpulso");
        LABELS.put("Grassy Seed", "Semilla Hierba");
        LABELS.put("Grassy Surge", "Herbogénesis");
        LABELS.put("Grassy Terrain", "Campo de Hierba");
        LABELS.put("Grav Apple", "Fuerza G");
        LABELS.put("Gravity", "Gravedad");
        LABELS.put("Great Ball", "Super Ball");
        LABELS.put("Greavard Wax", "Cera de Greavard");
        LABELS.put("Green Apricorn", "Bonguri Verde");
        LABELS.put("Green Bell Pepper", "Pimiento Verde");
        LABELS.put("Green Dish", "Plato Verde");
        LABELS.put("Green Petal", "Pétalo Verde");
        LABELS.put("Green Poké Ball Pick", "Palillo Ball Verde");
        LABELS.put("Green Scarf", "Pañuelo Verde");
        LABELS.put("Green Shard", "Parte Verde");
        LABELS.put("Greet Mail", "Carta Inicial");
        LABELS.put("Greninjite", "Greninjanita");
        LABELS.put("Grepa Berry", "Baya Uvav");
        LABELS.put("Grim Neigh", "Relincho Negro");
        LABELS.put("Grimer Candy", "Caramelo Grimer");
        LABELS.put("Grimer Toxin", "Toxinas de Grimer");
        LABELS.put("Grip Claw", "Garra Garfio");
        LABELS.put("Griseous Core", "Gran Griseosfera");
        LABELS.put("Griseous Orb", "Griseosfera");
        LABELS.put("Ground", "Tierra");
        LABELS.put("Ground Gem", "Gema Tierra");
        LABELS.put("Ground Memory", "Disco Tierra");
        LABELS.put("Ground Tera Shard", "Teralito Tierra");
        LABELS.put("Groundium Z", "Geostal Z");
        LABELS.put("Growl", "Gruñido");
        LABELS.put("Growlithe Candy", "Caramelo Growlithe");
        LABELS.put("Growlithe Fur", "Pelo de Growlithe");
        LABELS.put("Growth", "Desarrollo");
        LABELS.put("Growth Mulch", "Abono Rápido");
        LABELS.put("Grubbin Thread", "Hilo de Grubbin");
        LABELS.put("Grubby Hanky", "Pañuelo Sucio");
        LABELS.put("Grudge", "Rabia");
        LABELS.put("Guard Dog", "Perro Guardián");
        LABELS.put("Guard Spec.", "Protección X");
        LABELS.put("Guard Split", "Isoguardia");
        LABELS.put("Guard Swap", "Cambiadefensa");
        LABELS.put("Guardian of Alola", "Cólera del Guardián");
        LABELS.put("Guillotine", "Guillotina");
        LABELS.put("Gulp Missile", "Tragamisil");
        LABELS.put("Gulpin Mucus", "Secreción de Gulpin");
    }
    private static void labels11() {
        LABELS.put("Gunk Shot", "Lanzamugre");
        LABELS.put("Gust", "Tornado");
        LABELS.put("Guts", "Agallas");
        LABELS.put("Gyaradosite", "Gyaradosita");
        LABELS.put("Gyro Ball", "Giro Bola");
        LABELS.put("HM01", "MO01");
        LABELS.put("HM02", "MO02");
        LABELS.put("HM03", "MO03");
        LABELS.put("HM04", "MO04");
        LABELS.put("HM05", "MO05");
        LABELS.put("HM06", "MO06");
        LABELS.put("HM07", "MO07");
        LABELS.put("HM08", "MO08");
        LABELS.put("HP Up", "Más PS");
        LABELS.put("Haban Berry", "Baya Anjiro");
        LABELS.put("Hadron Engine", "Motor Hadrónico");
        LABELS.put("Hail", "Granizo");
        LABELS.put("Ham", "Jamón Cocido");
        LABELS.put("Hamburger", "Hamburguesa");
        LABELS.put("Hammer Arm", "Machada");
        LABELS.put("Happiny Dust", "Pertenencia de Happiny");
        LABELS.put("Happy Hour", "Paga Extra");
        LABELS.put("Harbor Mail", "Carta Puerto");
        LABELS.put("Hard Press", "Prensa Metálica");
        LABELS.put("Hard Stone", "Piedra Dura");
        LABELS.put("Harden", "Fortaleza");
        LABELS.put("Hardy", "Fuerte");
        LABELS.put("Harvest", "Cosecha");
        LABELS.put("Hasty", "Activa");
        LABELS.put("Hasty Mint", "Menta Activa");
        LABELS.put("Hatenna Dust", "Pertenencia de Hatenna");
        LABELS.put("Hawlucha Down", "Plumón de Hawlucha");
        LABELS.put("Hawluchanite", "Hawluchanita");
        LABELS.put("Haze", "Niebla");
        LABELS.put("Head Charge", "Ariete");
        LABELS.put("Head Smash", "Testarazo");
        LABELS.put("Headbutt", "Golpe Cabeza");
        LABELS.put("Headlong Rush", "Arremetida");
        LABELS.put("Heal Ball", "Sana Ball");
        LABELS.put("Heal Bell", "Cascabel Cura");
        LABELS.put("Heal Block", "Anticura");
        LABELS.put("Heal Order", "Auxilio");
        LABELS.put("Heal Powder", "Polvo Curación");
        LABELS.put("Heal Pulse", "Pulso Cura");
        LABELS.put("Healer", "Alma Cura");
        LABELS.put("Healing Wish", "Deseo Cura");
        LABELS.put("Health Candy", "Caramelo Vigor");
        LABELS.put("Health Candy L", "Caramelo Vigor +");
        LABELS.put("Health Candy XL", "Caramelo Vigor ++");
        LABELS.put("Health Feather", "Pluma Vigor");
        LABELS.put("Health Mochi", "Mochi Vigor");
        LABELS.put("Heart Mail", "Car. Corazón");
        LABELS.put("Heart Scale", "Escama Corazón");
        LABELS.put("Heart Stamp", "Arrumaco");
        LABELS.put("Heart Swap", "Cambiaalmas");
        LABELS.put("Hearthflame Mask", "Máscara Horno");
        LABELS.put("Heat Crash", "Golpe Calor");
        LABELS.put("Heat Rock", "Roca Calor");
        LABELS.put("Heat Wave", "Onda Ígnea");
        LABELS.put("Heatproof", "Ignífugo");
        LABELS.put("Heatranite", "Heatranita");
        LABELS.put("Heavy Ball", "Peso Ball");
        LABELS.put("Heavy Metal", "Metal Pesado");
        LABELS.put("Heavy Slam", "Cuerpo Pesado");
        LABELS.put("Heavy-Duty Boots", "Botas Gruesas");
        LABELS.put("Helix Fossil", "Fósil Hélix");
        LABELS.put("Helping Hand", "Refuerzo");
        LABELS.put("Heracronite", "Heracrossita");
        LABELS.put("Heracross Claw", "Garra de Heracross");
        LABELS.put("Herbed Sausage", "Butifarra");
        LABELS.put("Heroic Sword Pick", "Palillo Heroico");
        LABELS.put("Hex", "Infortunio");
        LABELS.put("Hi-tech Earbuds", "Supertapones");
        LABELS.put("Hidden Power", "Poder Oculto");
        LABELS.put("High Horsepower", "Fuerza Equina");
        LABELS.put("High Jump Kick", "Patada Salto Alta");
        LABELS.put("Hippopotas Sand", "Arena de Hippopotas");
        LABELS.put("Hitmonchan Candy", "Caramelo Hitmonchan");
        LABELS.put("Hitmonlee Candy", "Caramelo Hitmonlee");
        LABELS.put("Hold Back", "Clemencia");
        LABELS.put("Hold Hands", "Manos Juntas");
        LABELS.put("Holo Caster", "Holomisor");
        LABELS.put("Hondew Berry", "Baya Meluce");
        LABELS.put("Hone Claws", "Afilagarras");
        LABELS.put("Honey", "Miel");
        LABELS.put("Honey Gather", "Recogemiel");
        LABELS.put("Honor of Kalos", "Emblema Kalos");
        LABELS.put("Hoothoot Feather", "Pluma de Hoothoot");
        LABELS.put("Hoppip Leaf", "Hoja de Hoppip");
        LABELS.put("Horn Attack", "Cornada");
    }
    private static void labels12() {
        LABELS.put("Horn Drill", "Perforador");
        LABELS.put("Horn Leech", "Asta Drenaje");
        LABELS.put("Horsea Candy", "Caramelo Horsea");
        LABELS.put("Horseradish", "Rábano Picante");
        LABELS.put("Hospitality", "Hospitalidad");
        LABELS.put("Houndoominite", "Houndoomita");
        LABELS.put("Houndour Fang", "Colmillo de Houndour");
        LABELS.put("Howl", "Aullido");
        LABELS.put("Huge Power", "Potencia");
        LABELS.put("Hunger Switch", "Mutapetito");
        LABELS.put("Hurricane", "Vendaval");
        LABELS.put("Hustle", "Entusiasmo");
        LABELS.put("Hydration", "Hidratación");
        LABELS.put("Hydro Cannon", "Hidrocañón");
        LABELS.put("Hydro Pump", "Hidrobomba");
        LABELS.put("Hydro Steam", "Hidrovapor");
        LABELS.put("Hydro Vortex", "Hidrovórtice Abisal");
        LABELS.put("Hyper Beam", "Hiperrayo");
        LABELS.put("Hyper Cutter", "Corte Fuerte");
        LABELS.put("Hyper Drill", "Hipertaladro");
        LABELS.put("Hyper Fang", "Hipercolmillo");
        LABELS.put("Hyper Potion", "Hiperpoción");
        LABELS.put("Hyper Voice", "Vozarrón");
        LABELS.put("Hyperspace Fury", "Cerco Dimensión");
        LABELS.put("Hyperspace Hole", "Paso Dimensional");
        LABELS.put("Hypnosis", "Hipnosis");
        LABELS.put("Iapapa Berry", "Baya Pabaya");
        LABELS.put("Ice", "Hielo");
        LABELS.put("Ice Ball", "Bola Hielo");
        LABELS.put("Ice Beam", "Rayo Hielo");
        LABELS.put("Ice Body", "Gélido");
        LABELS.put("Ice Burn", "Llama Gélida");
        LABELS.put("Ice Face", "Cara de Hielo");
        LABELS.put("Ice Fang", "Colmillo Hielo");
        LABELS.put("Ice Gem", "Gema Hielo");
        LABELS.put("Ice Hammer", "Martillo Hielo");
        LABELS.put("Ice Heal", "Antihielo");
        LABELS.put("Ice Memory", "Disco Hielo");
        LABELS.put("Ice Punch", "Puño Hielo");
        LABELS.put("Ice Scales", "Escama de Hielo");
        LABELS.put("Ice Shard", "Esquirla Helada");
        LABELS.put("Ice Spinner", "Pirueta Helada");
        LABELS.put("Ice Stone", "Piedra Hielo");
        LABELS.put("Ice Tera Shard", "Teralito Hielo");
        LABELS.put("Iceroot Carrot", "Zanahoria Nívea");
        LABELS.put("Icicle Crash", "Chuzos");
        LABELS.put("Icicle Plate", "Tabla Helada");
        LABELS.put("Icicle Spear", "Carámbano");
        LABELS.put("Icium Z", "Criostal Z");
        LABELS.put("Icy Rock", "Roca Helada");
        LABELS.put("Icy Wind", "Viento Hielo");
        LABELS.put("Igglybuff Fluff", "Pelo de Igglybuff");
        LABELS.put("Ilima Normalium Z", "Cristal Z (Liam)");
        LABELS.put("Illuminate", "Iluminación");
        LABELS.put("Illumise Fluid", "Fluido de Illumise");
        LABELS.put("Illusion", "Ilusión");
        LABELS.put("Immunity", "Inmunidad");
        LABELS.put("Impidimp Hair", "Pelo de Impidimp");
        LABELS.put("Impish", "Agitada");
        LABELS.put("Impish Mint", "Menta Agitada");
        LABELS.put("Imposter", "Impostor");
        LABELS.put("Imprison", "Sellar");
        LABELS.put("Incinerate", "Calcinación");
        LABELS.put("Incinium Z", "Incinostal Z");
        LABELS.put("Indeedee Fur", "Pelo de Indeedee");
        LABELS.put("Infernal Parade", "Marcha Espectral");
        LABELS.put("Inferno", "Infierno");
        LABELS.put("Inferno Overdrive", "Hecatombe Pírica");
        LABELS.put("Infestation", "Acoso");
        LABELS.put("Infiltrator", "Allanamiento");
        LABELS.put("Ingrain", "Arraigo");
        LABELS.put("Innards Out", "Revés");
        LABELS.put("Inner Focus", "Fuerza Mental");
        LABELS.put("Inquiry Mail", "Carta Pregunta");
        LABELS.put("Insect Plate", "Tabla Bicho");
        LABELS.put("Insomnia", "Insomnio");
        LABELS.put("Instant Noodles", "Fideos de Bote");
        LABELS.put("Instruct", "Mandato");
        LABELS.put("Intimidate", "Intimidación");
        LABELS.put("Intrepid Sword", "Espada Indómita");
        LABELS.put("Intriguing Stone", "Piedra Insólita");
        LABELS.put("Ion Deluge", "Cortina Plasma");
        LABELS.put("Iron", "Hierro");
        LABELS.put("Iron Ball", "Bola Férrea");
        LABELS.put("Iron Barbs", "Punta Acero");
        LABELS.put("Iron Defense", "Defensa Férrea");
        LABELS.put("Iron Fist", "Puño Férreo");
        LABELS.put("Iron Head", "Cabeza de Hierro");
        LABELS.put("Iron Plate", "Tabla Acero");
        LABELS.put("Iron Tail", "Cola Férrea");
    }
    private static void labels13() {
        LABELS.put("Item Drop", "Tiraobjeto");
        LABELS.put("Item Urge", "Activaobjeto");
        LABELS.put("Ivy Cudgel", "Garrote Liana");
        LABELS.put("Jaboca Berry", "Baya Jaboca");
        LABELS.put("Jade Orb", "Esfera Verde");
        LABELS.put("Jalapeño", "Jalapeño");
        LABELS.put("Jam", "Confitura de Bayas");
        LABELS.put("Jangmo-o Scales", "Escama de Jangmo-o");
        LABELS.put("Jaw Fossil", "Fósil Mandíbula");
        LABELS.put("Jaw Lock", "Presa Maxilar");
        LABELS.put("Jet Ball", "Aero Ball");
        LABELS.put("Jet Punch", "Puño Jet");
        LABELS.put("Jigglypuff Candy", "Caramelo Jigglypuff");
        LABELS.put("Jolly", "Alegre");
        LABELS.put("Jolly Mint", "Menta Alegre");
        LABELS.put("Journal", "Diario");
        LABELS.put("Judgment", "Sentencia");
        LABELS.put("Jump Kick", "Patada Salto");
        LABELS.put("Jungle Healing", "Cura Selvática");
        LABELS.put("Justified", "Justiciero");
        LABELS.put("Jynx Candy", "Caramelo Jynx");
        LABELS.put("Kabuto Candy", "Caramelo Kabuto");
        LABELS.put("Kangaskhan Candy", "Caramelo Kangaskhan");
        LABELS.put("Kangaskhanite", "Kangaskhanita");
        LABELS.put("Karate Chop", "Golpe Kárate");
        LABELS.put("Kasib Berry", "Baya Drasi");
        LABELS.put("Kebia Berry", "Baya Kebia");
        LABELS.put("Kee Berry", "Baya Biglia");
        LABELS.put("Keen Eye", "Vista Lince");
        LABELS.put("Kelpsy Berry", "Baya Algama");
        LABELS.put("Ketchup", "Kétchup");
        LABELS.put("Key Stone", "Piedra Activadora");
        LABELS.put("Key to Room 1", "Ll. Habitación 1");
        LABELS.put("Key to Room 2", "Ll. Habitación 2");
        LABELS.put("Key to Room 4", "Ll. Habitación 4");
        LABELS.put("Key to Room 6", "Ll. Habitación 6");
        LABELS.put("Kinesis", "Kinético");
        LABELS.put("King’s Rock", "Roca del Rey");
        LABELS.put("King’s Shield", "Escudo Real");
        LABELS.put("Kiwi", "Kiwi");
        LABELS.put("Klawf Claw", "Pinza de Klawf");
        LABELS.put("Klawf Stick", "Surimi de Klawf");
        LABELS.put("Klefki Key", "Llave de Klefki");
        LABELS.put("Klutz", "Zoquete");
        LABELS.put("Knock Off", "Desarme");
        LABELS.put("Koffing Candy", "Caramelo Koffing");
        LABELS.put("Koffing Gas", "Gas de Koffing");
        LABELS.put("Kofu’s Wallet", "Cartera de Fuco");
        LABELS.put("Komala Claw", "Garra de Komala");
        LABELS.put("Kommonium Z", "Kommostal Z");
        LABELS.put("Koraidon’s Poké Ball", "Poké Ball de Koraidon");
        LABELS.put("Kowtow Cleave", "Genufendiente");
        LABELS.put("Krabby Candy", "Caramelo Krabby");
        LABELS.put("Kricketot Shell", "Muda de Kricketot");
        LABELS.put("Lagging Tail", "Cola Plúmbea");
        LABELS.put("Land’s Wrath", "Fuerza Telúrica");
        LABELS.put("Lansat Berry", "Baya Zonlan");
        LABELS.put("Lapras Candy", "Caramelo Lapras");
        LABELS.put("Large Leek", "Puerro Grueso");
        LABELS.put("Larvesta Fuzz", "Pelo de Larvesta");
        LABELS.put("Larvitar Claw", "Garra de Larvitar");
        LABELS.put("Laser Focus", "Aguzar");
        LABELS.put("Lash Out", "Desahogo");
        LABELS.put("Last Resort", "Última Baza");
        LABELS.put("Last Respects", "Homenaje Póstumo");
        LABELS.put("Latiasite", "Latiasita");
        LABELS.put("Latiosite", "Latiosita");
        LABELS.put("Lava Cookie", "Galleta Lava");
        LABELS.put("Lava Plume", "Humareda");
        LABELS.put("Lax", "Floja");
        LABELS.put("Lax Incense", "Incienso Suave");
        LABELS.put("Lax Mint", "Menta Floja");
        LABELS.put("Leaden Ball", "Kilo Ball");
        LABELS.put("Leader’s Crest", "Distintivo de Líder");
        LABELS.put("Leaf Blade", "Hoja Aguda");
        LABELS.put("Leaf Guard", "Defensa Hoja");
        LABELS.put("Leaf Letter", "Hoja Escrita");
        LABELS.put("Leaf Stone", "Piedra Hoja");
        LABELS.put("Leaf Storm", "Lluevehojas");
        LABELS.put("Leaf Tornado", "Ciclón de Hojas");
        LABELS.put("Leafage", "Follaje");
        LABELS.put("Leafy Tablecloth", "Mantel Naturaleza");
        LABELS.put("Lechonk Hair", "Pelo de Lechonk");
        LABELS.put("Leech Life", "Chupavidas");
        LABELS.put("Leech Seed", "Drenadoras");
        LABELS.put("Leek", "Puerro");
        LABELS.put("Leer", "Malicioso");
        LABELS.put("Left Poké Ball", "Poké Ball Ajena");
        LABELS.put("Leftovers", "Restos");
        LABELS.put("Legend Plate", "Tabla Legendaria");
    }
    private static void labels14() {
        LABELS.put("Legendary Clue 1", "Leyenda 1");
        LABELS.put("Legendary Clue 2", "Leyenda 2");
        LABELS.put("Legendary Clue 3", "Leyenda 3");
        LABELS.put("Legendary Clue?", "Leyenda (?)");
        LABELS.put("Lemonade", "Limonada");
        LABELS.put("Lens Case", "Portalentillas");
        LABELS.put("Leppa Berry", "Baya Zanama");
        LABELS.put("Letter", "Carta a Máximo");
        LABELS.put("Lettuce", "Lechuga");
        LABELS.put("Let’s Snuggle Forever", "Somanta Amistosa");
        LABELS.put("Level Ball", "Nivel Ball");
        LABELS.put("Levitate", "Levitación");
        LABELS.put("Libero", "Líbero");
        LABELS.put("Liberty Pass", "Tarjeta Libertad");
        LABELS.put("Lick", "Lengüetazo");
        LABELS.put("Lickitung Candy", "Caramelo Lickitung");
        LABELS.put("Liechi Berry", "Baya Lichi");
        LABELS.put("Life Dew", "Gota Vital");
        LABELS.put("Life Orb", "Vidasfera");
        LABELS.put("Lift Key", "Llave Ascensor");
        LABELS.put("Light Ball", "Bola Luminosa");
        LABELS.put("Light Clay", "Refleluz");
        LABELS.put("Light Metal", "Metal Liviano");
        LABELS.put("Light Screen", "Pantalla de Luz");
        LABELS.put("Light Stone", "Orbe Claro");
        LABELS.put("Light That Burns the Sky", "Fotodestrucción Apocalíptica");
        LABELS.put("Light of Ruin", "Luz Aniquiladora");
        LABELS.put("Lightning Rod", "Pararrayos");
        LABELS.put("Like Mail", "Carta Gustos");
        LABELS.put("Lilac Tablecloth", "Mantel Malva");
        LABELS.put("Limber", "Flexibilidad");
        LABELS.put("Lingering Aroma", "Olor Persistente");
        LABELS.put("Linking Cord", "Cordón Unión");
        LABELS.put("Liquid Ooze", "Lodo Líquido");
        LABELS.put("Liquid Voice", "Voz Fluida");
        LABELS.put("Liquidation", "Hidroariete");
        LABELS.put("Litleo Tuft", "Mechón de Litleo");
        LABELS.put("Litwick Soot", "Hollín de Litwick");
        LABELS.put("Loaded Dice", "Dado Trucado");
        LABELS.put("Lock Capsule", "Cápsula Candado");
        LABELS.put("Lock-On", "Fijar Blanco");
        LABELS.put("Lone Earring", "Pendiente");
        LABELS.put("Lonely", "Huraña");
        LABELS.put("Lonely Mint", "Menta Huraña");
        LABELS.put("Long Reach", "Remoto");
        LABELS.put("Looker Ticket", "Boleto Handsome");
        LABELS.put("Loot Sack", "Saca Botín");
        LABELS.put("Lopunnite", "Lopunnita");
        LABELS.put("Lost Item", "Objeto Perdido");
        LABELS.put("Lotad Leaf", "Hoja de Lotad");
        LABELS.put("Love Ball", "Amor Ball");
        LABELS.put("Love Sweet", "Confite Corazón");
        LABELS.put("Lovely Kiss", "Beso Amoroso");
        LABELS.put("Low Kick", "Patada Baja");
        LABELS.put("Low Sweep", "Puntapié");
        LABELS.put("Lucarionite", "Lucarita");
        LABELS.put("Lucarionite Z", "Lucarita Z");
        LABELS.put("Luck Incense", "Incienso Duplo");
        LABELS.put("Lucky Chant", "Conjuro");
        LABELS.put("Lucky Egg", "Huevo Suerte");
        LABELS.put("Lucky Punch", "Puño Suerte");
        LABELS.put("Lum Berry", "Baya Ziuela");
        LABELS.put("Lumina Crash", "Fotocolisión");
        LABELS.put("Luminous Moss", "Musgo Brillante");
        LABELS.put("Lumiose Galette", "Crêpe Luminalia");
        LABELS.put("Lunalium Z", "Lunalastal Z");
        LABELS.put("Lunar Blessing", "Plegaria Lunar");
        LABELS.put("Lunar Dance", "Danza Lunar");
        LABELS.put("Lunar Wing", "Pluma Lunar");
        LABELS.put("Lunge", "Plancha");
        LABELS.put("Lure", "Colonia");
        LABELS.put("Lure Ball", "Cebo Ball");
        LABELS.put("Luster Purge", "Resplandor");
        LABELS.put("Lustrous Globe", "Gran Lustresfera");
        LABELS.put("Lustrous Orb", "Lustresfera");
        LABELS.put("Luvdisc Scales", "Escama de Luvdisc");
        LABELS.put("Luxury Ball", "Lujo Ball");
        LABELS.put("Lycanium Z", "Lycanrostal Z");
        LABELS.put("Mach Bike", "Bici de Carreras");
        LABELS.put("Mach Punch", "Ultrapuño");
        LABELS.put("Machine Part", "Maquinaria");
        LABELS.put("Macho Brace", "Brazal Firme");
        LABELS.put("Machop Candy", "Caramelo Machop");
        LABELS.put("Magearnite", "Magearnita");
        LABELS.put("Magic Bounce", "Espejo Mágico");
        LABELS.put("Magic Coat", "Capa Mágica");
        LABELS.put("Magic Guard", "Muro Mágico");
        LABELS.put("Magic Powder", "Polvo Mágico");
        LABELS.put("Magic Room", "Zona Mágica");
        LABELS.put("Magical Heart Pick", "Palillo Corazón");
    }
    private static void labels15() {
        LABELS.put("Magical Leaf", "Hoja Mágica");
        LABELS.put("Magical Star Pick", "Palillo Estrella");
        LABELS.put("Magical Torque", "Feerichoque");
        LABELS.put("Magician", "Prestidigitador");
        LABELS.put("Magikarp Candy", "Caramelo Magikarp");
        LABELS.put("Magikarp Scales", "Escama de Magikarp");
        LABELS.put("Magma Armor", "Escudo Magma");
        LABELS.put("Magma Emblem", "Signo Magma");
        LABELS.put("Magma Stone", "Piedra Magma");
        LABELS.put("Magma Storm", "Lluvia Ígnea");
        LABELS.put("Magma Suit", "Traje Magma");
        LABELS.put("Magmar Candy", "Caramelo Magmar");
        LABELS.put("Magmarizer", "Magmatizador");
        LABELS.put("Magnemite Candy", "Caramelo Magnemite");
        LABELS.put("Magnemite Screw", "Tornillo de Magnemite");
        LABELS.put("Magnet", "Imán");
        LABELS.put("Magnet Bomb", "Bomba Imán");
        LABELS.put("Magnet Pull", "Imán");
        LABELS.put("Magnet Rise", "Levitón");
        LABELS.put("Magnetic Flux", "Aura Magnética");
        LABELS.put("Magnitude", "Magnitud");
        LABELS.put("Mago Berry", "Baya Ango");
        LABELS.put("Magost Berry", "Baya Aostan");
        LABELS.put("Make It Rain", "Fiebre Dorada");
        LABELS.put("Makeup Bag", "Kit Maquillaje");
        LABELS.put("Makuhita Sweat", "Sudor de Makuhita");
        LABELS.put("Malamarite", "Malamarita");
        LABELS.put("Malicious Armor", "Armadura Maldita");
        LABELS.put("Malicious Moonsault", "Hiperplancha Oscura");
        LABELS.put("Malignant Chain", "Cadena Virulenta");
        LABELS.put("Manectite", "Manectricita");
        LABELS.put("Mankey Candy", "Caramelo Mankey");
        LABELS.put("Mankey Fur", "Pelo de Mankey");
        LABELS.put("Maranga Berry", "Baya Maranga");
        LABELS.put("Marble", "Canica");
        LABELS.put("Mareanie Spike", "Aguijón de Mareanie");
        LABELS.put("Mareep Wool", "Lana de Mareep");
        LABELS.put("Marill Ball", "Pelota Marill");
        LABELS.put("Mark Charm", "Amuleto Emblema");
        LABELS.put("Marmalade", "Mermelada");
        LABELS.put("Marshadium Z", "Marshastal Z");
        LABELS.put("Marvel Scale", "Escama Especial");
        LABELS.put("Maschiff Fang", "Colmillo de Maschiff");
        LABELS.put("Master Ball", "Master Ball");
        LABELS.put("Masterpiece Teacup", "Cuenco Exquisito");
        LABELS.put("Mat Block", "Escudo Tatami");
        LABELS.put("Matcha Gotcha", "Cañón Batidor");
        LABELS.put("Mawilite", "Mawilita");
        LABELS.put("Max Airstream", "Maxiciclón");
        LABELS.put("Max Darkness", "Maxisombra");
        LABELS.put("Max Elixir", "Elixir Máximo");
        LABELS.put("Max Ether", "Éter Máximo");
        LABELS.put("Max Flare", "Maxignición");
        LABELS.put("Max Flutterby", "Maxinsecto");
        LABELS.put("Max Geyser", "Maxichorro");
        LABELS.put("Max Guard", "Maxibarrera");
        LABELS.put("Max Hailstorm", "Maxihelada");
        LABELS.put("Max Honey", "Maxipanal");
        LABELS.put("Max Knuckle", "Maxipuño");
        LABELS.put("Max Lightning", "Maxitormenta");
        LABELS.put("Max Lure", "Colonia Máxima");
        LABELS.put("Max Mindstorm", "Maxionda");
        LABELS.put("Max Mushrooms", "Maxiseta");
        LABELS.put("Max Ooze", "Maxiácido");
        LABELS.put("Max Overgrowth", "Maxiflora");
        LABELS.put("Max Phantasm", "Maxiespectro");
        LABELS.put("Max Potion", "Poción Máxima");
        LABELS.put("Max Quake", "Maxitemblor");
        LABELS.put("Max Repel", "Repelente Máximo");
        LABELS.put("Max Revive", "Revivir Máximo");
        LABELS.put("Max Rockfall", "Maxilito");
        LABELS.put("Max Starfall", "Maxiestela");
        LABELS.put("Max Steelspike", "Maximetal");
        LABELS.put("Max Strike", "Maxiataque");
        LABELS.put("Max Wyrmwind", "Maxidraco");
        LABELS.put("Mayonnaise", "Mayonesa");
        LABELS.put("Me First", "Yo Primero");
        LABELS.put("Meadow Plate", "Tabla Pradal");
        LABELS.put("Mean Look", "Mal de Ojo");
        LABELS.put("Mech Mail", "Carta Imán");
        LABELS.put("Medal Box", "Caja Insignias");
        LABELS.put("Medichamite", "Medichamita");
        LABELS.put("Medicine Pocket", "Botiquín");
        LABELS.put("Meditate", "Meditación");
        LABELS.put("Meditite Sweat", "Sudor de Meditite");
        LABELS.put("Mega Anchor", "Megaancla");
        LABELS.put("Mega Anklet", "Megatobillera");
        LABELS.put("Mega Bracelet", "Megapulsera");
        LABELS.put("Mega Charm", "Megacolgante");
        LABELS.put("Mega Cuff", "Megabrazalete");
    }
    private static void labels16() {
        LABELS.put("Mega Drain", "Megaagotar");
        LABELS.put("Mega Glasses", "Megagafas");
        LABELS.put("Mega Glove", "Megaguante");
        LABELS.put("Mega Kick", "Megapatada");
        LABELS.put("Mega Launcher", "Megadisparador");
        LABELS.put("Mega Pendant", "Megacollar");
        LABELS.put("Mega Punch", "Megapuño");
        LABELS.put("Mega Ring", "Mega-Aro");
        LABELS.put("Mega Sol", "Megasolar");
        LABELS.put("Mega Stickpin", "Megabroche");
        LABELS.put("Mega Tiara", "Megatiara");
        LABELS.put("Megahorn", "Megacuerno");
        LABELS.put("Meganiumite", "Meganiumita");
        LABELS.put("Meltan Candy", "Caramelo Meltan");
        LABELS.put("Member Card", "Carné Socio");
        LABELS.put("Memento", "Legado");
        LABELS.put("Menacing Moonraze Maelstrom", "Deflagración Lunar");
        LABELS.put("Mental Herb", "Hierba Mental");
        LABELS.put("Meowsticite", "Meowsticita");
        LABELS.put("Meowth Candy", "Caramelo Meowth");
        LABELS.put("Meowth Fur", "Pelo de Meowth");
        LABELS.put("Merciless", "Ensañamiento");
        LABELS.put("Metagrossite", "Metagrossita");
        LABELS.put("Metal Alloy", "Metal Compuesto");
        LABELS.put("Metal Burst", "Represión Metal");
        LABELS.put("Metal Claw", "Garra Metal");
        LABELS.put("Metal Coat", "Revest. Metálico");
        LABELS.put("Metal Powder", "Polvo Metálico");
        LABELS.put("Metal Sound", "Eco Metálico");
        LABELS.put("Meteor Assault", "Asalto Estelar");
        LABELS.put("Meteor Beam", "Rayo Meteórico");
        LABELS.put("Meteor Mash", "Puño Meteoro");
        LABELS.put("Meteorite", "Meteorito");
        LABELS.put("Meteorite Shard", "Frag. Meteorito");
        LABELS.put("Metronome", "Metrónomo");
        LABELS.put("Mew Candy", "Caramelo Mew");
        LABELS.put("Mewnium Z", "Mewstal Z");
        LABELS.put("Mewtwo Candy", "Caramelo Mewtwo");
        LABELS.put("Mewtwonite X", "Mewtwoita X");
        LABELS.put("Mewtwonite Y", "Mewtwoita Y");
        LABELS.put("Micle Berry", "Baya Lagro");
        LABELS.put("Mienfoo Claw", "Garra de Mienfoo");
        LABELS.put("Mighty Candy", "Caramelo Músculo");
        LABELS.put("Mighty Candy L", "Caramelo Músculo +");
        LABELS.put("Mighty Candy XL", "Caramelo Músculo ++");
        LABELS.put("Mighty Cleave", "Filo Potente");
        LABELS.put("Mild", "Afable");
        LABELS.put("Mild Mint", "Menta Afable");
        LABELS.put("Milk Drink", "Batido");
        LABELS.put("Mimic", "Mimético");
        LABELS.put("Mimicry", "Mimetismo");
        LABELS.put("Mimikium Z", "Mimikyustal Z");
        LABELS.put("Mimikyu Scrap", "Retal de Mimikyu");
        LABELS.put("Mind Blown", "Cabeza Sorpresa");
        LABELS.put("Mind Plate", "Tabla Mental");
        LABELS.put("Mind Reader", "Telépata");
        LABELS.put("Mind’s Eye", "Ojo Mental");
        LABELS.put("Minimize", "Reducción");
        LABELS.put("Mint Tablecloth", "Mantel Menta");
        LABELS.put("Minus", "Menos");
        LABELS.put("Miracle Eye", "Gran Ojo");
        LABELS.put("Miracle Seed", "Semilla Milagro");
        LABELS.put("Miraidon’s Poké Ball", "Poké Ball de Miraidon");
        LABELS.put("Mirror Armor", "Coraza Reflejo");
        LABELS.put("Mirror Coat", "Manto Espejo");
        LABELS.put("Mirror Herb", "Hierba Copia");
        LABELS.put("Mirror Move", "Espejo");
        LABELS.put("Mirror Shot", "Disparo Espejo");
        LABELS.put("Misdreavus Tears", "Lágrima de Misdreavus");
        LABELS.put("Mist", "Neblina");
        LABELS.put("Mist Ball", "Bola Neblina");
        LABELS.put("Misty Explosion", "Bruma Explosiva");
        LABELS.put("Misty Seed", "Semilla Bruma");
        LABELS.put("Misty Surge", "Nebulogénesis");
        LABELS.put("Misty Terrain", "Campo de Niebla");
        LABELS.put("Mixed Mushrooms", "Setas");
        LABELS.put("Modest", "Modesta");
        LABELS.put("Modest Mint", "Menta Modesta");
        LABELS.put("Mold Breaker", "Rompemoldes");
        LABELS.put("Moltres Candy", "Caramelo Moltres");
        LABELS.put("Monstrous Tablecloth", "Mantel Monstruoso");
        LABELS.put("Moody", "Veleta");
        LABELS.put("Moomoo Cheese", "Queso Mu-mu");
        LABELS.put("Moomoo Milk", "Leche Mu-mu");
        LABELS.put("Moon Ball", "Luna Ball");
        LABELS.put("Moon Flute", "Flauta Lunar");
        LABELS.put("Moon Stone", "Piedra Lunar");
        LABELS.put("Moonblast", "Fuerza Lunar");
        LABELS.put("Moongeist Beam", "Rayo Umbrío");
        LABELS.put("Moonlight", "Luz Lunar");
    }
    private static void labels17() {
        LABELS.put("Morning Sun", "Sol Matinal");
        LABELS.put("Morpeko Snack", "Semilla de Morpeko");
        LABELS.put("Mortal Spin", "Giro Mortífero");
        LABELS.put("Mosaic Mail", "Car. Mosaico");
        LABELS.put("Motor Drive", "Electromotor");
        LABELS.put("Mountain Gale", "Viento Carámbano");
        LABELS.put("Moxie", "Autoestima");
        LABELS.put("Mr. Mime Candy", "Caramelo Mr. Mime");
        LABELS.put("Mud Bomb", "Bomba Fango");
        LABELS.put("Mud Shot", "Disparo Lodo");
        LABELS.put("Mud Sport", "Chapoteo Lodo");
        LABELS.put("Mud-Slap", "Bofetón Lodo");
        LABELS.put("Mudbray Mud", "Lodo de Mudbray");
        LABELS.put("Muddy Water", "Agua Lodosa");
        LABELS.put("Multi-Attack", "Multiataque");
        LABELS.put("Multiscale", "Multiescamas");
        LABELS.put("Multitype", "Multitipo");
        LABELS.put("Mummy", "Momia");
        LABELS.put("Munchlax Fang", "Colmillo de Munchlax");
        LABELS.put("Murkrow Bauble", "Tesoro de Murkrow");
        LABELS.put("Muscle Band", "Cinta Fuerte");
        LABELS.put("Muscle Feather", "Pluma Músculo");
        LABELS.put("Muscle Mochi", "Mochi Músculo");
        LABELS.put("Mustard", "Mostaza");
        LABELS.put("Mycelium Might", "Poder Fúngico");
        LABELS.put("Mystery Egg", "Huevo Misterioso");
        LABELS.put("Mystic Water", "Agua Mística");
        LABELS.put("MysticTicket", "Misti-Ticket");
        LABELS.put("Mystical Fire", "Llama Embrujada");
        LABELS.put("Mystical Power", "Poder Místico");
        LABELS.put("N-Lunarizer", "Necroluna");
        LABELS.put("N-Solarizer", "Necrosol");
        LABELS.put("Nacli Salt", "Sal de Nacli");
        LABELS.put("Naive", "Ingenua");
        LABELS.put("Naive Mint", "Menta Ingenua");
        LABELS.put("Nanab Berry", "Baya Latano");
        LABELS.put("Nasty Plot", "Maquinación");
        LABELS.put("Natural Cure", "Cura Natural");
        LABELS.put("Natural Gift", "Don Natural");
        LABELS.put("Nature Power", "Adaptación");
        LABELS.put("Nature’s Madness", "Furia Natural");
        LABELS.put("Naughty", "Pícara");
        LABELS.put("Naughty Mint", "Menta Pícara");
        LABELS.put("Needle Arm", "Brazo Pincho");
        LABELS.put("Nest Ball", "Nido Ball");
        LABELS.put("Net Ball", "Malla Ball");
        LABELS.put("Neuroforce", "Fuerza Cerebral");
        LABELS.put("Neutralizing Gas", "Gas Reactivo");
        LABELS.put("Never-Ending Nightmare", "Presa Espectral");
        LABELS.put("Never-Melt Ice", "Hielo Perpetuo");
        LABELS.put("Nidoran♀ Candy", "Caramelo Nidoran♀");
        LABELS.put("Nidoran♂ Candy", "Caramelo Nidoran♂");
        LABELS.put("Night Daze", "Pulso Noche");
        LABELS.put("Night Shade", "Tinieblas");
        LABELS.put("Night Slash", "Tajo Umbrío");
        LABELS.put("Nightmare", "Pesadilla");
        LABELS.put("No Guard", "Indefenso");
        LABELS.put("No Retreat", "Bastión Final");
        LABELS.put("Noble Roar", "Rugido de Guerra");
        LABELS.put("Noibat Fur", "Pelo de Noibat");
        LABELS.put("Nomel Berry", "Baya Monli");
        LABELS.put("Noodles", "Fideos");
        LABELS.put("Normal", "Normal");
        LABELS.put("Normal Gem", "Gema Normal");
        LABELS.put("Normal Tera Shard", "Teralito Normal");
        LABELS.put("Normalium Z", "Normastal Z");
        LABELS.put("Normalize", "Normalidad");
        LABELS.put("Nosepass Fragment", "Fragmento de Nosepass");
        LABELS.put("Noxious Torque", "Ponzochoque");
        LABELS.put("Nugget", "Pepita");
        LABELS.put("Numel Lava", "Lava de Numel");
        LABELS.put("Nuzzle", "Moflete Estático");
        LABELS.put("Nymble Claw", "Garra de Nymble");
        LABELS.put("Oak’s Letter", "Carta Prof. Oak");
        LABELS.put("Oak’s Parcel", "Correo-Oak");
        LABELS.put("Oblivion Wing", "Ala Mortífera");
        LABELS.put("Oblivious", "Despiste");
        LABELS.put("Obstruct", "Obstrucción");
        LABELS.put("Occa Berry", "Baya Caoca");
        LABELS.put("Oceanic Operetta", "Sinfonía de la Diva Marina");
        LABELS.put("Octazooka", "Pulpocañón");
        LABELS.put("Octolock", "Octopresa");
        LABELS.put("Odd Incense", "Incienso Raro");
        LABELS.put("Odd Keystone", "Piedra Espíritu");
        LABELS.put("Oddish Candy", "Caramelo Oddish");
        LABELS.put("Odor Sleuth", "Rastreo");
        LABELS.put("Old Amber", "Ámbar Viejo");
        LABELS.put("Old Charm", "Talismán");
        LABELS.put("Old Gateau", "Barrita Plus");
        LABELS.put("Old Letter", "Carta Ajada");
    }
    private static void labels18() {
        LABELS.put("Old Rod", "Caña Vieja");
        LABELS.put("Old Sea Map", "Mapa Viejo");
        LABELS.put("Olive Oil", "Aceite de Oliva");
        LABELS.put("Omanyte Candy", "Caramelo Omanyte");
        LABELS.put("Ominous Wind", "Viento Aciago");
        LABELS.put("Onion", "Cebolla");
        LABELS.put("Onix Candy", "Caramelo Onix");
        LABELS.put("Opportunist", "Oportunista");
        LABELS.put("Oran Berry", "Baya Aranja");
        LABELS.put("Orange Dish", "Plato Naranja");
        LABELS.put("Orange Mail", "Car. Naranja");
        LABELS.put("Orange Petal", "Pétalo Naranja");
        LABELS.put("Oranguru Fur", "Pelo de Oranguru");
        LABELS.put("Order Up", "Oído Cocina");
        LABELS.put("Orichalcum Pulse", "Latido Oricalco");
        LABELS.put("Oricorio Feather", "Pluma de Oricorio");
        LABELS.put("Origin Ball", "Origen Ball");
        LABELS.put("Origin Pulse", "Pulso Primigenio");
        LABELS.put("Orthworm Tarnish", "Óxido de Orthworm");
        LABELS.put("Outrage", "Enfado");
        LABELS.put("Oval Charm", "Amuleto Oval");
        LABELS.put("Oval Stone", "Piedra Oval");
        LABELS.put("Overcoat", "Funda");
        LABELS.put("Overdrive", "Amplificador");
        LABELS.put("Overgrow", "Espesura");
        LABELS.put("Overheat", "Sofoco");
        LABELS.put("Own Tempo", "Ritmo Propio");
        LABELS.put("PP Max", "PP Máximos");
        LABELS.put("PP Up", "Más PP");
        LABELS.put("Pachirisu Fur", "Pelo de Pachirisu");
        LABELS.put("Pack of Potatoes", "Patatas");
        LABELS.put("Packaged Curry", "Curri de Bote");
        LABELS.put("Pain Split", "Divide Dolor");
        LABELS.put("Pair of Tickets", "Entrada para dos");
        LABELS.put("Pal Pad", "Bloc amigos");
        LABELS.put("Pamtre Berry", "Baya Plama");
        LABELS.put("Parabolic Charge", "Carga Parábola");
        LABELS.put("Paralyze Heal", "Antiparalizador");
        LABELS.put("Paras Candy", "Caramelo Paras");
        LABELS.put("Parasol Pick", "Palillo Sombrilla");
        LABELS.put("Parcel", "Paquete");
        LABELS.put("Parental Bond", "Amor Filial");
        LABELS.put("Park Ball", "Parque Ball");
        LABELS.put("Parting Shot", "Última Palabra");
        LABELS.put("Party Sparkler Pick", "Palillo Bengala");
        LABELS.put("Pass", "Magnetopase");
        LABELS.put("Pass Orb", "Regalosfera");
        LABELS.put("Passho Berry", "Baya Pasio");
        LABELS.put("Passimian Fur", "Pelo de Passimian");
        LABELS.put("Pasta", "Pasta");
        LABELS.put("Pastel Veil", "Velo Pastel");
        LABELS.put("Pawmi Fur", "Pelo de Pawmi");
        LABELS.put("Pawniard Blade", "Cuchilla de Pawniard");
        LABELS.put("Pay Day", "Día de Pago");
        LABELS.put("Payapa Berry", "Baya Payapa");
        LABELS.put("Payback", "Vendetta");
        LABELS.put("Peach Tablecloth", "Mantel Beis");
        LABELS.put("Peanut Butter", "Crema de Cacahuete");
        LABELS.put("Pearl", "Perla");
        LABELS.put("Pearl String", "Sarta Perlas");
        LABELS.put("Peat Block", "Bloque de Turba");
        LABELS.put("Pecha Berry", "Baya Meloc");
        LABELS.put("Peck", "Picotazo");
        LABELS.put("Pepper", "Pimienta");
        LABELS.put("Perish Body", "Cuerpo Mortal");
        LABELS.put("Perish Song", "Canto Mortal");
        LABELS.put("Permit", "Pase");
        LABELS.put("Persim Berry", "Baya Caquic");
        LABELS.put("Petal Blizzard", "Tormenta Floral");
        LABELS.put("Petal Dance", "Danza Pétalo");
        LABELS.put("Petaya Berry", "Baya Yapati");
        LABELS.put("Petilil Leaf", "Hoja de Petilil");
        LABELS.put("Pewter Crunchies", "Rokikos");
        LABELS.put("Phanpy Nail", "Uña de Phanpy");
        LABELS.put("Phantom Force", "Golpe Fantasma");
        LABELS.put("Phantump Twig", "Rama de Phantump");
        LABELS.put("Photo Album", "Álbum");
        LABELS.put("Photon Geyser", "Géiser Fotónico");
        LABELS.put("Pichu Fur", "Pelo de Pichu");
        LABELS.put("Pickle", "Pepinillo");
        LABELS.put("Pickpocket", "Hurto");
        LABELS.put("Pickup", "Recogida");
        LABELS.put("Picnic Set", "Kit de Pícnic");
        LABELS.put("Pidgeotite", "Pidgeotita");
        LABELS.put("Pidgey Candy", "Caramelo Pidgey");
        LABELS.put("Piercing Drill", "Turbotaladro");
        LABELS.put("Pika Papow", "Pikatormenta");
        LABELS.put("Pika-Pika Pick", "Palillo Pikachu");
        LABELS.put("Pikachu Candy", "Caramelo Pikachu");
        LABELS.put("Pikachu Cup", "Taza Pikachu");
    }
    private static void labels19() {
        LABELS.put("Pikanium Z", "Pikastal Z");
        LABELS.put("Pikashunium Z", "Ash-Pikastal Z");
        LABELS.put("Pin Missile", "Pin Misil");
        LABELS.put("Pinap Berry", "Baya Pinia");
        LABELS.put("Pincurchin Spines", "Púa de Pincurchin");
        LABELS.put("Pineapple", "Piña");
        LABELS.put("Pineco Husk", "Corteza de Pineco");
        LABELS.put("Pink Apricorn", "Bonguri Rosa");
        LABELS.put("Pink Bottle", "Termo Rosa");
        LABELS.put("Pink Cup", "Taza Rosa");
        LABELS.put("Pink Nectar", "Néctar Rosa");
        LABELS.put("Pink Petal", "Pétalo Rosa");
        LABELS.put("Pink Scarf", "Pañuelo Rosa");
        LABELS.put("Pink Tablecloth", "Mantel Rosa");
        LABELS.put("Pinsir Candy", "Caramelo Pinsir");
        LABELS.put("Pinsirite", "Pinsirita");
        LABELS.put("Pixie Plate", "Tabla Duende");
        LABELS.put("Pixilate", "Piel Feérica");
        LABELS.put("Plaid Tablecloth (B)", "Mantel Cuadros Azul");
        LABELS.put("Plaid Tablecloth (R)", "Mantel Cuadros Rojo");
        LABELS.put("Plaid Tablecloth (Y)", "Mantel Cuadros Ámbar");
        LABELS.put("Plasma Card", "Tarjeta Plasma");
        LABELS.put("Plasma Fists", "Puños Plasma");
        LABELS.put("Play Nice", "Camaradería");
        LABELS.put("Play Rough", "Carantoña");
        LABELS.put("Pluck", "Picoteo");
        LABELS.put("Plume Fossil", "Fósil Pluma");
        LABELS.put("Plus", "Más");
        LABELS.put("Poffin Case", "Pokochera");
        LABELS.put("Point Card", "Tarjeta Puntos");
        LABELS.put("Poison", "Veneno");
        LABELS.put("Poison Barb", "Flecha Venenosa");
        LABELS.put("Poison Fang", "Colmillo Veneno");
        LABELS.put("Poison Gas", "Gas Venenoso");
        LABELS.put("Poison Gem", "Gema Veneno");
        LABELS.put("Poison Heal", "Antídoto");
        LABELS.put("Poison Jab", "Puya Nociva");
        LABELS.put("Poison Memory", "Disco Veneno");
        LABELS.put("Poison Point", "Punto Tóxico");
        LABELS.put("Poison Powder", "Polvo Veneno");
        LABELS.put("Poison Puppeteer", "Títere Tóxico");
        LABELS.put("Poison Sting", "Picotazo Veneno");
        LABELS.put("Poison Tail", "Cola Veneno");
        LABELS.put("Poison Tera Shard", "Teralito Veneno");
        LABELS.put("Poison Touch", "Toque Tóxico");
        LABELS.put("Poisonium Z", "Toxistal Z");
        LABELS.put("Poké Ball", "Poké Ball");
        LABELS.put("Poké Doll", "Poké Muñeco");
        LABELS.put("Poké Flute", "Poké Flauta");
        LABELS.put("Poké Radar", "Pokéradar");
        LABELS.put("Poké Toy", "Pokéseñuelo");
        LABELS.put("Pokéblock Case", "Tubo Pokécubos");
        LABELS.put("Pokéblock Kit", "Kit de Pokécubos");
        LABELS.put("Pokémon Box Link", "Caja de Pokémon");
        LABELS.put("Polished Mud Ball", "Bola de Arcilla");
        LABELS.put("Poliwag Candy", "Caramelo Poliwag");
        LABELS.put("Poliwag Slime", "Secreción de Poliwag");
        LABELS.put("Polka-Dot Bottle", "Termo Lunares");
        LABELS.put("Polka-Dot Cup", "Taza Lunares");
        LABELS.put("Polka-Dot Tablecloth", "Mantel Lunares");
        LABELS.put("Pollen Puff", "Bola de Polen");
        LABELS.put("Poltchageist Powder", "Polvo de Poltchageist");
        LABELS.put("Poltergeist", "Poltergeist");
        LABELS.put("Pomeg Berry", "Baya Grana");
        LABELS.put("Ponyta Candy", "Caramelo Ponyta");
        LABELS.put("Poochyena Fang", "Colmillo de Poochyena");
        LABELS.put("Population Bomb", "Proliferación");
        LABELS.put("Porygon Candy", "Caramelo Porygon");
        LABELS.put("Potato Salad", "Ensaladilla");
        LABELS.put("Potato Tortilla", "Tortilla");
        LABELS.put("Potion", "Poción");
        LABELS.put("Pounce", "Brinco");
        LABELS.put("Pound", "Destructor");
        LABELS.put("Powder", "Polvo Explosivo");
        LABELS.put("Powder Jar", "Bote Polvos");
        LABELS.put("Powder Snow", "Nieve Polvo");
        LABELS.put("Power Anklet", "Franja Recia");
        LABELS.put("Power Band", "Banda Recia");
        LABELS.put("Power Belt", "Cinto Recio");
        LABELS.put("Power Bracer", "Brazal Recio");
        LABELS.put("Power Construct", "Agrupamiento");
        LABELS.put("Power Gem", "Joya de Luz");
        LABELS.put("Power Herb", "Hierba Única");
        LABELS.put("Power Lens", "Lente Recia");
        LABELS.put("Power Plant Pass", "Pase Central");
        LABELS.put("Power Shift", "Cambiapoder");
        LABELS.put("Power Split", "Isofuerza");
        LABELS.put("Power Spot", "Fuente Energía");
        LABELS.put("Power Swap", "Cambiafuerza");
        LABELS.put("Power Trick", "Truco Fuerza");
    }
    private static void labels20() {
        LABELS.put("Power Trip", "Chulería");
        LABELS.put("Power Weight", "Pesa Recia");
        LABELS.put("Power Whip", "Latigazo");
        LABELS.put("Power of Alchemy", "Reacción Química");
        LABELS.put("Power-Up Pocket", "Bolsillo Mejoras");
        LABELS.put("Power-Up Punch", "Puño Incremento");
        LABELS.put("Prankster", "Bromista");
        LABELS.put("Precipice Blades", "Filo del Abismo");
        LABELS.put("Precooked Burger", "Hamburguesas");
        LABELS.put("Premier Ball", "Honor Ball");
        LABELS.put("Present", "Presente");
        LABELS.put("Pressure", "Presión");
        LABELS.put("Pretty Feather", "Pluma Bella");
        LABELS.put("Primarium Z", "Primastal Z");
        LABELS.put("Primordial Sea", "Mar del Albor");
        LABELS.put("Prism Armor", "Armadura Prisma");
        LABELS.put("Prism Scale", "Escama Bella");
        LABELS.put("Prismatic Laser", "Láser Prisma");
        LABELS.put("Prison Bottle", "Vasija Castigo");
        LABELS.put("Professor’s Mask", "Máscara Profesor");
        LABELS.put("Prof’s Letter", "Carta Profesor");
        LABELS.put("Prop Case", "Neceser");
        LABELS.put("Propeller Tail", "Hélice Caudal");
        LABELS.put("Prosciutto", "Jamón Serrano");
        LABELS.put("Protean", "Mutatipo");
        LABELS.put("Protect", "Protección");
        LABELS.put("Protective Pads", "Paracontacto");
        LABELS.put("Protector", "Protector");
        LABELS.put("Protein", "Proteína");
        LABELS.put("Protosynthesis", "Paleosíntesis");
        LABELS.put("Psybeam", "Psicorrayo");
        LABELS.put("Psyblade", "Psicohojas");
        LABELS.put("Psych Up", "Autosugestión");
        LABELS.put("Psychic", "Psíquico");
        LABELS.put("Psychic Fangs", "Psicocolmillo");
        LABELS.put("Psychic Gem", "Gema Psíquica");
        LABELS.put("Psychic Memory", "Disco Psíquico");
        LABELS.put("Psychic Noise", "Psicorruido");
        LABELS.put("Psychic Seed", "Semilla Psique");
        LABELS.put("Psychic Surge", "Psicogénesis");
        LABELS.put("Psychic Tera Shard", "Teralito Psíquico");
        LABELS.put("Psychic Terrain", "Campo Psíquico");
        LABELS.put("Psychium Z", "Psicostal Z");
        LABELS.put("Psycho Boost", "Psicoataque");
        LABELS.put("Psycho Cut", "Psicocorte");
        LABELS.put("Psycho Shift", "Psicocambio");
        LABELS.put("Psyduck Candy", "Caramelo Psyduck");
        LABELS.put("Psyduck Down", "Plumón de Psyduck");
        LABELS.put("Psyshield Bash", "Asalto Barrera");
        LABELS.put("Psyshock", "Psicocarga");
        LABELS.put("Psystrike", "Onda Mental");
        LABELS.put("Psywave", "Psicoonda");
        LABELS.put("Pulverizing Pancake", "Arrojo Intempestivo");
        LABELS.put("Punching Glove", "Guante de Boxeo");
        LABELS.put("Pungent Root", "Hierba Intensa");
        LABELS.put("Punishment", "Castigo");
        LABELS.put("Punk Rock", "Punk Rock");
        LABELS.put("Pure Incense", "Incienso Puro");
        LABELS.put("Pure Power", "Energía Pura");
        LABELS.put("Purify", "Purificación");
        LABELS.put("Purifying Salt", "Sal Purificadora");
        LABELS.put("Purple Nectar", "Néctar Violeta");
        LABELS.put("Purple Petal", "Pétalo Violeta");
        LABELS.put("Pursuit", "Persecución");
        LABELS.put("Pyro Ball", "Balón Ígneo");
        LABELS.put("Pyroarite", "Pyroarita");
        LABELS.put("Qualot Berry", "Baya Ispero");
        LABELS.put("Quark Drive", "Carga Cuark");
        LABELS.put("Quash", "Último Lugar");
        LABELS.put("Queenly Majesty", "Regia Presencia");
        LABELS.put("Quick Attack", "Ataque Rápido");
        LABELS.put("Quick Ball", "Veloz Ball");
        LABELS.put("Quick Candy", "Caramelo Ímpetu");
        LABELS.put("Quick Candy L", "Caramelo Ímpetu +");
        LABELS.put("Quick Candy XL", "Caramelo Ímpetu ++");
        LABELS.put("Quick Claw", "Garra Rápida");
        LABELS.put("Quick Draw", "Mano Rápida");
        LABELS.put("Quick Feet", "Pies Rápidos");
        LABELS.put("Quick Guard", "Anticipo");
        LABELS.put("Quick Powder", "Polvo Veloz");
        LABELS.put("Quiet", "Mansa");
        LABELS.put("Quiet Mint", "Menta Mansa");
        LABELS.put("Quirky", "Rara");
        LABELS.put("Quiver Dance", "Danza Aleteo");
        LABELS.put("Qwilfish Spines", "Púa de Qwilfish");
        LABELS.put("RKS System", "Sistema Alfa");
        LABELS.put("RSVP Mail", "Carta Invitar");
        LABELS.put("Rabuta Berry", "Baya Rautan");
        LABELS.put("Radiant Petal", "Pétalo Fulgor");
        LABELS.put("Rage", "Furia");
    }
    private static void labels21() {
        LABELS.put("Rage Candy Bar", "Caramelo Furia");
        LABELS.put("Rage Fist", "Puño Furia");
        LABELS.put("Rage Powder", "Polvo Ira");
        LABELS.put("Raging Bull", "Furia Taurina");
        LABELS.put("Raging Fury", "Erupción de Ira");
        LABELS.put("Raichunite X", "Raichunita X");
        LABELS.put("Raichunite Y", "Raichunita Y");
        LABELS.put("Rain Dance", "Danza Lluvia");
        LABELS.put("Rain Dish", "Cura Lluvia");
        LABELS.put("Rainbow Flower", "Flor Irisada");
        LABELS.put("Rainbow Pass", "Iris-Ticket");
        LABELS.put("Rainbow Wing", "Ala Arcoíris");
        LABELS.put("Ralts Dust", "Pertenencia de Ralts");
        LABELS.put("Rapid Spin", "Giro Rápido");
        LABELS.put("Rare Bone", "Hueso Raro");
        LABELS.put("Rare Candy", "Caramelo Raro");
        LABELS.put("Rash", "Alocada");
        LABELS.put("Rash Mint", "Menta Alocada");
        LABELS.put("Rattata Candy", "Caramelo Rattata");
        LABELS.put("Rattled", "Cobardía");
        LABELS.put("Rawst Berry", "Baya Safre");
        LABELS.put("Razor Claw", "Garra Afilada");
        LABELS.put("Razor Fang", "Colmillo Agudo");
        LABELS.put("Razor Leaf", "Hoja Afilada");
        LABELS.put("Razor Shell", "Concha Filo");
        LABELS.put("Razor Wind", "Viento Cortante");
        LABELS.put("Razz Berry", "Baya Frambu");
        LABELS.put("Reaper Cloth", "Tela Terrible");
        LABELS.put("Receiver", "Receptor");
        LABELS.put("Reckless", "Audaz");
        LABELS.put("Recover", "Recuperación");
        LABELS.put("Recycle", "Reciclaje");
        LABELS.put("Red Apricorn", "Bonguri Rojo");
        LABELS.put("Red Bell Pepper", "Pimiento Rojo");
        LABELS.put("Red Card", "Tarjeta Roja");
        LABELS.put("Red Chain", "Cadena Roja");
        LABELS.put("Red Dish", "Plato Rojo");
        LABELS.put("Red Flute", "Flauta Roja");
        LABELS.put("Red Nectar", "Néctar Rojo");
        LABELS.put("Red Onion", "Cebolla Roja");
        LABELS.put("Red Orb", "Prisma Rojo");
        LABELS.put("Red Petal", "Pétalo Rojo");
        LABELS.put("Red Poké Ball Pick", "Palillo Ball Roja");
        LABELS.put("Red Scale", "Escama Roja");
        LABELS.put("Red Scarf", "Pañuelo Rojo");
        LABELS.put("Red Shard", "Parte Roja");
        LABELS.put("Red-Flag Pick", "Palillo Bandera Roja");
        LABELS.put("Reflect", "Reflejo");
        LABELS.put("Reflect Type", "Clonatipo");
        LABELS.put("Refresh", "Alivio");
        LABELS.put("Refrigerate", "Piel Helada");
        LABELS.put("Regenerator", "Regeneración");
        LABELS.put("Reins of Unity", "Riendas Unión");
        LABELS.put("Relaxed", "Plácida");
        LABELS.put("Relaxed Mint", "Menta Plácida");
        LABELS.put("Relic Band", "Brazal");
        LABELS.put("Relic Copper", "Real Cobre");
        LABELS.put("Relic Crown", "Corona Antigua");
        LABELS.put("Relic Gold", "Real Oro");
        LABELS.put("Relic Silver", "Real Plata");
        LABELS.put("Relic Song", "Canto Arcaico");
        LABELS.put("Relic Statue", "Efigie Antigua");
        LABELS.put("Relic Vase", "Ánfora");
        LABELS.put("Rellor Mud", "Barro de Rellor");
        LABELS.put("Repeat Ball", "Acopio Ball");
        LABELS.put("Repel", "Repelente");
        LABELS.put("Reply Mail", "Carta Respuesta");
        LABELS.put("Reset Urge", "Quitaestado");
        LABELS.put("Resist Feather", "Pluma Aguante");
        LABELS.put("Resist Mochi", "Mochi Aguante");
        LABELS.put("Rest", "Descanso");
        LABELS.put("Retaliate", "Represalia");
        LABELS.put("Retro Mail", "Carta Retro.");
        LABELS.put("Return", "Retribución");
        LABELS.put("Reveal Glass", "Espejo Veraz");
        LABELS.put("Revelation Dance", "Danza Despertar");
        LABELS.put("Revenge", "Desquite");
        LABELS.put("Reversal", "Inversión");
        LABELS.put("Revival Blessing", "Plegaria Vital");
        LABELS.put("Revival Herb", "Hierba Revivir");
        LABELS.put("Revive", "Revivir");
        LABELS.put("Rhyhorn Candy", "Caramelo Rhyhorn");
        LABELS.put("Ribbon Sweet", "Confite Lazo");
        LABELS.put("Rice", "Arroz");
        LABELS.put("Rich Mulch", "Abono Fértil");
        LABELS.put("Ride Pager", "Buscamontura");
        LABELS.put("Rindo Berry", "Baya Tamar");
        LABELS.put("Ring Target", "Blanco");
        LABELS.put("Riolu Fur", "Pelo de Riolu");
        LABELS.put("Ripen", "Maduración");
    }
    private static void labels22() {
        LABELS.put("Rising Voltage", "Alto Voltaje");
        LABELS.put("Rivalry", "Rivalidad");
        LABELS.put("Rm. 1 Key", "Ll. Cabina 1");
        LABELS.put("Rm. 2 Key", "Ll. Cabina 2");
        LABELS.put("Rm. 4 Key", "Ll. Cabina 4");
        LABELS.put("Rm. 6 Key", "Ll. Cabina 6");
        LABELS.put("Roar", "Rugido");
        LABELS.put("Roar of Time", "Distorsión");
        LABELS.put("Rock", "Roca");
        LABELS.put("Rock Blast", "Pedrada");
        LABELS.put("Rock Climb", "Treparrocas");
        LABELS.put("Rock Gem", "Gema Roca");
        LABELS.put("Rock Head", "Cabeza Roca");
        LABELS.put("Rock Incense", "Incienso Roca");
        LABELS.put("Rock Memory", "Disco Roca");
        LABELS.put("Rock Polish", "Pulimento");
        LABELS.put("Rock Slide", "Avalancha");
        LABELS.put("Rock Smash", "Golpe Roca");
        LABELS.put("Rock Tera Shard", "Teralito Roca");
        LABELS.put("Rock Throw", "Lanzarrocas");
        LABELS.put("Rock Tomb", "Tumba Rocas");
        LABELS.put("Rock Wrecker", "Romperrocas");
        LABELS.put("Rockium Z", "Litostal Z");
        LABELS.put("Rockruff Rock", "Roca de Rockruff");
        LABELS.put("Rocky Helmet", "Casco Dentado");
        LABELS.put("Rocky Payload", "Transportarrocas");
        LABELS.put("Role Play", "Imitación");
        LABELS.put("Roller Skates", "Patines");
        LABELS.put("Rolling Kick", "Patada Giro");
        LABELS.put("Rollout", "Rodar");
        LABELS.put("Rolycoly Coal", "Carbón de Rolycoly");
        LABELS.put("Rookidee Feather", "Pluma de Rookidee");
        LABELS.put("Room Service", "Servicio Raro");
        LABELS.put("Roost", "Respiro");
        LABELS.put("Root Fossil", "Fósil Raíz");
        LABELS.put("Rose Incense", "Incienso Floral");
        LABELS.put("Roseli Berry", "Baya Hibis");
        LABELS.put("Roto Bargain", "Cupón Rebaja");
        LABELS.put("Roto Boost", "Cupón Refuerzo");
        LABELS.put("Roto Catch", "Cupón Captura");
        LABELS.put("Roto Encounter", "Cupón Reclamo");
        LABELS.put("Roto Exp. Points", "Cupón Exp");
        LABELS.put("Roto Friendship", "Cupón Amistad");
        LABELS.put("Roto HP Restore", "Cupón PS");
        LABELS.put("Roto Hatch", "Cupón Eclosión");
        LABELS.put("Roto PP Restore", "Cupón PP");
        LABELS.put("Roto Prize Money", "Cupón Botín");
        LABELS.put("Roto Stealth", "Cupón Sigilo");
        LABELS.put("Rotom Bike", "Bici Rotom");
        LABELS.put("Rotom Catalog", "Catálogo Rotom");
        LABELS.put("Rotom Phone", "SmartRotom");
        LABELS.put("Rotom Sparks", "Electricidad de Rotom");
        LABELS.put("Rototiller", "Fertilizante");
        LABELS.put("Rough Skin", "Piel Tosca");
        LABELS.put("Round", "Canon");
        LABELS.put("Rowap Berry", "Baya Magua");
        LABELS.put("Ruby", "Rubí");
        LABELS.put("Rufflet Feather", "Pluma de Rufflet");
        LABELS.put("Ruination", "Calamidad");
        LABELS.put("Rule Book", "Reglamento");
        LABELS.put("Run Away", "Fuga");
        LABELS.put("Rusted Shield", "Escudo Oxidado");
        LABELS.put("Rusted Sword", "Espada Oxidada");
        LABELS.put("Sablenite", "Sableynita");
        LABELS.put("Sableye Gem", "Gema de Sableye");
        LABELS.put("Sachet", "Saquito Fragante");
        LABELS.put("Sacred Ash", "Ceniza Sagrada");
        LABELS.put("Sacred Fire", "Fuego Sagrado");
        LABELS.put("Sacred Sword", "Espada Santa");
        LABELS.put("Safari Ball", "Safari Ball");
        LABELS.put("Safeguard", "Velo Sagrado");
        LABELS.put("Safety Goggles", "Gafa Protectora");
        LABELS.put("Sail Fossil", "Fósil Aleta");
        LABELS.put("Salac Berry", "Baya Aslac");
        LABELS.put("Salad Mix", "Verduras");
        LABELS.put("Salamencite", "Salamencita");
        LABELS.put("Salandit Gas", "Gas de Salandit");
        LABELS.put("Salt", "Sal");
        LABELS.put("Salt Cure", "Salazón");
        LABELS.put("Salty Herba Mystica", "Especia Oculta Salada");
        LABELS.put("Sand Attack", "Ataque Arena");
        LABELS.put("Sand Force", "Poder Arena");
        LABELS.put("Sand Rush", "Ímpetu Arena");
        LABELS.put("Sand Spit", "Expulsarena");
        LABELS.put("Sand Stream", "Chorro Arena");
        LABELS.put("Sand Tomb", "Bucle Arena");
        LABELS.put("Sand Veil", "Velo Arena");
        LABELS.put("Sandile Claw", "Garra de Sandile");
        LABELS.put("Sandsear Storm", "Simún de Arena");
        LABELS.put("Sandshrew Candy", "Caramelo Sandshrew");
    }
    private static void labels23() {
        LABELS.put("Sandshrew Claw", "Garra de Sandshrew");
        LABELS.put("Sandstorm", "Tormenta de Arena");
        LABELS.put("Sandwich", "Bocadillo");
        LABELS.put("Sandygast Sand", "Arena de Sandygast");
        LABELS.put("Sap Sipper", "Herbívoro");
        LABELS.put("Sapphire", "Zafiro");
        LABELS.put("Sappy Seed", "Leafitobombas");
        LABELS.put("Sassy", "Grosera");
        LABELS.put("Sassy Mint", "Menta Grosera");
        LABELS.put("Sausages", "Salchichas");
        LABELS.put("Savage Spin-Out", "Guadaña Sedosa");
        LABELS.put("Scald", "Escaldar");
        LABELS.put("Scale Shot", "Ráfaga Escamas");
        LABELS.put("Scanner", "Escáner");
        LABELS.put("Scarlet Book", "Libro Escarlata");
        LABELS.put("Scary Face", "Cara Susto");
        LABELS.put("Scatterbug Powder", "Polvo de Scatterbug");
        LABELS.put("Sceptilite", "Sceptilita");
        LABELS.put("Schooling", "Banco");
        LABELS.put("Scizorite", "Scizorita");
        LABELS.put("Scolipite", "Scolipedita");
        LABELS.put("Scope Lens", "Periscopio");
        LABELS.put("Scorching Sands", "Arenas Ardientes");
        LABELS.put("Scovillainite", "Scovillainita");
        LABELS.put("Scraftinite", "Scraftita");
        LABELS.put("Scrappy", "Intrépido");
        LABELS.put("Scratch", "Arañazo");
        LABELS.put("Screech", "Chirrido");
        LABELS.put("Screen Cleaner", "Antibarrera");
        LABELS.put("Scroll of Darkness", "Manuscrito Sombras");
        LABELS.put("Scroll of Waters", "Manuscrito Aguas");
        LABELS.put("Scyther Candy", "Caramelo Scyther");
        LABELS.put("Scyther Claw", "Garra de Scyther");
        LABELS.put("Sea Incense", "Incienso Marino");
        LABELS.put("Seal Bag", "Bolsa Sellos");
        LABELS.put("Seal Case", "Caja Sellos");
        LABELS.put("Searing Shot", "Bomba Ígnea");
        LABELS.put("Searing Sunraze Smash", "Embestida Solar");
        LABELS.put("Secret Key", "Llave Secreta");
        LABELS.put("Secret Potion", "Poción Secreta");
        LABELS.put("Secret Power", "Daño Secreto");
        LABELS.put("Secret Sword", "Sable Místico");
        LABELS.put("Seed Bomb", "Bomba Germen");
        LABELS.put("Seed Flare", "Fulgor Semilla");
        LABELS.put("Seed Sower", "Disemillar");
        LABELS.put("Seedot Stem", "Tallo de Seedot");
        LABELS.put("Seel Candy", "Caramelo Seel");
        LABELS.put("Seismic Toss", "Sísmico");
        LABELS.put("Self-Destruct", "Autodestrucción");
        LABELS.put("Sentret Fur", "Pelo de Sentret");
        LABELS.put("Serene Grace", "Dicha");
        LABELS.put("Serious", "Seria");
        LABELS.put("Serious Mint", "Menta Seria");
        LABELS.put("Seviper Fang", "Colmillo de Seviper");
        LABELS.put("Sewaddle Leaf", "Hoja de Sewaddle");
        LABELS.put("Shaderoot Carrot", "Zanahoria Oscura");
        LABELS.put("Shadow Ball", "Bola Sombra");
        LABELS.put("Shadow Blast", "Soplo Oscuro");
        LABELS.put("Shadow Blitz", "Vigor Oscuro");
        LABELS.put("Shadow Bolt", "Rayo Oscuro");
        LABELS.put("Shadow Bone", "Hueso Sombrío");
        LABELS.put("Shadow Break", "Brío Oscuro");
        LABELS.put("Shadow Chill", "Hielo Oscuro");
        LABELS.put("Shadow Claw", "Garra Umbría");
        LABELS.put("Shadow Down", "Ocaso Oscuro");
        LABELS.put("Shadow End", "Fin Oscuro");
        LABELS.put("Shadow Fire", "Fuego Oscuro");
        LABELS.put("Shadow Force", "Golpe Umbrío");
        LABELS.put("Shadow Half", "Mitad Oscura");
        LABELS.put("Shadow Hold", "Traba Oscura");
        LABELS.put("Shadow Mail", "Carta Sombra");
        LABELS.put("Shadow Mist", "Bruma Oscura");
        LABELS.put("Shadow Panic", "Miedo Oscuro");
        LABELS.put("Shadow Punch", "Puño Sombra");
        LABELS.put("Shadow Rave", "Rabia Oscura");
        LABELS.put("Shadow Rush", "Carga Oscura");
        LABELS.put("Shadow Shed", "Muda Oscura");
        LABELS.put("Shadow Shield", "Guardia Espectro");
        LABELS.put("Shadow Sky", "Cielo Oscuro");
        LABELS.put("Shadow Sneak", "Sombra Vil");
        LABELS.put("Shadow Storm", "Tifón Oscuro");
        LABELS.put("Shadow Tag", "Sombra Trampa");
        LABELS.put("Shadow Wave", "Onda Oscura");
        LABELS.put("Shalour Sable", "Galleta Yantra");
        LABELS.put("Sharp Beak", "Pico Afilado");
        LABELS.put("Sharpedonite", "Sharpedonita");
        LABELS.put("Sharpen", "Afilar");
        LABELS.put("Sharpness", "Cortante");
        LABELS.put("Shattered Psyche", "Disruptor Psíquico");
        LABELS.put("Shed Shell", "Muda Concha");
    }
    private static void labels24() {
        LABELS.put("Shed Skin", "Mudar");
        LABELS.put("Shed Tail", "Autotomía");
        LABELS.put("Sheer Cold", "Frío Polar");
        LABELS.put("Sheer Force", "Potencia Bruta");
        LABELS.put("Shell Armor", "Caparazón");
        LABELS.put("Shell Bell", "Cascabel Concha");
        LABELS.put("Shell Side Arm", "Moluscañón");
        LABELS.put("Shell Smash", "Rompecoraza");
        LABELS.put("Shell Trap", "Coraza Trampa");
        LABELS.put("Shellder Candy", "Caramelo Shellder");
        LABELS.put("Shellder Pearl", "Perla de Shellder");
        LABELS.put("Shellos Mucus", "Secreción de Shellos");
        LABELS.put("Shelter", "Retracción");
        LABELS.put("Shield Dust", "Polvo Escudo");
        LABELS.put("Shields Down", "Escudo Limitado");
        LABELS.put("Shift Gear", "Cambio de Marcha");
        LABELS.put("Shinx Fang", "Colmillo de Shinx");
        LABELS.put("Shiny Charm", "Amuleto Iris");
        LABELS.put("Shiny Stone", "Piedra Día");
        LABELS.put("Shoal Salt", "Sal Cardumen");
        LABELS.put("Shoal Shell", "Concha Cardumen");
        LABELS.put("Shock Drive", "FulgoROM");
        LABELS.put("Shock Wave", "Onda Voltio");
        LABELS.put("Shore Up", "Recogearena");
        LABELS.put("Shroodle Ink", "Tinta de Shroodle");
        LABELS.put("Shroomish Spores", "Esporas de Shroomish");
        LABELS.put("Shuca Berry", "Baya Acardo");
        LABELS.put("Shuppet Scrap", "Retal de Shuppet");
        LABELS.put("Signal Beam", "Rayo Señal");
        LABELS.put("Silicobra Sand", "Arena de Silicobra");
        LABELS.put("Silk Scarf", "Pañuelo de Seda");
        LABELS.put("Silk Trap", "Telatrampa");
        LABELS.put("Silph Scope", "Visor Silph");
        LABELS.put("Silver Bottle", "Termo Plateado");
        LABELS.put("Silver Cup", "Taza Plateada");
        LABELS.put("Silver Leaf", "Hoja de Plata");
        LABELS.put("Silver Nanab Berry", "Baya Latano Plateada");
        LABELS.put("Silver Pick", "Palillo Plateado");
        LABELS.put("Silver Pinap Berry", "Baya Pinia Plateada");
        LABELS.put("Silver Powder", "Polvo Plata");
        LABELS.put("Silver Razz Berry", "Baya Frambu Plateada");
        LABELS.put("Silver Wind", "Viento Plata");
        LABELS.put("Silver Wing", "Ala Plateada");
        LABELS.put("Simple", "Simple");
        LABELS.put("Simple Beam", "Onda Simple");
        LABELS.put("Sing", "Canto");
        LABELS.put("Sinistea Chip", "Fragmento de Sinistea");
        LABELS.put("Sinister Arrow Raid", "Aluvión de Flechas Sombrías");
        LABELS.put("Sitrus Berry", "Baya Zidra");
        LABELS.put("Sizzly Slide", "Flarembestida");
        LABELS.put("Skarmorite", "Skarmorita");
        LABELS.put("Sketch", "Esquema");
        LABELS.put("Skiddo Leaf", "Hoja de Skiddo");
        LABELS.put("Skill Link", "Encadenado");
        LABELS.put("Skill Swap", "Intercambio");
        LABELS.put("Skitter Smack", "Golpe Rastrero");
        LABELS.put("Skrelp Kelp", "Alga de Skrelp");
        LABELS.put("Skull Bash", "Cabezazo");
        LABELS.put("Skull Fossil", "Fósil Cráneo");
        LABELS.put("Skwovet Fur", "Pelo de Skwovet");
        LABELS.put("Sky Attack", "Ataque Aéreo");
        LABELS.put("Sky Drop", "Caída Libre");
        LABELS.put("Sky Plate", "Tabla Cielo");
        LABELS.put("Sky Uppercut", "Gancho Alto");
        LABELS.put("Slack Off", "Relajo");
        LABELS.put("Slakoth Fur", "Pelo de Slakoth");
        LABELS.put("Slam", "Atizar");
        LABELS.put("Slash", "Cuchillada");
        LABELS.put("Sleep Powder", "Somnífero");
        LABELS.put("Sleep Talk", "Sonámbulo");
        LABELS.put("Slow Start", "Inicio Lento");
        LABELS.put("Slowbronite", "Slowbronita");
        LABELS.put("Slowpoke Candy", "Caramelo Slowpoke");
        LABELS.put("Slowpoke Claw", "Garra de Slowpoke");
        LABELS.put("Slowpoke Cup", "Taza Slowpoke");
        LABELS.put("Slowpoke Tail", "Cola Slowpoke");
        LABELS.put("Sludge", "Residuos");
        LABELS.put("Sludge Bomb", "Bomba Lodo");
        LABELS.put("Sludge Wave", "Onda Tóxica");
        LABELS.put("Slugma Lava", "Lava de Slugma");
        LABELS.put("Slush Rush", "Quitanieves");
        LABELS.put("Smack Down", "Antiaéreo");
        LABELS.put("Small Bouquet", "Ramo Pequeño");
        LABELS.put("Smart Candy", "Caramelo Intelecto");
        LABELS.put("Smart Candy L", "Caramelo Intelecto +");
        LABELS.put("Smart Candy XL", "Caramelo Intelecto ++");
        LABELS.put("Smart Strike", "Cuerno Certero");
        LABELS.put("Smelling Salts", "Estímulo");
        LABELS.put("Smiling Vee Pick", "Palillo Eevee Feliz");
        LABELS.put("Smog", "Polución");
    }
    private static void labels25() {
        LABELS.put("Smoke Ball", "Bola de Humo");
        LABELS.put("Smoke-Poke Tail", "Cola Ahumada");
        LABELS.put("Smoked Fillet", "Filete Ahumado");
        LABELS.put("Smokescreen", "Pantalla de Humo");
        LABELS.put("Smoliv Oil", "Aceite de Smoliv");
        LABELS.put("Smooth Rock", "Roca Suave");
        LABELS.put("Snap Trap", "Cepo");
        LABELS.put("Snarl", "Alarido");
        LABELS.put("Snatch", "Robo");
        LABELS.put("Sneasel Claw", "Garra de Sneasel");
        LABELS.put("Snipe Shot", "Disparo Certero");
        LABELS.put("Sniper", "Francotirador");
        LABELS.put("Snom Thread", "Hilo de Snom");
        LABELS.put("Snore", "Ronquido");
        LABELS.put("Snorlax Candy", "Caramelo Snorlax");
        LABELS.put("Snorlium Z", "Snorlastal Z");
        LABELS.put("Snorunt Fur", "Pelo de Snorunt");
        LABELS.put("Snover Berries", "Baya de Snover");
        LABELS.put("Snow Cloak", "Manto Níveo");
        LABELS.put("Snow Mail", "Carta Nieve");
        LABELS.put("Snow Warning", "Nevada");
        LABELS.put("Snowball", "Bola de Nieve");
        LABELS.put("Snowscape", "Paisaje Nevado");
        LABELS.put("Soak", "Empapar");
        LABELS.put("Soda Pop", "Refresco");
        LABELS.put("Soft Sand", "Arena Fina");
        LABELS.put("Soft-Boiled", "Ovocuración");
        LABELS.put("Solar Beam", "Rayo Solar");
        LABELS.put("Solar Blade", "Cuchilla Solar");
        LABELS.put("Solar Power", "Poder Solar");
        LABELS.put("Solganium Z", "Solgaleostal Z");
        LABELS.put("Solid Rock", "Roca Sólida");
        LABELS.put("Sonia’s Book", "Libro de Sonia");
        LABELS.put("Sonic Boom", "Bomba Sónica");
        LABELS.put("Soot Sack", "Saco Hollín");
        LABELS.put("Soothe Bell", "Cascabel Alivio");
        LABELS.put("Soul Dew", "Rocío Bondad");
        LABELS.put("Soul-Heart", "Coránima");
        LABELS.put("Soul-Stealing 7-Star Strike", "Constelación Robaalmas");
        LABELS.put("Soundproof", "Insonorizar");
        LABELS.put("Sour Herba Mystica", "Especia Oculta Ácida");
        LABELS.put("Space Mail", "Car. Sideral");
        LABELS.put("Spacial Rend", "Corte Vacío");
        LABELS.put("Spark", "Chispa");
        LABELS.put("Sparkling Aria", "Aria Burbuja");
        LABELS.put("Sparkling Stone", "Piedra Brillante");
        LABELS.put("Sparkly Swirl", "Sylveotornado");
        LABELS.put("Spearow Candy", "Caramelo Spearow");
        LABELS.put("Spectral Thief", "Robasombra");
        LABELS.put("Speed Boost", "Impulso");
        LABELS.put("Speed Swap", "Cambiavelocidad");
        LABELS.put("Spell Tag", "Hechizo");
        LABELS.put("Spelon Berry", "Baya Wikano");
        LABELS.put("Spice Mix", "Especias");
        LABELS.put("Spicy Extract", "Extracto Picante");
        LABELS.put("Spicy Herba Mystica", "Especia Oculta Picante");
        LABELS.put("Spicy Spray", "Salpicante");
        LABELS.put("Spider Web", "Telaraña");
        LABELS.put("Spike Cannon", "Clavo Cañón");
        LABELS.put("Spikes", "Púas");
        LABELS.put("Spiky Shield", "Barrera Espinosa");
        LABELS.put("Spin Out", "Quemarrueda");
        LABELS.put("Spinarak Thread", "Hilo de Spinarak");
        LABELS.put("Spirit Break", "Choque Anímico");
        LABELS.put("Spirit Shackle", "Puntada Sombría");
        LABELS.put("Spiritomb Fragment", "Fragmento de Spiritomb");
        LABELS.put("Spit Up", "Escupir");
        LABELS.put("Spite", "Rencor");
        LABELS.put("Splash", "Salpicadura");
        LABELS.put("Splash Plate", "Tabla Linfa");
        LABELS.put("Splintered Stormshards", "Tempestad Rocosa");
        LABELS.put("Splishy Splash", "Salpikasurf");
        LABELS.put("Spoink Pearl", "Perla de Spoink");
        LABELS.put("Spooky Plate", "Tabla Terror");
        LABELS.put("Spooky Tablecloth", "Mantel Espectral");
        LABELS.put("Spore", "Espora");
        LABELS.put("Sport Ball", "Competi Ball");
        LABELS.put("Spotlight", "Foco");
        LABELS.put("Sprayduck", "Psydugadera");
        LABELS.put("Springtide Storm", "Ciclón Primavera");
        LABELS.put("Sprinklotad", "Lotadgadera");
        LABELS.put("Squawkabilly Feather", "Pluma de Squawkabilly");
        LABELS.put("Squirt Bottle", "Regadera");
        LABELS.put("Squirtle Candy", "Caramelo Squirtle");
        LABELS.put("Stable Mulch", "Abono Fijador");
        LABELS.put("Stakeout", "Vigilante");
        LABELS.put("Stall", "Rezagado");
        LABELS.put("Stalwart", "Acérrimo");
        LABELS.put("Stamina", "Firmeza");
        LABELS.put("Stance Change", "Cambio Táctico");
    }
    private static void labels26() {
        LABELS.put("Stantler Hair", "Pelo de Stantler");
        LABELS.put("Star Piece", "Trozo Estrella");
        LABELS.put("Star Sweet", "Confite Estrella");
        LABELS.put("Staraptite", "Staraptorita");
        LABELS.put("Stardust", "Polvo Estelar");
        LABELS.put("Starf Berry", "Baya Arabol");
        LABELS.put("Starly Feather", "Pluma de Starly");
        LABELS.put("Starminite", "Starmita");
        LABELS.put("Staryu Candy", "Caramelo Staryu");
        LABELS.put("Static", "Electricidad Estática");
        LABELS.put("Steadfast", "Impasible");
        LABELS.put("Stealth Rock", "Trampa Rocas");
        LABELS.put("Steam Engine", "Combustible");
        LABELS.put("Steam Eruption", "Chorro de Vapor");
        LABELS.put("Steamroller", "Rodillo de Púas");
        LABELS.put("Steel", "Acero");
        LABELS.put("Steel Beam", "Metaláser");
        LABELS.put("Steel Bottle (B)", "Termo Metal Azul");
        LABELS.put("Steel Bottle (R)", "Termo Metal Rojo");
        LABELS.put("Steel Bottle (Y)", "Termo Metal Amarillo");
        LABELS.put("Steel Gem", "Gema Acero");
        LABELS.put("Steel Mail", "Carta Acero");
        LABELS.put("Steel Memory", "Disco Acero");
        LABELS.put("Steel Roller", "Allanador Férreo");
        LABELS.put("Steel Tera Shard", "Teralito Acero");
        LABELS.put("Steel Wing", "Ala de Acero");
        LABELS.put("Steelium Z", "Metalostal Z");
        LABELS.put("Steelixite", "Steelixita");
        LABELS.put("Steelworker", "Acero Templado");
        LABELS.put("Steely Spirit", "Alma Acerada");
        LABELS.put("Stellar", "Astral");
        LABELS.put("Stench", "Hedor");
        LABELS.put("Sticky Barb", "Toxiestrella");
        LABELS.put("Sticky Hold", "Viscosidad");
        LABELS.put("Sticky Web", "Red Viscosa");
        LABELS.put("Stockpile", "Reserva");
        LABELS.put("Stoked Sparksurfer", "Surfeo Galvánico");
        LABELS.put("Stomp", "Pisotón");
        LABELS.put("Stomping Tantrum", "Pataleta");
        LABELS.put("Stone Axe", "Hachazo Pétreo");
        LABELS.put("Stone Edge", "Roca Afilada");
        LABELS.put("Stone Plate", "Tabla Pétrea");
        LABELS.put("Stonjourner Stone", "Roca de Stonjourner");
        LABELS.put("Storage Key", "Llave Almacén");
        LABELS.put("Stored Power", "Poder Reserva");
        LABELS.put("Storm Drain", "Colector");
        LABELS.put("Storm Throw", "Llave Corsé");
        LABELS.put("Strange Ball", "Extraña Ball");
        LABELS.put("Strange Souvenir", "Estatuilla Rara");
        LABELS.put("Strange Steam", "Cautivapor");
        LABELS.put("Strawberry", "Fresa");
        LABELS.put("Strawberry Sweet", "Confite Fresa");
        LABELS.put("Strength", "Fuerza");
        LABELS.put("Strength Sap", "Absorbefuerza");
        LABELS.put("Stretchy Spring", "Muelle Estirado");
        LABELS.put("String Shot", "Disparo Demora");
        LABELS.put("Striped Bottle", "Termo Rayas");
        LABELS.put("Striped Cup", "Taza Raya Vertical");
        LABELS.put("Striped Tablecloth", "Mantel Rayas");
        LABELS.put("Strong Jaw", "Mandíbula Fuerte");
        LABELS.put("Struggle", "Forcejeo");
        LABELS.put("Struggle Bug", "Estoicismo");
        LABELS.put("Stuff Cheeks", "Atiborramiento");
        LABELS.put("Stun Spore", "Paralizador");
        LABELS.put("Stunky Fur", "Pelo de Stunky");
        LABELS.put("Sturdy", "Robustez");
        LABELS.put("Style Card", "Tarjeta Chic");
        LABELS.put("Submission", "Sumisión");
        LABELS.put("Substitute", "Sustituto");
        LABELS.put("Subzero Slammer", "Crioaliento Despiadado");
        LABELS.put("Sucker Punch", "Golpe Bajo");
        LABELS.put("Suction Cups", "Ventosas");
        LABELS.put("Suite Key", "Llave Suite");
        LABELS.put("Sun Flute", "Flauta Solar");
        LABELS.put("Sun Stone", "Piedra Solar");
        LABELS.put("Sunkern Leaf", "Hoja de Sunkern");
        LABELS.put("Sunny Day", "Día Soleado");
        LABELS.put("Sunrise Flower Pick", "Palillo Flor Rosa");
        LABELS.put("Sunset Flower Pick", "Palillo Flor Naranja");
        LABELS.put("Sunsteel Strike", "Meteoimpacto");
        LABELS.put("Super Fang", "Superdiente");
        LABELS.put("Super Luck", "Afortunado");
        LABELS.put("Super Lure", "Supercolonia");
        LABELS.put("Super Potion", "Superpoción");
        LABELS.put("Super Repel", "Superrepelente");
        LABELS.put("Super Rod", "Supercaña");
        LABELS.put("Supercell Slam", "Plancha Voltaica");
        LABELS.put("Superpower", "Fuerza Bruta");
        LABELS.put("Supersonic", "Supersónico");
        LABELS.put("Supersonic Skystrike", "Picado Supersónico");
    }
    private static void labels27() {
        LABELS.put("Supersweet Syrup", "Néctar Dulce");
        LABELS.put("Supreme Overlord", "General Supremo");
        LABELS.put("Surf", "Surf");
        LABELS.put("Surge Badge", "Medalla Fulgor");
        LABELS.put("Surge Surfer", "Cola Surf");
        LABELS.put("Surging Strikes", "Azote Torrencial");
        LABELS.put("Surprise Mulch", "Abono Sorpresa");
        LABELS.put("Surskit Syrup", "Néctar de Surskit");
        LABELS.put("Swablu Fluff", "Pluma de Swablu");
        LABELS.put("Swagger", "Fanfarronear");
        LABELS.put("Swallow", "Tragar");
        LABELS.put("Swampertite", "Swampertita");
        LABELS.put("Swarm", "Enjambre");
        LABELS.put("Sweet Apple", "Manzana Dulce");
        LABELS.put("Sweet Heart", "Corazón Dulce");
        LABELS.put("Sweet Herba Mystica", "Especia Oculta Dulce");
        LABELS.put("Sweet Kiss", "Beso Dulce");
        LABELS.put("Sweet Scent", "Dulce Aroma");
        LABELS.put("Sweet Veil", "Velo Dulce");
        LABELS.put("Swift", "Meteoros");
        LABELS.put("Swift Feather", "Pluma Ímpetu");
        LABELS.put("Swift Mochi", "Mochi Ímpetu");
        LABELS.put("Swift Swim", "Nado Rápido");
        LABELS.put("Swinub Hair", "Pelo de Swinub");
        LABELS.put("Switcheroo", "Trapicheo");
        LABELS.put("Sword of Ruin", "Espada Debacle");
        LABELS.put("Swords Dance", "Danza Espada");
        LABELS.put("Symbiosis", "Simbiosis");
        LABELS.put("Synchronize", "Sincronía");
        LABELS.put("Synchronoise", "Sincrorruido");
        LABELS.put("Synthesis", "Fotosíntesis");
        LABELS.put("Syrup Bomb", "Bomba Caramelo");
        LABELS.put("Syrupy Apple", "Manzana Melosa");
        LABELS.put("TM Case", "Estuche de MT");
        LABELS.put("TM00", "MT00");
        LABELS.put("TM01", "MT01");
        LABELS.put("TM02", "MT02");
        LABELS.put("TM03", "MT03");
        LABELS.put("TM04", "MT04");
        LABELS.put("TM05", "MT05");
        LABELS.put("TM06", "MT06");
        LABELS.put("TM07", "MT07");
        LABELS.put("TM08", "MT08");
        LABELS.put("TM09", "MT09");
        LABELS.put("TM10", "MT10");
        LABELS.put("TM100", "MT100");
        LABELS.put("TM101", "MT101");
        LABELS.put("TM102", "MT102");
        LABELS.put("TM103", "MT103");
        LABELS.put("TM104", "MT104");
        LABELS.put("TM105", "MT105");
        LABELS.put("TM106", "MT106");
        LABELS.put("TM107", "MT107");
        LABELS.put("TM108", "MT108");
        LABELS.put("TM109", "MT109");
        LABELS.put("TM11", "MT11");
        LABELS.put("TM110", "MT110");
        LABELS.put("TM111", "MT111");
        LABELS.put("TM112", "MT112");
        LABELS.put("TM113", "MT113");
        LABELS.put("TM114", "MT114");
        LABELS.put("TM115", "MT115");
        LABELS.put("TM116", "MT116");
        LABELS.put("TM117", "MT117");
        LABELS.put("TM118", "MT118");
        LABELS.put("TM119", "MT119");
        LABELS.put("TM12", "MT12");
        LABELS.put("TM120", "MT120");
        LABELS.put("TM121", "MT121");
        LABELS.put("TM122", "MT122");
        LABELS.put("TM123", "MT123");
        LABELS.put("TM124", "MT124");
        LABELS.put("TM125", "MT125");
        LABELS.put("TM126", "MT126");
        LABELS.put("TM127", "MT127");
        LABELS.put("TM128", "MT128");
        LABELS.put("TM129", "MT129");
        LABELS.put("TM13", "MT13");
        LABELS.put("TM130", "MT130");
        LABELS.put("TM131", "MT131");
        LABELS.put("TM132", "MT132");
        LABELS.put("TM133", "MT133");
        LABELS.put("TM134", "MT134");
        LABELS.put("TM135", "MT135");
        LABELS.put("TM136", "MT136");
        LABELS.put("TM137", "MT137");
        LABELS.put("TM138", "MT138");
        LABELS.put("TM139", "MT139");
        LABELS.put("TM14", "MT14");
        LABELS.put("TM140", "MT140");
    }
    private static void labels28() {
        LABELS.put("TM141", "MT141");
        LABELS.put("TM142", "MT142");
        LABELS.put("TM143", "MT143");
        LABELS.put("TM144", "MT144");
        LABELS.put("TM145", "MT145");
        LABELS.put("TM146", "MT146");
        LABELS.put("TM147", "MT147");
        LABELS.put("TM148", "MT148");
        LABELS.put("TM149", "MT149");
        LABELS.put("TM15", "MT15");
        LABELS.put("TM150", "MT150");
        LABELS.put("TM151", "MT151");
        LABELS.put("TM152", "MT152");
        LABELS.put("TM153", "MT153");
        LABELS.put("TM154", "MT154");
        LABELS.put("TM155", "MT155");
        LABELS.put("TM156", "MT156");
        LABELS.put("TM157", "MT157");
        LABELS.put("TM158", "MT158");
        LABELS.put("TM159", "MT159");
        LABELS.put("TM16", "MT16");
        LABELS.put("TM160", "MT160");
        LABELS.put("TM161", "MT161");
        LABELS.put("TM162", "MT162");
        LABELS.put("TM163", "MT163");
        LABELS.put("TM164", "MT164");
        LABELS.put("TM165", "MT165");
        LABELS.put("TM166", "MT166");
        LABELS.put("TM167", "MT167");
        LABELS.put("TM168", "MT168");
        LABELS.put("TM169", "MT169");
        LABELS.put("TM17", "MT17");
        LABELS.put("TM170", "MT170");
        LABELS.put("TM171", "MT171");
        LABELS.put("TM172", "MT172");
        LABELS.put("TM173", "MT173");
        LABELS.put("TM174", "MT174");
        LABELS.put("TM175", "MT175");
        LABELS.put("TM176", "MT176");
        LABELS.put("TM177", "MT177");
        LABELS.put("TM178", "MT178");
        LABELS.put("TM179", "MT179");
        LABELS.put("TM18", "MT18");
        LABELS.put("TM180", "MT180");
        LABELS.put("TM181", "MT181");
        LABELS.put("TM182", "MT182");
        LABELS.put("TM183", "MT183");
        LABELS.put("TM184", "MT184");
        LABELS.put("TM185", "MT185");
        LABELS.put("TM186", "MT186");
        LABELS.put("TM187", "MT187");
        LABELS.put("TM188", "MT188");
        LABELS.put("TM189", "MT189");
        LABELS.put("TM19", "MT19");
        LABELS.put("TM190", "MT190");
        LABELS.put("TM191", "MT191");
        LABELS.put("TM192", "MT192");
        LABELS.put("TM193", "MT193");
        LABELS.put("TM194", "MT194");
        LABELS.put("TM195", "MT195");
        LABELS.put("TM196", "MT196");
        LABELS.put("TM197", "MT197");
        LABELS.put("TM198", "MT198");
        LABELS.put("TM199", "MT199");
        LABELS.put("TM20", "MT20");
        LABELS.put("TM200", "MT200");
        LABELS.put("TM201", "MT201");
        LABELS.put("TM202", "MT202");
        LABELS.put("TM203", "MT203");
        LABELS.put("TM204", "MT204");
        LABELS.put("TM205", "MT205");
        LABELS.put("TM206", "MT206");
        LABELS.put("TM207", "MT207");
        LABELS.put("TM208", "MT208");
        LABELS.put("TM209", "MT209");
        LABELS.put("TM21", "MT21");
        LABELS.put("TM210", "MT210");
        LABELS.put("TM211", "MT211");
        LABELS.put("TM212", "MT212");
        LABELS.put("TM213", "MT213");
        LABELS.put("TM214", "MT214");
        LABELS.put("TM215", "MT215");
        LABELS.put("TM216", "MT216");
        LABELS.put("TM217", "MT217");
        LABELS.put("TM218", "MT218");
        LABELS.put("TM219", "MT219");
        LABELS.put("TM22", "MT22");
        LABELS.put("TM220", "MT220");
        LABELS.put("TM221", "MT221");
        LABELS.put("TM222", "MT222");
    }
    private static void labels29() {
        LABELS.put("TM223", "MT223");
        LABELS.put("TM224", "MT224");
        LABELS.put("TM225", "MT225");
        LABELS.put("TM226", "MT226");
        LABELS.put("TM227", "MT227");
        LABELS.put("TM228", "MT228");
        LABELS.put("TM229", "MT229");
        LABELS.put("TM23", "MT23");
        LABELS.put("TM24", "MT24");
        LABELS.put("TM25", "MT25");
        LABELS.put("TM26", "MT26");
        LABELS.put("TM27", "MT27");
        LABELS.put("TM28", "MT28");
        LABELS.put("TM29", "MT29");
        LABELS.put("TM30", "MT30");
        LABELS.put("TM31", "MT31");
        LABELS.put("TM32", "MT32");
        LABELS.put("TM33", "MT33");
        LABELS.put("TM34", "MT34");
        LABELS.put("TM35", "MT35");
        LABELS.put("TM36", "MT36");
        LABELS.put("TM37", "MT37");
        LABELS.put("TM38", "MT38");
        LABELS.put("TM39", "MT39");
        LABELS.put("TM40", "MT40");
        LABELS.put("TM41", "MT41");
        LABELS.put("TM42", "MT42");
        LABELS.put("TM43", "MT43");
        LABELS.put("TM44", "MT44");
        LABELS.put("TM45", "MT45");
        LABELS.put("TM46", "MT46");
        LABELS.put("TM47", "MT47");
        LABELS.put("TM48", "MT48");
        LABELS.put("TM49", "MT49");
        LABELS.put("TM50", "MT50");
        LABELS.put("TM51", "MT51");
        LABELS.put("TM52", "MT52");
        LABELS.put("TM53", "MT53");
        LABELS.put("TM54", "MT54");
        LABELS.put("TM55", "MT55");
        LABELS.put("TM56", "MT56");
        LABELS.put("TM57", "MT57");
        LABELS.put("TM58", "MT58");
        LABELS.put("TM59", "MT59");
        LABELS.put("TM60", "MT60");
        LABELS.put("TM61", "MT61");
        LABELS.put("TM62", "MT62");
        LABELS.put("TM63", "MT63");
        LABELS.put("TM64", "MT64");
        LABELS.put("TM65", "MT65");
        LABELS.put("TM66", "MT66");
        LABELS.put("TM67", "MT67");
        LABELS.put("TM68", "MT68");
        LABELS.put("TM69", "MT69");
        LABELS.put("TM70", "MT70");
        LABELS.put("TM71", "MT71");
        LABELS.put("TM72", "MT72");
        LABELS.put("TM73", "MT73");
        LABELS.put("TM74", "MT74");
        LABELS.put("TM75", "MT75");
        LABELS.put("TM76", "MT76");
        LABELS.put("TM77", "MT77");
        LABELS.put("TM78", "MT78");
        LABELS.put("TM79", "MT79");
        LABELS.put("TM80", "MT80");
        LABELS.put("TM81", "MT81");
        LABELS.put("TM82", "MT82");
        LABELS.put("TM83", "MT83");
        LABELS.put("TM84", "MT84");
        LABELS.put("TM85", "MT85");
        LABELS.put("TM86", "MT86");
        LABELS.put("TM87", "MT87");
        LABELS.put("TM88", "MT88");
        LABELS.put("TM89", "MT89");
        LABELS.put("TM90", "MT90");
        LABELS.put("TM91", "MT91");
        LABELS.put("TM92", "MT92");
        LABELS.put("TM93", "MT93");
        LABELS.put("TM94", "MT94");
        LABELS.put("TM95", "MT95");
        LABELS.put("TM96", "MT96");
        LABELS.put("TM97", "MT97");
        LABELS.put("TM98", "MT98");
        LABELS.put("TM99", "MT99");
        LABELS.put("TMV Pass", "Abono del TMV");
        LABELS.put("TR00", "DT00");
        LABELS.put("TR01", "DT01");
        LABELS.put("TR02", "DT02");
        LABELS.put("TR03", "DT03");
        LABELS.put("TR04", "DT04");
    }
    private static void labels30() {
        LABELS.put("TR05", "DT05");
        LABELS.put("TR06", "DT06");
        LABELS.put("TR07", "DT07");
        LABELS.put("TR08", "DT08");
        LABELS.put("TR09", "DT09");
        LABELS.put("TR10", "DT10");
        LABELS.put("TR11", "DT11");
        LABELS.put("TR12", "DT12");
        LABELS.put("TR13", "DT13");
        LABELS.put("TR14", "DT14");
        LABELS.put("TR15", "DT15");
        LABELS.put("TR16", "DT16");
        LABELS.put("TR17", "DT17");
        LABELS.put("TR18", "DT18");
        LABELS.put("TR19", "DT19");
        LABELS.put("TR20", "DT20");
        LABELS.put("TR21", "DT21");
        LABELS.put("TR22", "DT22");
        LABELS.put("TR23", "DT23");
        LABELS.put("TR24", "DT24");
        LABELS.put("TR25", "DT25");
        LABELS.put("TR26", "DT26");
        LABELS.put("TR27", "DT27");
        LABELS.put("TR28", "DT28");
        LABELS.put("TR29", "DT29");
        LABELS.put("TR30", "DT30");
        LABELS.put("TR31", "DT31");
        LABELS.put("TR32", "DT32");
        LABELS.put("TR33", "DT33");
        LABELS.put("TR34", "DT34");
        LABELS.put("TR35", "DT35");
        LABELS.put("TR36", "DT36");
        LABELS.put("TR37", "DT37");
        LABELS.put("TR38", "DT38");
        LABELS.put("TR39", "DT39");
        LABELS.put("TR40", "DT40");
        LABELS.put("TR41", "DT41");
        LABELS.put("TR42", "DT42");
        LABELS.put("TR43", "DT43");
        LABELS.put("TR44", "DT44");
        LABELS.put("TR45", "DT45");
        LABELS.put("TR46", "DT46");
        LABELS.put("TR47", "DT47");
        LABELS.put("TR48", "DT48");
        LABELS.put("TR49", "DT49");
        LABELS.put("TR50", "DT50");
        LABELS.put("TR51", "DT51");
        LABELS.put("TR52", "DT52");
        LABELS.put("TR53", "DT53");
        LABELS.put("TR54", "DT54");
        LABELS.put("TR55", "DT55");
        LABELS.put("TR56", "DT56");
        LABELS.put("TR57", "DT57");
        LABELS.put("TR58", "DT58");
        LABELS.put("TR59", "DT59");
        LABELS.put("TR60", "DT60");
        LABELS.put("TR61", "DT61");
        LABELS.put("TR62", "DT62");
        LABELS.put("TR63", "DT63");
        LABELS.put("TR64", "DT64");
        LABELS.put("TR65", "DT65");
        LABELS.put("TR66", "DT66");
        LABELS.put("TR67", "DT67");
        LABELS.put("TR68", "DT68");
        LABELS.put("TR69", "DT69");
        LABELS.put("TR70", "DT70");
        LABELS.put("TR71", "DT71");
        LABELS.put("TR72", "DT72");
        LABELS.put("TR73", "DT73");
        LABELS.put("TR74", "DT74");
        LABELS.put("TR75", "DT75");
        LABELS.put("TR76", "DT76");
        LABELS.put("TR77", "DT77");
        LABELS.put("TR78", "DT78");
        LABELS.put("TR79", "DT79");
        LABELS.put("TR80", "DT80");
        LABELS.put("TR81", "DT81");
        LABELS.put("TR82", "DT82");
        LABELS.put("TR83", "DT83");
        LABELS.put("TR84", "DT84");
        LABELS.put("TR85", "DT85");
        LABELS.put("TR86", "DT86");
        LABELS.put("TR87", "DT87");
        LABELS.put("TR88", "DT88");
        LABELS.put("TR89", "DT89");
        LABELS.put("TR90", "DT90");
        LABELS.put("TR91", "DT91");
        LABELS.put("TR92", "DT92");
        LABELS.put("TR93", "DT93");
        LABELS.put("TR94", "DT94");
    }
    private static void labels31() {
        LABELS.put("TR95", "DT95");
        LABELS.put("TR96", "DT96");
        LABELS.put("TR97", "DT97");
        LABELS.put("TR98", "DT98");
        LABELS.put("TR99", "DT99");
        LABELS.put("Tablets of Ruin", "Tablilla Debacle");
        LABELS.put("Tachyon Cutter", "Tajo Taquión");
        LABELS.put("Tackle", "Placaje");
        LABELS.put("Tadbulb Mucus", "Secreción de Tadbulb");
        LABELS.put("Tail Glow", "Luminicola");
        LABELS.put("Tail Slap", "Plumerazo");
        LABELS.put("Tail Whip", "Agitacola");
        LABELS.put("Tailwind", "Viento Afín");
        LABELS.put("Take Down", "Derribo");
        LABELS.put("Take Heart", "Bálsamo Osado");
        LABELS.put("Tamato Berry", "Baya Tamate");
        LABELS.put("Tandemaus Fur", "Pelo de Tandemaus");
        LABELS.put("Tanga Berry", "Baya Yecana");
        LABELS.put("Tangela Candy", "Caramelo Tangela");
        LABELS.put("Tangled Feet", "Tumbos");
        LABELS.put("Tangling Hair", "Rizos Rebeldes");
        LABELS.put("Tapunium Z", "Tapistal Z");
        LABELS.put("Tar Shot", "Alquitranazo");
        LABELS.put("Tarountula Thread", "Hilo de Tarountula");
        LABELS.put("Tart Apple", "Manzana Ácida");
        LABELS.put("Tatsugiri Scales", "Escama de Tatsugiri");
        LABELS.put("Tatsugirinite", "Tatsugirita");
        LABELS.put("Taunt", "Mofa");
        LABELS.put("Tauros Candy", "Caramelo Tauros");
        LABELS.put("Tauros Hair", "Pelo de Tauros");
        LABELS.put("Tea", "Té");
        LABELS.put("Teachy TV", "Poké Tele");
        LABELS.put("Teal Mask", "Máscara Turquesa");
        LABELS.put("Teal Style Card", "Tarjeta Chic Verde");
        LABELS.put("Tearful Look", "Ojos Llorosos");
        LABELS.put("Teatime", "Hora del Té");
        LABELS.put("Technician", "Experto");
        LABELS.put("Techno Blast", "Tecno Shock");
        LABELS.put("Tectonic Rage", "Barrena Telúrica");
        LABELS.put("Teddiursa Claw", "Garra de Teddiursa");
        LABELS.put("Teeter Dance", "Danza Caos");
        LABELS.put("Telekinesis", "Telequinesis");
        LABELS.put("Telepathy", "Telepatía");
        LABELS.put("Teleport", "Teletransporte");
        LABELS.put("Temper Flare", "Cólera Ardiente");
        LABELS.put("Tentacool Candy", "Caramelo Tentacool");
        LABELS.put("Tera Blast", "Teraexplosión");
        LABELS.put("Tera Orb", "Orbe Teracristal");
        LABELS.put("Tera Shell", "Teracaparazón");
        LABELS.put("Tera Shift", "Teracambio");
        LABELS.put("Tera Starstorm", "Teraclúster");
        LABELS.put("Teraform Zero", "Teraformación 0");
        LABELS.put("Teravolt", "Terravoltaje");
        LABELS.put("Terrain Extender", "Cubresuelos");
        LABELS.put("Terrain Pulse", "Pulso de Campo");
        LABELS.put("Thanks Mail", "Carta Gracias");
        LABELS.put("Thermal Exchange", "Termoconversión");
        LABELS.put("Thick Club", "Hueso Grueso");
        LABELS.put("Thick Fat", "Sebo");
        LABELS.put("Thief", "Ladrón");
        LABELS.put("Thousand Arrows", "Mil Flechas");
        LABELS.put("Thousand Waves", "Mil Temblores");
        LABELS.put("Thrash", "Saña");
        LABELS.put("Throat Chop", "Golpe Mordaza");
        LABELS.put("Throat Spray", "Espray Bucal");
        LABELS.put("Thunder", "Trueno");
        LABELS.put("Thunder Cage", "Electrojaula");
        LABELS.put("Thunder Fang", "Colmillo Rayo");
        LABELS.put("Thunder Punch", "Puño Trueno");
        LABELS.put("Thunder Shock", "Impactrueno");
        LABELS.put("Thunder Stone", "Piedra Trueno");
        LABELS.put("Thunder Wave", "Onda Trueno");
        LABELS.put("Thunderbolt", "Rayo");
        LABELS.put("Thunderclap", "Relámpago Súbito");
        LABELS.put("Thunderous Kick", "Patada Relámpago");
        LABELS.put("Tickle", "Cosquillas");
        LABELS.put("Tidal Bell", "Cascabel Oleaje");
        LABELS.put("Tidy Up", "Limpieza General");
        LABELS.put("Timburr Sweat", "Sudor de Timburr");
        LABELS.put("Timer Ball", "Turno Ball");
        LABELS.put("Timid", "Miedosa");
        LABELS.put("Timid Mint", "Menta Miedosa");
        LABELS.put("Tin of Beans", "Lata de Habas");
        LABELS.put("Tinkatink Hair", "Pelo de Tinkatink");
        LABELS.put("Tinted Lens", "Cromolente");
        LABELS.put("Tiny Bamboo Shoot", "Bambú Pequeño");
        LABELS.put("Tiny Mushroom", "Miniseta");
        LABELS.put("Toedscool Flaps", "Pliegue de Toedscool");
        LABELS.put("Tofu", "Tofu");
        LABELS.put("Tomato", "Tomate");
    }
    private static void labels32() {
        LABELS.put("Topsy-Turvy", "Reversión");
        LABELS.put("Torch Song", "Canto Ardiente");
        LABELS.put("Torkoal Coal", "Carbón de Torkoal");
        LABELS.put("Torment", "Tormento");
        LABELS.put("Torrent", "Torrente");
        LABELS.put("Tough Candy", "Caramelo Aguante");
        LABELS.put("Tough Candy L", "Caramelo Aguante +");
        LABELS.put("Tough Candy XL", "Caramelo Aguante ++");
        LABELS.put("Tough Claws", "Garra Dura");
        LABELS.put("Town Map", "Mapa");
        LABELS.put("Toxel Sparks", "Electricidad de Toxel");
        LABELS.put("Toxic", "Tóxico");
        LABELS.put("Toxic Boost", "Ímpetu Tóxico");
        LABELS.put("Toxic Chain", "Cadena Tóxica");
        LABELS.put("Toxic Debris", "Capa Tóxica");
        LABELS.put("Toxic Orb", "Toxisfera");
        LABELS.put("Toxic Plate", "Tabla Tóxica");
        LABELS.put("Toxic Spikes", "Púas Tóxicas");
        LABELS.put("Toxic Thread", "Hilo Venenoso");
        LABELS.put("Trace", "Calco");
        LABELS.put("Trailblaze", "Abrecaminos");
        LABELS.put("Transform", "Transformación");
        LABELS.put("Transistor", "Transistor");
        LABELS.put("Travel Trunk", "Maleta");
        LABELS.put("Tri Attack", "Triataque");
        LABELS.put("Tri-Pass", "Tri-Ticket");
        LABELS.put("Triage", "Primer Auxilio");
        LABELS.put("Trick", "Truco");
        LABELS.put("Trick Room", "Espacio Raro");
        LABELS.put("Trick-or-Treat", "Halloween");
        LABELS.put("Triple Arrows", "Triple Flecha");
        LABELS.put("Triple Axel", "Triple Axel");
        LABELS.put("Triple Dive", "Triple Inmersión");
        LABELS.put("Triple Kick", "Triple Patada");
        LABELS.put("Trop Kick", "Patada Tropical");
        LABELS.put("Tropic Mail", "Car. Tropic.");
        LABELS.put("Tropical Shell", "Concha Tropical");
        LABELS.put("Tropius Leaf", "Hoja de Tropius");
        LABELS.put("Truant", "Ausente");
        LABELS.put("Trump Card", "As Oculto");
        LABELS.put("Tunnel Mail", "Carta Mina");
        LABELS.put("Turboblaze", "Turbollama");
        LABELS.put("Twin Beam", "Láser Doble");
        LABELS.put("Twineedle", "Doble Ataque");
        LABELS.put("Twinkle Tackle", "Arrumaco Sideral");
        LABELS.put("Twisted Spoon", "Cuchara Torcida");
        LABELS.put("Twister", "Ciclón");
        LABELS.put("Tynamo Slime", "Secreción de Tynamo");
        LABELS.put("Tyranitarite", "Tyranitarita");
        LABELS.put("U-turn", "Ida y Vuelta");
        LABELS.put("Ultra Ball", "Ultra Ball");
        LABELS.put("Ultranecrozium Z", "Ultranecrostal Z");
        LABELS.put("Unaware", "Ignorante");
        LABELS.put("Unburden", "Liviano");
        LABELS.put("Unnerve", "Nerviosismo");
        LABELS.put("Unown Report", "Bloc Unown");
        LABELS.put("Unremarkable Teacup", "Cuenco Mediocre");
        LABELS.put("Unseen Fist", "Puño Invisible");
        LABELS.put("Upgrade", "Mejora");
        LABELS.put("Upper Hand", "Palma Rauda");
        LABELS.put("Uproar", "Alboroto");
        LABELS.put("Utility Umbrella", "Parasol Multiuso");
        LABELS.put("V-create", "V de Fuego");
        LABELS.put("Vacuum Wave", "Onda Vacío");
        LABELS.put("Varoom Fume", "Gas de Varoom");
        LABELS.put("Vee-Vee Pick", "Palillo Eevee");
        LABELS.put("Veevee Volley", "Eevimpacto");
        LABELS.put("Veluza Fillet", "Carne de Veluza");
        LABELS.put("Venom Drench", "Trampa Venenosa");
        LABELS.put("Venonat Candy", "Caramelo Venonat");
        LABELS.put("Venonat Fang", "Colmillo de Venonat");
        LABELS.put("Venoshock", "Carga Tóxica");
        LABELS.put("Venusaurite", "Venusaurita");
        LABELS.put("Vessel of Ruin", "Caldero Debacle");
        LABELS.put("Victory Dance", "Danza Triunfal");
        LABELS.put("Victory Star", "Tinovictoria");
        LABELS.put("Victreebelite", "Victreebelita");
        LABELS.put("Vine Whip", "Látigo Cepa");
        LABELS.put("Vinegar", "Vinagre");
        LABELS.put("Violet Book", "Libro Púrpura");
        LABELS.put("Vise Grip", "Agarre");
        LABELS.put("Vital Spirit", "Espíritu Vital");
        LABELS.put("Vital Throw", "Llave Vital");
        LABELS.put("Volbeat Fluid", "Fluido de Volbeat");
        LABELS.put("Volt Absorb", "Absorbe Electricidad");
        LABELS.put("Volt Switch", "Voltiocambio");
        LABELS.put("Volt Tackle", "Placaje Eléctrico");
        LABELS.put("Voltorb Candy", "Caramelo Voltorb");
        LABELS.put("Voltorb Sparks", "Electricidad de Voltorb");
        LABELS.put("Vs. Recorder", "Cámara Lucha");
    }
    private static void labels33() {
        LABELS.put("Vs. Seeker", "Buscapelea");
        LABELS.put("Vullaby Feather", "Pluma de Vullaby");
        LABELS.put("Vulpix Candy", "Caramelo Vulpix");
        LABELS.put("Vulpix Fur", "Pelo de Vulpix");
        LABELS.put("Wacan Berry", "Baya Gualot");
        LABELS.put("Wailmer Pail", "Wailmegadera");
        LABELS.put("Wake-Up Slap", "Espabila");
        LABELS.put("Wandering Spirit", "Alma Errante");
        LABELS.put("Wasabi", "Wasabi");
        LABELS.put("Water", "Agua");
        LABELS.put("Water Absorb", "Absorbe Agua");
        LABELS.put("Water Bubble", "Pompa");
        LABELS.put("Water Compaction", "Hidrorrefuerzo");
        LABELS.put("Water Gem", "Gema Agua");
        LABELS.put("Water Gun", "Pistola Agua");
        LABELS.put("Water Memory", "Disco Agua");
        LABELS.put("Water Pledge", "Voto Agua");
        LABELS.put("Water Pulse", "Hidropulso");
        LABELS.put("Water Shuriken", "Shuriken de Agua");
        LABELS.put("Water Sport", "Hidrochorro");
        LABELS.put("Water Spout", "Salpicar");
        LABELS.put("Water Stone", "Piedra Agua");
        LABELS.put("Water Tera Shard", "Teralito Agua");
        LABELS.put("Water Veil", "Velo Agua");
        LABELS.put("Watercress", "Berros");
        LABELS.put("Waterfall", "Cascada");
        LABELS.put("Waterium Z", "Hidrostal Z");
        LABELS.put("Watmel Berry", "Baya Sambia");
        LABELS.put("Wattrel Feather", "Pluma de Wattrel");
        LABELS.put("Wave Crash", "Envite Acuático");
        LABELS.put("Wave Incense", "Incienso Acua");
        LABELS.put("Wave Mail", "Carta Ola");
        LABELS.put("Weak Armor", "Armadura Frágil");
        LABELS.put("Weakness Policy", "Seguro Debilidad");
        LABELS.put("Weather Ball", "Meteorobola");
        LABELS.put("Weedle Candy", "Caramelo Weedle");
        LABELS.put("Well-Baked Body", "Cuerpo Horneado");
        LABELS.put("Wellspring Mask", "Máscara Fuente");
        LABELS.put("Wepear Berry", "Baya Peragu");
        LABELS.put("Whimsical Tablecloth", "Mantel Cuco");
        LABELS.put("Whipped Cream", "Nata Montada");
        LABELS.put("Whipped Dream", "Dulce de Nata");
        LABELS.put("Whirlpool", "Torbellino");
        LABELS.put("Whirlwind", "Remolino");
        LABELS.put("White Apricorn", "Bonguri Blanco");
        LABELS.put("White Dish", "Plato Blanco");
        LABELS.put("White Flute", "Flauta Blanca");
        LABELS.put("White Herb", "Hierba Blanca");
        LABELS.put("White Mane Hair", "Crin Blanca");
        LABELS.put("White Smoke", "Humo Blanco");
        LABELS.put("Wicked Blow", "Golpe Oscuro");
        LABELS.put("Wicked Torque", "Ominochoque");
        LABELS.put("Wide Guard", "Vasta Guardia");
        LABELS.put("Wide Lens", "Lupa");
        LABELS.put("Wiglett Sand", "Arena de Wiglett");
        LABELS.put("Wiki Berry", "Baya Wiki");
        LABELS.put("Wild Charge", "Voltio Cruel");
        LABELS.put("Wildbolt Storm", "Electormenta");
        LABELS.put("Will-O-Wisp", "Fuego Fatuo");
        LABELS.put("Wimp Out", "Huida");
        LABELS.put("Wind Power", "Energía Eólica");
        LABELS.put("Wind Rider", "Surcavientos");
        LABELS.put("Wing Attack", "Ataque Ala");
        LABELS.put("Wing Ball", "Ala Ball");
        LABELS.put("Wingull Feather", "Pluma de Wingull");
        LABELS.put("Winking Pika Pick", "Palillo Pikaguiño");
        LABELS.put("Wise Glasses", "Gafas Especiales");
        LABELS.put("Wish", "Deseo");
        LABELS.put("Wishing Chip", "Trocito Deseo");
        LABELS.put("Wishing Piece", "Trozo Deseo");
        LABELS.put("Wishing Star", "Estrella Deseo");
        LABELS.put("Withdraw", "Refugio");
        LABELS.put("Wonder Guard", "Superguarda");
        LABELS.put("Wonder Room", "Zona Extraña");
        LABELS.put("Wonder Skin", "Piel Milagro");
        LABELS.put("Wood Hammer", "Mazazo");
        LABELS.put("Wood Mail", "Carta Madera");
        LABELS.put("Wooden Crown", "Corona Tallada");
        LABELS.put("Wooper Slime", "Secreción de Wooper");
        LABELS.put("Work Up", "Avivar");
        LABELS.put("Works Key", "Llave Central");
        LABELS.put("Worry Seed", "Abatidoras");
        LABELS.put("Wrap", "Constricción");
        LABELS.put("Wring Out", "Estrujón");
        LABELS.put("X Accuracy", "Precisión X");
        LABELS.put("X Accuracy 2", "Precisión X 2");
        LABELS.put("X Accuracy 3", "Precisión X 3");
        LABELS.put("X Accuracy 6", "Precisión X 6");
        LABELS.put("X Attack", "Ataque X");
        LABELS.put("X Attack 2", "Ataque X 2");
    }
    private static void labels34() {
        LABELS.put("X Attack 3", "Ataque X 3");
        LABELS.put("X Attack 6", "Ataque X 6");
        LABELS.put("X Defense", "Defensa X");
        LABELS.put("X Defense 2", "Defensa X 2");
        LABELS.put("X Defense 3", "Defensa X 3");
        LABELS.put("X Defense 6", "Defensa X 6");
        LABELS.put("X Sp. Atk", "At. Especial X");
        LABELS.put("X Sp. Atk 2", "At. Esp. X 2");
        LABELS.put("X Sp. Atk 3", "At. Esp. X 3");
        LABELS.put("X Sp. Atk 6", "At. Esp. X 6");
        LABELS.put("X Sp. Def", "Def. Especial X");
        LABELS.put("X Sp. Def 2", "Def. Esp. X 2");
        LABELS.put("X Sp. Def 3", "Def. Esp. X 3");
        LABELS.put("X Sp. Def 6", "Def. Esp. X 6");
        LABELS.put("X Speed", "Velocidad X");
        LABELS.put("X Speed 2", "Velocidad X 2");
        LABELS.put("X Speed 3", "Velocidad X 3");
        LABELS.put("X Speed 6", "Velocidad X 6");
        LABELS.put("X-Scissor", "Tijera X");
        LABELS.put("Xtransceiver", "Videomisor");
        LABELS.put("Yache Berry", "Baya Rimoya");
        LABELS.put("Yanma Spike", "Aguijón de Yanma");
        LABELS.put("Yarn Ball", "Pelota Ovillo");
        LABELS.put("Yawn", "Bostezo");
        LABELS.put("Yellow Apricorn", "Bonguri Amarillo");
        LABELS.put("Yellow Bell Pepper", "Pimiento Amarillo");
        LABELS.put("Yellow Bottle", "Termo Amarillo");
        LABELS.put("Yellow Cup", "Taza Amarilla");
        LABELS.put("Yellow Dish", "Plato Amarillo");
        LABELS.put("Yellow Flute", "Flauta Amarilla");
        LABELS.put("Yellow Nectar", "Néctar Amarillo");
        LABELS.put("Yellow Petal", "Pétalo Amarillo");
        LABELS.put("Yellow Scarf", "Pañuelo Amarillo");
        LABELS.put("Yellow Shard", "Parte Amarilla");
        LABELS.put("Yellow Tablecloth", "Mantel Amarillo");
        LABELS.put("Yogurt", "Yogur");
        LABELS.put("Yungoos Fur", "Pelo de Yungoos");
        LABELS.put("Z-Power Ring", "Superpulsera Z");
        LABELS.put("Z-Ring", "Pulsera Z");
        LABELS.put("Zangoose Claw", "Garra de Zangoose");
        LABELS.put("Zap Cannon", "Electrocañón");
        LABELS.put("Zap Plate", "Tabla Trueno");
        LABELS.put("Zapdos Candy", "Caramelo Zapdos");
        LABELS.put("Zen Headbutt", "Cabezazo Zen");
        LABELS.put("Zen Mode", "Modo Daruma");
        LABELS.put("Zeraorite", "Zeraoranita");
        LABELS.put("Zero to Hero", "Cambio Heroico");
        LABELS.put("Zinc", "Zinc");
        LABELS.put("Zing Zap", "Electropunzada");
        LABELS.put("Zippy Zap", "Pikaturbo");
        LABELS.put("Zoom Lens", "Telescopio");
        LABELS.put("Zorua Fur", "Pelo de Zorua");
        LABELS.put("Zubat Candy", "Caramelo Zubat");
        LABELS.put("Zygarde Cube", "Arca de Zygarde");
        LABELS.put("Zygardite", "Zygardita");
        LABELS.put("★And15", "★And15");
        LABELS.put("★And337", "★And337");
        LABELS.put("★And390", "★And390");
        LABELS.put("★And458", "★And458");
        LABELS.put("★And603", "★And603");
        LABELS.put("★Aql7235", "★Aql7235");
        LABELS.put("★Aql7525", "★Aql7525");
        LABELS.put("★Aql7557", "★Aql7557");
        LABELS.put("★Aql7595", "★Aql7595");
        LABELS.put("★Aql7602", "★Aql7602");
        LABELS.put("★Aqr7950", "★Aqr7950");
        LABELS.put("★Aqr8232", "★Aqr8232");
        LABELS.put("★Aqr8264", "★Aqr8264");
        LABELS.put("★Aqr8414", "★Aqr8414");
        LABELS.put("★Aqr8499", "★Aqr8499");
        LABELS.put("★Aqr8518", "★Aqr8518");
        LABELS.put("★Aqr8610", "★Aqr8610");
        LABELS.put("★Aqr8709", "★Aqr8709");
        LABELS.put("★Ara6585", "★Ara6585");
        LABELS.put("★Ari546", "★Ari546");
        LABELS.put("★Ari553", "★Ari553");
        LABELS.put("★Ari617", "★Ari617");
        LABELS.put("★Ari951", "★Ari951");
        LABELS.put("★Aur1577", "★Aur1577");
        LABELS.put("★Aur1605", "★Aur1605");
        LABELS.put("★Aur1612", "★Aur1612");
        LABELS.put("★Aur1641", "★Aur1641");
        LABELS.put("★Aur1708", "★Aur1708");
        LABELS.put("★Aur2088", "★Aur2088");
        LABELS.put("★Aur2095", "★Aur2095");
        LABELS.put("★Boo5235", "★Boo5235");
        LABELS.put("★Boo5340", "★Boo5340");
        LABELS.put("★Boo5351", "★Boo5351");
        LABELS.put("★Boo5435", "★Boo5435");
        LABELS.put("★Boo5506", "★Boo5506");
    }
    private static void labels35() {
        LABELS.put("★Boo5602", "★Boo5602");
        LABELS.put("★Boo5733", "★Boo5733");
        LABELS.put("★CMa2282", "★CMa2282");
        LABELS.put("★CMa2294", "★CMa2294");
        LABELS.put("★CMa2491", "★CMa2491");
        LABELS.put("★CMa2618", "★CMa2618");
        LABELS.put("★CMa2646", "★CMa2646");
        LABELS.put("★CMa2657", "★CMa2657");
        LABELS.put("★CMa2693", "★CMa2693");
        LABELS.put("★CMa2827", "★CMa2827");
        LABELS.put("★CMi2845", "★CMi2845");
        LABELS.put("★CMi2943", "★CMi2943");
        LABELS.put("★CVn4785", "★CVn4785");
        LABELS.put("★CVn4846", "★CVn4846");
        LABELS.put("★CVn4915", "★CVn4915");
        LABELS.put("★Cap7754", "★Cap7754");
        LABELS.put("★Cap7773", "★Cap7773");
        LABELS.put("★Cap7776", "★Cap7776");
        LABELS.put("★Cap8278", "★Cap8278");
        LABELS.put("★Cap8322", "★Cap8322");
        LABELS.put("★Car2326", "★Car2326");
        LABELS.put("★Car3307", "★Car3307");
        LABELS.put("★Car3685", "★Car3685");
        LABELS.put("★Car3699", "★Car3699");
        LABELS.put("★Cas153", "★Cas153");
        LABELS.put("★Cas168", "★Cas168");
        LABELS.put("★Cas21", "★Cas21");
        LABELS.put("★Cas219", "★Cas219");
        LABELS.put("★Cas265", "★Cas265");
        LABELS.put("★Cas403", "★Cas403");
        LABELS.put("★Cas542", "★Cas542");
        LABELS.put("★Cen5267", "★Cen5267");
        LABELS.put("★Cen5288", "★Cen5288");
        LABELS.put("★Cen5459", "★Cen5459");
        LABELS.put("★Cen5460", "★Cen5460");
        LABELS.put("★Cen551", "★Cen551");
        LABELS.put("★Cep8162", "★Cep8162");
        LABELS.put("★Cep8238", "★Cep8238");
        LABELS.put("★Cep8417", "★Cep8417");
        LABELS.put("★Cep8974", "★Cep8974");
        LABELS.put("★Cet188", "★Cet188");
        LABELS.put("★Cet539", "★Cet539");
        LABELS.put("★Cet681", "★Cet681");
        LABELS.put("★Cet804", "★Cet804");
        LABELS.put("★Cet911", "★Cet911");
        LABELS.put("★Cnc3208", "★Cnc3208");
        LABELS.put("★Cnc3249", "★Cnc3249");
        LABELS.put("★Cnc3268", "★Cnc3268");
        LABELS.put("★Cnc3429", "★Cnc3429");
        LABELS.put("★Cnc3449", "★Cnc3449");
        LABELS.put("★Cnc3461", "★Cnc3461");
        LABELS.put("★Cnc3572", "★Cnc3572");
        LABELS.put("★Cnc3627", "★Cnc3627");
        LABELS.put("★Col1956", "★Col1956");
        LABELS.put("★Col2040", "★Col2040");
        LABELS.put("★Col2177", "★Col2177");
        LABELS.put("★Com4968", "★Com4968");
        LABELS.put("★Crt4287", "★Crt4287");
        LABELS.put("★Cru4656", "★Cru4656");
        LABELS.put("★Cru4700", "★Cru4700");
        LABELS.put("★Cru4730", "★Cru4730");
        LABELS.put("★Cru4763", "★Cru4763");
        LABELS.put("★Cru4853", "★Cru4853");
        LABELS.put("★Crv4623", "★Crv4623");
        LABELS.put("★Crv4662", "★Crv4662");
        LABELS.put("★Crv4757", "★Crv4757");
        LABELS.put("★Crv4786", "★Crv4786");
        LABELS.put("★Cyg7417", "★Cyg7417");
        LABELS.put("★Cyg7528", "★Cyg7528");
        LABELS.put("★Cyg7796", "★Cyg7796");
        LABELS.put("★Cyg7924", "★Cyg7924");
        LABELS.put("★Cyg7949", "★Cyg7949");
        LABELS.put("★Cyg8301", "★Cyg8301");
        LABELS.put("★Del7852", "★Del7852");
        LABELS.put("★Del7882", "★Del7882");
        LABELS.put("★Del7906", "★Del7906");
        LABELS.put("★Dra4434", "★Dra4434");
        LABELS.put("★Dra5291", "★Dra5291");
        LABELS.put("★Dra5744", "★Dra5744");
        LABELS.put("★Dra6132", "★Dra6132");
        LABELS.put("★Dra6370", "★Dra6370");
        LABELS.put("★Dra6396", "★Dra6396");
        LABELS.put("★Dra6536", "★Dra6536");
        LABELS.put("★Dra6636", "★Dra6636");
        LABELS.put("★Dra6688", "★Dra6688");
        LABELS.put("★Dra6705", "★Dra6705");
        LABELS.put("★Dra7310", "★Dra7310");
        LABELS.put("★Dra7462", "★Dra7462");
        LABELS.put("★Equ8131", "★Equ8131");
        LABELS.put("★Eri1084", "★Eri1084");
    }
    private static void labels36() {
        LABELS.put("★Eri1231", "★Eri1231");
        LABELS.put("★Eri1298", "★Eri1298");
        LABELS.put("★Eri1325", "★Eri1325");
        LABELS.put("★Eri1393", "★Eri1393");
        LABELS.put("★Eri1464", "★Eri1464");
        LABELS.put("★Eri1666", "★Eri1666");
        LABELS.put("★Eri472", "★Eri472");
        LABELS.put("★Eri850", "★Eri850");
        LABELS.put("★Eri874", "★Eri874");
        LABELS.put("★Eri897", "★Eri897");
        LABELS.put("★Eri984", "★Eri984");
        LABELS.put("★Gem2216", "★Gem2216");
        LABELS.put("★Gem2286", "★Gem2286");
        LABELS.put("★Gem2421", "★Gem2421");
        LABELS.put("★Gem2473", "★Gem2473");
        LABELS.put("★Gem2484", "★Gem2484");
        LABELS.put("★Gem2650", "★Gem2650");
        LABELS.put("★Gem2777", "★Gem2777");
        LABELS.put("★Gem2891", "★Gem2891");
        LABELS.put("★Gem2930", "★Gem2930");
        LABELS.put("★Gem2990", "★Gem2990");
        LABELS.put("★Gru8353", "★Gru8353");
        LABELS.put("★Gru8425", "★Gru8425");
        LABELS.put("★Gru8636", "★Gru8636");
        LABELS.put("★Her6008", "★Her6008");
        LABELS.put("★Her6117", "★Her6117");
        LABELS.put("★Her6148", "★Her6148");
        LABELS.put("★Her6406", "★Her6406");
        LABELS.put("★Her6410", "★Her6410");
        LABELS.put("★Her6526", "★Her6526");
        LABELS.put("★Hya3418", "★Hya3418");
        LABELS.put("★Hya3482", "★Hya3482");
        LABELS.put("★Hya3748", "★Hya3748");
        LABELS.put("★Hya3845", "★Hya3845");
        LABELS.put("★Hya3903", "★Hya3903");
        LABELS.put("★Leo3773", "★Leo3773");
        LABELS.put("★Leo3852", "★Leo3852");
        LABELS.put("★Leo3905", "★Leo3905");
        LABELS.put("★Leo3982", "★Leo3982");
        LABELS.put("★Leo4031", "★Leo4031");
        LABELS.put("★Leo4057", "★Leo4057");
        LABELS.put("★Leo4357", "★Leo4357");
        LABELS.put("★Leo4359", "★Leo4359");
        LABELS.put("★Leo4534", "★Leo4534");
        LABELS.put("★Lep1829", "★Lep1829");
        LABELS.put("★Lep1865", "★Lep1865");
        LABELS.put("★Lib5531", "★Lib5531");
        LABELS.put("★Lib5603", "★Lib5603");
        LABELS.put("★Lib5685", "★Lib5685");
        LABELS.put("★Lib5787", "★Lib5787");
        LABELS.put("★Lyr7001", "★Lyr7001");
        LABELS.put("★Lyr7106", "★Lyr7106");
        LABELS.put("★Lyr7178", "★Lyr7178");
        LABELS.put("★Lyr7298", "★Lyr7298");
        LABELS.put("★Oct7228", "★Oct7228");
        LABELS.put("★Oph6056", "★Oph6056");
        LABELS.put("★Oph6075", "★Oph6075");
        LABELS.put("★Oph6149", "★Oph6149");
        LABELS.put("★Oph6378", "★Oph6378");
        LABELS.put("★Oph6556", "★Oph6556");
        LABELS.put("★Oph6603", "★Oph6603");
        LABELS.put("★Ori1543", "★Ori1543");
        LABELS.put("★Ori1713", "★Ori1713");
        LABELS.put("★Ori1790", "★Ori1790");
        LABELS.put("★Ori1852", "★Ori1852");
        LABELS.put("★Ori1879", "★Ori1879");
        LABELS.put("★Ori1899", "★Ori1899");
        LABELS.put("★Ori1903", "★Ori1903");
        LABELS.put("★Ori1948", "★Ori1948");
        LABELS.put("★Ori2004", "★Ori2004");
        LABELS.put("★Ori2061", "★Ori2061");
        LABELS.put("★Pav7790", "★Pav7790");
        LABELS.put("★Peg39", "★Peg39");
        LABELS.put("★Peg8308", "★Peg8308");
        LABELS.put("★Peg8450", "★Peg8450");
        LABELS.put("★Peg8634", "★Peg8634");
        LABELS.put("★Peg8650", "★Peg8650");
        LABELS.put("★Peg8684", "★Peg8684");
        LABELS.put("★Peg8775", "★Peg8775");
        LABELS.put("★Peg8781", "★Peg8781");
        LABELS.put("★Peg8880", "★Peg8880");
        LABELS.put("★Peg8905", "★Peg8905");
        LABELS.put("★Per1017", "★Per1017");
        LABELS.put("★Per1131", "★Per1131");
        LABELS.put("★Per1228", "★Per1228");
        LABELS.put("★Per834", "★Per834");
        LABELS.put("★Per936", "★Per936");
        LABELS.put("★Per941", "★Per941");
        LABELS.put("★Phe338", "★Phe338");
        LABELS.put("★Phe99", "★Phe99");
    }
    private static void labels37() {
        LABELS.put("★PsA8728", "★PsA8728");
        LABELS.put("★Psc361", "★Psc361");
        LABELS.put("★Psc437", "★Psc437");
        LABELS.put("★Psc510", "★Psc510");
        LABELS.put("★Psc596", "★Psc596");
        LABELS.put("★Psc8773", "★Psc8773");
        LABELS.put("★Pup3045", "★Pup3045");
        LABELS.put("★Pup3165", "★Pup3165");
        LABELS.put("★Pup3185", "★Pup3185");
        LABELS.put("★Sco5928", "★Sco5928");
        LABELS.put("★Sco5944", "★Sco5944");
        LABELS.put("★Sco5953", "★Sco5953");
        LABELS.put("★Sco5984", "★Sco5984");
        LABELS.put("★Sco6027", "★Sco6027");
        LABELS.put("★Sco6084", "★Sco6084");
        LABELS.put("★Sco6134", "★Sco6134");
        LABELS.put("★Sco6165", "★Sco6165");
        LABELS.put("★Sco6241", "★Sco6241");
        LABELS.put("★Sco6247", "★Sco6247");
        LABELS.put("★Sco6252", "★Sco6252");
        LABELS.put("★Sco6508", "★Sco6508");
        LABELS.put("★Sco6527", "★Sco6527");
        LABELS.put("★Sco6553", "★Sco6553");
        LABELS.put("★Sco6630", "★Sco6630");
        LABELS.put("★Ser5854", "★Ser5854");
        LABELS.put("★Ser5879", "★Ser5879");
        LABELS.put("★Ser7141", "★Ser7141");
        LABELS.put("★Sge7479", "★Sge7479");
        LABELS.put("★Sgr6746", "★Sgr6746");
        LABELS.put("★Sgr6812", "★Sgr6812");
        LABELS.put("★Sgr6859", "★Sgr6859");
        LABELS.put("★Sgr6879", "★Sgr6879");
        LABELS.put("★Sgr6913", "★Sgr6913");
        LABELS.put("★Sgr7116", "★Sgr7116");
        LABELS.put("★Sgr7121", "★Sgr7121");
        LABELS.put("★Sgr7194", "★Sgr7194");
        LABELS.put("★Sgr7264", "★Sgr7264");
        LABELS.put("★Sgr7337", "★Sgr7337");
        LABELS.put("★Sgr7343", "★Sgr7343");
        LABELS.put("★Sgr7348", "★Sgr7348");
        LABELS.put("★Sgr7597", "★Sgr7597");
        LABELS.put("★Tau1165", "★Tau1165");
        LABELS.put("★Tau1346", "★Tau1346");
        LABELS.put("★Tau1373", "★Tau1373");
        LABELS.put("★Tau1409", "★Tau1409");
        LABELS.put("★Tau1412", "★Tau1412");
        LABELS.put("★Tau1457", "★Tau1457");
        LABELS.put("★Tau1791", "★Tau1791");
        LABELS.put("★Tau1910", "★Tau1910");
        LABELS.put("★TrA6217", "★TrA6217");
        LABELS.put("★Tri544", "★Tri544");
        LABELS.put("★UMa3323", "★UMa3323");
        LABELS.put("★UMa3569", "★UMa3569");
        LABELS.put("★UMa3594", "★UMa3594");
        LABELS.put("★UMa4033", "★UMa4033");
        LABELS.put("★UMa4069", "★UMa4069");
        LABELS.put("★UMa4295", "★UMa4295");
        LABELS.put("★UMa4301", "★UMa4301");
        LABELS.put("★UMa4375", "★UMa4375");
        LABELS.put("★UMa4377", "★UMa4377");
        LABELS.put("★UMa4518", "★UMa4518");
        LABELS.put("★UMa4554", "★UMa4554");
        LABELS.put("★UMa4660", "★UMa4660");
        LABELS.put("★UMa4905", "★UMa4905");
        LABELS.put("★UMa5054", "★UMa5054");
        LABELS.put("★UMa5191", "★UMa5191");
        LABELS.put("★UMi424", "★UMi424");
        LABELS.put("★UMi5563", "★UMi5563");
        LABELS.put("★UMi5735", "★UMi5735");
        LABELS.put("★UMi6789", "★UMi6789");
        LABELS.put("★Vel3485", "★Vel3485");
        LABELS.put("★Vel3634", "★Vel3634");
        LABELS.put("★Vel3734", "★Vel3734");
        LABELS.put("★Vir4540", "★Vir4540");
        LABELS.put("★Vir4689", "★Vir4689");
        LABELS.put("★Vir4825", "★Vir4825");
        LABELS.put("★Vir4910", "★Vir4910");
        LABELS.put("★Vir4932", "★Vir4932");
        LABELS.put("★Vir5056", "★Vir5056");
        LABELS.put("★Vir5107", "★Vir5107");
        LABELS.put("★Vir5315", "★Vir5315");
        LABELS.put("★Vir5338", "★Vir5338");
        LABELS.put("★Vir5359", "★Vir5359");
        LABELS.put("★Vir5409", "★Vir5409");
        LABELS.put("★Vul7405", "★Vul7405");
    }
    private NeuralLabels() {}
}
