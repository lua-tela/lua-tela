local url = request.path:match("/request/headers(.*)")

assert(url)

local userAgent = request.getHeader('User-Agent')
assert(userAgent)
assert(userAgent:match(".*(Apache-HttpClient).*"))

if url == '/test-names' then
    local tbl = {
        'this', 'is', 'my',
        'header', 'there', 'are',
        'many', 'like', 'it',
        'but', 'this', 'one',
        'is', 'mine',
    }

    local headers = request.getHeaders()
    assert(headers)
    assert(type(headers) == 'table')

    local contains
    for _, v1 in ipairs(tbl) do
        contains = false

        for _, v2 in ipairs(headers) do
            if v1 == v2 then
                contains = true
                break
            end
        end

        assert(contains, 'must contain header "' .. v1 .. '"')
    end

    assert(contains)

    return 70005
elseif url:match('^/test.*') then
    local count = tonumber(url:match("/test(.*)"))

    for i = 1, count do
        local header = request.getHeader('Header' .. i)
        assert(header, 'on header ' .. i)
        assert(header == 'Value' .. i, 'on header ' .. i)
    end

    return 70000 + count
end

return 70000