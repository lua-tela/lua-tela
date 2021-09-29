local url = request.path:match("/request/files(.*)")

assert(url)

if url == '' then
    assert(not request.FILES)

    return 10000
elseif url == '/info.txt' then
    assert(request.FILES)

    local infotxt = request.FILES.infotxt

    assert(infotxt)
    assert(infotxt:getContentType():match('.*text/plain.*'))
    assert(infotxt:getName() == 'info.txt')
    assert(infotxt:toString() == "this is my file. there are many like it. but this one is mine")
    assert(infotxt:getSize() == 61)

    assert(request.hasParam("alsopost"))
    assert(request.getParam("alsopost") == 'exists')

    return 10001
end

error('not a valid path')