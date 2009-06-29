package org.subethamail.smtp.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * @author Jeff Schnitzer
 */
public class EmailUtils
{
	/**
	 * @return true if the string is a valid email address
	 */
	public static boolean isValidEmailAddress(String address)
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

	/**
	 * Extracts the email address within a <> after a specified offset.
	 */
	public static String extractEmailAddress(String args, int offset)
	{
		String address = args.substring(offset).trim();
		if (address.indexOf('<') == 0)
			address = address.substring(1, address.indexOf('>'));

		return address;
	}
}
