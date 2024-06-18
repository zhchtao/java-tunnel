package tools.jetcache.utils;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Author taotao
 * @Date 2024/11/26 17:36
 */
public class JetCacheUtils {
    public static String getSpelArea(Method method) {
        String area = getArea(method);
        if (StringUtils.startsWith(area, "spel:")) {
            return StringUtils.substringAfter(area, "spel:");
        }
        return null;
    }
    public static String getArea(Method method) {
        Cached annotation = method.getAnnotation(Cached.class);
        if (annotation != null) {
            return annotation.area();
        }
        CacheUpdate cacheUpdate = method.getAnnotation(CacheUpdate.class);
        if (cacheUpdate != null) {
            return cacheUpdate.area();
        }
        CacheInvalidate cacheInvalidate = method.getAnnotation(CacheInvalidate.class);
        if (cacheInvalidate != null) {
            return cacheInvalidate.area();
        }
        return null;
    }

    public static boolean hasSpelArea(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(JetCacheUtils::getSpelArea)
                .map(StringUtils::isNotBlank)
                .filter(BooleanUtils::isTrue)
                .findFirst()
                .orElse(false);
    }

    @SneakyThrows
    public static Method getMethod(Class clazz, String name, Class... types) {
        return clazz.getDeclaredMethod(name, types);
    }
}
