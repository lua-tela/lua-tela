--[[
--------------------------------------------------
        Generated by Lua Tela on
        Wednesday, November 10, 2021 at 10:57:32 PM Eastern Standard Time
--------------------------------------------------
        Patch #1
        Elapsed Time: 0.9678ms
--------------------------------------------------
]]

if patchNo ~= 1 then
    return false
end
patchNo = patchNo + 1

models['point'] = {
    ['id'] = { 'id', {primary=true} },
    ['x'] = { 'float', {primary=false} },
    ['y'] = { 'float', {primary=false} },
    ['z'] = { 'float', {primary=false} },
}

return true