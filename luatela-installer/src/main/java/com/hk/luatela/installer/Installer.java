package com.hk.luatela.installer;

import java.text.DateFormat;
import java.util.*;
import java.util.function.Supplier;

public class Installer
{
	private static Command currentCommand;

	private static void intro()
	{
		System.out.println("--[===========================[ LUA-TELA ]===========================]");
		System.out.println("Lua-Tela Web Framework (the power within)");
		System.out.println("/*");
		System.out.println(" * tool for Lua-Tela web framework.");
		System.out.println(" */");
		System.out.println();
		System.out.print("Date: ");
		System.out.println(FULL_FORMAT.format(new Date()));
	}

	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread(Installer::close));

		in = new Scanner(System.in);
		if(args != null && args.length > 0)
		{
			intro();
			System.out.println();
			System.out.println("cmd: " + String.join(" ", args));
			Supplier<Command> supplier = commands.get(args[0]);

			if(supplier != null)
			{
				currentCommand = supplier.get();
				currentCommand.execute(new LinkedList<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length))));
				currentCommand.close();
				currentCommand = null;
			}
			else
				unknownCommand(args[0]);
		}
		else
		{
			intro();
			System.out.println("Type help or a command...");
			System.out.println();

			String command;
			boolean doExit = false;
			do {
				System.out.print("cmd: ");

				command = in.nextLine().trim();

				if (command.isEmpty())
					continue;
				else if (command.equalsIgnoreCase("exit") ||
						command.equalsIgnoreCase("close"))
					doExit = true;
				else
					processCommand(command);
				System.out.println();
			} while (!doExit);
		}

		in.close();
		System.out.println("Tchau!");
	}

	private static void processCommand(String command)
	{
		LinkedList<String> arguments = new LinkedList<>();

		StringBuilder argument = new StringBuilder();
		char c;
		for (int i = 0; i < command.length(); i++)
		{
			c = command.charAt(i);
			if (c == '"')
			{
				int j;
				for (j = i + 1; j < command.length(); j++)
				{
					c = command.charAt(j);

					if (c == '\\')
					{
						if (j < command.length() - 1)
						{
							c = command.charAt(++j);

							if (c == '\\')
								argument.append('\\');
							else if (c == '"')
								argument.append('"');
							else
							{
								argument.append('\\');
								j--;
							}
						}
						else
							argument.append('\\');
					}
					else if (c == '"')
						break;
					else
						argument.append(c);
				}
				i = j;

				arguments.add(argument.toString());
				argument.setLength(0);
			}
			else if (!Character.isWhitespace(c))
			{
				argument.append(c);
				int j;
				for (j = i + 1; j < command.length(); j++)
				{
					c = command.charAt(j);
					if (Character.isWhitespace(c))
						break;
					else
						argument.append(c);
				}
				i = j - 1;

				arguments.add(argument.toString());
				argument.setLength(0);
			}
		}

		command = arguments.removeFirst();

		Supplier<Command> supplier = commands.get(command);

		if(supplier != null)
		{
			currentCommand = supplier.get();
			currentCommand.execute(arguments);
			currentCommand.close();
			currentCommand = null;
		}
		else
			unknownCommand(command);
	}

	private static void close()
	{
		if(currentCommand != null)
			currentCommand.close();
	}

	private static void unknownCommand(String command)
	{
		System.out.println("Unknown command: '" + command + "', try 'help' to see a list of available commands.");
	}

	static List<String> splitToLinesByLen(String string, int length)
	{
		List<String> lines = new ArrayList<>();

		String[] sp = string.split("\n");
		int idx;
		for (String s : sp)
		{
			while (s.length() > length)
			{
				idx = length;

				while (idx > 0)
				{
					if (s.charAt(idx) == ' ')
						break;

					idx--;
				}

				if (idx == 0)
				{
					for(idx = length; idx < s.length(); idx++)
					{
						if (Character.isWhitespace(s.charAt(idx)))
							break;
					}

					if(idx == s.length())
					{
						lines.add(s);
						s = "";
					}
					else
					{
						lines.add(s.substring(0, idx));
						s = s.substring(idx + 1);
					}
				}
				else
				{
					lines.add(s.substring(0, idx));
					s = s.substring(idx + 1);
				}
			}

			if (!s.isEmpty())
				lines.add(s);
		}

		return lines;
	}

	static String getParam(LinkedList<String> arguments, String param)
	{
		Iterator<String> itr = arguments.iterator();

		while(itr.hasNext())
		{
			if(itr.next().equals(param))
			{
				if(itr.hasNext())
				{
					itr.remove();
					String str = itr.next();
					itr.remove();
					return str;
				}
				break;
			}
		}
		return null;
	}

	private static Scanner in;
	private static final Map<String, Supplier<Command>> commands = new TreeMap<>();
	public static final DateFormat FULL_FORMAT = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

	static
	{
		commands.put("help", HelpCommand::new);
		commands.put("run", RunCommand::new);
		commands.put("patch-compare", PatchCompareCommand::new);
	}

	private static class HelpCommand extends Command
	{
		@Override
		void execute(LinkedList<String> arguments)
		{
			if (arguments != null && arguments.size() != 0)
			{
				String command = arguments.removeFirst();

				Supplier<Command> supplier = commands.get(command);

				if(supplier != null)
				{
					Command prev = currentCommand;
					currentCommand = supplier.get();
					currentCommand.help();
					currentCommand = prev;
				}
				else
					unknownCommand(command);
			}
			else
				help();
		}

		@Override
		void help()
		{
			System.out.println("Available commands:");

			for (String command : commands.keySet())
				System.out.println("\t" + command);
			System.out.println("\texit/close");
		}

		@Override
		public void close() {}
	}

	static abstract class Command
	{
		protected final Scanner in;

		Command()
		{
			this.in = Installer.in;
		}

		abstract void execute(LinkedList<String> arguments);

		abstract void help();

		void close() {}
	}
}
