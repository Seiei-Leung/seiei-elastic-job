package top.taka.seieiElasticJob.annotation;

import java.lang.annotation.*;

/**
 * 声明 @SeieiElasticJobConfig 注解，声明 esjob 的作业配置
 */
// 当我们使用@interface定义一个注解时，需要确定这个注解的 生命周期 和 需要用到哪些地方
// @Target 指定注解使用的目标范围（类、方法、字段等）
/*
    @Target(ElementType.TYPE)——接口、类、枚举、注解
    @Target(ElementType.FIELD)——字段、枚举的常量
    @Target(ElementType.METHOD)——方法
    @Target(ElementType.PARAMETER)——方法参数
    @Target(ElementType.CONSTRUCTOR) ——构造函数
    @Target(ElementType.LOCAL_VARIABLE)——局部变量
    @Target(ElementType.ANNOTATION_TYPE)——注解
    @Target(ElementType.PACKAGE)——包
 */
@Target(ElementType.TYPE)
// @Retention 用来确定这个注解的生命周期
/*
    RetentionPolicy.SOURCE：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；被编译器忽略
    RetentionPolicy.CLASS：注解被保留到class文件，但 JVM 加载 class 文件时候被遗弃，这是默认的生命周期，即在class文件中存在，但 JVM 将会忽略，运行时无法获得。
    RetentionPolicy.RUNTIME：注解不仅被保存到class文件中，JVM 加载class文件之后，将被JVM保留,所以他们能在运行时被 JVM 或其他使用反射机制的代码所读取和使用。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SeieiElasticJobConfig {

    String jobName(); // 作业名称

    String cron() default ""; // cron表达式，用于控制作业触发时间

    int shardingTotalCount() default 1; // 作业分片总数

    String shardingItemParameters() default "";  // 分片序列号和参数用等号分隔，多个键值对用逗号分隔
                                                // 分片序列号从0开始，不可大于或等于作业分片总数
                                                // 如：
                                                //        0=a,1=b,2=c

    String jobParameter() default "";    // 作业自定义参数
                                        // 作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业
                                        // 例：每次获取的数据量、作业实例从数据库读取的主键等

    boolean failover() default false; // 是否开启任务执行失效转移，开启表示如果作业在一次任务执行中途宕机，允许将该次未完成的任务在另一作业节点上补偿执行

    boolean misfire() default true; // 是否开启错过任务重新执行

    String description() default  ""; // 作业描述信息

    boolean overwrite() default false; // 本地配置是否可覆盖注册中心配置

    boolean streamingProcess() default false; // 是否流式处理数据

    String scriptCommandLine() default ""; // 脚本型作业执行命令行

    boolean monitorExecution() default false; // 是否监控作业运行时状态

    int monitorPort() default -1; // 作业监控端口

    int maxTimeDiffSeconds() default -1; // 最大允许的本机与注册中心的时间误差秒数

    String jobShardingStrategyClass() default ""; // 作业分片策略实现类全路径

    int reconcileIntervalMinutes() default 10; // 修复作业服务器不一致状态服务调度间隔时间，配置为小于1的任意值表示不执行修复

    String eventTraceRdbDataSource() default ""; // 作业事件追踪的数据源Bean引用

    String listener() default "";	// 前置后置任务监听实现类，需实现 ElasticJobListener 接口

    boolean disabled() default false;	// 作业是否禁止启动

    String distributedListener() default ""; // 前置后置任务分布式监听实现类，需继承 AbstractDistributeOnceElasticJobListener 类

    long startedTimeoutMilliseconds() default Long.MAX_VALUE; // 最后一个作业执行前的执行方法的超时时间

    long completedTimeoutMilliseconds() default Long.MAX_VALUE; // 最后一个作业执行后的执行方法的超时时间

    String jobExceptionHandler() default "com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler"; // 扩展异常处理类

    String executorServiceHandler() default "com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler"; // 扩展作业处理线程池类
}
