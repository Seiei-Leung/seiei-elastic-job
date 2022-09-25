package top.taka.seieiElasticJob.annotation;

import org.springframework.context.annotation.Import;
import top.taka.seieiElasticJob.autoConfigure.SeieiElasticJobAutoConfiguration;

import java.lang.annotation.*;

/**
 * 声明 @EnableSeieiElasticJob 注解，表明该项目要启动定时任务
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
// @Import 注解是用来导入配置类或者一些需要前置加载的类
@Import(SeieiElasticJobAutoConfiguration.class)
// @Documented注解标记的元素，Javadoc工具会将此注解标记元素的注解信息包含在javadoc中
@Documented
// 被 @Inherited 注解修饰的注解，如果作用于某个类上，其子类是可以继承的该注解的
@Inherited
// @interface 用来声明一个注解，其中的每一个方法实际上是声明了一个配置参数
// 方法的名称就是参数的名称，返回值类型就是参数的类型（返回值类型只能是基本类型、Class、String、enum）。可以通过default来声明参数的默认值。
public @interface EnableSeieiElasticJob {
}