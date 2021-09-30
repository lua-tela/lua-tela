local url = request.path:match("/context/paths(.*)")

assert(url)

if url == '' then
    return 13000
end

error('not a valid path')