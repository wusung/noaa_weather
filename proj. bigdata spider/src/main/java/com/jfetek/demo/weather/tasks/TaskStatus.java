package com.jfetek.demo.weather.tasks;


public enum TaskStatus {
	YET("yet"),
	EXECUTING("executing"),
	DONE("done"),
	ERROR("error"),
	FETAL("fetal");
	
	public final String text;
	TaskStatus(String text) {
		this.text = text;
	}
	
	public boolean equals(String text) {
		return this.text.equals(text);
	}
	
	public static TaskStatus of(String text) {
		for (TaskStatus ts : TaskStatus.values()) {
			if (ts.text.equals(text)) return ts;
		}
		return null;
	}
	public static TaskStatus of(int ordinal) {
		TaskStatus[] values = TaskStatus.values();
		if (ordinal < 0 || ordinal >= values.length) return null;
		return values[ordinal];
	}
	
	@Override
	public String toString() {
		return this.text;
	}
}
