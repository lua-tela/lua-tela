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
--- path('/johndoe-homepage'):path('/contact-me')
--- path('/johndoe-', 'homepage'):path('/contact-me')

path('/'):topage('index')
path('/contact'):topage()
path('/johndoe-homepage', path('/contact', '-me')):topage('johndoepage')

--- request table
local requestPth = path('/request')

requestPth:path('/get'):topage() -- 20000
requestPth:path('/post'):topage() -- 21000
requestPth:path('/paths'):topage() -- 30000
requestPth:path('/sessions'):topage() -- 40000
requestPth:path('/files'):topage() -- 10000
requestPth:path('/headers'):topage() -- 70000
requestPth:path('/body'):topage() -- 17000

local responsePth = path('/response')

responsePth:path('/content-type'):topage() -- 60000
responsePth:path('/content-size'):topage() -- 50000
responsePth:path('/headers'):topage() -- 80000
responsePth:path('/status'):topage() -- 90000
responsePth:path('/serve'):topage() -- 11000
responsePth:path('/redirects'):topage() -- 12000
responsePth:path('/paths'):topage() -- 18000

local contextPth = path('/context')

contextPth:path('/paths'):topage() -- 13000
contextPth:path('/attributes'):topage() -- 14000
contextPth:path('/mime-type'):topage() -- 15000
contextPth:path('/escaping'):topage() -- 16000

local templatesPth = path('/templates')

templatesPth:totemplate('index')