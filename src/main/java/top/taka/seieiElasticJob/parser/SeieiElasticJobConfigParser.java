package top.taka.seieiElasticJob.parser;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.taka.seieiElasticJob.annotation.SeieiElasticJobConfig;
import top.taka.seieiElasticJob.enums.ElasticJobType;

import java.util.List;
import java.util.Map;

/**
 * @EnableSeieiElasticJob 和 @SeieiElasticJobConfig 注解解析器
 * ApplicationListener<ApplicationReadyEvent> 接口的 onApplicationEvent 方法是，spring 容器所有 bean 组件加载初始化完成之后的生命周期接口
 */
// 需要添加 @Component 注解注入到 Spirng 中
@Component
public class SeieiElasticJobConfigParser implements ApplicationListener<ApplicationReadyEvent> {

    private final static Logger logger = LoggerFactory.getLogger(SeieiElasticJobConfigParser.class);

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     * ApplicationListener<ApplicationReadyEvent> 接口的 onApplicationEvent 方法是，spring 容器所有 bean 组件加载初始化完成之后的生命周期接口
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        try {
            // Spring 应用容器
            ApplicationContext applicationContext = event.getApplicationContext();
            // 获取所有标记了 @SeieiElasticJobConfig 注解的组件类列表
            Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(SeieiElasticJobConfig.class);
            // 组件列表
            for (Object configBean : beanMap.values()) {
                // 获取组件的 Class
                Class<?> clazz = configBean.getClass();
                String className = clazz.getName();
                // 确保获取到的 Class 是正确的，以防一些内部类，内部类使用 getName 方法获取出来的名称带有 $ 符号
                // 意义不大，因为正常来讲是添加在主类上，由主类实现诸如 SimpleJob 接口，而非注册到内部类中
                // 如果用户注册到内部类中，而该主类又没有实现 SimpleJob 接口，那么这样获取这个 Class 也没用
                if (className.indexOf("$") != -1) {
                    className = clazz.getName().substring(0, clazz.getName().indexOf("$"));
                    clazz = Class.forName(className);
                }
                // 获取该类的所有接口类型列表，判断该作业实现是什么类型的任务
                String jobType = "";
                Class<?>[] interfaces = clazz.getInterfaces();
                // 循环接口列表
                for (Class<?> interfaceItem : interfaces) {
                    // 获取
                    if (interfaceItem.getSimpleName().equals(ElasticJobType.SIMPLE.value)) {
                        jobType = ElasticJobType.SIMPLE.value;
                        break;
                    }
                    if (interfaceItem.getSimpleName().equals(ElasticJobType.DATAFLOW.value)) {
                        jobType = ElasticJobType.DATAFLOW.value;
                        break;
                    }
                    if (interfaceItem.getSimpleName().equals(ElasticJobType.SCRIPT.value)) {
                        jobType = ElasticJobType.SCRIPT.value;
                        break;
                    }
                }
                // 获取注解（配置项注解）
                SeieiElasticJobConfig seieiElasticJobConfig = clazz.getAnnotation(SeieiElasticJobConfig.class);
                // 获取在该注解声明的参数
                String jobName = seieiElasticJobConfig.jobName();
                String cron = seieiElasticJobConfig.cron();
                int shardingTotalCount = seieiElasticJobConfig.shardingTotalCount();
                String shardingItemParameters = seieiElasticJobConfig.shardingItemParameters();
                String jobParameter = seieiElasticJobConfig.jobParameter();
                boolean failover = seieiElasticJobConfig.failover();
                boolean misfire = seieiElasticJobConfig.misfire();
                String description = seieiElasticJobConfig.description();
                boolean overwrite = seieiElasticJobConfig.overwrite();
                boolean streamingProcess = seieiElasticJobConfig.streamingProcess();
                String scriptCommandLine = seieiElasticJobConfig.scriptCommandLine();
                boolean monitorExecution = seieiElasticJobConfig.monitorExecution();
                int monitorPort = seieiElasticJobConfig.monitorPort();
                int maxTimeDiffSeconds = seieiElasticJobConfig.maxTimeDiffSeconds();
                String jobShardingStrategyClass = seieiElasticJobConfig.jobShardingStrategyClass();
                int reconcileIntervalMinutes = seieiElasticJobConfig.reconcileIntervalMinutes();
                String eventTraceRdbDataSource = seieiElasticJobConfig.eventTraceRdbDataSource();
                boolean disabled = seieiElasticJobConfig.disabled();
                String jobExceptionHandler = seieiElasticJobConfig.jobExceptionHandler();
                String executorServiceHandler = seieiElasticJobConfig.executorServiceHandler();

                // 定义 JobCoreConfiguration(作业核心配置)
                JobCoreConfiguration jobCoreConfig = JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                        .shardingItemParameters(shardingItemParameters)
                        .jobParameter(jobParameter)
                        .failover(failover)
                        .misfire(misfire)
                        .description(description)
                        .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandler)
                        .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandler)
                        .build();

                // 根据 Job 类型不同，创建不同的任务
                JobTypeConfiguration jobTypeConfiguration = null;
                // 简单任务
                if (jobType.equals(ElasticJobType.SIMPLE.value)) {
                    jobTypeConfiguration = new SimpleJobConfiguration(jobCoreConfig, clazz.getCanonicalName());
                }
                // 流任务
                else if (jobType.equals(ElasticJobType.DATAFLOW.value)) {
                    jobTypeConfiguration = new DataflowJobConfiguration(jobCoreConfig, clazz.getCanonicalName(), streamingProcess);
                }
                // 脚本任务
                else if (jobType.equals(ElasticJobType.SCRIPT.value)) {
                    jobTypeConfiguration = new ScriptJobConfiguration(jobCoreConfig, scriptCommandLine);
                }

                // 配置 LiteJobConfiguration
                LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration)
                        .overwrite(overwrite)
                        .disabled(disabled)
                        .monitorPort(monitorPort)
                        .monitorExecution(monitorExecution)
                        .maxTimeDiffSeconds(maxTimeDiffSeconds)
                        .jobShardingStrategyClass(jobShardingStrategyClass)
                        .reconcileIntervalMinutes(reconcileIntervalMinutes)
                        .build();

                /* 构建 JobScheduler 配置，并注入到 spring 容器 */

                // 创建一个 spring 的 BeanDefinitionBuilder
                BeanDefinitionBuilder factory = BeanDefinitionBuilder.genericBeanDefinition(JobScheduler.class);
                // 模式：多例 取消了也没有影响，但教程就有说要使用多例模式
                //factory.setScope("prototype");

                /* 填充构造器参数 */
                // 填充 Zookeeper 注册中心
                factory.addConstructorArgValue(zookeeperRegistryCenter);
                // 填充 作业配置
                factory.addConstructorArgValue(liteJobConfiguration);
                // 填充 日志数据库事件溯源配置
                if (StringUtils.isNotBlank(eventTraceRdbDataSource)) {
                    BeanDefinitionBuilder rdbFactory = BeanDefinitionBuilder.genericBeanDefinition(JobEventRdbConfiguration.class);
                    //rdbFactory.setScope("prototype");
                    // 填充构造器参数
                    // addConstructorArgReference，根据 BeanName 获取 Spring 容器里的 Bean 作为参数
                    rdbFactory.addConstructorArgReference(eventTraceRdbDataSource);
                    // 将 日志数据库事件溯源配置 填充到上述 factory 的构造器参数中
                    factory.addConstructorArgValue(rdbFactory.getBeanDefinition());
                }
                // 添加监听
                List<BeanDefinition> elasticJobListeners = getTargetElasticJobListeners(seieiElasticJobConfig);
                factory.addConstructorArgValue(elasticJobListeners);

                /* 注入到 Spring 容器中 */
                // 获取注册类
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
                // 创建 bean 名
                String registerBeanName = seieiElasticJobConfig.jobName() + "SpringJobScheduler";
                // 注册到容器中
                defaultListableBeanFactory.registerBeanDefinition(registerBeanName, factory.getBeanDefinition());
                // 获取注册到容器的 JobScheduler 实例，并调用 init 方法
                JobScheduler jobScheduler = (JobScheduler) applicationContext.getBean(registerBeanName);
                jobScheduler.init();
                logger.info("启动seiei-elastic-job作业: " + jobName);
            }
            logger.info("共计启动elastic-job作业数量为: {} 个", beanMap.values().size());
        } catch (ClassNotFoundException e) {
            logger.error("elasticjob 启动异常, 系统强制退出", e);
            System.exit(1);
        }
    }

    /**
     * 获取监听 BeanDefinition
     * @param config 配置信息
     * @return
     */
    private List<BeanDefinition> getTargetElasticJobListeners(SeieiElasticJobConfig config) {
        // ManagedList 存储管理运行中的 bean 引用（用于解析为bean对象）
        // 直接使用 ArrayList 会报错
        List<BeanDefinition> result = new ManagedList<BeanDefinition>();
        // 普通监听
        if (StringUtils.isNotBlank(config.listener())) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(config.listener());
            //beanDefinitionBuilder.setScope("prototype");
            result.add(beanDefinitionBuilder.getBeanDefinition());
        }
        // 分布式监听
        else if (StringUtils.isNotBlank(config.distributedListener())) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(config.distributedListener());
            long startedTimeoutMilliseconds = config.startedTimeoutMilliseconds();
            long completedTimeoutMilliseconds = config.completedTimeoutMilliseconds();
            beanDefinitionBuilder.addConstructorArgValue(Long.valueOf(startedTimeoutMilliseconds));
            beanDefinitionBuilder.addConstructorArgValue(Long.valueOf(completedTimeoutMilliseconds));
            //beanDefinitionBuilder.setScope("prototype");
            result.add(beanDefinitionBuilder.getBeanDefinition());
        }
        return result;
    }

}
