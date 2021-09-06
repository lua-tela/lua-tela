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
---


path('/').topage('index')
path('/contact').topage()