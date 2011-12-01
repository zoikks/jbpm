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

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.junit.runner.notification.RunNotifier;

public class Context {

	private KnowledgeBase kBaseDI = null;
	private StatefulKnowledgeSession session = null;
	private JPAWorkingMemoryDbLogger logger = null;
	private KnowledgeRuntimeLogger log = null;
	private EntityManagerFactory emf;
	private int sessionId = -1;
	private TestTaskClient taskClient;
	private LifeCyclePhase[] phases;
	private boolean disposeSessionPerTest;
	
	private RunNotifier notifier;
	
	private Class<?> testClass = null;
	
	public Context() {
		
	}

	public void setkBaseDI(KnowledgeBase kBaseDI) {
		this.kBaseDI = kBaseDI;
	}

	public KnowledgeBase getkBaseDI() {
		return kBaseDI;
	}

	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
		if (session != null) {
			this.sessionId = session.getId();
		}
	}

	public StatefulKnowledgeSession getSession() {
		return session;
	}

	public boolean isDisposeSessionPerTest() {
		
		return this.disposeSessionPerTest;
	}

	public void setTestClass(Class<?> testClass) {
		this.testClass = testClass;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public void setLogger(JPAWorkingMemoryDbLogger logger) {
		this.logger = logger;
	}

	public JPAWorkingMemoryDbLogger getLogger() {
		return logger;
	}

	public void setLog(KnowledgeRuntimeLogger log) {
		this.log = log;
	}

	public KnowledgeRuntimeLogger getLog() {
		return log;
	}

	public void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public EntityManagerFactory getEmf() {
		return emf;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setTaskClient(TestTaskClient taskClient) {
		this.taskClient = taskClient;
	}

	public TestTaskClient getTaskClient() {
		return taskClient;
	}

	public void setPhases(LifeCyclePhase[] phases) {
		this.phases = phases;
	}

	public LifeCyclePhase[] getPhases() {
		return phases;
	}

	public void setDisposeSessionPerTest(boolean disposeSessionPerTest) {
		this.disposeSessionPerTest = disposeSessionPerTest;
	}

    public void setNotifier(RunNotifier notifier) {
        this.notifier = notifier;
    }

    public RunNotifier getNotifier() {
        return notifier;
    }
}
