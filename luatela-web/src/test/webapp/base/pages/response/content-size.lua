local url = request.path:match("/response/content-size(.*)")

assert(url)

local function toFunction(value)
    local str = tostring(value)
    assert(#str == 5)

    return function(_, pass)
        if pass == 1 then
            return str:sub(1, 2)
        elseif pass == 2 then
            return true
        elseif pass == 3 then
            return str:sub(3, 3)
        elseif pass == 4 then
            return true
        elseif pass == 5 then
            return str:sub(4, 5)
        end
    end
end

if url == '' then
    return 50000
elseif url == '/none' then
    return toFunction(50001)
elseif url == '/five' then
    assert(response.setContentSize(5))

    return toFunction(50002)
end

error('not a valid path')