package com.kit.KittenVax;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.kit.KittenVax.agents.Vet;
import com.kit.api.RestClient;


import akka.actor.typed.ActorSystem;

@SpringBootApplication
public class KittenVaxApplication {

	private static String dbPath = "/home/chris/workspace/KittenVax/db.json";
	
	public static void main(String[] args) {
		SpringApplication.run(KittenVaxApplication.class, args);
		
		manageServer(true);
		start();
	}
	
	/* Executes server at port 3000. Presumably you'll need to change the location of the script and change the location of the database within the script */
	public static void manageServer(boolean purge) {
		RestClient client = new RestClient();
		try {
			Runtime.getRuntime().exec("/home/chris/workspace/KittenVax/startServer.sh");
		} 
		catch (IOException e) {
			System.err.println("Failed to run server start script");
			return;
		}
		if(purge) client.purge(dbPath);
	}
	
	private static void start() {
		ActorSystem<Vet.Command> mySystem = ActorSystem.create(Vet.create(), "mySystem");
		mySystem.tell(new Vet.Start(5, 5, mySystem));

		try {
		      System.out.println(">>> Press ENTER to exit <<<");
		      System.in.read();
		    } catch (IOException ignored) {
		    } finally {
		      mySystem.terminate();
		    }
	}

}
