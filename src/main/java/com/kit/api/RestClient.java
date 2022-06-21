package com.kit.api;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestClient {
	private static final String URI = "http://localhost:3000";
	
	private Client client = ClientBuilder.newClient();
	
	WebTarget target = client.target(URI);
	
	
	public String getRequest() {
		Response response = target.path("books").queryParam("id", "1", "The Tale of Two Cities").request().get();
//		Response response = target.path("books").queryParam("id", "1", "The Tale of Two Cities").request().accept(MediaType.APPLICATION_JSON).get();
		System.out.println(response.getStatus());
		return response.readEntity(String.class);
	}
}
