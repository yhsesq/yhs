/******************************************************************************
 *
 * Copyright (c) 2000 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: josh $
 * $Date: 2001/02/03 00:47:01 $
 * $Name:  $
 *****************************************************************************/
package mindbright.util;

public final class Queue {

    final static int QUEUE_DEPTH   = 512;
    final static int QUEUE_HIWATER = 384;

    Object[]   queue;
    boolean    isWaitGet;
    boolean    isWaitPut;
    boolean    isBlocking;
    int        rOffset;
    int        wOffset;
    int        maxQueueDepth;

    // Copies used for saving real values when disabling queue
    //
    int        rOffsetCP;
    int        wOffsetCP;
    int        maxQueueDepthCP;

    public Queue() {
	this.queue      = new Object[QUEUE_DEPTH + 1];
	this.isWaitGet  = false;
	this.isWaitPut  = false;
	this.isBlocking = true;
	this.rOffset    = 0;
	this.wOffset    = 0;
	this.maxQueueDepth = QUEUE_DEPTH;
    }

    public synchronized void setMaxDepth(int maxDepth) {
	maxQueueDepth = maxDepth;
    }

    public synchronized void putLast(Object obj) {
	putFlowControl();
	queue[wOffset++] = obj;
	if(wOffset == (QUEUE_DEPTH + 1))
	    wOffset = 0;
	if(isWaitGet)
	    this.notify();
    }

    public synchronized void putFirst(Object obj) {
	putFlowControl();
	rOffset--;
	if(rOffset == -1)
	    rOffset = QUEUE_DEPTH;
	queue[rOffset] = obj;
	if(isWaitGet)
	    this.notify();
    }

    public synchronized void release() {
	if(isWaitGet)
	    this.notify();
    }

    public synchronized void disable() {
	rOffsetCP       = rOffset;
	wOffsetCP       = wOffset;
	maxQueueDepthCP = maxQueueDepth;
	rOffset         = 0;
	wOffset         = 0;
	maxQueueDepth   = 0;
    }

    public synchronized void enable() {
	rOffset       = rOffsetCP;
	wOffset       = wOffsetCP;
	maxQueueDepth = maxQueueDepthCP;
	if(!isEmpty()) {
	    this.release();
	}
	if(isWaitPut && (freeSpace() > (QUEUE_DEPTH - QUEUE_HIWATER))) {
	    this.notifyAll();
	    isWaitPut = false;
	}
    }

    public synchronized void setBlocking(boolean block) {
	isBlocking = block;
	release();
    }

    public synchronized boolean isEmpty() {
	return (rOffset == wOffset);
    }

    private final void putFlowControl() {
	int fs = freeSpace();
	if(fs == (QUEUE_DEPTH - maxQueueDepth)) {
	    isWaitPut = true;
	}
	if(isWaitPut) {
	    try {
		this.wait();
	    } catch (InterruptedException e) {
		// !!!
	    }
	}
    }

    private final int freeSpace() {
	int fSpc = rOffset - wOffset;
	if(fSpc <= 0)
	    fSpc += (QUEUE_DEPTH + 1);
	fSpc--;
	return fSpc;
    }

    public synchronized Object getFirst() {
	Object obj = null;
	if(isEmpty()) {
	    if(!isBlocking) {
		return null;
	    }
	    isWaitGet = true;
	    try {
		this.wait();
	    } catch (InterruptedException e) {
		// !!!
	    }
	}
	isWaitGet = false;
	obj = queue[rOffset];
	queue[rOffset++] = null;
	if(rOffset == (QUEUE_DEPTH + 1))
	    rOffset = 0;
	if(isWaitPut && (freeSpace() > (QUEUE_DEPTH - QUEUE_HIWATER))) {
	    this.notifyAll();
	    isWaitPut = false;
	}
	return obj;
    }
}
