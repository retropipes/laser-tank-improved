-- assets
local assets = {
    data = {},
    fonts = {},
    images = {
        attributes = {},
        objects = {},
        ui = {}
    },
    sounds = {}
}

function love.load()
    -- Initialize game
    initGame()
    -- Populate asset cache
    populateAssetCache()
end

function love.resize(w, h)
    return push:resize(w, h)
end

function love.draw()
    push:start()
    love.graphics.draw(assets.images.ui.logo)
    love.graphics.print({{0, 0, 0, 1}, "Press ESCAPE to quit"}, 60, 350)
    if joystick then
        love.graphics.print({{0, 0, 0, 1}, "Joystick detected; button 9 also quits"}, 60, 370)
    end
    loveframes.draw()
    push:finish()
end

function love.update(dt)
    if love.keyboard.isScancodeDown("escape") then
        quitGame()
    end
    if joystick then
        if joystick:isDown(9) then
            quitGame()
        end
    end
    loveframes.update(dt)
end

function love.mousepressed(x, y, button)
    loveframes.mousepressed(x, y, button)
end
 
function love.mousereleased(x, y, button)
    loveframes.mousereleased(x, y, button)
end
 
function love.keypressed(key, unicode)
    loveframes.keypressed(key, unicode)
end
 
function love.keyreleased(key)
    loveframes.keyreleased(key)
end

function quitGame()
    love.audio.stop()
    love.event.quit(0)
end

function initGame()
    -- load libraries
    loadLibraries()
    -- Set up screen
    setupScreen()
    -- Set up joysticks
    setupJoysticks()
    -- Set up locales (BROKEN)
    --setupLocales()
end

function loadLibraries()
    i18n = require "lib.i18n"
    push = require "lib.push"
    loveframes = require "lib.loveframes"
end

function setupScreen()
    local gameWidth, gameHeight = 1080, 720
    local windowWidth, windowHeight = love.window.getDesktopDimensions()
    push:setupScreen(gameWidth, gameHeight, windowWidth, windowHeight, {fullscreen = true, resizable = false, highdpi = true, pixelperfect = true, canvas = false, stretched = false})
end

function setupJoysticks()
    local joysticks = love.joystick.getJoysticks()
    local joycount = love.joystick.getJoystickCount()
    if joycount >= 1 then
        joystick = joysticks[1]
    end
end

function setupLocales()
    -- Load all the translation data files
    local dir = love.filesystem.getSource()
    i18n.loadFile(dir.."/assets/locale/de.lua")
    i18n.loadFile(dir.."/assets/locale/en.lua")
    i18n.loadFile(dir.."/assets/locale/es.lua")
    i18n.loadFile(dir.."/assets/locale/fr.lua")
    i18n.loadFile(dir.."/assets/locale/hr.lua")
    i18n.loadFile(dir.."/assets/locale/nl.lua")
    i18n.loadFile(dir.."/assets/locale/pt.lua")
    i18n.loadFile(dir.."/assets/locale/sr.lua")
    i18n.loadFile(dir.."/assets/locale/sv.lua")
    i18n.loadFile(dir.."/assets/locale/tr.lua")
    i18n.loadFile(dir.."/assets/locale/zc.lua")
    i18n.loadFile(dir.."/assets/locale/zh.lua")
    -- Set the default locale
    i18n.setLocale("en")
end

function populateAssetCache()
    -- data
    assets.data.menus = require "assets.data.menus"
    -- fonts
    assets.fonts.normal = love.graphics.newFont("assets/fonts/DejaVuLGCSansMono.ttf")
    assets.fonts.bold = love.graphics.newFont("assets/fonts/DejaVuLGCSansMonoBold.ttf")
    assets.fonts.boldOblique = love.graphics.newFont("assets/fonts/DejaVuLGCSansMonoBoldOblique.ttf")
    assets.fonts.oblique = love.graphics.newFont("assets/fonts/DejaVuLGCSansMonoOblique.ttf")
    -- attribute images
    -- object images
    -- UI images
    assets.images.ui.logo = love.graphics.newImage("assets/images/ui/logo.png")
    -- sounds
    assets.sounds.alert = love.audio.newSource("assets/sounds/alert.wav", "static")
    assets.sounds.anti1 = love.audio.newSource("assets/sounds/anti1.wav", "static")
    assets.sounds.anti2 = love.audio.newSource("assets/sounds/anti2.wav", "static")
    assets.sounds.barrier = love.audio.newSource("assets/sounds/barrier.wav", "static")
    assets.sounds.boing = love.audio.newSource("assets/sounds/boing.wav", "static")
    assets.sounds.boost = love.audio.newSource("assets/sounds/boost.wav", "static")
    assets.sounds.bricks = love.audio.newSource("assets/sounds/bricks.wav", "static")
    assets.sounds.button = love.audio.newSource("assets/sounds/button.wav", "static")
    assets.sounds.click = love.audio.newSource("assets/sounds/click.wav", "static")
    assets.sounds.control = love.audio.newSource("assets/sounds/control.wav", "static")
    assets.sounds.crack = love.audio.newSource("assets/sounds/crack.wav", "static")
    assets.sounds.crush = love.audio.newSource("assets/sounds/crush.wav", "static")
    assets.sounds.die = love.audio.newSource("assets/sounds/die.wav", "static")
    assets.sounds.doorCloses = love.audio.newSource("assets/sounds/door_closes.wav", "static")
    assets.sounds.doorOpens = love.audio.newSource("assets/sounds/door_opens.wav", "static")
    assets.sounds.down = love.audio.newSource("assets/sounds/down.wav", "static")
    assets.sounds.endLevel = love.audio.newSource("assets/sounds/endlevel.wav", "static")
    assets.sounds.error = love.audio.newSource("assets/sounds/error.wav", "static")
    assets.sounds.falling = love.audio.newSource("assets/sounds/falling.wav", "static")
    assets.sounds.fire = love.audio.newSource("assets/sounds/fire.wav", "static")
    assets.sounds.freeze = love.audio.newSource("assets/sounds/freeze.wav", "static")
    assets.sounds.generate = love.audio.newSource("assets/sounds/generate.wav", "static")
    assets.sounds.grab = love.audio.newSource("assets/sounds/grab.wav", "static")
    assets.sounds.head = love.audio.newSource("assets/sounds/head.wav", "static")
    assets.sounds.highScore = love.audio.newSource("assets/sounds/high_score.wav", "static")
    assets.sounds.intoPit = love.audio.newSource("assets/sounds/into_pit.wav", "static")
    assets.sounds.kaboom = love.audio.newSource("assets/sounds/kaboom.wav", "static")
    assets.sounds.laserDie = love.audio.newSource("assets/sounds/laserdie.wav", "static")
    assets.sounds.light = love.audio.newSource("assets/sounds/light.wav", "static")
    assets.sounds.lightFuse = love.audio.newSource("assets/sounds/light_fuse.wav", "static")
    assets.sounds.magnet = love.audio.newSource("assets/sounds/magnet.wav", "static")
    assets.sounds.melt = love.audio.newSource("assets/sounds/melt.wav", "static")
    assets.sounds.message = love.audio.newSource("assets/sounds/message.wav", "static")
    assets.sounds.missile = love.audio.newSource("assets/sounds/missile.wav", "static")
    assets.sounds.move = love.audio.newSource("assets/sounds/move.wav", "static")
    assets.sounds.power = love.audio.newSource("assets/sounds/power.wav", "static")
    assets.sounds.push1 = love.audio.newSource("assets/sounds/push1.wav", "static")
    assets.sounds.push2 = love.audio.newSource("assets/sounds/push2.wav", "static")
    assets.sounds.push3 = love.audio.newSource("assets/sounds/push3.wav", "static")
    assets.sounds.question = love.audio.newSource("assets/sounds/question.wav", "static")
    assets.sounds.reflect = love.audio.newSource("assets/sounds/reflect.wav", "static")
    assets.sounds.rotate = love.audio.newSource("assets/sounds/rotate.wav", "static")
    assets.sounds.shadow = love.audio.newSource("assets/sounds/shadow.wav", "static")
    assets.sounds.sink = love.audio.newSource("assets/sounds/sink.wav", "static")
    assets.sounds.spark = love.audio.newSource("assets/sounds/spark.wav", "static")
    assets.sounds.spring = love.audio.newSource("assets/sounds/spring.wav", "static")
    assets.sounds.stun = love.audio.newSource("assets/sounds/stun.wav", "static")
    assets.sounds.stunned = love.audio.newSource("assets/sounds/stunned.wav", "static")
    assets.sounds.trap = love.audio.newSource("assets/sounds/trap.wav", "static")
    assets.sounds.turn = love.audio.newSource("assets/sounds/turn.wav", "static")
    assets.sounds.unlock = love.audio.newSource("assets/sounds/unlock.wav", "static")
    assets.sounds.up = love.audio.newSource("assets/sounds/up.wav", "static")
    assets.sounds.useFailed = love.audio.newSource("assets/sounds/use_failed.wav", "static")
    assets.sounds.warning = love.audio.newSource("assets/sounds/warning.wav", "static")
end