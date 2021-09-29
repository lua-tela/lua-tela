local url = request.path:match("/response/status(.*)")

assert(url)

if url == '/not-found' then
    assert(response.setStatus(404))
    return 90001
elseif url == '/found' then
    assert(response.setStatus(302))
    return 90002
elseif url == '/accepted' then
    assert(response.setStatus(202))
    return 90003
elseif url == '/forbidden' then
    assert(response.setStatus(403))
    return 90004
elseif url == '/bad-gateway' then
    assert(response.setStatus(502))
    return 90005
end

return 90000