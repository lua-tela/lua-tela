local url = request.path:match("/request/get(.*)")

assert(url)
assert(request.GET)

if url == '/single' then
    assert(request.method == 'get')

    local query = 'key=value'
    assert(tostring(request.GET) == query)

    assert(request.GET['key'])
    assert(request.GET['key'] == 'value')

    return 20001
elseif url == '/ampersand' then
    assert(request.method == 'get')

    local query = 'ampersand=%26'
    assert(tostring(request.GET) == query)

    assert(request.GET['ampersand'])
    assert(request.GET['ampersand'] == '&')

    return 20002
elseif url == '/querycookie' then
    assert(request.method == 'get')

    local query = 'B00B135'
    assert(tostring(request.GET) == query)

    assert(request.GET['B00B135'])
    assert(request.GET['B00B135'] == true)

    return 20003
elseif url:find('/(get|post)-catdog') then
    assert(request.method == url:match('/(.*)-catdog'))
    assert(request.GET['cat'])
    assert(request.GET['cat'] == 'dog')

    return 20004
elseif url == '/multiple' then
    local query = 'key1=value%31&key2=&key4&keyA=2&keyB=%26&keyC&keyD=8'
    assert(tostring(request.GET) == query)

    assert(request.GET['key1'])
    assert(request.GET['key2'])
    assert(not request.GET['key3'])
    assert(request.GET['key4'])
    assert(request.GET['keyA'])
    assert(request.GET['keyB'])
    assert(request.GET['keyC'])
    assert(request.GET['keyD'])
    assert(not request.GET['keyE'])

    assert(request.GET['key1'] == 'value1')
    assert(request.GET['key2'] == '')
    assert(request.GET['key3'] == nil)
    assert(request.GET['key4'] == true)
    assert(request.GET['keyA'] == '2')
    assert(request.GET['keyB'] == '&')
    assert(request.GET['keyC'] == true)
    assert(request.GET['keyD'] == '8')

    return 20005
elseif url == '/flags' then
    local query = 'flag1&flag2&flag3'
    assert(tostring(request.GET) == query)

    assert(request.GET['flag1'])
    assert(request.GET['flag2'])
    assert(request.GET['flag3'])

    assert(request.GET['flag1'] == true)
    assert(request.GET['flag2'] == true)
    assert(request.GET['flag3'] == true)

    return 20006
end

return 20000