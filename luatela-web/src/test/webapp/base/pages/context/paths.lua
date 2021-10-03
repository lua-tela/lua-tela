local url = request.path:match("/context/paths(.*)")

assert(url == '')

assert(context.realPath("WEB-INF"))
assert(context.realPath("base/pages") == context.dataPath("pages"))
assert(context.realPath("base/res") == context.dataPath("res"))
assert(context.dataPath("res/my.txt") == context.resPath("my.txt"))

return 13000