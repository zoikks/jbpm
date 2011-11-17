package org.jbpm.test;

import org.junit.runners.model.Statement;

public class JbpmStatement extends Statement {

	private Object target;
	private Statement next;
	
	public JbpmStatement(Statement statement, Object target) {
		this.next = statement;
		this.target = target;
	}
	
	public Object getTarget() {
		return this.target;
	}

	@Override
	public void evaluate() throws Throwable {
		System.out.println("Setting knowledge base on " + this.target);
		System.out.println("Setting knowledge session on " + this.target);
		
		next.evaluate();
		
	}
}
