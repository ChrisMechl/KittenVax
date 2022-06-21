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
	
	public static class KittenMessage implements Vet.Command{
		public final ArrayList<Kitten> batch;
		ActorRef<Command> replyTo;
		
		public KittenMessage(ArrayList<Kitten> batch, ActorRef<Command> replyTo) {
			this.batch = batch;
			this.replyTo = replyTo;
		}
		
		@Override 
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof KittenMessage)) return false;
			
			KittenMessage msg = (KittenMessage) o;
			return (!msg.replyTo.equals(this.replyTo) && msg.batch.size() == this.batch.size()) ? true : false;
		}
		
		@Override 
		public int hashCode() {
			System.out.println(replyTo.toString());
			return Objects.hash(replyTo.toString(), batch.size());
			
		}
	}
	
	private int nKittens;
	private int nTimes;

	public KittenGen(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(KittenGen::new);
	}
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(Vet.Start.class, this::onStart)
				.build();
	}
	
	/* On Start receive, generate kittens and send reply with kitten batch */
	private Behavior<Command> onStart(Vet.Start msg) {
		this.nKittens = msg.nKittens;
		this.nTimes = msg.nTimes;
		
		ArrayList<Kitten> curBatch = genKittens(msg.nKittens);
		KittenMessage reply = new KittenMessage(curBatch, msg.replyTo);
		sendReply(reply);
		
		return this;
	}
	
	/* TODO ??? Should make part of other function ??? */
	/* Sends reply to Vet */
	private void sendReply(KittenMessage msg) {
		msg.replyTo.tell(msg);
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
