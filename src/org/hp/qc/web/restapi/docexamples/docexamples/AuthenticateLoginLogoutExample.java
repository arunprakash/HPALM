package org.hp.qc.web.restapi.docexamples.docexamples;

import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.*;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * This example shows how to login/logout/authenticate to the server with REST.
 * note that this is a  rather "thin" layer over {@link RestConnector} because
 * these operations are *almost* HTML standards.
 */
public class AuthenticateLoginLogoutExample {

	public static void main(String[] args) throws Exception {
		new AuthenticateLoginLogoutExample().authenticateLoginLogoutExample(
				"http://" + Constants.HOST + ":" +
						Constants.PORT + "/qcbin",
						Constants.DOMAIN,
						Constants.PROJECT,
						Constants.USERNAME,
						Constants.PASSWORD);
	}

	public void authenticateLoginLogoutExample(final String serverUrl,
			final String domain, final String project, String username,
			String password) throws Exception {

		RestConnector con =
				RestConnector.getInstance().init(
						new HashMap<String, String>(),
						serverUrl,
						domain,
						project);

		AuthenticateLoginLogoutExample example =
				new AuthenticateLoginLogoutExample();

		//if we're authenticated we'll get a null, otherwise a URL where we should login at (we're not logged in, so we'll get a URL).
		String authenticationPoint = example.isAuthenticated();
		Assert.assertTrue(
				"response from isAuthenticated means we're authenticated. that can't be.",
				authenticationPoint != null);

		//now we login to previously returned URL.
		boolean loginResponse =
				example.login(authenticationPoint, username, password);
		Assert.assertTrue("failed to login.", loginResponse);
		Assert.assertTrue(
				"login did not cause creation of Light Weight Single Sign On(LWSSO) cookie.",
				con.getCookieString().contains("LWSSO_COOKIE_KEY"));

		//proof that we are indeed logged in
		Assert.assertNull(
				"isAuthenticated returned not authenticated after login.",
				example.isAuthenticated());

		//and now we logout
		example.logout();

		// And now we can see that we are indeed logged out
		//because isAuthenticated once again returns a url, and not null.
		Assert.assertNotNull(
				"isAuthenticated returned authenticated after logout.",
				example.isAuthenticated());
	}

	private RestConnector con;

	public AuthenticateLoginLogoutExample() {
		con = RestConnector.getInstance();
	}

	/**
	 * @param username
	 * @param password
	 * @return true if authenticated at the end of this method.
	 * @throws Exception
	 *
	 * convenience method used by other examples to do their login
	 */
	public boolean login(String username, String password) throws Exception {

		String authenticationPoint = this.isAuthenticated();
		if (authenticationPoint != null) {
			return this.login(authenticationPoint, username, password);
		}
		return true;
	}

	/**
	 * @param loginUrl
	 *            to authenticate at
	 * @param username
	 * @param password
	 * @return true on operation success, false otherwise
	 * @throws Exception
	 *
	 * Logging in to our system is standard http login (basic authentication),
	 * where one must store the returned cookies for further use.
	 */
	public boolean login(String loginUrl, String username, String password)
			throws Exception {

		//create a string that lookes like:
		// "Basic ((username:password)<as bytes>)<64encoded>"
		byte[] credBytes = (username + ":" + password).getBytes();
		String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

		Map<String, String> map = new HashMap<String, String>();
		map.put("Authorization", credEncodedString);

		Response response = con.httpGet(loginUrl, null, map);

		boolean ret = response.getStatusCode() == HttpURLConnection.HTTP_OK;

		return ret;
	}

	/**
	 * @return true if logout successful
	 * @throws Exception
	 *             close session on server and clean session cookies on client
	 */
	public boolean logout() throws Exception {

		//note the get operation logs us out by setting authentication cookies to:
		// LWSSO_COOKIE_KEY="" via server response header Set-Cookie
		Response response =
				con.httpGet(con.buildUrl("authentication-point/logout"),
						null, null);

		return (response.getStatusCode() == HttpURLConnection.HTTP_OK);

	}

	/**
	 * @return null if authenticated.<br>
	 *         a url to authenticate against if not authenticated.
	 * @throws Exception
	 */
	public String isAuthenticated() throws Exception {

		String isAuthenticateUrl = con.buildUrl("rest/is-authenticated");
		String ret;

		Response response = con.httpGet(isAuthenticateUrl, null, null);
		int responseCode = response.getStatusCode();

		//if already authenticated
		if (responseCode == HttpURLConnection.HTTP_OK) {

			ret = null;
		}

		//if not authenticated - get the address where to authenticate
		// via WWW-Authenticate
		else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {

			Iterable<String> authenticationHeader =
					response.getResponseHeaders().get("WWW-Authenticate");

			String newUrl =
					authenticationHeader.iterator().next().split("=")[1];
			newUrl = newUrl.replace("\"", "");
			newUrl += "/authenticate";
			ret = newUrl;
		}

		//Not ok, not unauthorized. An error, such as 404, or 500
		else {

			throw response.getFailure();
		}

		return ret;
	}

}
