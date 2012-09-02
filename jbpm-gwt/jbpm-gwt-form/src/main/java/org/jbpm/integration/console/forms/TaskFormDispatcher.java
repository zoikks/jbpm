/**
 * Copyright 2010 JBoss Inc
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

package org.jbpm.integration.console.forms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;

import org.drools.KnowledgeBaseFactory;
import org.drools.impl.EnvironmentFactory;
import org.drools.marshalling.ObjectMarshallingStrategy;
import org.drools.marshalling.ObjectMarshallingStrategy.Context;
import org.drools.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jbpm.integration.console.TaskClientFactory;
import org.jbpm.task.Content;
import org.jbpm.task.I18NText;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.utils.ContentMarshallerContext;
import org.jbpm.task.utils.ContentMarshallerHelper;
import org.jbpm.task.utils.MarshalledContentWrapper;

/**
 * @author Kris Verlaenen
 */
public class TaskFormDispatcher extends AbstractFormDispatcher {

    private static int clientCounter = 0;
    
    private TaskService service;
    private boolean local = false;
    private ContentMarshallerContext marshaller = new ContentMarshallerContext();

    public void connect() {
        if (service == null) {

            Properties properties = new Properties();
            try {
                properties.load(AbstractFormDispatcher.class.getResourceAsStream("/jbpm.console.properties"));
            } catch (IOException e) {
                throw new RuntimeException("Could not load jbpm.console.properties", e);
            }

            service =TaskClientFactory.newInstance(properties, "org.jbpm.integration.console.forms.TaskFormDispatcher"+clientCounter);
	    clientCounter++;
        }
    }

    public DataHandler provideForm(FormAuthorityRef ref) {
        connect();
        Task task = service.getTask(new Long(ref.getReferenceId()));
        
        Object input = null;
        long contentId = task.getTaskData().getDocumentContentId();
        if (contentId != -1) {
            Content content = null;
            
            content = service.getContent(contentId);
            
            ByteArrayInputStream bis = new ByteArrayInputStream(content.getContent());
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(bis);
                input = in.readObject();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // check if a template exists
        String name = null;
        List<I18NText> names = task.getNames();
        for (I18NText text: names) {
            if ("en-UK".equals(text.getLanguage())) {
                name = text.getText();
            }
        }
        InputStream template = getTemplate(name);
        if (template == null) {
            template = TaskFormDispatcher.class.getResourceAsStream("/DefaultTask.ftl");
        }

        // merge template with process variables
        Map<String, Object> renderContext = new HashMap<String, Object>();
        renderContext.put("task", task);
        renderContext.put("content", input);
        if (input instanceof Map) {
            Map<?, ?> map = (Map) input;
            for (Map.Entry<?, ?> entry: map.entrySet()) {
                if (entry.getKey() instanceof String) {
                	MarshalledContentWrapper val = (MarshalledContentWrapper)entry.getValue();
                	
                	Environment env = EnvironmentFactory.newEnvironment();
                	ContentMarshallerContext cmc = new ContentMarshallerContext();
                	ObjectMarshallingStrategy[] strats = (ObjectMarshallingStrategy[])env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
                	cmc.addStrategy(strats[0]);	// Should only have one option here...
                	Object o = ContentMarshallerHelper.unmarshall("org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy", val.getContent(), cmc, env);
                	
                	renderContext.put((String) entry.getKey(), o);
                }
            }
        }
     
        return processTemplate(name, template, renderContext);
    }

}
