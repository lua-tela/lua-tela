assert(request.path)
assert(type(request.path) == 'string')

local url = request.path:match("/request/paths(.*)")

assert(url)
assert(request.ctx == '')
assert(request.url:sub(1, 4) == 'http')

if url == '' then
    return 30000
elseif url == '/this/is/my/path' then
    return 30001
else
    return 30002
end
