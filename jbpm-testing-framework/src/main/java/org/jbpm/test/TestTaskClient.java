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
package org.jbpm.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import org.jbpm.eventmessaging.EventKey;
import org.jbpm.eventmessaging.EventResponseHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.AsyncTaskService;
import org.jbpm.task.Attachment;
import org.jbpm.task.Comment;
import org.jbpm.task.Content;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.jbpm.task.service.TaskClientHandler.AddAttachmentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.AddCommentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.AddTaskResponseHandler;
import org.jbpm.task.service.TaskClientHandler.DeleteAttachmentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.DeleteCommentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.GetContentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.GetTaskResponseHandler;
import org.jbpm.task.service.TaskClientHandler.QueryGenericResponseHandler;
import org.jbpm.task.service.TaskClientHandler.SetDocumentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.TaskOperationResponseHandler;
import org.jbpm.task.service.TaskClientHandler.TaskSummaryResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;

public class TestTaskClient implements TaskService, AsyncTaskService {
	
	private LifeCyclePhase[] phases = null;

	private TaskService taskService = null;
	private AsyncTaskService asyncTaskService = null;
	
	public TestTaskClient(TaskService taskService) {
        this.taskService = taskService;
    }
	
	public TestTaskClient(AsyncTaskService asyncTaskService) {
        this.asyncTaskService = asyncTaskService;
    }
	
	public void performLifeCycle(String user, String targetUser, String locale, HashMap<?, ?> input) {
		performLifeCycle(user, targetUser, locale, input, this.phases);
	}

	public void performLifeCycle(String user, String targetUser, String locale, HashMap<?, ?> input, LifeCyclePhase[] phasesIn) {
		if (phasesIn == null) {
			throw new IllegalArgumentException("No human task life cycle configured!");
		}
		sleep();
		BlockingTaskOperationResponseHandler taskOperationHandler = null;
		BlockingTaskSummaryResponseHandler taskSummaryHandler = new BlockingTaskSummaryResponseHandler();
		List<TaskSummary> result = null;
		if (asyncTaskService != null) {
		    getTasksAssignedAsPotentialOwner(user, locale, taskSummaryHandler);
		    result = taskSummaryHandler.getResults();
		} else {
		    result = getTasksAssignedAsPotentialOwner(user, locale);
		}
		
		if (result == null || result.size()< 1) {
		    throw new RuntimeException("No user task was found for " + user + " on task server");
		}
		TaskSummary task1 = result.get(0);
		long taskId = task1.getId();
		
		for (LifeCyclePhase phase : phasesIn) {
			switch (phase) {
			case CLAIM:
				if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				claim(taskId, user, taskOperationHandler);
				} else {
				    claim(taskId, user);
				}
				break;
	
			case START:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				start(taskId, user, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
			        start(taskId, user);
			    }
				break;
			
			case COMPLETE:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				complete(taskId, user, buildContentData(input), taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    complete(taskId, user, buildContentData(input));
                }
				break;
				
			case FAIL:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				fail(taskId, user, buildFaultData(input), taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    fail(taskId, user, buildFaultData(input));
                }
				break;
				
			case REMOVE:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				remove(taskId, user, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
    			} else {
                    remove(taskId, user);
                }
				break;
				
			case SKIP:
			    if (this.asyncTaskService != null) {
        			taskOperationHandler = new BlockingTaskOperationResponseHandler();
        			skip(taskId, user, taskOperationHandler);
        			taskOperationHandler.waitTillDone(1000);
        		} else {
                    skip(taskId, user);
                }
				break;
				
			case STOP:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				stop(taskId, user, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    stop(taskId, user);
                }
				break;
				
			case RELEASE:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				release(taskId, user, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    release(taskId, user);
                }
				break;
				
			case SUSPEND:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				suspend(taskId, user, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    suspend(taskId, user);
                }
				break;
				
			case RESUME:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				resume(taskId, user, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    resume(taskId, user);
                }
				break;
				
			case DELEGATE:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				delegate(taskId, user, targetUser, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    delegate(taskId, user, targetUser);
                }
				break;
				
			case FORWARD:
			    if (this.asyncTaskService != null) {
    				taskOperationHandler = new BlockingTaskOperationResponseHandler();
    				forward(taskId, user, targetUser, taskOperationHandler);
    				taskOperationHandler.waitTillDone(1000);
			    } else {
                    forward(taskId, user, targetUser);
                }
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
    public void activate(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.activate(taskId, userId);
        
    }
    public void addAttachment(long taskId, Attachment attachment,
            Content content) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.addAttachment(taskId, attachment, content);
        
    }
    public void addComment(long taskId, Comment comment) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.addComment(taskId, comment);
        
    }
    public void addTask(Task task, ContentData content) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.addTask(task, content);
        
    }
    public void claim(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.claim(taskId, userId);
        
    }
    public void claim(long taskId, String userId, List<String> groupIds) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.claim(taskId, userId, groupIds);
        
    }
    public void complete(long taskId, String userId, ContentData outputData) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.complete(taskId, userId, outputData);
        
    }
    public void delegate(long taskId, String userId, String targetUserId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.delegate(taskId, userId, targetUserId);
        
    }
    public void deleteAttachment(long taskId, long attachmentId, long contentId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.deleteAttachment(taskId, attachmentId, contentId);
        
    }
    public void deleteComment(long taskId, long commentId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.deleteComment(taskId, commentId);
        
    }
    public void deleteFault(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.deleteFault(taskId, userId);
        
    }
    public void deleteOutput(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.deleteOutput(taskId, userId);
        
    }
    public void fail(long taskId, String userId, FaultData faultData) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.fail(taskId, userId, faultData);
        
    }
    public void forward(long taskId, String userId, String targetEntityId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.forward(taskId, userId, targetEntityId);
        
    }
    public Content getContent(long contentId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        
        return this.taskService.getContent(contentId);
    }
    public List<TaskSummary> getSubTasksAssignedAsPotentialOwner(long parentId,
            String userId, String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getSubTasksAssignedAsPotentialOwner(parentId, userId, language);
    }
    public List<TaskSummary> getSubTasksByParent(long parentId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getSubTasksByParent(parentId);
    }
    public Task getTask(long taskId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTask(taskId);
    }
    public Task getTaskByWorkItemId(long workItemId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTaskByWorkItemId(workItemId);
    }
    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(
            String userId, String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsBusinessAdministrator(userId, language);
    }
    public List<TaskSummary> getTasksAssignedAsExcludedOwner(String userId,
            String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsExcludedOwner(userId, language);
    }
    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
            String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsPotentialOwner(userId, language);
    }
    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
            List<String> groupIds, String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsPotentialOwner(userId, groupIds, language);
    }
    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
            List<String> groupIds, String language, int firstResult,
            int maxResult) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsPotentialOwner(userId, groupIds, language, firstResult, maxResult);
    }
    public List<TaskSummary> getTasksAssignedAsRecipient(String userId,
            String language) {

        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsRecipient(userId, language);
    }
    public List<TaskSummary> getTasksAssignedAsTaskInitiator(String userId,
            String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsTaskInitiator(userId, language);
    }
    public List<TaskSummary> getTasksAssignedAsTaskStakeholder(String userId,
            String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksAssignedAsTaskStakeholder(userId, language);
    }
    public List<TaskSummary> getTasksOwned(String userId, String language) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.getTasksOwned(userId, language);
    }
    public void nominate(long taskId, String userId,
            List<OrganizationalEntity> potentialOwners) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.nominate(taskId, userId, potentialOwners);
        
    }
    public List<?> query(String qlString, Integer size, Integer offset) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        return this.taskService.query(qlString, size, offset);
    }
    public void register(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.register(taskId, userId);
        
    }
    public void release(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.release(taskId, userId);
        
    }
    public void remove(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.remove(taskId, userId);
        
    }
    public void resume(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.resume(taskId, userId);
        
    }
    public void setDocumentContent(long taskId, Content content) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.setDocumentContent(taskId, content);
        
    }
    public void setFault(long taskId, String userId, FaultData fault) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.setFault(taskId, userId, fault);
        
    }
    public void setOutput(long taskId, String userId,
            ContentData outputContentData) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.setOutput(taskId, userId, outputContentData);
        
    }
    public void setPriority(long taskId, String userId, int priority) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.setPriority(taskId, userId, priority);
        
    }
    public void skip(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.skip(taskId, userId);
        
    }
    public void start(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.start(taskId, userId);
        
    }
    public void stop(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.stop(taskId, userId);
        
    }
    public void suspend(long taskId, String userId) {
        if (this.taskService == null) {
            throw new RuntimeException("Sync operation was invoked but no sync client was found!");
        }
        this.taskService.suspend(taskId, userId);
        
    }

    
    // start implementation of async task service
    public void activate(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.activate(taskId, userId, responseHandler);
    }

    public void addAttachment(long taskId, Attachment attachment,
            Content content, AddAttachmentResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.addAttachment(taskId, attachment, content, responseHandler);
        
    }

    public void addComment(long taskId, Comment comment,
            AddCommentResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.addComment(taskId, comment, responseHandler);
    }

    public void addTask(Task task, ContentData content,
            AddTaskResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.addTask(task, content, responseHandler);
        
    }

    public void claim(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.claim(taskId, userId, responseHandler);
    }

    public void claim(long taskId, String userId, List<String> groupIds,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.claim(taskId, userId, groupIds, responseHandler);
    }

    public void complete(long taskId, String userId, ContentData outputData,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.complete(taskId, userId, outputData, responseHandler);
        
    }

    public void delegate(long taskId, String userId, String targetUserId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.delegate(taskId, userId, targetUserId, responseHandler);
    }

    public void deleteAttachment(long taskId, long attachmentId,
            long contentId, DeleteAttachmentResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.deleteAttachment(taskId, attachmentId, contentId, responseHandler);
    }

    public void deleteComment(long taskId, long commentId,
            DeleteCommentResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.deleteComment(taskId, commentId, responseHandler);
    }

    public void deleteFault(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.deleteFault(taskId, userId, responseHandler);
    }

    public void deleteOutput(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.deleteOutput(taskId, userId, responseHandler);
    }

    public void fail(long taskId, String userId, FaultData faultData,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.fail(taskId, userId, faultData, responseHandler);
    }

    public void forward(long taskId, String userId, String targetEntityId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.forward(taskId, userId, targetEntityId, responseHandler);
    }

    public void getContent(long contentId,
            GetContentResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getContent(contentId, responseHandler);
    }

    public void getSubTasksAssignedAsPotentialOwner(long parentId,
            String userId, String language,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getSubTasksAssignedAsPotentialOwner(parentId, userId, language, responseHandler);
        
    }

    public void getSubTasksByParent(long parentId,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getSubTasksByParent(parentId, responseHandler);
    }

    public void getTask(long taskId, GetTaskResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTask(taskId, responseHandler);
    }

    public void getTaskByWorkItemId(long workItemId,
            GetTaskResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTaskByWorkItemId(workItemId, responseHandler);
    }

    public void getTasksAssignedAsBusinessAdministrator(String userId,
            String language, TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsBusinessAdministrator(userId, language, responseHandler);
    }

    public void getTasksAssignedAsExcludedOwner(String userId, String language,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsExcludedOwner(userId, language, responseHandler);
    }

    public void getTasksAssignedAsPotentialOwner(String userId,
            String language, TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsPotentialOwner(userId, language, responseHandler);
    }

    public void getTasksAssignedAsPotentialOwner(String userId,
            List<String> groupIds, String language,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsPotentialOwner(userId, groupIds, language, responseHandler);
    }

    public void getTasksAssignedAsPotentialOwner(String userId,
            List<String> groupIds, String language, int firstResult,
            int maxResult, TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsPotentialOwner(userId, groupIds, language, firstResult, maxResult, responseHandler);
    }

    public void getTasksAssignedAsRecipient(String userId, String language,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsRecipient(userId, language, responseHandler);
    }

    public void getTasksAssignedAsTaskInitiator(String userId, String language,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsTaskInitiator(userId, language, responseHandler);
    }

    public void getTasksAssignedAsTaskStakeholder(String userId,
            String language, TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksAssignedAsTaskStakeholder(userId, language, responseHandler);
    }

    public void getTasksOwned(String userId, String language,
            TaskSummaryResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.getTasksOwned(userId, language, responseHandler);
    }

    public void nominate(long taskId, String userId,
            List<OrganizationalEntity> potentialOwners,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.nominate(taskId, userId, potentialOwners, responseHandler);
    }

    public void query(String qlString, Integer size, Integer offset,
            QueryGenericResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.query(qlString, size, offset, responseHandler);
    }

    public void register(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.register(taskId, userId, responseHandler);
    }

    public void release(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.release(taskId, userId, responseHandler);
    }

    public void remove(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.remove(taskId, userId, responseHandler);
    }

    public void resume(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.resume(taskId, userId, responseHandler);
    }

    public void setDocumentContent(long taskId, Content content,
            SetDocumentResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.setDocumentContent(taskId, content, responseHandler);
    }

    public void setFault(long taskId, String userId, FaultData fault,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.setFault(taskId, userId, fault, responseHandler);
    }

    public void setOutput(long taskId, String userId,
            ContentData outputContentData,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.setOutput(taskId, userId, outputContentData, responseHandler);
    }

    public void setPriority(long taskId, String userId, int priority,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.setPriority(taskId, userId, priority, responseHandler);
    }

    public void skip(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.skip(taskId, userId, responseHandler);
    }

    public void start(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.start(taskId, userId, responseHandler);
    }

    public void stop(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.stop(taskId, userId, responseHandler);
    }

    public void suspend(long taskId, String userId,
            TaskOperationResponseHandler responseHandler) {
        if (this.asyncTaskService == null) {
            throw new RuntimeException("ASync operation was invoked but no async client was found!");
        }
        this.asyncTaskService.suspend(taskId, userId, responseHandler);
    }

    public boolean connect() {
        if (this.asyncTaskService != null) {
            return this.asyncTaskService.connect();
        } else {
            return this.taskService.connect();
        }
        
    }

    public boolean connect(String address, int port) {
        if (this.asyncTaskService != null) {
            return this.asyncTaskService.connect(address, port);
        } else {
            return this.taskService.connect(address, port);
        }
        
    }

    public void disconnect() throws Exception {

        if (this.asyncTaskService != null) {
            this.asyncTaskService.disconnect();
        } else {
            this.taskService.disconnect();
        }
        
    }

    public void registerForEvent(EventKey key, boolean remove,
            EventResponseHandler responseHandler) {
        if (this.asyncTaskService != null) {
            this.asyncTaskService.registerForEvent(key, remove, responseHandler);
        } else {
            this.taskService.registerForEvent(key, remove, responseHandler);
        }
    }
}
