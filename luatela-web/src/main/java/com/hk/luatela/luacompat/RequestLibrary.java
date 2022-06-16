package com.hk.luatela.luacompat;

import com.hk.json.Json;
import com.hk.lua.*;
import com.hk.luatela.LuaContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused", "unchecked"})
public enum RequestLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	host() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.request.getRemoteHost()));
		}
	},
	port() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newNumber(ctx.request.getRemotePort()));
		}
	},
	user() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.request.getRemoteUser()));
		}
	},
	address() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.request.getRemoteAddr()));
		}
	},
	method() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.method));
		}
	},
	url() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.url));
		}
	},
	ctx() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.ctx));
		}
	},
	path() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.rawSet(name(), Lua.newString(ctx.path));
		}
	},
	body() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			table.rawSet(name(), bodyTable(ctx));
		}
	},
	isNewSess() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			return Lua.newBool(ctx.request.getSession().isNew());
		}

		@Override
		public void accept(Environment env, LuaObject table)
		{
			super.accept(env, table);

			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);

			HttpSession session = ctx.request.getSession();
			if(session.isNew())
				session.setAttribute("lua", new HashMap<>());
		}
	},
	hasSess() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING);

			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			Map<String, Object> map =
					(Map<String, Object>) ctx.request.getSession().getAttribute("lua");
			return Lua.newBool(map.containsKey(args[0].getString()));
		}
	},
	getSess() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING);

			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			Map<String, Object> map =
					(Map<String, Object>) ctx.request.getSession().getAttribute("lua");
			return Lua.newLuaObject(map.get(args[0].getString()));
		}
	},
	setSess() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING, LuaType.ANY);

			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			Map<String, Object> map =
					(Map<String, Object>) ctx.request.getSession().getAttribute("lua");
			map.put(args[0].getString(), toObj(args[1]));
			return Lua.TRUE;
		}
	},
	removeSess() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			Map<String, Object> map =
					(Map<String, Object>) ctx.request.getSession().getAttribute("lua");
			if(args.length > 0)
			{
				Lua.checkArgs(name(), args, LuaType.STRING);
				Object obj = map.remove(args[0].getString());
				return obj == null ? Lua.NIL : Lua.newLuaObject(obj);
			}
			else
				ctx.request.getSession().invalidate();

			return Lua.TRUE;
		}
	},
	getSessID() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			return Lua.newString(ctx.request.getSession().getId());
		}
	},
	getHeader() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING);
			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			return Lua.newString(ctx.request.getHeader(args[0].getString()));
		}
	},
	getHeaders() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			LuaObject tbl = Lua.NIL;
			Enumeration<String> values;
			if(args.length == 0)
			{
				values = ctx.request.getHeaderNames();
			}
			else
			{
				Lua.checkArgs(name(), args, LuaType.STRING);
				values = ctx.request.getHeaders(args[0].getString());
			}

			long idx = 1;
			while(values.hasMoreElements())
			{
				if(idx == 1)
					tbl = Lua.newTable();

				tbl.rawSet(idx++, Lua.newString(values.nextElement()));
			}
			return tbl;
		}
	},
	FILES() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			LuaObject tbl = ctx.getFileTable(env.interp);
			if(tbl != null)
				table.rawSet(name(), tbl);
		}
	},
	GET() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);

			String query = ctx.request.getQueryString();
			List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
			Map<String, List<LuaObject>> map = new HashMap<>();

			List<LuaObject> list;
			LuaObject val;
			for (NameValuePair pair : pairs)
			{
				list = map.get(pair.getName());
				if(pair.getValue() == null)
					val = Lua.TRUE;
				else
					val = Lua.newString(pair.getValue());

				if(list == null)
					map.put(pair.getName(), new ArrayList<>(Collections.singleton(val)));
				else
					list.add(val);
			}

			LuaObject[] arr;
			LuaObject tbl = Lua.newTable();
			for (Map.Entry<String, List<LuaObject>> entry : map.entrySet())
			{
				arr = entry.getValue().toArray(new LuaObject[0]);
				tbl.rawSet(entry.getKey(), arr.length == 1 ? arr[0] : Lua.newVarargs(arr));
			}

			table.rawSet(name(), tbl);

			LuaObject metatable = Lua.newTable();

			metatable.rawSet("__name", "*QUERY");
			metatable.rawSet("__index", metatable);
			metatable.rawSet("__tostring", Lua.newMethod((interp, args) -> Lua.newString(query)));

			tbl.setMetatable(metatable);
		}
	},
	POST() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);

			LuaObject tbl = Lua.NIL;

			if(env.interp.hasExtra("post"))
			{
				tbl = Lua.newTable();
				Map<String, LuaObject> post = (Map<String, LuaObject>) env.interp.getExtra("post");

				for (Map.Entry<String, LuaObject> entry : post.entrySet())
					tbl.rawSet(entry.getKey(), entry.getValue());
			}
			else if(ctx.method.equals("post") || ctx.method.equals("put"))
			{
				tbl = Lua.newTable();
				Map<String, String[]> map = ctx.request.getParameterMap();

				String[] arr;
				for (Map.Entry<String, String[]> entry : map.entrySet())
				{
					arr = entry.getValue();

					switch (arr.length)
					{
					case 0:
						tbl.rawSet(entry.getKey(), Lua.TRUE);
						break;
					case 1:
						tbl.rawSet(entry.getKey(), Lua.newString(arr[0]));
						break;
					default:
						LuaObject[] objs = new LuaObject[arr.length];
						for (int i = 0; i < arr.length; i++)
							objs[i] = Lua.newString(arr[i]);

						tbl.rawSet(entry.getKey(), Lua.newVarargs(objs));
						break;
					}
				}
			}

			table.rawSet(name(), tbl);
		}
	};

	@Override
	public LuaObject call(LuaInterpreter interp, LuaObject[] args)
	{
		throw new Error();
	}

	@Override
	public void accept(Environment env, LuaObject table)
	{
		String name = toString();
		if(name != null && !name.trim().isEmpty())
			table.rawSet(name, Lua.newMethod(this));
	}

	private static LuaObject bodyTable(LuaContext ctx)
	{
		HttpServletRequest request = ctx.request;
		LuaObject table = Lua.newTable();

		LuaObject func = Lua.newMethod((interp, args) -> {
			if(table.rawGet("used").getBoolean())
				throw new LuaException("request body already used!");
			table.rawSet("used", "string");

			try
			{
				CharArrayWriter wtr = new CharArrayWriter();
				Reader rdr = request.getReader();
				char[] cs = new char[512];
				int len;
				while((len = rdr.read(cs)) > 0)
					wtr.write(cs, 0, len);

				return Lua.newString(wtr.toString());
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		});
		table.rawSet("tostring", func);
		table.rawSet("toreader", Lua.newMethod((interp, args) -> {
			if(table.rawGet("used").getBoolean())
				throw new LuaException("request body already used!");
			table.rawSet("used", "reader");

			try
			{
				return new LuaReader(request.getReader());
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}));
		table.rawSet("tofile", Lua.newMethod((interp, args) -> {
			Lua.checkArgs("tofile", args, LuaType.STRING);

			try
			{
				if(table.rawGet("used").getBoolean())
					throw new LuaException("request body already used!");
				table.rawSet("used", "file");

				Path path = Paths.get(args[0].getString());
				if(!path.isAbsolute())
					path = ctx.luaTela.dataroot.resolve(path);

				OutputStream out = Files.newOutputStream(path);
				InputStream in = request.getInputStream();
				byte[] arr = new byte[1024];
				int read, sum = 0;

				while ((read = in.read(arr)) != -1)
				{
					out.write(arr, 0, read);
					sum += read;
				}

				return Lua.newNumber(sum);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}));
		table.rawSet("used", Lua.FALSE);
		table.rawSet("tojson", Lua.newMethod((interp, args) -> {
			if(table.rawGet("used").getBoolean())
				throw new LuaException("request body already used!");
			table.rawSet("used", "json");

			try
			{
				return LuaLibraryJson.toLua(Json.read(request.getReader()));
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}));

		LuaObject metatable = Lua.newTable();

//		metatable.rawSet("__name", "*REQUEST_BODY");
		metatable.rawSet("__index", metatable);
		metatable.rawSet("__tostring", func);

		table.setMetatable(metatable);

		return table;
	}

	private static Object toObj(LuaObject val)
	{
		switch(val.type())
		{
			case NIL:
				return null;
			case BOOLEAN:
				return val.getBoolean();
			case STRING:
				return val.getString();
			case FLOAT:
				return val.getDouble();
			case INTEGER:
				return val.getLong();
			case TABLE:
				Map<Object, Object> map = new LinkedHashMap<>();
				for(Map.Entry<LuaObject, LuaObject> ent : val.getEntries())
					map.put(toObj(ent.getKey()), toObj(ent.getValue()));

				return map;
			default:
				throw new LuaException("Cannot set " + val.name() + " in the session");
		}
	}
}
