if patchNo ~= 1 then
    return false
end
patchNo = patchNo + 1

models['point'] = {}
models['point']['x'] = { 'float', {primary=true} }
models['point']['y'] = { 'float', {primary=true} }
models['point']['z'] = { 'float', {primary=true} }

return true