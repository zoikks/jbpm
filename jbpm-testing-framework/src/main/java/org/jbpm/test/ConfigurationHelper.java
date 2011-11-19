package org.jbpm.test;

import java.io.File;
import java.util.Properties;

import javax.persistence.Persistence;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;
import org.jbpm.test.annotation.HumanTaskSupport;
import org.jbpm.test.annotation.KnowledgeSession;
import org.jbpm.test.annotation.WorkItemHandler;

import bitronix.tm.TransactionManagerServices;

public class ConfigurationHelper {
    
    public static final String KNOWLEDGEBASE_SOURCE = "knowledge.base.source";
    public static final String KNOWLEDGEBASE_SHARED_KEY = "knowledge.base.shared.key";
    
    public static final String KNOWLEDGESESSION_DISPOSE_PER_TEST = "session.dispose.per.test";
    public static final String KNOWLEDGESESSION_LOGGER = "session.logger";
    public static final String KNOWLEDGESESSION_PERSISTENCE_UNIT = "session.persistence.unit";
    public static final String KNOWLEDGESESSION_PERSISTENCE_RELOAD_SESSION = "session.persistence.reload";
    public static final String KNOWLEDGESESSION_WORKITEM_HANDLERS = "session.workitem.handlers";
    
    public static final String HUMAN_TASK_USERS = "human.task.users";
    public static final String HUMAN_TASK_GROUPS = "human.task.groups";
    public static final String HUMAN_TASK_PERSISTENCE_UNIT = "human.task.persistence.unit";
    public static final String HUMAN_TASK_HOST = "human.task.host";
    public static final String HUMAN_TASK_PORT = "human.task.port";
    
    protected static final String OUTPUT_DIR = System.getProperty("build.dir", "target") + File.separator;
    

    public static KnowledgeBase buildKnowledgeBase(Properties jbpmTestConfiguration) {
        String sharedKey = jbpmTestConfiguration.getProperty(KNOWLEDGEBASE_SHARED_KEY);
        KnowledgeBase kBase = null;
        if (!JbpmJUnitRunner.kbCache.containsKey(sharedKey)) {
            String[] sources = jbpmTestConfiguration.getProperty(KNOWLEDGEBASE_SOURCE).split(";");
            kBase = buildInternalKnowledgeBase(sources);
            if (sharedKey != null && sharedKey.length() > 0) {
                JbpmJUnitRunner.kbCache.put(sharedKey, kBase);  
            }
        } else {
            kBase = JbpmJUnitRunner.kbCache.get(sharedKey);
        }
        return kBase;
    }
    
    public static KnowledgeBase buildKnowledgeBase(Class<?> testClass) {
        org.jbpm.test.annotation.KnowledgeBase kBaseA = (org.jbpm.test.annotation.KnowledgeBase) testClass.getAnnotation(org.jbpm.test.annotation.KnowledgeBase.class);
        KnowledgeBase kBase = null;
        if (!JbpmJUnitRunner.kbCache.containsKey(kBaseA.sharedKey())) {
            kBase = buildInternalKnowledgeBase(kBaseA.source());
            if (kBaseA.sharedKey() != null && kBaseA.sharedKey().length() > 0) {
                JbpmJUnitRunner.kbCache.put(kBaseA.sharedKey(), kBase); 
            }
        } else {
            kBase = JbpmJUnitRunner.kbCache.get(kBaseA.sharedKey());
        }
        return kBase;
    }
    
    protected static KnowledgeBase buildInternalKnowledgeBase(String[] sources) {
        // build knowledge base
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (String resource : sources) {
            if (resource.toLowerCase().endsWith("bpmn") || resource.toLowerCase().endsWith("bpmn2")) {
                kbuilder.add(ResourceFactory.newClassPathResource(resource), ResourceType.BPMN2);
            } else if (resource.toLowerCase().endsWith("drl")) {
                kbuilder.add(ResourceFactory.newClassPathResource(resource), ResourceType.DRL);
            }
        }
        return kbuilder.newKnowledgeBase();
    }
    
    public static TestTaskClient buildTaskClient(Class<?> testClass) {
        HumanTaskSupport htSupport = testClass.getAnnotation(HumanTaskSupport.class);
        TestTaskClient taskClient = new TestTaskClient(new MinaTaskClientConnector("MinaConnector",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        taskClient.connect(htSupport.host(), htSupport.port());
        
        return taskClient;
    }
    
    public static TestTaskClient buildTaskClient(Properties jbpmTestConfiguration) {
        
        TestTaskClient taskClient = new TestTaskClient(new MinaTaskClientConnector("MinaConnector",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        taskClient.connect(jbpmTestConfiguration.getProperty(HUMAN_TASK_HOST, "localhost"), 
                Integer.parseInt(jbpmTestConfiguration.getProperty(HUMAN_TASK_PORT, "9123")));
        
        return taskClient;
    }
    
    protected static void registerWorkItemHandlers(Class<?> testClass, StatefulKnowledgeSession session) {
           
        WorkItemHandler[] handlers = testClass.getAnnotation(KnowledgeSession.class).handlers();
        for (WorkItemHandler handler : handlers) {
            String workItem = (String) handler.taskName();
            
            try {
                session.getWorkItemManager().registerWorkItemHandler(workItem, (org.drools.runtime.process.WorkItemHandler) handler.handler().newInstance());
              
                
            } catch (Exception e) {
                System.err.println("Error while registering handler " + handler.handler() + " for work item " + workItem);
            }
        }
    }
    
    protected static Environment buildEnvironment(String puName, Context context) {

        Environment environment = KnowledgeBaseFactory.newEnvironment();
        if (context.getEmf() == null || !context.getEmf().isOpen()) {
            context.setEmf(Persistence.createEntityManagerFactory(puName));
        }
        environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, context.getEmf());
        environment.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
     
        return environment;
    }
    
    protected static KnowledgeSessionConfiguration buildConfiguration() {
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
        
        return config;
    }
    @SuppressWarnings("unchecked")
    protected static StatefulKnowledgeSession getSession(Properties jbpmTestConfiguration, String testName, Class<?> testClass, Context context) {
        if (context.getSession() == null) {
            String puName = jbpmTestConfiguration.getProperty(KNOWLEDGESESSION_PERSISTENCE_UNIT);
            boolean reload = Boolean.parseBoolean(jbpmTestConfiguration.getProperty(KNOWLEDGESESSION_PERSISTENCE_RELOAD_SESSION, "false"));
            
            if (puName != null && puName.length() > 0) {
                if (reload && context.getSessionId() > -1) {
                    context.setSession(JPAKnowledgeService.loadStatefulKnowledgeSession(context.getSessionId(), context.getkBaseDI(), buildConfiguration(), buildEnvironment(puName, context)));
                } else {
                    context.setSession(JPAKnowledgeService.newStatefulKnowledgeSession(context.getkBaseDI(), buildConfiguration(), buildEnvironment(puName, context)));
                }
            } else {
                context.setSession(context.getkBaseDI().newStatefulKnowledgeSession());
            }
        }
        
        Logger logger = Logger.valueOf(jbpmTestConfiguration.getProperty(KNOWLEDGESESSION_LOGGER, "NONE"));

        switch (logger) {
        case CONSOLE:
            KnowledgeRuntimeLoggerFactory.newConsoleLogger(context.getSession());
            break;
        case FILE:
            context.setLog(KnowledgeRuntimeLoggerFactory.newFileLogger(context.getSession(), OUTPUT_DIR + testClass.getName() +"." + testName + ".log"));
            break;
        case JPA:
            context.setLogger(new JPAWorkingMemoryDbLogger(context.getSession()));
            break;

        default:
            break;
        }
        
        String workItemHandlersAsString = jbpmTestConfiguration.getProperty(KNOWLEDGESESSION_WORKITEM_HANDLERS);
        if (workItemHandlersAsString != null) {
            // format should be:
            // taskName:className;taskName2:className2
            
            String[] handlers = workItemHandlersAsString.split(";");
            
            for (String handlerDef : handlers) {
                String[] handlerDefParts = handlerDef.split(":");
                
                try {
                    Class<org.drools.runtime.process.WorkItemHandler> handlerClass = (Class<org.drools.runtime.process.WorkItemHandler>) Class.forName(handlerDefParts[1]);
                    context.getSession().getWorkItemManager().registerWorkItemHandler(handlerDefParts[0], handlerClass.newInstance());
                  
                    
                } catch (Exception e) {
                    System.err.println("Error while registering handler " + handlerDefParts[1] + " for work item " + handlerDefParts[0]);
                }
            }
        }
        context.setDisposeSessionPerTest(Boolean.parseBoolean(jbpmTestConfiguration.getProperty(KNOWLEDGESESSION_DISPOSE_PER_TEST, "true")));
        return context.getSession();
    }
    protected static StatefulKnowledgeSession getSession(Class<?> testClass, String testName, Context context) {
        if (context.getSession() == null) {
            org.jbpm.test.annotation.Persistence p = (org.jbpm.test.annotation.Persistence) testClass.getAnnotation(KnowledgeSession.class).persistence();
            String puName = p.persistenceUnit();
            
            if (puName != null && puName.length() > 0) {
                if (p.reloadSession() && context.getSessionId() > -1) {
                    context.setSession(JPAKnowledgeService.loadStatefulKnowledgeSession(context.getSessionId(), context.getkBaseDI(), buildConfiguration(), buildEnvironment(puName, context)));
                } else {
                    context.setSession(JPAKnowledgeService.newStatefulKnowledgeSession(context.getkBaseDI(), buildConfiguration(), buildEnvironment(puName, context)));
                }
            } else {
                context.setSession(context.getkBaseDI().newStatefulKnowledgeSession());
            }
        }
        Logger logger = testClass.getAnnotation(KnowledgeSession.class).logger();

        switch (logger) {
        case CONSOLE:
            KnowledgeRuntimeLoggerFactory.newConsoleLogger(context.getSession());
            break;
        case FILE:
            context.setLog(KnowledgeRuntimeLoggerFactory.newFileLogger(context.getSession(), OUTPUT_DIR + testClass.getName() +"." + testName + ".log"));
            break;
        case JPA:
            context.setLogger(new JPAWorkingMemoryDbLogger(context.getSession()));
            break;

        default:
            break;
        }
        
        registerWorkItemHandlers(testClass, context.getSession());
        context.setDisposeSessionPerTest(testClass.getAnnotation(KnowledgeSession.class).disposePerTest());
        return context.getSession();
    }
}
