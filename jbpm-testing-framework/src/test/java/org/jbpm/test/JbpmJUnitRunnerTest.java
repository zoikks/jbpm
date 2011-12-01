/**
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.test;


import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.process.workitem.wsht.WSHumanTaskHandler;
import org.jbpm.test.annotation.*;
import static org.jbpm.test.matcher.ProcessStateMatcher.isInState;
import static org.jbpm.test.matcher.CompletedActivitiesMatcher.completedActivities;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JbpmJUnitRunner.class)
@KnowledgeBase(source={"script.bpmn2","service.bpmn2","usertask.bpmn2"}, sharedKey="common")
@KnowledgeSession(handlers={@WorkItemHandler(taskName="Service Task", handler=ServiceTaskHandler.class),
		@WorkItemHandler(taskName="Human Task", handler=WSHumanTaskHandler.class)}, logger=Logger.CONSOLE)
@HumanTaskSupport(persistenceUnit="org.jbpm.task", users={"john", "mike", "Administrator"})
public class JbpmJUnitRunnerTest {
	
	protected org.drools.KnowledgeBase knowledgeBase;
	protected StatefulKnowledgeSession session;
	protected TestTaskClient taskClient;

	@Test
	public void testRunner() {
		ProcessInstance instance = session.startProcess("ScriptTask");
		assertThat(ProcessInstance.STATE_COMPLETED, isInState(instance));
		String[] expected = {"Hello", "StartProcess", "EndProcess"}; 
        assertThat(expected, completedActivities(instance));
	}

	@Test
	public void testRunner2() {
		ProcessInstance instance = session.startProcess("ServiceProcess");
		assertThat(ProcessInstance.STATE_COMPLETED, isInState(instance));
		String[] expected = {"Hello", "StartProcess", "EndProcess"}; 
        assertThat(expected, completedActivities(instance));
	}
	
	@Test
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.DELEGATE})
	public void testHumanTask() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");

		taskClient.performLifeCycle("john", "mike", "en-UK", null);
		taskClient.performLifeCycle("mike", null, "en-UK", null, new LifeCyclePhase[]{LifeCyclePhase.START, LifeCyclePhase.COMPLETE});
		
		assertThat(ProcessInstance.STATE_COMPLETED, isInState(instance));
		String[] expected = {"Hello", "StartProcess", "EndProcess"}; 
        assertThat(expected, completedActivities(instance));
	}
	
	@Test
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.COMPLETE})
	public void testHumanTask2() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");
		
		taskClient.performLifeCycle("john", null, "en-UK", null);
		
		assertThat(ProcessInstance.STATE_COMPLETED, isInState(instance));
		String[] expected = {"Hello", "StartProcess", "EndProcess"}; 
        assertThat(expected, completedActivities(instance));
	}
}
