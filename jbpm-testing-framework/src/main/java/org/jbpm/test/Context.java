package org.jbpm.test;

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;

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
}
