-- Loaded src/test/resources/base-just-patch-unchanged/models.lua

assert(database.model)
assert(type(database.model) == 'function')

local nothing1 = database.model 'doesnotexist'
assert(nothing1 == nil)

local nothing2 = database.model 'definitelynotreal'
assert(nothing2 == nil)

Point = database.model 'point'
assert(Point)
assert(type(Point) == "*MODEL")
assert(tostring(Point) == "model 'point'")
assert(database.model 'point' == Point)

newpoint = Point{x=1, y=2, z=3}
assert(newpoint)
assert(type(newpoint) == '*INSTANCE')
assert(tostring(newpoint) == "'point' instance")
assert(newpoint == newpoint)

assert(newpoint.x == 1)
assert(newpoint.y == 2)
assert(newpoint.z == 3)
newpoint.x = 4
newpoint.y = 5
newpoint.z = 6
assert(newpoint.x == 4)
assert(newpoint.y == 5)
assert(newpoint.z == 6)
newpoint.x = 7
newpoint.y = 8
newpoint.z = 9
assert(newpoint.x == 7)
assert(newpoint.y == 8)
assert(newpoint.z == 9)

function testSuccess()
    assert(not success)
    assert(msg:find 'attempt to index')
end

success, msg = pcall(function()
    newpoint.a = -4
end)
testSuccess()

success, msg = pcall(function()
    newpoint.b = -5
end)
testSuccess()

success, msg = pcall(function()
    newpoint.x1 = 0
end)
testSuccess()

assert(newpoint.a == nil)
assert(newpoint.b == nil)
assert(newpoint.c == nil)
assert(newpoint.w == nil)
assert(newpoint.x1 == nil)

newpoint:save()
assert(newpoint.id == 1)

return true