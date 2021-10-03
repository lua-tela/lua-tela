local url = request.path:match("/context/mime-type(.*)")

assert(url == '')

assert(context.getMimeType(context.resPath("my.json")) == 'application/json')
assert(context.getMimeType(context.resPath("my.txt")) == 'text/plain')
assert(context.getMimeType(context.dataPath("WEB-INF/web.xml")) == 'application/xml')

return 15000