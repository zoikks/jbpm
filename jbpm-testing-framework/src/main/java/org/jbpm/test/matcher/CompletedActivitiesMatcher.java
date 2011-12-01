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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.drools.event.process.ProcessEventListener;
import org.drools.runtime.process.ProcessInstance;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.jbpm.test.RecordingProcessEventListener;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.internal.matchers.TypeSafeMatcher;

public class CompletedActivitiesMatcher  extends TypeSafeMatcher<String[]> {

    private ProcessInstance process;
    private List<String> completedNodes = null;
    
    public CompletedActivitiesMatcher(ProcessInstance processInstance) {
        this.process = processInstance;
    }

    public void describeTo(Description description) {

        if (this.completedNodes != null) {
            for(String completedNode : completedNodes){
                 description.appendText(completedNode + " -> ");
            }
        } else {
            description.appendText("Completed nodes information not available \n");
        }
    }

    @Override
    public boolean matchesSafely(String[] items) {
        Collection<ProcessEventListener> eventListeners = ((WorkflowProcessInstanceImpl)process).getKnowledgeRuntime().getProcessEventListeners();
        for (ProcessEventListener eventListener : eventListeners) {
            if (eventListener instanceof RecordingProcessEventListener) {
                completedNodes = ((RecordingProcessEventListener) eventListener).getCompletedNodes();
                break;
            }
        }
        if (this.completedNodes != null && completedNodes.containsAll(Arrays.asList(items))) {
            return true;
        }
        return false;
        
    }

    @Factory
    public static <T> Matcher<String[]> completedActivities(ProcessInstance processInstance) {
        return new CompletedActivitiesMatcher(processInstance);
    }
}