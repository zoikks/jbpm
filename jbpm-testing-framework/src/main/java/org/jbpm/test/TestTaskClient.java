package org.jbpm.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.jbpm.task.AccessType;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskClientConnector;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;

public class TestTaskClient extends TaskClient {
	
	private LifeCyclePhase[] phases = null;

	public TestTaskClient(TaskClientConnector connector) {
		super(connector);
	}
	public void performLifeCycle(String user, String targetUser, String locale, HashMap<?, ?> input) {
		performLifeCycle(user, targetUser, locale, input, this.phases);
	}

	public void performLifeCycle(String user, String targetUser, String locale, HashMap<?, ?> input, LifeCyclePhase[] phasesIn) {
		if (phasesIn == null) {
			throw new IllegalArgumentException("No human task life cycle configured!");
		}
		sleep();
		
		BlockingTaskSummaryResponseHandler taskSummaryHandler = new BlockingTaskSummaryResponseHandler();
		getTasksAssignedAsPotentialOwner(user, locale, taskSummaryHandler);
		
		// TODO secure that task was found, or throw an exception
		TaskSummary task1 = taskSummaryHandler.getResults().get(0);
		long taskId = task1.getId();
		
		for (LifeCyclePhase phase : phasesIn) {
			switch (phase) {
			case CLAIM:
				
				BlockingTaskOperationResponseHandler taskOperationHandler = new BlockingTaskOperationResponseHandler();
				claim(taskId, user, taskOperationHandler);
				break;
	
			case START:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				start(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
			
			case COMPLETE:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				complete(taskId, user, buildContentData(input), taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case FAIL:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				fail(taskId, user, buildFaultData(input), taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case REMOVE:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				remove(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case SKIP:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				skip(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case STOP:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				stop(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case RELEASE:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				release(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case SUSPEND:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				suspend(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case RESUME:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				resume(taskId, user, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case DELEGATE:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				delegate(taskId, user, targetUser, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			case FORWARD:
				taskOperationHandler = new BlockingTaskOperationResponseHandler();
				forward(taskId, user, targetUser, taskOperationHandler);
				taskOperationHandler.waitTillDone(1000);
				break;
				
			default:
				break;
			}
		}
		
		sleep();
	}

	protected void setPhases(LifeCyclePhase[] phases) {
		this.phases = phases;
	}

	protected LifeCyclePhase[] getPhases() {
		return phases;
	}
	
	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
	}
	
	private ContentData buildContentData(HashMap<?, ?> input) {
		
		if(input == null) {
			return null;
		}
		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(input);
			out.close();
			ContentData contentData = new ContentData();
			contentData.setContent(bos.toByteArray());
			contentData.setAccessType(AccessType.Inline);

			return contentData;
		} catch (IOException e) {

			return null;
		}
	}
	
	private FaultData buildFaultData(HashMap<?, ?> input) {
		
		if(input == null) {
			return null;
		}
		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(input);
			out.close();
			FaultData faultData = new FaultData();
			faultData.setContent(bos.toByteArray());
			faultData.setAccessType(AccessType.Inline);

			return faultData;
		} catch (IOException e) {

			return null;
		}
	}
}
