local url = request.path:match("/response/headers(.*)")

assert(url)

if url:match('^/test.*') then
    local count = tonumber(url:match("/test(.*)"))

    for i = 1, count do
        assert(response.setHeader('Header' .. i, 'Value' .. i), 'on header ' .. i)
    end

    return 80000 + count
end

return 80000