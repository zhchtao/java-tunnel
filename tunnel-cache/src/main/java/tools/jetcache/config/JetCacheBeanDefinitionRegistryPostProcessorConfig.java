package tools.jetcache.config;

import com.alicp.jetcache.autoconfigure.ConfigTree;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import tools.jetcache.constants.Constants;
import tools.jetcache.utils.JetCacheUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * @Author taotao
 * @Date 2024/11/22 16:17
 *
 * 根据jetCache配置文件，自动生成对应area的cache bean
 * 适配spring boot 2.x
 */
@Configuration
@ConditionalOnExpression("!T(org.springframework.boot.SpringBootVersion).getVersion().startsWith('1.')")
@Slf4j
public class JetCacheBeanDefinitionRegistryPostProcessorConfig implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(this.getClass().getClassLoader(), false, new HashMap<>());
    private ConfigurableEnvironment environment;
    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor () {
        return beanFactory -> beanFactory.setBeanClassLoader(byteArrayClassLoader);
    }

    @SneakyThrows
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        log.info("jetCache postProcessBeanDefinitionRegistry start");
        long startTime = System.currentTimeMillis();
        Function<String, Class> classFunction = className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        };

        ConfigTree resolver = new ConfigTree(environment, "jetcache.remote.");
        Set<String> cacheAreaNames = resolver.directChildrenKeys();

        Method[] defineClass = new Method[1];

        Arrays.stream(beanDefinitionRegistry.getBeanDefinitionNames())
                .map(beanDefinitionRegistry::getBeanDefinition)
                .filter(beanDefinition -> beanDefinition instanceof AnnotatedBeanDefinition)
                .map(beanDefinition -> (AnnotatedBeanDefinition) beanDefinition)
                .filter(this::isSpelAreaCache)
                .forEach(beanDefinition -> {
                    Class<?> aClass = classFunction.apply(beanDefinition.getBeanClassName());
                    cacheAreaNames.forEach(cacheAreaName -> {

                        if (!JetCacheUtils.hasSpelArea(aClass)) {
                            return;
                        }

                        DynamicType.Builder<?> builder = getBuilder(cacheAreaName, aClass);

                        DynamicType.Unloaded<?> makeClass = builder.make();
                        Class<?> dynamicClass = makeClass
                                .load(byteArrayClassLoader)
                                .getLoaded();

                        Class<? extends ClassLoader> currentClassLoader = byteArrayClassLoader.getParent().getClass();
                        /**
                         * PandoraBootstrap 特殊处理
                         * 普通spring boot项目，只需要通过BeanFactoryPostProcessor配置自定义ClassLoader即可
                         */
                        if (StringUtils.startsWith(currentClassLoader.getName(), "com.taobao.pandora.boot.loader.")) {
                            try {
                                if (null == defineClass[0]) {
                                    defineClass[0] = JetCacheUtils.getMethod(ClassLoader.class, "defineClass", String.class, byte[].class, int.class, int.class);
                                }
                                byte[] classBytes = makeClass.getBytes();
                                defineClass[0].setAccessible(true);
                                defineClass[0].invoke(byteArrayClassLoader.getParent(), dynamicClass.getName(), classBytes, 0, classBytes.length);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(e);
                            }
                        }

                        log.info("dynamic generic cache class: {}", dynamicClass.getName());
                        registerBeanDefinition(beanDefinitionRegistry, cacheAreaName, dynamicClass);
                    });
                });

        log.info("jetCache postProcessBeanDefinitionRegistry end,{}ms", System.currentTimeMillis() - startTime);
    }

    private boolean isSpelAreaCache(AnnotatedBeanDefinition annotatedBeanDefinition) {
        AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
        return Arrays.stream(Constants.INCLUDE_ANNOTATIONS)
                .filter(annotation -> !metadata.getAnnotatedMethods(annotation.getName()).isEmpty())
                .map(o -> true)
                .findFirst()
                .orElse(false);
    }

    private DynamicType.Builder<?> getBuilder(String cacheAreaName, Class<?> aClass) {
        try {

            DynamicType.Builder<?> builder = new ByteBuddy()
                    .subclass(aClass)
                    .name(aClass.getName() + cacheAreaName)
                    .annotateType(AnnotationDescription.Builder.ofType(AutoGeneric.class).build());

            for (Method method : aClass.getDeclaredMethods()) {

                List<AnnotationDescription> annotationList = new ArrayList<>();
                for (Class annotationClass : Constants.INCLUDE_ANNOTATIONS) {
                    if (!method.isAnnotationPresent(annotationClass)) {
                        continue;
                    }
                    // 动态修改注解的 "area" 属性
                    AnnotationDescription.Builder annotationBuilder = AnnotationDescription.Builder.ofType(annotationClass);

                    Method[] methods = annotationClass.getMethods();
                    Object annotation = method.getAnnotation(annotationClass);
                    for (Method annotationMethod : methods) {
                        if (annotationMethod.getDeclaringClass() != annotationClass) {
                            continue;
                        }
                        if (annotationMethod.getName().equals("area")) {
                            annotationBuilder = annotationBuilder.define(annotationMethod.getName(), cacheAreaName);
                            continue;
                        }
                        Object invoke = annotationMethod.invoke(annotation);
                        if (invoke instanceof Enum) {
                            annotationBuilder = annotationBuilder.define(annotationMethod.getName(), (Enum<?>) invoke);
                            continue;
                        }
                        annotationBuilder = annotationBuilder.define(annotationMethod.getName(), AnnotationValue.ForConstant.of(invoke));
                    }
                    annotationList.add(annotationBuilder.build());
                }
                builder = builder.method(ElementMatchers.named(method.getName()))
                        .intercept(MethodCall.invokeSuper().withAllArguments()) // 调用原方法
                        .annotateMethod(annotationList);

            }
            return builder;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void registerBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String cacheAreaName, Class<?> dynamicClass) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(dynamicClass);
        beanDefinitionRegistry.registerBeanDefinition(dynamicClass.getSuperclass().getSimpleName() + cacheAreaName, beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    /**
     * 标记是否为自动生成的class
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AutoGeneric {
    }
}
