package com.kit.KittenVax.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.kit.KittenVax.Kitten;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
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
	
	private ActorRef<Command> kgen;
	private ArrayList<ActorRef<Command>> children;
	
	/* Default constructor */
	private Vet(ActorContext<Command> context) {
		super(context);
		children = new ArrayList<ActorRef<Command>>();
	}

	/* Public factory method for Vet */
	public static Behavior<Command> create(){
		return Behaviors.setup(Vet::new);
	}
	
	/* On Start receive from main method, create KittenGen and forward Start message */
	/* On KittenMessage (reply from KittenGen), send batch to be filtered on vax status and delegate to child to vax unvaxxed */
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(Start.class, this::start)
				.onMessage(KittenGen.KittenMessage.class, this::delegateBatch)
				.onMessage(Vaxxer.VaxxerMessage.class, this::sendVaxxed)
				.onMessage(Vaxxer.VaxFailed.class, this::vaxFailed)
				.build();
	}
	
	/* On Start receive from main method, create KittenGen and forward Start message */
	private Behavior<Command> start(Start msg){
		kgen = getContext().spawn(KittenGen.create(), "k-gen");
		kgen.tell(msg);
		return this;
		
	}
	
	/* On KittenMessage (reply from KittenGen), send batch to be filtered on vax status and delegate to child to vax unvaxxed */
	private Behavior<Command> delegateBatch(KittenGen.KittenMessage msg) {
		/* Uses filterVaxxed to get a List of only the vaxxed kittens and removes them from the message batch */
		List<Kitten> filtered = filterVaxxed(msg.batch);
		/* Creates a child to handle vaxxing the unvaxxed kittens */
		ActorRef<Command> child = getContext().spawn(Behaviors.supervise(Vaxxer.create()).onFailure(SupervisorStrategy.restart()), "child");
		/* Saves the child's ref */
		children.add(child);		
		/* Forwards the KittenMessage to the child */
		child.tell(msg);
		
		//TODO send self vaxxermessage with already filtered kittens
		return this;
	}
	
	/* Receives a batch of vaxxed kittens to send to the server */
	private Behavior<Command> sendVaxxed(Vaxxer.VaxxerMessage msg){
		//TODO handle sending to server
		return this;
	}
	
	/* In the case of three consecutive failures to vax a batch of kittens by Vaxxer, this message is sent from Vaxxer */
	/* Handling this is probably out of scope so we'll just print a message letting the user know what happened */
	private Behavior<Command> vaxFailed(Vaxxer.VaxFailed msg){
		System.out.println("Uh oh, vaccination of the following kittens failed - ");
		for(Kitten k : msg.msg.batch) {
			System.out.print(k + ", ");
		}
		return this;
	}
	
	/* Filters vaxxed kittens from the batch that was received from KittenMessage.
	 * Return the list of vaxxed kittens to be sent to the server and remove them 
	 * from the batch so it only contains kittens that still need vax so the batch
	 * can be sent to children in delegateBatch()
	 */
	public static List<Kitten> filterVaxxed(ArrayList<Kitten> batch){
		/* Get a list of the kittens that are already vaxxed */
		List<Kitten> filtered = batch
				.stream()
				.filter(k -> k.isVaxxed())
				.collect(Collectors.toList());
		/* Remove vaxxed kittens from batch so it only contains kittens needing vax */
		batch.removeIf(k -> k.isVaxxed());
		
		return filtered;
	}
	
	
}
