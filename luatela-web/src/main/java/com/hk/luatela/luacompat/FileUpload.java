package com.hk.luatela.luacompat;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hk.luatela.LuaTela;
import org.apache.commons.fileupload.FileItem;

public class FileUpload extends LuaUserdata
{
	private final FileItem fi;

	public FileUpload(FileItem fi)
	{
		this.fi = fi;
		metatable = uploadMetatable;
	}

	private static LuaObject getField(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newString(((FileUpload) args[0]).fi.getFieldName());
		else
			throw new LuaException("bad argument #1 to 'getField' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject getName(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newString(((FileUpload) args[0]).fi.getName());
		else
			throw new LuaException("bad argument #1 to 'getName' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject getContentType(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newString(((FileUpload) args[0]).fi.getContentType());
		else
			throw new LuaException("bad argument #1 to 'getContentType' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject getHeader(LuaInterpreter interp, LuaObject[] args)
	{
		Lua.checkArgs("getHeader", args, LuaType.ANY, LuaType.STRING);

		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newString(((FileUpload) args[0]).fi.getHeaders().getHeader(args[1].getString()));
		else
			throw new LuaException("bad argument #1 to 'getHeader' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject getHeaders(LuaInterpreter interp, LuaObject[] args)
	{
		Lua.checkArgs("getHeaders", args, LuaType.ANY, LuaType.STRING);

		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newLuaObject(((FileUpload) args[0]).fi.getHeaders().getHeaders(args[1].getString()));
		else
			throw new LuaException("bad argument #1 to 'getHeaders' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject getHeaderNames(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newLuaObject(((FileUpload) args[0]).fi.getHeaders().getHeaderNames());
		else
			throw new LuaException("bad argument #1 to 'getHeaderNames' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject getSize(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
			return Lua.newNumber(((FileUpload) args[0]).fi.getSize());
		else
			throw new LuaException("bad argument #1 to 'getSize' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject toFile(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
		{
			FileUpload fu = (FileUpload) args[0];
			Path dataRoot = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class).dataroot;
			File uploads = dataRoot.resolve("uploads").toFile();

			File saveTo;
			if(args.length >= 2)
			{
				if(!args[1].isString())
					throw new LuaException("bad argument #2 to 'toFile' (expected string, got " + (args.length >= 2 ? args[1].name() : "nil") + ")");
				String s = args[1].getString();

				if(Paths.get(s).isAbsolute())
					saveTo = new File(s);
				else if(s.startsWith("\\") || s.startsWith("/") ||
						s.startsWith(File.separator))
					saveTo = new File(uploads, s.substring(1));
				else
					saveTo = new File(uploads, s);
			}
			else
				saveTo = new File(uploads, fu.fi.getName());

			try
			{
				fu.fi.write(saveTo);
				return Lua.newBoolean(true);
			}
			catch (Exception ex)
			{
				throw new LuaException(ex.getLocalizedMessage());
			}
		}
		else
			throw new LuaException("bad argument #1 to 'toFile' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	private static LuaObject toString(LuaInterpreter interp, LuaObject[] args)
	{
		if(args.length >= 1 && args[0] instanceof FileUpload)
		{
			FileUpload fu = (FileUpload) args[0];
			try
			{
				return Lua.newString(fu.fi.getString());
			}
			catch (Exception ex)
			{
				return Lua.newVarargs(Lua.nil(), Lua.newString(ex.getLocalizedMessage()));
			}
		}
		else
			throw new LuaException("bad argument #1 to 'toFile' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
	}

	@Override
	public final String name()
	{
		return NAME;
	}

	@Override
	public Object getUserdata()
	{
		return this;
	}

	@Override
	public String getString(LuaInterpreter interp)
	{
		return "'" + fi.getName() + "' file upload (field: " + fi.getFieldName() + ", type: " + fi.getContentType() + ")";
	}

	private static final String NAME = "UPLOAD*";
    private static final LuaObject uploadMetatable = Lua.newTable();

    static
    {
        uploadMetatable.rawSet("__name", NAME);
        uploadMetatable.rawSet("__index", uploadMetatable);
        uploadMetatable.rawSet("getField", Lua.newFunc(FileUpload::getField));
        uploadMetatable.rawSet("getName", Lua.newFunc(FileUpload::getName));
        uploadMetatable.rawSet("getContentType", Lua.newFunc(FileUpload::getContentType));
        uploadMetatable.rawSet("getHeader", Lua.newFunc(FileUpload::getHeader));
        uploadMetatable.rawSet("getHeaders", Lua.newFunc(FileUpload::getHeaders));
        uploadMetatable.rawSet("getHeaderNames", Lua.newFunc(FileUpload::getHeaderNames));
        uploadMetatable.rawSet("getSize", Lua.newFunc(FileUpload::getSize));
        uploadMetatable.rawSet("toFile", Lua.newFunc(FileUpload::toFile));
        uploadMetatable.rawSet("toString", Lua.newFunc(FileUpload::toString));
    }
}
