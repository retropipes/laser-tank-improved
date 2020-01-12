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
    assets.data.menus = require "assets.data.menus"
    assets.fonts.normal = love.graphics.newFont("assets/fonts/DejaVuLGCSansMono.ttf")
    assets.fonts.bold = love.graphics.newFont("assets/fonts/DejaVuLGCSansMonoBold.ttf")
    assets.fonts.boldOblique = love.graphics.newFont("assets/fonts/DejaVuLGCSansMonoBoldOblique.ttf")
    assets.fonts.oblique = love.graphics.newFont("assets/fonts/DejaVuLGCSansMonoOblique.ttf")
    assets.images.ui.logo = love.graphics.newImage("assets/images/ui/logo.png")
end