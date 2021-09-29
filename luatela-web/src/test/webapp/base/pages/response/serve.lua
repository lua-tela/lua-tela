local url = request.path:match("/response/serve(.*)")

assert(url)
if url == '' then
    return 11000
elseif url == '/my.txt' then
    assert(response.serve('my.txt'))
    return
elseif url == '/my.css' then
    assert(response.serve('my.txt', 'text/css'))
    return
elseif url == '/does.not.exist' then
    assert(not response.serve('not.found'))
    return 11001
end

error('not a valid path')