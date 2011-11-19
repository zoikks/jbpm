package org.jbpm.test.matcher;

import org.drools.runtime.process.ProcessInstance;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.junit.internal.matchers.TypeSafeMatcher;


/**
* 
* User: salaboy
* Date: 2/15/11
* Time: 8:59 AM
* 
*/
public class CurrentActivitiesCountMatcher extends TypeSafeMatcher<Integer> {

    private ProcessInstance processInstance;

    public CurrentActivitiesCountMatcher(ProcessInstance processInstance){
        this.processInstance = processInstance;
    }
    @Override
    public boolean matchesSafely(Integer numberOfActivities) {
         return (numberOfActivities == ((WorkflowProcessInstance)processInstance).getNodeInstances().size());
    }

    public void describeTo(Description description) {
        description.appendText("the current process have "+((WorkflowProcessInstance)processInstance).getNodeInstances().size() +" running activities");
    }
    @Factory
     public static <T> Matcher<Integer> currentActivitiesCount(ProcessInstance process) {
       return new CurrentActivitiesCountMatcher(process);
     }
}