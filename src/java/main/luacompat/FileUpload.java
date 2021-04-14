package main.luacompat;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;
import java.io.File;
import java.nio.file.Paths;
import main.db.LuaBase;
import org.apache.commons.fileupload.FileItem;

public class FileUpload extends LuaUserdata
{
    private final LuaInterpreter interp;
    private final FileItem fi;
    
    public FileUpload(LuaInterpreter interp, FileItem fi)
    {
        this.interp = interp;
        this.fi = fi;
        metatable = uploadMetatable;
    }
    
    private static LuaObject getField(LuaObject[] args)
    {
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newString(((FileUpload) args[0]).fi.getFieldName());
        else
            throw new LuaException("bad argument #1 to 'getField' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject getName(LuaObject[] args)
    {
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newString(((FileUpload) args[0]).fi.getName());
        else
            throw new LuaException("bad argument #1 to 'getName' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject getContentType(LuaObject[] args)
    {
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newString(((FileUpload) args[0]).fi.getContentType());
        else
            throw new LuaException("bad argument #1 to 'getContentType' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject getHeader(LuaObject[] args)
    {
        Lua.checkArgs("getHeader", args, LuaType.ANY, LuaType.STRING);
            
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newString(((FileUpload) args[0]).fi.getHeaders().getHeader(args[1].getString()));
        else
            throw new LuaException("bad argument #1 to 'getHeader' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject getHeaders(LuaObject[] args)
    {
        Lua.checkArgs("getHeaders", args, LuaType.ANY, LuaType.STRING);
            
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newLuaObject(((FileUpload) args[0]).fi.getHeaders().getHeaders(args[1].getString()));
        else
            throw new LuaException("bad argument #1 to 'getHeaders' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject getHeaderNames(LuaObject[] args)
    {            
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newLuaObject(((FileUpload) args[0]).fi.getHeaders().getHeaderNames());
        else
            throw new LuaException("bad argument #1 to 'getHeaderNames' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject getSize(LuaObject[] args)
    {            
        if(args.length >= 1 && args[0] instanceof FileUpload)
            return Lua.newNumber(((FileUpload) args[0]).fi.getSize());
        else
            throw new LuaException("bad argument #1 to 'getSize' (expected UPLOAD*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static LuaObject toFile(LuaObject[] args)
    {
        if(args.length >= 1 && args[0] instanceof FileUpload)
        {
            FileUpload fu = (FileUpload) args[0];
            LuaBase db = fu.interp.getExtra("db", LuaBase.class);
            
            File saveTo;
            if(args.length >= 2)
            {
                if(!args[1].isString())
                    throw new LuaException("bad argument #2 to 'toFile' (expected string, got " + (args.length >= 2 ? args[1].name() : "nil") + ")");
                String s = args[1].getString();
                
                if(Paths.get(s).isAbsolute())
                    saveTo = new File(s);
                else if(s.startsWith(File.separator) || s.startsWith("/"))
                    saveTo = new File(db.base, "uploads" + s);
                else
                    saveTo = new File(db.base, "uploads" + File.separator + s);
            }
            else
                saveTo = new File(db.base, "uploads" + File.separator + fu.fi.getName());

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
    
    private static LuaObject toString(LuaObject[] args)
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
    public String getString()
    {
        return "'" + fi.getName() + "' file upload (field: " + fi.getFieldName() + ", type: " + fi.getContentType() + ")";
    }
    
    private static final String NAME = "UPLOAD*";
    private static final LuaObject uploadMetatable = Lua.newTable();
    
    static
    {
        uploadMetatable.setIndex("__name", NAME);
        uploadMetatable.setIndex("__index", uploadMetatable);
        uploadMetatable.setIndex("getField", Lua.newFunc(FileUpload::getField));
        uploadMetatable.setIndex("getName", Lua.newFunc(FileUpload::getName));
        uploadMetatable.setIndex("getContentType", Lua.newFunc(FileUpload::getContentType));
        uploadMetatable.setIndex("getHeader", Lua.newFunc(FileUpload::getHeader));
        uploadMetatable.setIndex("getHeaders", Lua.newFunc(FileUpload::getHeaders));
        uploadMetatable.setIndex("getHeaderNames", Lua.newFunc(FileUpload::getHeaderNames));
        uploadMetatable.setIndex("getSize", Lua.newFunc(FileUpload::getSize));
        uploadMetatable.setIndex("toFile", Lua.newFunc(FileUpload::toFile));
        uploadMetatable.setIndex("toString", Lua.newFunc(FileUpload::toString));
    }
}
