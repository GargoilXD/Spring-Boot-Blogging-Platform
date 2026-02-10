package com.blog.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Set<Class<?>> SENSITIVE_TYPES = Set.of(char[].class,String.class);
    private static final Set<String> EXCLUDE_ARG_LOGGING = Set.of(
        "authenticate","login",
        "register","updatePassword",
        "changePassword","validatePassword",
        "save"
    );
    
    @Pointcut("execution(* com.blog.Service..*.*(..))")
    public void serviceLayer() {}
    
    @Pointcut("execution(* com.blog.Controller..*.*(..))")
    public void controllerLayer() {}
    
    @Pointcut("serviceLayer() || controllerLayer()")
    public void applicationLayer() {}

    @Before("applicationLayer()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String className = getSimpleClassName(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        logger.debug("Entering: {}.{}", className, methodName);
        if (!EXCLUDE_ARG_LOGGING.contains(methodName.toLowerCase())) {
            String argsStr = maskSensitiveArgs(joinPoint.getArgs(), joinPoint);
            if (!argsStr.isEmpty()) {
                logger.trace("  Args: {}", argsStr);
            }
        }
    }
    
    @AfterReturning(pointcut = "applicationLayer()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        String className = getSimpleClassName(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        if (EXCLUDE_ARG_LOGGING.contains(methodName.toLowerCase())) {
            logger.debug("Exiting: {}.{}() [result hidden]", className, methodName);
            return;
        }
        String resultStr;
        while (true) {
            if (result == null){
                resultStr = "null";
                break;
            }
            if (result instanceof String && ((String) result).length() > 200) {
                resultStr = ((String) result).substring(0, 200) + "... [truncated]";
                break;
            }
            if (result.getClass().isArray() || result instanceof Iterable) {
                resultStr = "***COLLECTION***";
                break;
            }
            resultStr = result.toString();
            break;
        }
        logger.debug("Exiting: {}.{}() -> {}", className, methodName, resultStr);
    }
    @AfterThrowing(pointcut = "applicationLayer()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Throwable exception) {
        String className = getSimpleClassName(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        logger.error("Exception in {}.{}(): {}", className, methodName, exception.getMessage(), exception);
    }

    private String getSimpleClassName(JoinPoint joinPoint) {
        String fullClassName = joinPoint.getTarget().getClass().getName();
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }
    private String maskSensitiveArgs(Object[] args, JoinPoint joinPoint) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) return "null";
                if (SENSITIVE_TYPES.contains(arg.getClass())) return "***MASKED***";
                if (arg.getClass().isArray() && arg.getClass().getComponentType() == char.class) {
                    return "***PASSWORD***";
                }
                String objStr = arg.toString();
                if (objStr.length() > 100) {
                    return objStr.substring(0, 100) + "... [truncated]";
                }
                return objStr;
            })
            .collect(Collectors.joining(", "));
    }
}