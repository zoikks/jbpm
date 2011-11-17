package org.jbpm.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbpm.test.LifeCyclePhase;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LifeCycle {

	LifeCyclePhase[] phases();
}
