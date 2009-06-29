package org.subethamail.smtp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.ExtraDotOutputStream;


/**
 * A very low level abstraction of the STMP stream which knows how to handle
 * the raw protocol for lines, whitespace, etc.
 *
 * @author Jeff Schnitzer
 */
public class SMTPClient
{
	/** */
	private static Logger log = LoggerFactory.getLogger(SMTPClient.class);

	/** Just for display purposes */
	String hostPort;

	/** The raw socket */
	Socket socket;

	/** */
	BufferedReader reader;

	/** Output streams used for data */
	OutputStream rawOutput;
	ExtraDotOutputStream dataOutput;

	/** Note we bypass this during DATA */
	PrintWriter writer;

	/**
	 * Result of an SMTP exchange.
	 */
	public static class Response
	{
		int code;
		String message;

		public Response(int code, String text)
		{
			this.code = code;
			this.message = text;
		}

		public int getCode() { return this.code; }
		public String getMessage() { return this.message; }

		public boolean isSuccess()
		{
			return this.code >= 100 && this.code < 400;
		}

		@Override
		public String toString() { return this.code + " " + this.message; }
	}

	/**
	 * Establishes a connection to host and port and negotiated the inital EHLO
	 * exchange.
	 *
	 * @throws UnknownHostException if the hostname cannot be resolved
	 * @throws IOException if there is a problem connecting to the port
	 */
	public SMTPClient(String host, int port) throws UnknownHostException, IOException
	{
		this.hostPort = host + ":" + port;

		this.socket = new Socket(host, port);
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

		this.rawOutput = this.socket.getOutputStream();
		this.dataOutput = new ExtraDotOutputStream(this.rawOutput);
		this.writer = new PrintWriter(this.rawOutput, true);
	}

	/**
	 * @return a nice pretty description of who we are connected to
	 */
	public String getHostPort()
	{
		return this.hostPort;
	}

    /**
	 * Sends a message to the server, ie "HELO foo.example.com". A newline will
	 * be appended to the message.
	 *
	 * @param msg should not have any newlines
	 */
	protected void send(String msg) throws IOException
	{
		// Force \r\n since println() behaves differently on different platforms
		this.writer.print(msg + "\r\n");
		this.writer.flush();
	}

	/**
	 * Note that the response text comes back without trailing newlines.
	 */
	protected Response receive() throws IOException
	{
		StringBuilder builder = new StringBuilder();
		String line = null;

		boolean done = false;
		while (!done)
		{
			line = this.reader.readLine();

			builder.append(line.substring(4));

			if (line.charAt(3) == '-')
				builder.append('\n');
			else
				done = true;
		}

		String code = line.substring(0, 3);

		return new Response(Integer.parseInt(code), builder.toString());
	}

    /**
	 * Sends a message to the server, ie "HELO foo.example.com". A newline will
	 * be appended to the message.
	 *
	 * @param msg should not have any newlines
	 * @return the response from the server
	 */
	public Response sendReceive(String msg) throws IOException
	{
		this.send(msg);
		return this.receive();
	}

	/** If response is not success, throw an exception */
	public void receiveAndCheck() throws IOException, SMTPException
	{
		Response resp = this.receive();
		if (!resp.isSuccess())
			throw new SMTPException(resp);
	}

	/** If response is not success, throw an exception */
	public void sendAndCheck(String msg) throws IOException, SMTPException
	{
		this.send(msg);
		this.receiveAndCheck();
	}

	/** Logs but otherwise ignores errors */
	public void close()
	{
		if (!this.socket.isClosed())
		{
			try
			{
				this.socket.close();
			}
			catch (IOException ex)
			{
				log.error("Problem closing connection to " + this.hostPort, ex);
			}
		}
	}

	/** */
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " { " + this.hostPort + "}";
	}
}
