local url = request.path:match("/context/escaping(.*)")

assert(url == '')

local html = '<input type="text" name="oh yea!"/>'
local escapedHtml = context.escapeHTML(html)

assert(escapedHtml ~= html)
assert(not escapedHtml:match(".*[\\<\\>\\\"].*"))

assert(context.urlEncode('hello world') == 'hello+world')
assert(context.urlDecode('hello+world') == 'hello world')

assert(context.urlEncode(html) ~= html)
assert(context.urlDecode(context.urlEncode(html)) ~= context.urlEncode(html))
assert(context.urlDecode(context.urlEncode(html)) == html)

return 16000