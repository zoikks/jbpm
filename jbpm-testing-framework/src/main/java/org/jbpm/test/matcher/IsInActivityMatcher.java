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

