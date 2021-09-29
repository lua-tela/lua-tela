---
--- The path() method is used to construct a valid URL path.
--- This method is very versatile in the sense that it can be joined
--- together in various configurations to construct said path.
--- Here are a couple of examples of how the same path can be
--- recreated in various different ways. Let's say the path is:
--- '/johndoe-homepage/contact-me' all of these examples would lead
--- to this url path:
--- path('/johndoe-homepage/contact-me')
--- path('/johndoe-homepage', '/contact-me')
--- path('/johndoe', '-', 'homepage', '/contact-me')
--- path('/johndoe-homepage').path('/contact-me')
--- path('/johndoe-', 'homepage').path('/contact-me')

path('/').topage('index')
path('/contact').topage()
path('/johndoe-homepage', path('/contact', '-me')).topage('johndoepage')

--- request table
local requestPth = path('/request')

requestPth.path('/params').topage()
requestPth.path('/paths').topage()
requestPth.path('/sessions').topage()
requestPth.path('/files').topage()
requestPth.path('/headers').topage()

local responsePth = path('/response')

responsePth.path('/content-type').topage()
responsePth.path('/content-size').topage()
responsePth.path('/headers').topage()
responsePth.path('/status').topage()
