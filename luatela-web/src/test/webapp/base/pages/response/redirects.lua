local url = request.path:match("/response/redirects(.*)")

assert(url)
local path = request.url .. request.ctx

if url == '' then
    assert(response.redirect(path .. '/response/redirects/hop'))
    return
elseif url == '/hop' then
    assert(response.redirect(path .. '/response/redirects/to'))
    return
elseif url == '/to' then
    assert(response.redirect(path .. '/response/redirects/the'))
    return
elseif url == '/the' then
    assert(response.redirect(path .. '/response/redirects/beat'))
    return
elseif url == '/beat' then
    assert(response.redirect(path .. '/response/redirects/destination'))
    return
elseif url == '/destination' then
    return 12000
end

error('not a valid path')