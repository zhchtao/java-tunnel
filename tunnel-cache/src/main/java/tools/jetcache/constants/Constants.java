package tools.jetcache.constants;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;

/**
 * @Author taotao
 * @Date 2024/11/26 17:51
 */
public class Constants {

    public static final String POINTCUT_AROUND = "!@within(tools.jetcache.config.JetCacheBeanDefinitionRegistryPostProcessorConfig.AutoGeneric) " +
            "&& (@annotation(com.alicp.jetcache.anno.Cached) || @annotation(com.alicp.jetcache.anno.CacheInvalidate) || @annotation(com.alicp.jetcache.anno.CacheUpdate))";

    public static final String ARGS_NAME = "args";

    public static final Class[] INCLUDE_ANNOTATIONS = new Class[] {
        Cached.class,
        CacheInvalidate.class,
        CacheUpdate.class
    };;
}
