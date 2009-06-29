package org.subethamail.smtp.command;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.io.CharTerminatedInputStream;
import org.subethamail.smtp.io.DotUnstuffingInputStream;
import org.subethamail.smtp.io.ReceivedHeaderStream;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class DataCommand extends BaseCommand
{
    private final static char[] SMTP_TERMINATOR = { '\r', '\n', '.', '\r', '\n' };
    private final static int BUFFER_SIZE = 1024 * 32;	// 32k seems reasonable

    /** */
    public DataCommand()
	{
		super("DATA",
				"Following text is collected as the message.\n"
				+ "End data with <CR><LF>.<CR><LF>");
	}

    /** */
	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		if (!sess.getHasMailFrom())
		{
			sess.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (sess.getRecipientCount() == 0)
		{
			sess.sendResponse("503 Error: need RCPT command");
			return;
		}

		sess.sendResponse("354 End data with <CR><LF>.<CR><LF>");

		InputStream stream = sess.getRawInput();
		stream = new BufferedInputStream(stream, BUFFER_SIZE);
		stream = new CharTerminatedInputStream(stream, SMTP_TERMINATOR);
		stream = new DotUnstuffingInputStream(stream);
		stream = new ReceivedHeaderStream(stream, sess.getHelo(), sess.getRemoteAddress().getAddress(), sess.getServer().getHostName());

		try
		{
			sess.getMessageHandler().data(stream);

			// Just in case the handler didn't consume all the data, we might as well
			// suck it up so it doesn't pollute further exchanges.  This code used to
			// throw an exception, but this seems an arbitrary part of the contract that
			// we might as well relax.
			while (stream.available() > 0)
				stream.read();

			sess.sendResponse("250 Ok");
		}
		catch (RejectException ex)
		{
			sess.sendResponse(ex.getMessage());
		}

		sess.resetMessageState(); // reset session, but don't require new HELO/EHLO
	}
}
