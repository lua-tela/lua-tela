package com.hk.luatela.luacompat;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.luatela.LuaContext;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"unused", "unchecked"})
public enum RequestLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	hasParam() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(toString(), args, LuaType.STRING);

			Map<String, LuaObject> params = interp.getExtra("params", Map.class);

			if(params == null)
			{
				LuaContext ctx = interp.getExtra("context", LuaContext.class);
				return Lua.newBoolean(ctx.request.getParameter(args[0].getString()) != null);
			}
			else
			{
				return Lua.newBoolean(params.containsKey(args[0].getString()));
			}
		}
	},
	getParam() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(toString(), args, LuaType.STRING);

			Map<String, LuaObject> params = interp.getExtra("params", Map.class);

			if(params == null)
			{
				LuaContext ctx = interp.getExtra("context", LuaContext.class);
				String s = ctx.request.getParameter(args[0].getString());
				if(s == null && args.length > 1)
					return args[1];
				else
					return Lua.newString(s);
			}
			else
			{
				LuaObject obj = params.get(args[0].getString());
				if(obj == null)
					return args.length > 1 ? args[1] : Lua.nil();
				else
					return obj;
			}
		}
	},
	host() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.request.getRemoteHost()));
		}
	},
	port() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newNumber(ctx.request.getRemotePort()));
		}
	},
	user() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.request.getRemoteUser()));
		}
	},
	address() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.request.getRemoteAddr()));
		}
	},
	method() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.method));
		}
	},
	url() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.url));
		}
	},
	ctx() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.ctx));
		}
	},
	path() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			if(ctx != null)
				table.setIndex(env.interp, toString(), Lua.newString(ctx.path));
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
			Lua.checkArgs(toString(), args, LuaType.STRING);

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
			Lua.checkArgs(toString(), args, LuaType.STRING);

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
			Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.ANY);

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
				Lua.checkArgs(toString(), args, LuaType.STRING);
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
	FILES() {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
			LuaObject tbl = ctx.getFileTable(env.interp);
			if(tbl != null)
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
