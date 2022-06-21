package com.kit.KittenVax;

public class Kitten {
	private String name;
	private boolean vaxxed = false;
	
	public Kitten() {}
	
	public Kitten(boolean vaxxed) {
		this.vaxxed = vaxxed;
	}
	
	public Kitten(String name) {
		this.name = name;
	}
	
	public Kitten(String name, boolean vaxxed) {
		this.name = name;
		this.vaxxed = vaxxed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isVaxxed() {
		return vaxxed;
	}

	public void setVaxxed(boolean vaxxed) {
		this.vaxxed = vaxxed;
	}

	@Override
	public String toString() {
		return "The kitten's name is " + name + " and they " + ((this.vaxxed) ? "are" : "aren't") + " vaxxed";
	}
	
	
	
}
