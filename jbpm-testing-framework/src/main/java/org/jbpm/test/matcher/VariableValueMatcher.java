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



import java.util.regex.Pattern;

import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.NodeInstanceResolverFactory;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.mvel2.MVEL;
/**
* Created by IntelliJ IDEA.
* User: salaboy
* Date: 2/15/11
* Time: 9:49 AM
* To change this template use File | Settings | File Templates.
*/
public class VariableValueMatcher extends TypeSafeMatcher<String> {

    private static final Pattern PARAMETER_MATCHER = Pattern.compile("#\\{(\\S+)\\}", Pattern.DOTALL);
    private ProcessInstance processInstance;
    private String expression;

    public VariableValueMatcher(ProcessInstance processInstance, String expression) {
        this.processInstance = processInstance;
        this.expression = expression;
    }

    @Override
    public boolean matchesSafely(String item) {
        
        if (ProcessInstance.STATE_COMPLETED == processInstance.getState() || ProcessInstance.STATE_ABORTED == processInstance.getState()) {
            java.util.regex.Matcher matcher = PARAMETER_MATCHER.matcher(this.expression);
            while (matcher.find()) {
                String paramName = matcher.group(1);

                return resolveVariable(paramName, item, ((WorkflowProcessInstanceImpl)processInstance).getVariables());
            }
        } else {
           for(NodeInstance currentNodeInstance : ((WorkflowProcessInstance)processInstance).getNodeInstances()){
                NodeInstanceImpl currentNode = (NodeInstanceImpl) currentNodeInstance;
                java.util.regex.Matcher matcher = PARAMETER_MATCHER.matcher(this.expression);
                while (matcher.find()) {
                    String paramName = matcher.group(1);
    
                    return resolveVariable(paramName, item, new NodeInstanceResolverFactory(currentNode));
                }
            }
        }

        return false;
    }


    public void describeTo(Description description) {
        description.appendText("the expression "+this.expression +" cannot be resolved from the current Node Instances");
    }

    @Factory
    public static <T> Matcher<String> variableValue(ProcessInstance processInstance, String expression) {
        return new VariableValueMatcher(processInstance, expression);
    }

    protected boolean resolveVariable(String paramName, String item, Object input) {
        try {
            
            Object variableValue = MVEL.eval(paramName, input);
            String variableValueString = variableValue == null ? "" : variableValue.toString();
            return variableValueString.equals(item);
        } catch (Throwable t) {
            Object variableValue = ((WorkflowProcessInstance)processInstance).getVariable(paramName);
            if (variableValue != null) {
                String variableValueString = variableValue.toString();
                return variableValueString.equals(item);
            }
            
            System.err.println("Could not find variable scope for variable " + paramName);
            System.err.println("when trying to replace variable in string for process instance " + processInstance);
            System.err.println("Continuing without setting parameter.");
            return false;
        }
    }
}

