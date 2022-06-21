package com.kit.KittenVax;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.kit.KittenVax.agents.KittenGen;
import com.kit.KittenVax.agents.Vet;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;

@SpringBootTest
class KittenVaxApplicationTests {

	static final ActorTestKit testKit = ActorTestKit.create();
	TestProbe<Vet.Command> probe = testKit.createTestProbe();
	
	int nKittens = 5;
	int nTimes = 1;
	
	
	@Test
	public void startMsgTest() {
		/* Spawn KittenGen */
		ActorRef<Vet.Command> kittenGen = testKit.spawn(KittenGen.create(), "k-gen");
		/* Send Start message to KittenGen */
		kittenGen.tell(new Vet.Start(nKittens, nTimes, probe.ref()));
		
		/* Create fake batch of kittens to test against response */
		ArrayList<Kitten> a = KittenGen.genKittens(nKittens);
		/* If the response message has the same sized batch and replyTo, it is the expected message */
		probe.expectMessage(new KittenGen.KittenMessage(a, kittenGen));
	}
	
	
	
	@AfterClass
	public static void cleanup() {
		testKit.shutdownTestKit();
	}
}
