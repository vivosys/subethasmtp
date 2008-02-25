package org.subethamail.smtp.server.io;

import java.io.File;
import java.io.IOException;

import javax.mail.util.SharedFileInputStream;

/**
 * This class uses a temporary file to store big messages and asks JVM
 * to delete them when destroyed.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SharedTmpFileInputStream 
	extends SharedFileInputStream 
{
	private File tempFile;
	
	public SharedTmpFileInputStream(File f) throws IOException 
	{
		super(f);
		this.tempFile = f;
	}

	public void close() throws IOException 
	{
		super.close();
		if (in == null)
			this.tempFile.deleteOnExit();
	}
}
