package com.kit.KittenVax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
	
	/* Tests that KittenGen responds with a KittenMessage when receiving a Start Message */
	@Test
	public void startMsgTest() {
		/* Spawn KittenGen */
		ActorRef<Vet.Command> kittenGen = testKit.spawn(KittenGen.create(), "k-gen");
		/* Send Start message to KittenGen */
		kittenGen.tell(new Vet.Start(nKittens, nTimes, probe.ref()));
		
		/* Create fake batch of kittens to test against response */
		ArrayList<Kitten> kList = KittenGen.genKittens(nKittens);
		/* If the response message has the same sized batch and replyTo, it is the expected message */
		probe.expectMessage(new KittenGen.KittenMessage(kList, kittenGen));
	}
	
	/* Checks that Vet.filterVaxxed returns a List of vaxxed kittens and modifies the input ArrayList to only contain non-vaxxed kittens*/
	@Test
	public void testKittenVaxFilter() {
		/* Create ArrayList of Kittens where two are vaxxed and three are not */
		ArrayList<Kitten> kList = new ArrayList<Kitten>(5);
		kList.add(new Kitten(true));
		kList.add(new Kitten(false));
		kList.add(new Kitten(false));
		kList.add(new Kitten(false));
		kList.add(new Kitten(true));
		
		List<Kitten> vaxxed = Vet.filterVaxxed(kList);
		/* Two kittens were vaxxed and put in the vaxxed List */
		assertEquals(2, vaxxed.size());
		/* Three kittens were not vaxxed and remained in the original kList */
		assertEquals(3, kList.size());
		
		/* Assert all members of vaxxed are vaxxed */
		for(int i = 0; i < 2; i++) {
			assertTrue(vaxxed.get(i).isVaxxed());
		}
		/* Assert all members of kList(unvaxxed) are not vaxxed */
		for(int i = 0; i < 3; i++) {
			assertTrue(!kList.get(i).isVaxxed());
		}
	}
	
	@AfterClass
	public static void cleanup() {
		testKit.shutdownTestKit();
	}
}
