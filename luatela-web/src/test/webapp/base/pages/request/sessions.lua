local url = request.path:match("/request/sessions(.*)")

assert(url)

if url == '/new' then
    assert(request.isNewSess())
    assert(request.setSess("exists", request.getSessID()))
elseif url == '/test' then
    assert(not request.isNewSess())
    assert(request.hasSess('exists'))
    assert(request.getSess('exists') == request.getSessID())
    assert(request.setSess("mykey", "myvalue"))
    assert(request.setSess("mycode", 742))
    assert(request.setSess("a table", { key=true }))
elseif url == '/check' then
    assert(request.getSess("mykey") == 'myvalue')
    assert(request.getSess("mycode") == 742)
    assert(request.hasSess("a table"))
elseif url == '/remove' then
    assert(request.removeSess())
else
    error('not a valid path')
end

return 39610