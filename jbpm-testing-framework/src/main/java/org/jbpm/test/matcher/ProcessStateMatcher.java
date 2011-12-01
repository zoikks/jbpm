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
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Created by IntelliJ IDEA. User: salaboy Date: 2/15/11 Time: 8:38 AM To change
 * this template use File | Settings | File Templates.
 */
public class ProcessStateMatcher extends TypeSafeMatcher<Integer> {

    private ProcessInstance processInstance;

    public ProcessStateMatcher(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @Override
    public boolean matchesSafely(Integer state) {
        return (this.processInstance.getState() == state);
    }

    public void describeTo(Description description) {
        description.appendText("is not in " + this.processInstance.getState() + " State");

    }

    @Factory
    public static <T> Matcher<Integer> isInState(ProcessInstance process) {
        return new ProcessStateMatcher(process);
    }
}