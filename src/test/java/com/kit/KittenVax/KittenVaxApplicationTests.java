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
		ActorRef<Vet.Command> kittenGen = testKit.spawn(KittenGen.create(), "k-gen");
		kittenGen.tell(new Vet.Start(nKittens, nTimes, probe.ref()));
		
		ArrayList<Kitten> a = KittenGen.genKittens(nKittens);
		
		probe.expectMessage(new KittenGen.KittenMessage(a, kittenGen));
		
	}
	
	@Test
	public void test() {
		
	}
	
	@AfterClass
	public static void cleanup() {
		testKit.shutdownTestKit();
	}
}
