package org.jbpm.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbpm.test.Logger;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KnowledgeSession {

	boolean disposePerTest() default true;
	WorkItemHandler[] handlers() default {};
	Logger logger() default Logger.NONE;
	Persistence persistence() default @Persistence;
}
