package top.taka.seieiElasticJob.autoConfigure;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 设置在 spring.factories 的自动装配类
 */
@Configuration
/**
 * 当配置文件中包含 elastic,job.zk.namespace 和 elastic,job.zk.serverLists 才会加载该配置类
 */
@ConditionalOnProperty(prefix = "elastic.job.zk", name = {"namespace", "serverLists"}, matchIfMissing = false)
// 扫描配置信息 Bean 和 注释解析器 SeieiElasticJobConfigParser
@ComponentScan({"top.taka.seieiElasticJob.*"})
public class SeieiElasticJobAutoConfiguration {

    final static Logger logger = LoggerFactory.getLogger(SeieiElasticJobAutoConfiguration.class);

    @Autowired
    private SeieiElasticJobProperties seieiElasticJobProperties;

    /**
     * Zookeeper 注册中心
     * @return
     */
    // @Component 用于自动检测， Spring 使用类路径扫描，从而自动装配 bean
    // @Bean 则纯粹是声明性的，而不是让 Spring 像上面那样扫描从而自动装配它，
    // 它将 bean 的声明与类定义分离，并允许您精确地创建和配置（即它可以作为一个自定义方法返回值，而不是独立的一个定义类）
    // 所以 @bean 则常和 @Configuration 注解搭配使用
    // 它的使用场景就是：如果想将第三方的类变成组件，你又没有没有源代码，也就没办法使用 @Component 进行自动配置，这种时候使用 @Bean 就比较合适了
    // 如下：
    @Bean
    public ZookeeperRegistryCenter zookeeperRegistryCenter() {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(seieiElasticJobProperties.getServerLists(), seieiElasticJobProperties.getNamespace());
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(seieiElasticJobProperties.getBaseSleepTimeMilliseconds());
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(seieiElasticJobProperties.getMaxSleepTimeMilliseconds());
        zookeeperConfiguration.setMaxRetries(seieiElasticJobProperties.getMaxRetries());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(seieiElasticJobProperties.getConnectionTimeoutMilliseconds());
        zookeeperConfiguration.setSessionTimeoutMilliseconds(seieiElasticJobProperties.getSessionTimeoutMilliseconds());
        ZookeeperRegistryCenter zookeeperRegistryCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zookeeperRegistryCenter.init();
        logger.info("初始化 Zookeeper 注册中心成功！ServerLists：{}，Namespace：{}", seieiElasticJobProperties.getServerLists(), seieiElasticJobProperties.getNamespace());
        return zookeeperRegistryCenter;
    }


}
