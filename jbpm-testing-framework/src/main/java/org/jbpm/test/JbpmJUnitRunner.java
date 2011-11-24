package org.jbpm.test;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.test.annotation.HumanTaskSupport;
import org.jbpm.test.annotation.KnowledgeSession;
import org.jbpm.test.annotation.LifeCycle;
import org.junit.Ignore;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class JbpmJUnitRunner extends BlockJUnit4ClassRunner {

	protected static ConcurrentHashMap<String, KnowledgeBase> kbCache = new ConcurrentHashMap<String, KnowledgeBase>();
	protected static HumanTaskService taskService;
	
	protected static Properties jbpmTestConfiguration;
	static {
		// setup data source
		try {
			if (JbpmJUnitRunner.class.getResource("/jbpm-test.properties") != null) {
				jbpmTestConfiguration = new Properties();
				jbpmTestConfiguration.load(JbpmJUnitRunner.class.getResourceAsStream("/jbpm-test.properties"));
				
			}
			
			if (JbpmJUnitRunner.class.getResource("/datasource.properties") != null) {
				Properties props = new Properties();
				props.load(JbpmJUnitRunner.class.getResourceAsStream("/datasource.properties"));
				
				PoolingDataSource ds = new PoolingDataSource();
				ds.setUniqueName(props.getProperty("uniqueName"));
				ds.setClassName(props.getProperty("className"));
				ds.setMaxPoolSize(Integer.valueOf(props.getProperty("maxPoolSize")));
				ds.setAllowLocalTransactions(Boolean.valueOf(props.getProperty("allowLocalTransactions")));
				ds.getDriverProperties().put("user", props.getProperty("user"));
				ds.getDriverProperties().put("password", props.getProperty("password"));
				ds.getDriverProperties().put("URL", props.getProperty("URL"));
				ds.init();
			}
		} catch (Exception e) {
			System.err.println("Error when setting up connection pool " + e.getMessage());
		}
	}
	
	private Object target = null;
	private Context context = null;
	private String currentTestName = null;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JbpmJUnitRunner(Class testClass) throws InitializationError {
		super(testClass);
		this.context = new Context();
		this.context.setTestClass(testClass);
		if (testClass.isAnnotationPresent(KnowledgeSession.class)) {
			this.context.setkBaseDI(ConfigurationHelper.buildKnowledgeBase(testClass));
			// setup human task service if configured
			if (taskService == null && testClass.isAnnotationPresent(HumanTaskSupport.class)) {
				taskService = new HumanTaskService();
				taskService.start((HumanTaskSupport) testClass.getAnnotation(HumanTaskSupport.class));
			}
		} else {
			this.context.setkBaseDI(ConfigurationHelper.buildKnowledgeBase(jbpmTestConfiguration));
			// setup human task service if configured
			if (taskService == null && jbpmTestConfiguration.containsKey(ConfigurationHelper.HUMAN_TASK_PERSISTENCE_UNIT)) {
				taskService = new HumanTaskService();
				taskService.start(jbpmTestConfiguration);
			}
		}
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		LifeCycle lifeCycle = null;
		if (method.getAnnotation(Ignore.class) == null) {
			currentTestName = testName(method);
			lifeCycle = method.getMethod().getAnnotation(LifeCycle.class);
			if (lifeCycle != null) {
				this.context.setPhases(lifeCycle.phases());
			}
			
		}
		super.runChild(method, notifier);
		if (method.getAnnotation(Ignore.class) == null) {
			if (lifeCycle != null) {
				this.context.getTaskClient().setPhases(null);
				this.context.setPhases(null);
			}
			if (this.context.isDisposeSessionPerTest()) {
				if (this.context.getLogger() != null) {
					this.context.getLogger().dispose();
				}
				if (this.context.getLog() != null) {
					this.context.getLog().close();
				}
				this.context.getSession().dispose();
				this.context.setSession(null);
			}
			if (this.context.getTaskClient() != null) {
				try {
					this.context.getTaskClient().disconnect();
				} catch (Exception e) {
				}
			}
		}
	}
	
	

	@Override
	public void run(RunNotifier notifier) {
		if (this.context.getTestClass().isAnnotationPresent(KnowledgeSession.class)) {
			if (taskService == null && this.context.getTestClass().isAnnotationPresent(HumanTaskSupport.class)) {
				taskService = new HumanTaskService();
				taskService.start((HumanTaskSupport) this.context.getTestClass().getAnnotation(HumanTaskSupport.class));
			}
		} else {
			// setup human task service if configured
			if (taskService == null && jbpmTestConfiguration.containsKey(ConfigurationHelper.HUMAN_TASK_PERSISTENCE_UNIT)) {
				taskService = new HumanTaskService();
				taskService.start(jbpmTestConfiguration);
			}
		}
		
		super.run(notifier);
		if (this.context.getEmf() != null) {
			this.context.getEmf().close();
			this.context.setEmf(null);
		}
		if (taskService != null) {
			taskService.stop();
			taskService = null;
		}
	}

	@Override
	protected Object createTest() throws Exception {
		target = super.createTest();
		// perform dependency injection
		Field[] fields = target.getClass().getDeclaredFields();
		boolean humanTaskRequired = false;
		boolean humanTaskLocal = false;
		if (target.getClass().isAnnotationPresent(KnowledgeSession.class)) {
			humanTaskRequired = target.getClass().isAnnotationPresent(HumanTaskSupport.class);
			if (humanTaskRequired) {
			    humanTaskLocal = TaskServerType.LOCAL.equals(target.getClass().getAnnotation(HumanTaskSupport.class).type()) ? true : false;
			}
		} else {
			humanTaskRequired = jbpmTestConfiguration.containsKey(ConfigurationHelper.HUMAN_TASK_PERSISTENCE_UNIT);
			if (humanTaskRequired) {
                humanTaskLocal = TaskServerType.LOCAL.equals(TaskServerType.valueOf(jbpmTestConfiguration.getProperty(ConfigurationHelper.HUMAN_TASK_TYPE, "MINA_ASYNC"))) ? true : false;
            }
		}
		for (Field f : fields) {
			if (KnowledgeBase.class.isAssignableFrom(f.getType())) {
				f.setAccessible(true);
				try {
					f.set(target, this.context.getkBaseDI());
				} catch (Exception e) {
					System.err.println("Not possible to set KnowledgeBase, " + e.getMessage());
				}
				continue;
			}
			if(StatefulKnowledgeSession.class.isAssignableFrom(f.getType())) {
				
				f.setAccessible(true);
				try {
					StatefulKnowledgeSession session = null;
					if (target.getClass().isAnnotationPresent(KnowledgeSession.class)) {
						session = ConfigurationHelper.getSession(target.getClass(), currentTestName, context);
					} else {
						session = ConfigurationHelper.getSession(jbpmTestConfiguration, currentTestName, target.getClass(), context);
					}
					f.set(target, session);
				} catch (Exception e) {
					System.err.println("Not possible to set StatefulKnowledgeSession, " + e.getMessage());
				}
				continue;
			}
			
			if(TestTaskClient.class.isAssignableFrom(f.getType()) && humanTaskRequired) {
				
				f.setAccessible(true);
				try {
					TestTaskClient taskClient = null;
					if (target.getClass().isAnnotationPresent(KnowledgeSession.class)) {
						taskClient = ConfigurationHelper.buildTaskClient(target.getClass());
					} else {
						taskClient = ConfigurationHelper.buildTaskClient(jbpmTestConfiguration);
					}
					this.context.setTaskClient(taskClient);
					this.context.getTaskClient().setPhases(this.context.getPhases());
					f.set(target, this.context.getTaskClient());
				} catch (Exception e) {
					System.err.println("Not possible to set TaskClient, " + e.getMessage());
				}
				continue;
			}
		}
		// FIXME this probably should be changed but so far cannot be done since default constructor will
		// not be sufficient of SyncWSHumanTaskHandler
		if (humanTaskRequired && humanTaskLocal) {
		    this.context.getSession().getWorkItemManager().registerWorkItemHandler("Human Task", new SyncWSHumanTaskHandler(this.context.getTaskClient(), this.context.getSession()));
		}
		return target;
	}
}
