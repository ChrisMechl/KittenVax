package com.kit.KittenVax.agents;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.kit.KittenVax.Kitten;
import com.kit.KittenVax.agents.Vet.Command;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class KittenGen extends AbstractBehavior<Vet.Command>{
	
	/* Message sent to Vet actor that contains -
	 * 
	 * batch: The ArrayList of n kittens (n from the Start message received)
	 * replyTo: The Vet ref (this message will be forwarded to the Vaxxer child and that child needs to send its response to Vet)
	 * attempts: The number of tries Vaxxer has made to vax the unvaxxed kittens 
	 */
	public static class KittenMessage implements Vet.Command{
		public final ArrayList<Kitten> batch;
		ActorRef<Command> replyTo;
		/* KittenMessage will always start with 0 attempts. This is changed by Vaxxer in case of failure to parse unvaxxed kittens */
		public int attempts = 0;
		public int count = 0;
		
		public KittenMessage(ArrayList<Kitten> batch, ActorRef<Command> replyTo) {
			this.batch = batch;
			this.replyTo = replyTo;
		}
		
		@Override 
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof KittenMessage)) return false;
			
			KittenMessage msg = (KittenMessage) o;
			return (msg.replyTo.toString().equals(this.replyTo.toString()) && msg.batch.size() == this.batch.size()) ? true : false;
		}
		
		@Override 
		public int hashCode() {
			return Objects.hash(replyTo.toString(), batch.size());
			
		}
	}

	public KittenGen(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(KittenGen::new);
	}
	
	/* Only receives the Start message which starts the generating of kittens for a single batch */
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(Vet.Start.class, this::onStart)
				.build();
	}
	
	//TODO handle generating nBatches here or in Vet?
	/* On Start receive, generate kittens and send reply with kitten batch */
	private Behavior<Command> onStart(Vet.Start msg) {
		for(int i = 0; i < msg.nTimes; i++) {
			/* Creates n kittens using genKittens() method and stores in curBatch */
			ArrayList<Kitten> curBatch = genKittens(msg.nKittens);
			/* Creates KittenMessage that will be forwarded to Vaxxer. The replyTo field is the Vet ref because that's who
			 * the Vaxxer will be sending the VaxxerMessage to */
			KittenMessage reply = new KittenMessage(curBatch, msg.replyTo);
			reply.count = i;
			msg.replyTo.tell(reply);
		}
		
		return this;
	}
	
	/* Creates n kittens, with a 20% chance of being vaxxed already,
	 * store them in an ArrayList of kittens, and return the ArrayList
	 */
	public static ArrayList<Kitten> genKittens(int n){
		ArrayList<Kitten> kittens = new ArrayList<Kitten>(n);
		Random r = new Random();
		
		/* Generate n kittens which have a 20% chance of being vaxxed */
		for(int i = 0; i < n; i++) {
			/* If the random value from 0-9 is greater than 6, the kitten should start as vaxxed */
			int random = r.nextInt(10);
			boolean vax = (random > 6) ? true : false;
			
			Kitten k = new Kitten(vax);
			kittens.add(k);
		}
		return kittens;
	}

}
