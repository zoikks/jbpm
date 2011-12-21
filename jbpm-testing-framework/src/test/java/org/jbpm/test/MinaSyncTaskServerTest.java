package org.jbpm.test;

import static org.jbpm.test.matcher.CompletedActivitiesMatcher.completedActivities;
import static org.jbpm.test.matcher.ProcessStateMatcher.isInState;
import static org.junit.Assert.assertThat;

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
@KnowledgeBase(source={"usertask.bpmn2"})
@KnowledgeSession(handlers={@WorkItemHandler(taskName="Service Task", handler=ServiceTaskHandler.class),
        @WorkItemHandler(taskName="Human Task", handler=WSHumanTaskHandler.class)}, logger=Logger.CONSOLE)
@HumanTaskSupport(persistenceUnit="org.jbpm.task", users={"john", "mike", "Administrator"}, 
        type=TaskServerType.MINA_SYNC, port=9123)
public class MinaSyncTaskServerTest {
    
    protected org.drools.KnowledgeBase knowledgeBase;
    protected StatefulKnowledgeSession session;
    protected TestTaskClient taskClient;
    
    @Test
    @LifeCycle(phases={LifeCyclePhase.START, LifeCyclePhase.COMPLETE})
    public void testHumanTask() throws Exception {
        ProcessInstance instance = session.startProcess("UserTask");
        
        taskClient.performLifeCycle("john", null, "en-UK", null);
        
        assertThat(ProcessInstance.STATE_COMPLETED, isInState(instance));
        String[] expected = {"Hello", "StartProcess", "EndProcess"}; 
        assertThat(expected, completedActivities(instance));
    }

}
