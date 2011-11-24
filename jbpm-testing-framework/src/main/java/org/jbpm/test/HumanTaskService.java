package org.jbpm.test;

import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.apache.mina.transport.socket.SocketSessionConfig;
import org.drools.SystemEventListenerFactory;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.jbpm.test.annotation.HumanTaskSupport;

public class HumanTaskService {

    private EntityManagerFactory emf;
    private Thread serverThread;
    private TaskService taskService;
    private TaskServer taskServer;
    private boolean started = false;
    
    public HumanTaskService() {
        
    }
    public void start(HumanTaskSupport htSupport) {
        
        if (!this.started) {
            emf = javax.persistence.Persistence.createEntityManagerFactory(htSupport.persistenceUnit());
            taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
            
            switch (htSupport.type()) {
            case MINA_ASYNC:
                // start server
                this.taskServer = new TestMinaTaskServer(taskService, htSupport.port(), htSupport.host());
                this.serverThread = new TaskServerThread(taskServer);
                serverThread.start();
                this.setStarted(true);
                break;
                
            case MINA_SYNC:
                // start server
                this.taskServer = new TestMinaTaskServer(taskService, htSupport.port(), htSupport.host());
                this.serverThread = new TaskServerThread(taskServer);
                serverThread.start();
                this.setStarted(true);
                break;

            case LOCAL:
                // start server
                this.setStarted(true);
                break;
            default:
                break;
            }
            

            TaskServiceSession session = taskService.createSession();
            for (String user : htSupport.users()) {
                session.addUser(new User(user));
            }
            
            for (String group : htSupport.groups()) {
                session.addGroup(new Group(group));
            }
            session.dispose();
        }
    }

    public void start(Properties jbpmTestConfiguration) {
        
        if (!this.started) {
            emf = javax.persistence.Persistence.createEntityManagerFactory(jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_PERSISTENCE_UNIT));
            taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
            int port = Integer.parseInt(jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_PORT, "9123"));
            
            switch (TaskServerType.valueOf(jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_TYPE, "MINA_ASYNC"))) {
            case MINA_ASYNC:
                // start server
                this.taskServer = new TestMinaTaskServer(taskService, port, jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_HOST, "localhost"));
                this.serverThread = new TaskServerThread(taskServer);
                serverThread.start();
                this.setStarted(true);
                break;
                
            case MINA_SYNC:
                // start server
                this.taskServer = new TestMinaTaskServer(taskService, port, jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_HOST, "localhost"));
                this.serverThread = new TaskServerThread(taskServer);
                serverThread.start();
                this.setStarted(true);
                break;

            case LOCAL:
                // start server
                this.setStarted(true);
                break;
            default:
                break;
            }

            TaskServiceSession session = taskService.createSession();
            // format should be user-id separated with semicolon ';', for instance user1;user2
            String usersAsString = jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_USERS);
            if (usersAsString != null) {
                String[] users = usersAsString.split(";");
                for (String user : users) {
                    session.addUser(new User(user));
                }
            }
            // format should be group-id separated with semicolon ';', for instance group1;group2
            String groupsAsString = jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_GROUPS);
            if (groupsAsString != null) {
                String[] groups = groupsAsString.split(";");
                for (String group : groups) {
                    session.addGroup(new Group(group));
                }
            }
            session.dispose();
        }
    }


    public void stop() {
        if (this.taskServer != null) {
            try {
                if (this.taskServer instanceof MinaTaskServer) {
                    ((MinaTaskServer)this.taskServer).getIoAcceptor().unbind();
                }
                this.taskServer.stop();
            } catch (Exception e) {
                // swallow exception
            }
            this.taskServer = null;
        }
        if (this.emf != null) {
            this.emf.close();
            this.emf = null;
        }
        if (this.serverThread != null) {
            try {
                this.serverThread.interrupt();
            } catch (Exception e) {
                // Swallow interruption exception
            }
            this.serverThread = null;
        }

        this.started = false;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
    public boolean isStarted() {
        return started;
    }
    
    public class TaskServerThread extends Thread {
        public TaskServerThread(Runnable runnable) {
            super(runnable);
        }

        @Override
        public void run() {
            try {
                super.run();
            } catch (Exception e) {
                // swallow exception
            }
        }
    }
    
    public class TestMinaTaskServer extends MinaTaskServer {


        public TestMinaTaskServer(TaskService service, int port,
                String localInterface) {
            super(service, port, localInterface);
        }

        @Override
        public void run() {
            try {
                super.run();
            } catch (Exception e) {
                // swallow exception
            }
        }

        @Override
        public void start() throws IOException {
            try {
                super.start();
                ((SocketSessionConfig)getIoAcceptor().getSessionConfig()).setSoLinger(0);
            } catch (Exception e) {
                //swallow exception
            }
        }
    }
    
    public TaskServiceSession getSession() {
        return this.taskService.createSession();
    }
}
