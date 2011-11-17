package org.jbpm.test;

import static org.jbpm.test.JbpmAssert.assertProcessInstanceComplete;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.process.workitem.wsht.WSHumanTaskHandler;
import org.jbpm.test.annotation.HumanTaskSupport;
import org.jbpm.test.annotation.KnowledgeBase;
import org.jbpm.test.annotation.KnowledgeSession;
import org.jbpm.test.annotation.LifeCycle;
import org.jbpm.test.annotation.WorkItemHandler;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JbpmJUnitRunner.class)
@KnowledgeBase(source={"script.bpmn2","service.bpmn2","usertask.bpmn2"}, sharedKey="common")
@KnowledgeSession(handlers={@WorkItemHandler(taskName="Service Task", handler=ServiceTaskHandler.class),
		@WorkItemHandler(taskName="Human Task", handler=WSHumanTaskHandler.class)}, logger=Logger.CONSOLE)
@HumanTaskSupport(persistenceUnit="org.jbpm.task", users={"john", "Administrator"})
public class CopyOfJbpmJUnitRunnerTest {
	
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
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.SUSPEND, LifeCyclePhase.RESUME, LifeCyclePhase.COMPLETE})
	public void testHumanTask() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");

		taskClient.performLifeCycle("john", null, "en-UK", null);
		
		assertProcessInstanceComplete(instance);
	}
	
	@Test
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.FAIL})
	public void testHumanTask2() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");
		
		taskClient.performLifeCycle("john", null, "en-UK", null);
		
		assertProcessInstanceComplete(instance);
	}
}
