prefix = '/response/paths'
local url = request.path:match(prefix .. "(.*)")

assert(url)

function doReturn18000(path, pass)
    if path ~= '/response/paths' then
        error(path)
    end

    return pass == 1 and 18000
end

if url == '' then
    return doReturn18000
elseif url == '/cont' then
    return { 18, nil, '00', 1 }
end

local paths = {}
local ones = {
    'eleven', 'twelve', 'thirteen', 'fourteen', 'fifteen',
    'sixteen', 'seventeen', 'eighteen', 'nineteen'
}
for i = 1, 9 do
    paths[prefix .. '/eighteen-thousand-and-' .. ones[i]] = tostring(18010 + i)
end

local function fallback(tbl, path)
    assert(tbl)

    assert(path:sub(1, #prefix + 1) == prefix .. '/')
    path = path:sub(#prefix + 2, #path)

    local integer = math.tointeger(path)

    if integer and integer >= 18000 and integer < 19000 then
        return integer
    else
        return 'NaN'
    end
end

setmetatable(paths, { __index = fallback })

return paths
