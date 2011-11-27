package org.jbpm.test.matcher;

import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.junit.internal.matchers.TypeSafeMatcher;



/**
* Created by IntelliJ IDEA.
* User: salaboy
* Date: 2/14/11
* Time: 11:26 PM
* To change this template use File | Settings | File Templates.
*/
public class IsInActivityMatcher extends TypeSafeMatcher<String> {
    private ProcessInstance process;
    public IsInActivityMatcher(ProcessInstance process){
       this.process = process;
    }

    @Override
    public boolean matchesSafely(String activityName) {
        for(NodeInstance nodeInstance : ((WorkflowProcessInstance)process).getNodeInstances()){
            // handle subprocess 
            if ( nodeInstance instanceof CompositeContextNodeInstance) {
                for (NodeInstance ni : ((CompositeContextNodeInstance) nodeInstance).getNodeInstances(true)) {
                    if(activityName.equals(ni.getNodeName())){
                        return true;
                    } 
                }
            }
            else if(activityName.equals(nodeInstance.getNodeName())){
                return true;
            }
        }
        return false;

    }



    @Factory
     public static <T> Matcher<String> isInActivity(ProcessInstance process) {
       return new IsInActivityMatcher(process);
     }


    public void describeTo(Description description) {
        description.appendText("the current process is executing the following list of activities: \n");
         for(NodeInstance nodeInstance : ((WorkflowProcessInstance)process).getNodeInstances()){
              description.appendText(" -> "+nodeInstance.getNodeName());
        }
    }
}

