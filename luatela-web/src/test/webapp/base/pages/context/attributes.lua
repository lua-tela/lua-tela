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

print(lastCode, code)

if url:match("\\/set\\/\\d+") then
    code = checkCode(tonumber(url:match("\\/set\\/(\\d+)")))
elseif url == '/add' then
    code = checkCode(code + 1)
elseif url == '/sub' then
    code = checkCode(code - 1)
elseif url ~= '' then
    checkCode(-1)
end

if code ~= lastCode then
    print("setting", code)
    context.setAttr("code", code)
end

return 14000 + code