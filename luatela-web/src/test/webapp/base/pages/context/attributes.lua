local url = request.path:match("/context/attributes(.*)")

assert(url)

local function checkCode(newcode)
    if newcode > 999 or newcode < 0 then
        error('not a valid path')
    end
    return newcode
end

local lastCode = context.getAttr("code")
local code = lastCode or 0
local skip

if url:match("\\/set\\/\\d+") then
    code = checkCode(tonumber(url:match("\\/set\\/(\\d+)")))
elseif url == '/add' then
    code = checkCode(code + 1)
elseif url == '/sub' then
    code = checkCode(code - 1)
elseif url == '/done' then
    local names = context.getAttrNames()

    assert(names)
    assert(type(names) == 'table')
    assert(#names == 1)
    assert(names[1] == "code")

    assert(context.removeAttr("code") == code)
    assert(not context.removeAttr("not code"))
    code = 0

    names = context.getAttrNames()

    assert(names)
    assert(type(names) == 'table')
    assert(#names == 0)

    skip = true
elseif url ~= '' then
    checkCode(-1)
end

if not skip then
    if code ~= lastCode then
        assert(context.setAttr("code", code) == code)
    end

    assert(context.hasAttr("code"))
end

return 14000 + code