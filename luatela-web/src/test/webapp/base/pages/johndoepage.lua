local txt = html.create()

txt:wr('{')
txt:wr('"key": "value",')
txt:wr('"path": "'):wr(request.path):wr('"')
txt:wr('}')

return txt