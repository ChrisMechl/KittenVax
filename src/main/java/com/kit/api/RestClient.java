package com.kit.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.kit.KittenVax.Kitten;

public class RestClient {
	private static final String URI = "http://localhost:3000/kittens";
	
	private static Gson gson = new Gson();	
	
	/* Resets the json file at the path location. It creates a single array with a "kittens" tag */
	public boolean purge(String path) {
		BufferedWriter db;
		try {
			db = new BufferedWriter(new FileWriter(path));
		}
		catch(IOException e) {
			System.err.println("Failed to open file at " + path);
			System.err.println("Make sure server isn't running");
			return false;
		}
		
		String defaultDB = "{\"kittens\":[]}";
		
		try {
			db.write(defaultDB);
		} catch (IOException e) {
			System.err.println("Failed to write to file at " + path);
			System.err.println("Make sure server isn't running");
			return false;
		}
		finally {
			try {
				db.close();
			} catch (IOException e) {
				System.err.println("Failed to close writer");
			}
		}
		
		System.out.println("Purged database");
		return true;
	}
	
	/* Attempts to post the given kitten to the server */
	public int postRequest(Kitten k) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(URI);
		StringEntity toPersist;
		
		try {	
			toPersist = new StringEntity(gson.toJson(k));
		} catch (UnsupportedEncodingException e) {
			System.err.println("Failed creating Json representation of Kitten: " + k);
			return -1;			
		}
		
		post.setEntity(toPersist);
		post.setHeader("Content-type", "application/json");
		
		HttpResponse resp;
		try {
			resp = client.execute(post);
		}
		catch (ClientProtocolException e) {
			System.err.println("Error in HTTP protocol for POST with Kitten: " + k);
			return -1;
		}
		catch (IOException e) {
			System.err.println("Failure to POST due to IO Exception");
			return -1;
		}
		
		return resp.getStatusLine().getStatusCode();		
	}
	
	/* Prints kittens by name */
	public String getByName(Kitten k) {
		HttpClient client = HttpClientBuilder.create().build();
		URIBuilder builder;
		HttpResponse resp;
		HttpGet get;
		InputStream in;
		
		try {
			builder = new URIBuilder(URI);
			get = new HttpGet(builder.build());
			builder.setParameter("name", k.getName());
		} 
		catch (URISyntaxException e1) {
			System.err.println("Failed to create GET URI due to bad syntax");
			return null;
		}
		
		try {
			resp = client.execute(get);
			in = resp.getEntity().getContent();
		} 
		catch (ClientProtocolException e) {
			System.err.println("Error in HTTP protocol for GET with Kitten " + k);
			return null;
		} 
		catch (IOException e) {
			System.err.println("Failure to GET due to IO Exception");
			return null;
		}
		
		String text = new BufferedReader(
			      new InputStreamReader(in, StandardCharsets.UTF_8))
			        .lines()
			        .filter(c -> !c.equals("[") && !c.equals("]"))
			        .collect(Collectors.joining("\n"));
		
		return text;
	}
	
	/* Gets all Kittens and prints them to console */
	public String getAll() {
		HttpClient client = HttpClientBuilder.create().build();
		URIBuilder builder;
		HttpResponse resp;
		HttpGet get;
		InputStream in;
		
		try {
			builder = new URIBuilder(URI);
			get = new HttpGet(builder.build());
		} 
		catch (URISyntaxException e1) {
			System.err.println("Failed to create GET URI due to bad syntax");
			return null;
		}
		
		try {
			resp = client.execute(get);
			in = resp.getEntity().getContent();
		} 
		catch (ClientProtocolException e) {
			System.err.println("Error in HTTP protocol for GET all Kittens");
			return null;
		} 
		catch (IOException e) {
			System.err.println("Failure to GET due to IO Exception");
			return null;
		}
		
		String text = new BufferedReader(
			      new InputStreamReader(in, StandardCharsets.UTF_8))
			        .lines()
			        .filter(c -> !c.equals("[") && !c.equals("]"))
			        .collect(Collectors.joining("\n"));
		
		return text;
	}
}
