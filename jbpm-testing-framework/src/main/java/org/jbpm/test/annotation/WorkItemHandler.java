package org.jbpm.test.annotation;

public @interface WorkItemHandler {

    String taskName();
    Class<?> handler();
}
