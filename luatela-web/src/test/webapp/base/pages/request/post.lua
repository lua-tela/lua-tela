local url = request.path:match("/request/post(.*)")

assert(url)

if url == '' then
    assert(not request.POST)

    return 21000
end

assert(request.POST)
assert(request.method == 'post')

if url == '/single' then
    assert(request.POST['key'])
    assert(not request.POST['nokey'])

    assert(request.POST['key'] == 'value')
    assert(request.POST['nokey'] == nil)

    return 21001
elseif url == '/multiple' then
    assert(request.POST['and'])
    assert(request.POST['name'])
    assert(request.POST['john'])

    assert(request.POST['and'] == 'his')
    assert(request.POST['name'] == 'was')
    assert(request.POST['john'] == 'cena')

    return 21002
elseif url == '/empty' then
    assert(request.POST['empty1'])
    assert(request.POST['empty2'])

    assert(request.POST['empty1'] == '')
    assert(request.POST['empty2'] == '')

    return 21003
elseif url:find('/catdog') then
    assert(request.POST['cat'])
    assert(request.POST['cat'] == 'dog')

    return 21004
end