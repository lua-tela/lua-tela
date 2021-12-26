package com.hk.luatela.runner;

import com.hk.lua.*;
import com.hk.luatela.InitializationException;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Runner implements Closeable
{
	private ScheduledExecutorService executor;
	private LuaInterpreter interp;
	PrintStream out;
	final Consumer<LuaInterpreter> preparer;
	int tasks;

	public Runner(Consumer<LuaInterpreter> preparer, Path initPath)
	{
		this.preparer = preparer;
		if(Files.exists(initPath))
		{
			String source = initPath.getFileName().toString();

			try
			{
				interp = Lua.reader(Files.newBufferedReader(initPath), source);

				interp.compile();
			}
			catch (LuaException e)
			{
				e.printStackTrace();
				throw new InitializationException("There was a problem compiling '" + initPath + "'");
			}
			catch (IOException e)
			{
				throw new InitializationException(e);
			}

			Lua.importStandard(interp);

			interp.setExtra("runner", this);

			interp.importLib(new LuaLibrary<>(null, RunnerLibrary.class));
		}
	}

	boolean wasCreated()
	{
		return executor != null;
	}

	void create()
	{
		executor = Executors.newSingleThreadScheduledExecutor();

		out.println("Runner service created:");
	}

	void scheduleSingle(LuaObject function, long amount, TimeUnit unit)
	{
		tasks++;
		executor.schedule(new LuaRunnable(function, true), amount, unit);
	}

	void schedule(LuaObject function, long amount, TimeUnit unit)
	{
		tasks++;
		executor.scheduleAtFixedRate(new LuaRunnable(function, false), amount, amount, unit);
	}

	public int getTasks()
	{
		return tasks;
	}

	public void collect(PrintStream out)
	{
		this.out = out;
		preparer.accept(interp);
		try
		{
			interp.execute();
		}
		catch(LuaException ex)
		{
			ex.printStackTrace();
			throw new InitializationException();
		}
		finally
		{
			this.out = null;
		}
	}

	@Override
	public void close()
	{
		executor.shutdownNow();
	}

	class LuaRunnable implements Runnable
	{
		private final LuaObject function;
		private final boolean single;
		long count;

		LuaRunnable(LuaObject function, boolean single)
		{
			this.function = function;
			this.single = single;
			count = 1L;
		}

		@Override
		public void run()
		{
			try
			{
				if (single)
					function.call(interp);
				else
					function.call(interp, Lua.newNumber(count++));
			}
			catch (LuaException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
