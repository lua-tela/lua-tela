local url = request.path:match("/request/params(.*)")

assert(url)

if url == '' then
    return 81142
end

assert(request.hasParam 'key1')
assert(request.hasParam 'key2')
assert(not request.hasParam 'key3')
assert(request.hasParam 'keyA')
assert(request.hasParam 'keyB')
assert(not request.hasParam 'keyC')
assert(request.hasParam 'keyD')

assert(request.getParam('key1') == 'value1')
assert(request.getParam('key2') == '')
assert(request.getParam('key3') == nil)
assert(request.getParam('keyA') == '2')
assert(request.getParam('keyB') == '4')
assert(request.getParam('keyC') == nil)
assert(request.getParam('keyD') == '8')

return 'true'