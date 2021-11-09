package com.hk.luatela.luacompat;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.luatela.LuaContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings({"unused", "unchecked"})
public enum RequestLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	host() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.request.getRemoteHost()));
		}
	},
	port() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newNumber(ctx.request.getRemotePort()));
		}
	},
	user() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.request.getRemoteUser()));
		}
	},
	address() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.request.getRemoteAddr()));
		}
	},
	method() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.method));
		}
	},
	url() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.url));
		}
	},
	ctx() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.ctx));
		}
	},
	path() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, name(), Lua.newString(ctx.path));
		}
	},
	isNewSess() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			LuaContext ctx = interp.getExtra("context", LuaContext.class);
			return Lua.newBoolean(ctx.request.getSession().isNew());
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
			return Lua.newBoolean(map.containsKey(args[0].getString()));
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
			return Lua.newBoolean(true);
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
				return obj == null ? Lua.nil() : Lua.newLuaObject(obj);
			}
			else
				ctx.request.getSession().invalidate();

			return Lua.newBoolean(true);
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
			LuaObject tbl = Lua.nil();
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
				table.setIndex(env.interp, name(), tbl);
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
					val = Lua.newBoolean(true);
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
				tbl.rawSet(entry.getKey(), arr.length == 0 ? arr[0] : Lua.newVarargs(arr));
			}

			table.setIndex(env.interp, name(), tbl);

			LuaObject metatable = Lua.newTable();

			metatable.rawSet("__name", "*QUERY");
			metatable.rawSet("__index", metatable);
			metatable.rawSet("__tostring", Lua.newFunc((interp, args) -> Lua.newString(query)));

			tbl.setMetatable(metatable);
		}
	},
	POST() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);

			LuaObject tbl = Lua.nil();

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
						tbl.rawSet(entry.getKey(), Lua.newBoolean(true));
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

			table.setIndex(env.interp, name(), tbl);
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
			table.setIndex(env.interp, name, Lua.newFunc(this));
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
				return val.getFloat();
			case INTEGER:
				return val.getInteger();
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
