assert(model)
assert(type(model) == 'function')

assert(field)
assert(type(field) == 'function')

local fields = { 'string', 'float', 'integer', 'id' }

for _, f in ipairs(fields) do
    local varfield = field(f)
    assert(type(varfield) == 'function')
    varfield = varfield {}
    assert(type(varfield) == 'table')
    assert(type(varfield['__builder']) == '*FIELDBUILDER')
end

return true