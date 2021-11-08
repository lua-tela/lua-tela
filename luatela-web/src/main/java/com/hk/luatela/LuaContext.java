package com.hk.luatela;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.luatela.luacompat.FileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuaContext
{
	public final LuaTela luaTela;
	public final String url, ctx, path, method;
	public final HttpServletRequest request;
	public final HttpServletResponse response;

	public LuaContext(LuaTela luaTela, HttpServletRequest request, HttpServletResponse response, String method)
	{
		this.luaTela = luaTela;
		this.request = request;
		this.response = response;
		String path = request.getRequestURI();
		url = request.getRequestURL().substring(0, request.getRequestURL().length() - path.length());
		ctx = request.getContextPath();
		this.path = path.substring(ctx.length());
		this.method = method;
	}

	public LuaObject getFileTable(LuaInterpreter interp)
	{
		Path dataRoot = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class).dataRoot;
		File uploads = dataRoot.resolve("uploads").toFile();
		LuaObject tbl = Lua.newTable();
		try
		{
			uploads.mkdirs();

			DiskFileItemFactory diff = new DiskFileItemFactory();
			diff.setSizeThreshold(500 * 1024); // 500 KB
			diff.setRepository(uploads);

			ServletFileUpload sfu = new ServletFileUpload(diff);
			sfu.setFileSizeMax(500 * 1024 * 1024); // 500 MB

			List<FileItem> lst = sfu.parseRequest(new ServletRequestContext(request));

			long index = 1;
			FileUpload fu;
			Map<String, LuaObject> post = new HashMap<>();
			for(FileItem fi : lst)
			{
				if(fi.isFormField())
				{
					post.put(fi.getFieldName(), Lua.newString(fi.getString()));
				}
				else if(fi.getSize() != 0)
				{
					fu = new FileUpload(fi);
					tbl.rawSet(index++, fu);
					tbl.rawSet(fi.getFieldName(), fu);
					post.put(fi.getFieldName(), fu);
				}
			}
			if(index == 1)
				tbl = null;
			else
				interp.setExtra("post", post);
		}
		catch (FileUploadBase.InvalidContentTypeException ex)
		{
			tbl = null;
		}
		catch (FileUploadException ex)
		{
			throw new RuntimeException(ex);
		}
		return tbl;
	}
}
