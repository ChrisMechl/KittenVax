package com.kit.KittenVax.agents;

import java.util.ArrayList;
import java.util.Objects;

import com.kit.KittenVax.Kitten;
import com.kit.KittenVax.agents.KittenGen.KittenMessage;
import com.kit.KittenVax.agents.Vet.Command;

import akka.actor.typed.Behavior;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Vaxxer extends AbstractBehavior<Command>{
	
	/* Message to send to Vet once kittens have been vaxxed */
	public static class VaxxerMessage implements Command{
		ArrayList<Kitten> batch;
		
		public VaxxerMessage(ArrayList<Kitten> batch) {
			this.batch = batch;
		}
		
		@Override 
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof VaxxerMessage)) return false;
			
			VaxxerMessage msg = (VaxxerMessage) o;
			return (this.batch.size() == msg.batch.size()) ? true : false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.batch.size());
		}
	}
	
	public static class VaxFailed implements Command{
		KittenMessage msg;
		
		public VaxFailed(KittenMessage msg) {
			this.msg = msg;
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof VaxFailed)) return false;
			
			VaxFailed msg = (VaxFailed) o;
			return (msg.msg.batch.size() == this.msg.batch.size() && this.msg.replyTo.toString().equals(msg.msg.replyTo.toString()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.msg.batch.size());
		}
	}
	
	/* The current message to be resent to self in case of exception */
	private KittenGen.KittenMessage msg;
	
	public static Behavior<Command> create(){
		return Behaviors.setup(Vaxxer::new);
	}
	
	private Vaxxer(ActorContext<Command> context) {
		super(context);
	}
	
	/* Sends KittenMessage to vax() method and handles restart signal in case of exception during excecution */
	@Override
	public Receive<Command> createReceive(){
		return newReceiveBuilder()
				.onMessage(KittenGen.KittenMessage.class, this::vax)
				.onSignal(PreRestart.class, signal -> preRestart())
				.build();
	}
	
	/* Saves the message in case of exception to allow for resending, and vaxxes the kittens */
	public Behavior<Command> vax(KittenGen.KittenMessage msg){
		this.msg = msg;		
		
		/* Used to test what Vaxxer will do if it encounters an exception */
		if(msg.fails > msg.attempts) {
			throw new RuntimeException("Failing for the " + this.msg.attempts + " time");
		}
		
		/* Go through batch and vax each kitten */
		for(Kitten k : msg.batch) {
			k.setVaxxed(true);
		}
		/* Sends the newly vaxxed kitten batch back to the Vet to be sent to the server */
		msg.replyTo.tell(new VaxxerMessage(msg.batch));
		return this;
	}
	
	/* Signal routing in case of exception. Will allow for three attempts before ditching message and letting vet know of failed batch vax.
	 * Resends the message to self to try again once the child is restarted */
	private Behavior<Command> preRestart(){
		if(msg.attempts < 3) {
			msg.attempts++;
			getContext().getSelf().tell(msg);
		}
		else {
			msg.replyTo.tell(new VaxFailed(msg));
		}
		return this;		
	}	
}
