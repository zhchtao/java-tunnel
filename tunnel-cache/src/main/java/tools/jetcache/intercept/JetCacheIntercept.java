package tools.jetcache.intercept;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import tools.jetcache.constants.Constants;
import tools.jetcache.utils.JetCacheUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @Author taotao
 * @Date 2024/11/22 18:52
 * 根据区域选择对应的缓存实例
 */
@Aspect
@Order(0)
public class JetCacheIntercept {
    @Resource
    private Map<String, Object> beans;
    private ExpressionParser parser = new SpelExpressionParser();
    @Around(Constants.POINTCUT_AROUND)
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        String epelArea = JetCacheUtils.getSpelArea(method);
        if (StringUtils.isBlank(epelArea)) {
            return joinPoint.proceed();
        }
        // 创建 SpEL 上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        context.setVariable(Constants.ARGS_NAME, args);
        IntStream.range(0, parameterNames.length)
                .forEach(i ->
                        context.setVariable(parameterNames[i], args[i])
                );
        context.setBeanResolver((ctx, name) -> beans.get(name));

        String area = parser.parseExpression(epelArea) //NOSONAR 表达式并非用户输入，不存在安全问题
                .getValue(context, String.class);
        
        Object jetCacheService = beans.get(method.getDeclaringClass().getSimpleName() + area);
        if (null == jetCacheService) {
            throw new IllegalArgumentException("区域配置不存在,area:" + area);
        }

        return method.invoke(jetCacheService, args);
    }
}
