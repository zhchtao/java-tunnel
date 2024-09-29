package tools.jetcache.config;

import lombok.SneakyThrows;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.util.ClassUtils;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author taotao
 * @Date 2024/11/22 16:17
 * 适配spring boot 1.x
 */
@Configuration
@ConditionalOnExpression("T(org.springframework.boot.SpringBootVersion).getVersion().startsWith('1.')")
public class JetCacheSpringBoot1BeanDefinitionRegistryPostProcessorConfig extends JetCacheBeanDefinitionRegistryPostProcessorConfig {

    private Map<Class<?>, Map<Member, String[]>> parameterNamesCache;

    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Class<?> spelEvaluatorClass = Class.forName("com.alicp.jetcache.anno.method.SpelEvaluator");
        Field field = spelEvaluatorClass.getDeclaredField("parameterNameDiscoverer");
        field.setAccessible(true);
        DefaultParameterNameDiscoverer defaultParameterNameDiscoverer = (DefaultParameterNameDiscoverer) field.get(null);

        field = PrioritizedParameterNameDiscoverer.class.getDeclaredField("parameterNameDiscoverers");
        field.setAccessible(true);
        LocalVariableTableParameterNameDiscoverer nameDiscoverer = ((List<ParameterNameDiscoverer>)field.get(defaultParameterNameDiscoverer))
                .stream()
                .filter(o -> o instanceof LocalVariableTableParameterNameDiscoverer)
                .map(o -> (LocalVariableTableParameterNameDiscoverer) o)
                .findFirst()
                .orElse(null);

        field = LocalVariableTableParameterNameDiscoverer.class.getDeclaredField("parameterNamesCache");
        field.setAccessible(true);
        parameterNamesCache = (Map<Class<?>, Map<Member, String[]>>) field.get(nameDiscoverer);

        super.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
    }

    @SneakyThrows
    @Override
    protected void registerBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String cacheAreaName, Class<?> dynamicClass) {
        super.registerBeanDefinition(beanDefinitionRegistry, cacheAreaName, dynamicClass);

        try (InputStream is = dynamicClass.getResourceAsStream(ClassUtils.getClassFileName(dynamicClass.getSuperclass()))){

            ClassReader classReader = new ClassReader(is);
            Map<Member, String[]> memberMap = new ConcurrentHashMap<>();
            Constructor<?> constructor = Class.forName("org.springframework.core.LocalVariableTableParameterNameDiscoverer$ParameterNameDiscoveringVisitor")
                    .getConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            classReader.accept((ClassVisitor) constructor.newInstance(dynamicClass, memberMap), 0);
            parameterNamesCache.put(dynamicClass, memberMap);
        }
    }
}
