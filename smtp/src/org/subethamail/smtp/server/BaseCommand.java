package org.subethamail.smtp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
abstract public class BaseCommand implements Command
{
	private String name;
	private static Map<String, HelpMessage> helpMessageMap = new HashMap<String, HelpMessage>();
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(BaseCommand.class);
	
	public BaseCommand(String name, String help)
	{
		this.name = name;
		setHelp(new HelpMessage(name, help));
	}
	
	public BaseCommand(String name, String help, String argumentDescription)
	{
		this.name = name;
		setHelp(new HelpMessage(name, help, argumentDescription));
	}
	
	/**
	 * This is the main method that you need to override in order to implement a command.
	 */
	abstract public void execute(String commandString, ConnectionContext context) throws IOException;
	
	public void setHelp(HelpMessage helpMessage)
	{
		helpMessageMap.put(helpMessage.getName().toUpperCase(), helpMessage);
	}
	
	public HelpMessage getHelp(String commandName)
		throws CommandException
	{
		HelpMessage msg = helpMessageMap.get(commandName.toUpperCase());
		if (msg == null)
			throw new CommandException();
		return msg;
	}
	
	public Map<String, HelpMessage> getHelp()
	{
		return helpMessageMap;
	}
	
	protected String getArgPredicate(String commandString)
	{
		if (commandString == null || commandString.length() < 4)
			return "";
		
		return commandString.substring(4).trim();
	}
	
	public String getName()
	{
		return name;
	}
	
	protected boolean isValidEmailAddress(String address)
	{
		// MAIL FROM: <>
		if (address.length() == 0)
			return true;

		boolean result = false;
		try
		{
			InternetAddress[] ia = InternetAddress.parse(address, true);
			if (ia.length == 0)
				result = false;
			else
				result = true;
		}
		catch (AddressException ae)
		{
			result = false;
		}
		return result;
	}
	
	protected static void getTokenizedString(StringBuilder sb, Collection<String> items, String delim)
	{
		for( Iterator<String> it=items.iterator(); it.hasNext(); )
		{
			sb.append(it.next());
			if( it.hasNext() )
			{
				sb.append(delim);
			}
		}
	}
	
	protected String[] getArgs(String commandString)
	{
		List<String> strings = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(commandString);
		while (stringTokenizer.hasMoreTokens())
		{
			strings.add(stringTokenizer.nextToken());
		}
		
		return strings.toArray(new String[strings.size()]);
	}
	
	protected String extractEmailAddress(String args, int subcommandOffset)
	{
		String address = args.substring(subcommandOffset).trim();
		if (address.indexOf('<') == 0)
			address = address.substring(1, address.indexOf('>'));
		
		return address;
	}
}
