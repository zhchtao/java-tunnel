package tools.jetcache.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import tools.jetcache.intercept.JetCacheIntercept;

/**
 * @Author taotao
 * @Date 2024/11/25 15:06
 */
@Configuration
@EnableAspectJAutoProxy
@Import({
        JetCacheBeanDefinitionRegistryPostProcessorConfig.class,
        JetCacheSpringBoot1BeanDefinitionRegistryPostProcessorConfig.class
})
public class Config {
    @Bean
    public JetCacheIntercept jetCacheIntercept() {
        return new JetCacheIntercept();
    }
}
