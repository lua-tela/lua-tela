local somedata = require 'libs/somedata.lua'

local txt = html.create()
txt:prln("<!DOCTYPE html>")

txt:open("html")
txt:open("head")
txt:el("title", somedata.title())
txt:close("head")
txt:open("body")
txt:prln(somedata.mywhat('line'))
txt:el("magic", nil, "number", tostring(somedata.magicnumber()))
txt:close("body")
txt:close("html")
return txt