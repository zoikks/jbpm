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