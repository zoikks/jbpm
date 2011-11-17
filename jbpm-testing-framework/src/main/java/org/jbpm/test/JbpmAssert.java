package org.jbpm.test;

import static org.junit.Assert.*;

import org.drools.runtime.process.ProcessInstance;

public class JbpmAssert {

	protected JbpmAssert() {
		
	}
	
	public static void assertProcessInstanceComplete(ProcessInstance pi) {
		assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState());
	}
	
	public static void assertProcessInstanceActive(ProcessInstance pi) {
		assertEquals(ProcessInstance.STATE_ACTIVE, pi.getState());
	}
	
	public static void assertProcessInstanceAborted(ProcessInstance pi) {
		assertEquals(ProcessInstance.STATE_ABORTED, pi.getState());
	}
	
	public static void assertProcessInstancePending(ProcessInstance pi) {
		assertEquals(ProcessInstance.STATE_PENDING, pi.getState());
	}
	
	public static void assertProcessInstanceSuspended(ProcessInstance pi) {
		assertEquals(ProcessInstance.STATE_SUSPENDED, pi.getState());
	}
}
