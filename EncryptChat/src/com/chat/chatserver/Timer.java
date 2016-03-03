package com.chat.chatserver;

/**
 * A timer is used to count time.
 *
 */
public class Timer {
	private long start;// the unit is millisecond
	private long length;// the unit is second

	public Timer(long start, long length) {
		super();
		this.start = start;
		this.length = length;
	}

	public long getStart() {
		return start;
	}

	public long getLength() {
		return length;
	}

	public synchronized boolean isOutDeadLine() {
		long now = System.currentTimeMillis();
		return (now - start) / 1000 > length;
	}
}