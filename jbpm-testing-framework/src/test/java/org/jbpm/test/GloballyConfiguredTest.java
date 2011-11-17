package org.jbpm.test;

import static org.jbpm.test.JbpmAssert.assertProcessInstanceComplete;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.test.annotation.LifeCycle;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JbpmJUnitRunner.class)
public class GloballyConfiguredTest {

	protected org.drools.KnowledgeBase knowledgeBase;
	protected StatefulKnowledgeSession session;
	protected TestTaskClient taskClient;

	@Test
	public void testRunner() {
		ProcessInstance instance = session.startProcess("ScriptTask");
		assertProcessInstanceComplete(instance);
	}

	@Test
	public void testRunner2() {
		ProcessInstance instance = session.startProcess("ServiceProcess");
		assertProcessInstanceComplete(instance);
	}
	
	@Test
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.COMPLETE})
	public void testHumanTask() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");

		taskClient.performLifeCycle("john", null, "en-UK", null);
		
		assertProcessInstanceComplete(instance);
	}
	
	@Test
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.COMPLETE})
	public void testHumanTask2() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");
		
		taskClient.performLifeCycle("john", null, "en-UK", null);
		
		assertProcessInstanceComplete(instance);
	}
}
