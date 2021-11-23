local hamlet = '2beornot2be'

local function testPath(p, str)
    str = str or hamlet

    assert(p)
    assert(type(p) == '*PATH')
    assert(tostring(p) == str)
end

assert(path)
assert(type(path) == 'function')

local p = path()

testPath(p, '')
testPath(path '2beornot2be')
testPath(path(hamlet))
testPath(path('2beor', 'not2be'))
testPath(path('2', 'be', 'or', 'not', '2', 'be'))
testPath(path('2beor', path('not2be')))
testPath(path('2', 'be', 'or', path('not2be')))
testPath(path('2beor', path('not', '2', 'be')))
testPath(path('2', 'be', 'or', path('not', '2', 'be')))
testPath(path(path('2beor'), path('not2be')))
testPath(path(path('2', 'be', 'or'), path('not', '2', 'be')))
p = path('2', 'be')
testPath(p, '2be')
testPath(path(p, 'or', 'not', p))
testPath(path('2', path('be', path('or', path('not', path('2', path('be')))))))
testPath(path(path(path(path(path(path('2'), 'be'), 'or'), 'not'), '2'), 'be'))
testPath(path(2, 'beornot', 2, 'be'))
testPath(path(2, 'beornot', path(2), 'be'))
testPath(path(path(2, 'beornot'), path(2), 'be'))
testPath(path('2beor'):path('not2be'))
testPath(path('2', 'be', 'or'):path('not2be'))
testPath(path('2', 'be', 'or'):path('not', 2, 'be'))
testPath(path('2', 'be'):path('or', 'not'):path('2', 'be'))
testPath(path('2'):path('be'):path('or'):path('not'):path(2):path('be'))
p = path(2):path('be', path('or'):path('not', path('2'):path('be')))
testPath(p)

local s = '(\\\\|/)'
assert(p:getfile():find('src' .. s .. 'test' .. s .. 'resources' .. s .. 'routes' .. s .. '2beornot2be'))

finished()