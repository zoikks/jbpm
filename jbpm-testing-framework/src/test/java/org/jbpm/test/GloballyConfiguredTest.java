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
import org.jbpm.test.annotation.LifeCycle;

import static org.jbpm.test.matcher.CompletedActivitiesMatcher.completedActivities;
import static org.jbpm.test.matcher.ProcessStateMatcher.isInState;
import static org.junit.Assert.*;

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
	@LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.COMPLETE})
	public void testHumanTask() throws Exception {
		ProcessInstance instance = session.startProcess("UserTask");

		taskClient.performLifeCycle("john", null, "en-UK", null);
		
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
