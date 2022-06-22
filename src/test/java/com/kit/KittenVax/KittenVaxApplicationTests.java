package com.kit.KittenVax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.kit.KittenVax.agents.KittenGen;
import com.kit.KittenVax.agents.Vaxxer;
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
	public void testStartMsg() {
		/* Spawn KittenGen */
		ActorRef<Vet.Command> kittenGen = testKit.spawn(KittenGen.create(), "k-gen");
		/* Send Start message to KittenGen */
		kittenGen.tell(new Vet.Start(nKittens, nTimes, probe.ref()));
		
		/* Create fake batch of kittens to test against response */
		ArrayList<Kitten> kList = KittenGen.genKittens(nKittens);
		/* If the response message has the same sized batch and replyTo, it is the expected message */
		probe.expectMessage(new KittenGen.KittenMessage(kList, probe.ref()));
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
		
		ArrayList<Kitten> vaxxed = (ArrayList<Kitten>) Vet.filterVaxxed(kList);
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
	
	/* Tests that sending a batch of unvaxxed kittens to Vaxxer will return a batch of vaxxed kittens to Vet */
	@Test
	public void testVaxxerChild() {
		/* Create some unvaxxed kittens */
		ArrayList<Kitten> unvaxxed = new ArrayList<Kitten>(4);
		unvaxxed.add(new Kitten(false));
		unvaxxed.add(new Kitten(false));
		unvaxxed.add(new Kitten(false));
		unvaxxed.add(new Kitten(false));
		
		/* And some vaxxed kittens of the same size batch */
		ArrayList<Kitten> vaxxed = new ArrayList<Kitten>(4);
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		
		ActorRef<Vet.Command> vaxr = testKit.spawn(Vaxxer.create(), "vaxr");
		vaxr.tell(new KittenGen.KittenMessage(unvaxxed, probe.getRef()));
		
		probe.expectMessage(new Vaxxer.VaxxerMessage(vaxxed));
	}
	
	/* Checks that Vet can successfully send itself a Vaxxer.VaxxerMessage which contains the already vaxxed kittens received from KittenGen */
	@Test
	public void testSelfSendVaxxedKittens() {
		/* Create a few vaxxed kittens */
		ArrayList<Kitten> vaxxed = new ArrayList<Kitten>(3);
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		
		/* Because the Vet filter function removes elements from the original list (vaxxed) we need to use
		 * a clone of the vaxxed list to check against message equality. We could also just make another list
		 * with three true vaxxed Kittens but this is faster
		 */
		ArrayList<Kitten> compare = (ArrayList<Kitten>) vaxxed.clone();
		
		/* Send Vet the KittenMessage */
		ActorRef<Vet.Command> vet = testKit.spawn(Vet.create());
		vet.tell(new KittenGen.KittenMessage(vaxxed, probe.getRef()));
		/* If the probe received a VaxxerMessage with a batch of size 3, the expected message is correct */
		probe.expectMessage(new Vaxxer.VaxxerMessage(compare));
	}
	
	/* Tests that an incoming KittenMessage to Vet with some kittens already vaxxed will result in Vet sending
	 * itself a VaxxerMessage containing only the already vaxxed kittens. The unvaxxed kittens will be forwarded
	 * to Vaxxer which will then send Vet another VaxxerMessage containing the same number of vaxxed kittens as
	 * it received unvaxxed. */
	@Test
	public void testVetForwardKittenMessage() {
		
		/* Create some unvaxxed kittens */
		ArrayList<Kitten> unvaxxed = new ArrayList<Kitten>(4);
		unvaxxed.add(new Kitten(false));
		unvaxxed.add(new Kitten(false));
		unvaxxed.add(new Kitten(false));
		unvaxxed.add(new Kitten(true));
		
		/* And some vaxxed kittens of different size batch */
		ArrayList<Kitten> vaxxed = new ArrayList<Kitten>(4);
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		vaxxed.add(new Kitten(true));
		
		ArrayList<Kitten> single = new ArrayList<Kitten>(1);
		single.add(new Kitten(true));
		
		/* Send a KittenMessage (response from KittenGen) to vet */
		ActorRef<Vet.Command> vet = testKit.spawn(Vet.create()); 
		/* Vet forwards it to Vaxxer with the replyTo field as the probe */
		vet.tell(new KittenGen.KittenMessage(unvaxxed, probe.getRef()));
		
		/* The probe will get two responses
		 * The first being a self send that contains a batch of kittens that are already vaxxed (1 kitten)
		 * The second will be from Vaxxer and contain 3 newly vaxxed kittens
		 */
		probe.expectMessage(new Vaxxer.VaxxerMessage(single));
		probe.expectMessage(new Vaxxer.VaxxerMessage(vaxxed));	
	}
	
	@AfterClass
	public static void cleanup() {
		testKit.shutdownTestKit();
	}
}
