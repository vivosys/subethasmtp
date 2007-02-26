package org.subethamail.smtp.test.command;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.MessageListenerAdapter;
import org.subethamail.smtp.test.ServerTestCase;
import org.subethamail.smtp.util.Base64;

/**
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public class AuthLoginTest extends ServerTestCase
{

	public AuthLoginTest(String name)
	{
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.subethamail.smtp.test.ServerTestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		((MessageListenerAdapter) wiser.getServer().getMessageHandlerFactory())
				.setAuthenticationHandlerFactory(new AuthHandlerFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.subethamail.smtp.test.ServerTestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for AUTH LOGIN. 
	 * The sequence under test is as follows:
	 * <ol>
	 * <li>HELO test</li>
	 * <li>User starts AUTH LOGIN</li>
	 * <li>User sends username</li>
	 * <li>User cancels authentication by sending "*"</li>
	 * <li>User restarts AUTH LOGIN</li>
	 * <li>User sends username</li>
	 * <li>User sends password</li>
	 * <li>We expect login to be successful. Also the Base64 transformations are tested.</li>
	 * <li>User issues another AUTH command</li>
	 * <li>We expect an error message</li>
	 * </ol>
	 * {@link org.subethamail.smtp.command.AuthCommand#execute(java.lang.String, org.subethamail.smtp.server.ConnectionContext)}.
	 */
	public void testExecute() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("AUTH LOGIN");
		expect("334");

		String enc_username = Base64.encodeToString(
				AuthHandlerFactory.REQUIRED_USERNAME.getBytes(), false);

		send(enc_username);
		expect("334");

		send("*");
		expect("501");

		send("AUTH LOGIN");
		expect("334");

		send(enc_username);
		expect("334");

		String enc_pwd = Base64.encodeToString(
				AuthHandlerFactory.REQUIRED_PASSWORD.getBytes(), false);
		send(enc_pwd);
		expect("235");

		send("AUTH");
		expect("503");
	}

	public class AuthHandlerFactory implements AuthenticationHandlerFactory
	{
		static final String REQUIRED_USERNAME = "myUserName";

		static final String REQUIRED_PASSWORD = "mySecret01";

		public AuthenticationHandler create()
		{
			PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
			UsernamePasswordValidator validator = new UsernamePasswordValidator()
			{
				public void login(String username, String password)
						throws LoginFailedException
				{
					if (!username.equals(REQUIRED_USERNAME)
							|| !password.equals(REQUIRED_PASSWORD))
					{
						throw new LoginFailedException();
					}
				}
			};
			ret.addPlugin(new PlainAuthenticationHandler(validator));
			ret.addPlugin(new LoginAuthenticationHandler(validator));
			return ret;
		}
	}

}
