package org.subethamail.smtp.command;

import org.subethamail.smtp.util.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class DataTest extends ServerTestCase
{
	/** */
	public DataTest(String name)
	{
		super(name);
	}

	/** */
	public void testNeedMail() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("DATA");
		this.expect("503 Error: need MAIL command");
	}

	/** */
	public void testNeedRcpt() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: success@subethamail.org");
		this.expect("250");

		this.send("DATA");
		this.expect("503 Error: need RCPT command");
	}

	/** */
	public void testData() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: success@subethamail.org");
		this.expect("250");

		this.send("RCPT TO: success@subethamail.org");
		this.expect("250");

		this.send("DATA");
		this.expect("354 End data with <CR><LF>.<CR><LF>");
	}

	/** */
	public void testRsetAfterData() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: success@subethamail.org");
		this.expect("250");

		this.send("RCPT TO: success@subethamail.org");
		this.expect("250");

		this.send("DATA");
		this.expect("354 End data with <CR><LF>.<CR><LF>");

		this.send("alsdkfj \r\n.");

		this.send("RSET");
		this.expect("250 Ok");

		this.send("HELO foo.com");
		this.expect("250");
	}
}
