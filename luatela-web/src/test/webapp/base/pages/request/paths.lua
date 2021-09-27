local url = request.path:match("/request/paths(.*)")

assert(url)
assert(request.ctx == '')
assert(request.url:sub(1, 4) == 'http')

if url == '' then
    return 92713
elseif url == '/this/is/my/path' then
    return 50245
else
    return 69213
end
