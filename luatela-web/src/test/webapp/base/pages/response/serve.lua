local url = request.path:match("/response/status(.*)")

assert(url)

if url == '/w3.css' then
    response.serve('')
end

error('not a valid path')