package com.kit.KittenVax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.kit.KittenVax.agents.Vet;
import com.kit.api.RestClient;


import akka.actor.typed.ActorSystem;

@SpringBootApplication
public class KittenVaxApplication {

	public static void main(String[] args) {
		SpringApplication.run(KittenVaxApplication.class, args);
		
		RestClient client = new RestClient();
		System.out.println(client.getRequest());
	}
	
	private void start() {
		ActorSystem<Vet.Command> mySystem = ActorSystem.create(Vet.create(), "mySystem");
	}

}
