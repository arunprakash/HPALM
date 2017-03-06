package org.hp.qc.web.restapi.docexamples.docexamples;

import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Assert;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Constants;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Response;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.RestConnector;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * This example shows multiple ways to create an entity, and how to remove
 * entities.
 *
 */
public class CreateDeleteExample {

	public static void main(String[] args) throws Exception {
		new CreateDeleteExample().createDeleteExample("http://" + Constants.HOST + ":" + Constants.PORT + "/qcbin",
				Constants.DOMAIN, Constants.PROJECT, Constants.USERNAME, Constants.PASSWORD);
	}

	public void createDeleteExample(final String serverUrl, final String domain, final String project, String username,
			String password) throws Exception {

		RestConnector con = RestConnector.getInstance().init(new HashMap<String, String>(), serverUrl, domain, project);

		// use the login example to learn how to authenticate properly.
		// we use that functionality here to login so that we can do more
		// complex actions.
		AuthenticateLoginLogoutExample login = new AuthenticateLoginLogoutExample();
		CreateDeleteExample example = new CreateDeleteExample();

		boolean loginResult = login.login(username, password);
		Assert.assertTrue("failed to login", loginResult);

		// After login set the session
		con.getQCSession();

		// Build the location of the requirements collection, and the
		// XML for an entity of type requirement
		String requirementsUrl = con.buildEntityCollectionUrl("requirement");

		// create the entity on the server-side, keep it's url
		String newlyCreatedEntityUrl = example.createEntity(requirementsUrl, Constants.entityToPostXml);

		String deletedEntityResponse = example.deleteEntity(newlyCreatedEntityUrl);
		Assert.assertTrue("deleted entity didn't contain posted field.",
				deletedEntityResponse.contains(Constants.entityToPostFieldXml));

		/*
		 * Now do the same only this time using an object, and not string xml.
		 * (Though we do build the object from the xml, we could have
		 * instantiated it differently, theoretically.) The reason we build it
		 * with post and not createEntity, is that when posting, the returned
		 * value is an xml representation
		 */
		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Content-Type", "application/xml");
		requestHeaders.put("Accept", "application/xml");
		Response postedEntityResponse = con.httpPost(requirementsUrl, Constants.entityToPostXml.getBytes(),
				requestHeaders);

		Assert.assertEquals("failed to post new entity.", postedEntityResponse.getStatusCode(),
				HttpURLConnection.HTTP_CREATED);

		String postedEntityResponseXml = postedEntityResponse.toString();
		Assert.assertTrue("element returned from server doesn't contain posted data",
				postedEntityResponseXml.contains(Constants.entityToPostFieldXml));

		// getting the location of the new entity from the post response...
		newlyCreatedEntityUrl = postedEntityResponse.getResponseHeaders().get("Location").iterator().next();

		deletedEntityResponse = example.deleteEntity(newlyCreatedEntityUrl);
		Assert.assertEquals("posted entity does not equal deleted entity response", deletedEntityResponse,
				postedEntityResponseXml);

		login.logout();
	}

	private RestConnector con;

	public CreateDeleteExample() {
		con = RestConnector.getInstance();
	}

	/**
	 * @param collectionUrl
	 *            the url of the collection of the relevant entity type.
	 * @param postedEntityXml
	 *            the xml describing an instance of said entity type.
	 * @return the url of the newly created entity.
	 */
	public String createEntity(String collectionUrl, String postedEntityXml) throws Exception {

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Content-Type", "application/xml");
		requestHeaders.put("Accept", "application/xml");

		// As can be seen in the implementation below, creating an entity
		// is simply posting its xml into the correct collection.
		Response response = con.httpPost(collectionUrl, postedEntityXml.getBytes(), requestHeaders);

		Exception failure = response.getFailure();
		if (failure != null) {
			throw failure;
		}

		/*
		 * Note that we also get the xml of the newly created entity. at the
		 * same time we get the url where it was created in a location response
		 * header.
		 */
		String entityUrl = response.getResponseHeaders().get("Location").iterator().next();

		return entityUrl;
	}

	/**
	 * @param entityUrl
	 *            the url of the entity that we wish to remove
	 * @return xml of deleted entity
	 */
	public String deleteEntity(String entityUrl) throws Exception {

		// As we can see from the implementation below, to delete an entity
		// is simply to use HTTP delete on its url.
		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept", "application/xml");

		Response serverResponse = con.httpDelete(entityUrl, requestHeaders);
		if (serverResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
			throw new Exception(serverResponse.toString());
		}

		return serverResponse.toString();
	}

}