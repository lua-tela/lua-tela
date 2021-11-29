local url = request.path:match("/request/body(.*)")

assert(url)
assert(request.body)
assert(type(request.body) == 'table')
assert(type(request.body.tostring) == 'function')
assert(type(request.body.toreader) == 'function')
assert(type(request.body.tofile) == 'function')
assert(not request.body.used)
if url ~= '' then
    -- https://stackoverflow.com/q/978061/4418599
    assert(request.method == 'post')
end

if url:find('^/string.*') then
    local b = math.tointeger(url:match('^/string(.*)'))
    local str
    if b == 1 then
        str = 'this is my string'
    elseif b == 2 then
        str = 'quavo, offset, and takeoff'
    else
        error('unknown flag, ' .. b)
    end

    assert(not request.body.used)
    assert(request.body.tostring() == str)
    assert(request.body.used == 'string')
    local function dofail()
        request.body.tostring()
        return true
    end

    local res, err = pcall(dofail)
    assert(not res)
    assert(err:find('.*already used.*'))

    return 17000 + b
elseif url:find('^/tostring.*') then
    local b = math.tointeger(url:match('^/tostring(.*)'))
    local str
    if b == 1 then
        str = 'this is my string'
    elseif b == 2 then
        str = 'quavo, offset, and takeoff'
    else
        error('unknown flag, ' .. b)
    end

    assert(not request.body.used)
    assert(request.body.tostring() == str)
    assert(request.body.used == 'string')
    local function dofail()
        tostring(request.body)
        return true
    end

    local res, err = pcall(dofail)
    assert(not res)
    assert(err:find('.*already used.*'))

    return 17002 + b
elseif url == '/toreader' then
    assert(not request.body.used)

    --- @type file
    local reader = request.body.toreader()
    assert(reader)
    assert(type(reader) == 'FILE*')

    assert(reader:read('l') == 'but it\'s tru tho')
    assert(reader:read('L') == 'ikno it\'s tru tho\n')
    assert(reader:read('a') == 'rep the north like I\'m Trudeau')

    assert(request.body.used == 'reader')
    local function dofail()
        request.body.toreader()
        return true
    end

    local res, err = pcall(dofail)
    assert(not res)
    assert(err:find('.*already used.*'))

    return 17005
elseif url == '/tofile' then
    assert(not request.body.used)

    local length = request.body.tofile(context.resPath 'rap.txt')
    assert(length == 654)
    assert(request.body.used == 'file')

    local function dofail()
        request.body.tofile('yeet')
        return true
    end

    local res, err = pcall(dofail)
    assert(not res)
    assert(err:find('.*already used.*'))

    return 17006
elseif url == '/json' then
    local movie = json.read(tostring(request.body))

    assert(movie.title == 'The Shawshank Redemption')
    assert(movie.director == 'Frank Darabont')
    assert(movie.screenplay == 'Stephen King')
    assert(movie.releasedate == 'September 22, 1994')

    local cast = movie.cast
    assert(#cast == 5)
    assert(cast[1] == 'Morgan Freeman')
    -- intentional skip
    assert(cast[3] == 'Clancy Brown')
    assert(cast[4] == 'Bob Gunton')
    assert(cast[5] == 'James Whitmore')

    return 17007
end

return 17000