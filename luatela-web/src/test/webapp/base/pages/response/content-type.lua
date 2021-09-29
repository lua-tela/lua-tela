local url = request.path:match("/response/content-type(.*)")

assert(url)

if url == '' then
    return 60000
elseif url == '.json' then
    assert(response.setContentType("application/json"))

    return 60001
elseif url == '.html' then
    assert(response.setContentEncoding("UTF-8"))
    assert(response.setContentType("text/html"))

    return 60002
end

error('not a valid path')