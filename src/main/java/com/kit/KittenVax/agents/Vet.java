package com.kit.KittenVax.agents;

import java.util.Objects;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Vet extends AbstractBehavior<Vet.Command>{
	
	/* Top level message for Vet and KittenGen */
	public interface Command {}
	
	
	/* Start command to be sent from vet to kittenGen
	 * 
	 * nKittens: Number of kittens to generate per batch
	 * nTimes: Number of batches to generate nKittens
	 */
	public static class Start implements Command{
		public final int nKittens;
		public final int nTimes;
		public final ActorRef<Command> replyTo;
		
		public Start(int nKittens, int nTimes, ActorRef<Command> replyTo) {
			this.nKittens = nKittens;
			this.nTimes = nTimes;
			this.replyTo = replyTo;
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof Start)) return false;
			
			Start msg = (Start) o;
			return (msg.nKittens == this.nKittens && msg.nTimes == this.nTimes && msg.replyTo == this.replyTo) ? true : false;
		}
		
		@Override 
		public int hashCode() {
			return Objects.hash(this.nKittens, this.nTimes, this.replyTo);
		}
	}
	
	
	/* Default constructor */
	private Vet(ActorContext<Command> context) {
		super(context);
	}

	public static Behavior<Command> create(){
		return Behaviors.setup(Vet::new);
	}
	
	@Override
	public Receive<Command> createReceive() {
		return null;
	}
}
