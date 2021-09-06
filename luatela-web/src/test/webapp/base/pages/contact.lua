local txt = html.create()
txt:prln("<!DOCTYPE html>")

txt:open("html")
txt:open("head")
txt:el("title", "Hello World!")
txt:close("head")
txt:open("body")
txt:prln("This is my line, there are many like it but this one is mine.")
txt:close("body")
txt:close("html")

return txt