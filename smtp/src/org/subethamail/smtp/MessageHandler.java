/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp;

import java.io.IOException;
import java.io.InputStream;

/**
 * The interface that defines the conversational exchange of a single message
 * on an SMTP connection.
 *
 * @author Jeff Schnitzer
 */
public interface MessageHandler extends AuthenticationHandler
{
	/**
	 * Called first, after the MAIL FROM during a SMTP exchange.
	 *
	 * @param from is the sender as specified by the client.  It will
	 *  be a rfc822-compliant email address, already validated by
	 *  the server.
	 * @throws RejectException if the sender should be denied.
	 */
	public void from(String from) throws RejectException;
	
	/**
	 * Called once for every RCPT TO during a SMTP exchange.
	 * This will occur after a from() call.
	 *
	 * @param recipient is a rfc822-compliant email address,
	 *  validated by the server.
	 * @throws RejectException if the recipient should be denied.
	 */
	public void recipient(String recipient) throws RejectException;
	
	/**
	 * Called when the DATA part of the SMTP exchange begins.  Will
	 * only be called if at least one recipient was accepted.
	 *
	 * @param data will be the smtp data stream, stripped of any extra '.' chars
	 *
	 * @throws RejectException if at any point the data should be rejected.
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public void data(InputStream data) throws RejectException, TooMuchDataException, IOException;
	
	/**
	 * This method is called whenever a RSET command is sent. It should
	 * be used to clean up any pending deliveries.
	 */
	public void resetMessageState();	
}
