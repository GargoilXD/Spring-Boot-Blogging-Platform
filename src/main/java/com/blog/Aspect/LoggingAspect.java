package com.blog.Aspect;

import org.aspectj.lang.JoinPoint;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Set<String> EXCLUDE_ARG_LOGGING = Set.of(
            "authenticate", "login", "register",
            "updatePassword", "changePassword", "validatePassword",
            "save"
    );
    private static final Set<Class<?>> SENSITIVE_TYPES = Set.of(char[].class, String.class);
    private static final Set<String> EXCLUDE_RESULT_LOGGING = Set.of(
            "authenticate", "login", "generateToken", "extractClaims",
            "validatePassword", "hashPassword"
    );
    private static final Set<String> EXPECTED_EXCEPTION_TYPES = Set.of(
            "AuthenticationException",
            "EntityNotFoundException",
            "EntityExistsException",
            "IllegalArgumentException",
            "ConstraintViolationException",
            "MethodArgumentNotValidException",
            "ResourceAccessException",
            "SecurityException"
    );
    private static final long SLOW_THRESHOLD_MS = 500;
    @Pointcut("execution(public * com.blog.Service..*.*(..))")
    public void serviceLayer() {}
    @Pointcut("execution(public * com.blog.API.Rest..*.*(..))")
    public void restLayer() {}
    @Pointcut("execution(public * com.blog.Security..*.*(..))")
    public void securityLayer() {}
    @Pointcut("execution(public * com.blog.ExceptionHandler..*.*(..))")
    public void exceptionHandlerLayer() {}
    @Pointcut("serviceLayer() || restLayer()")
    public void applicationLayer() {}

    @Around("applicationLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String className = simpleClassName(pjp);
        String methodName = pjp.getSignature().getName();
        String thread = Thread.currentThread().getName();
        boolean isAsync = isAsyncThread(thread);
        if (logger.isDebugEnabled()) {
            if (isAsync) {
                logger.debug(">>> [async:{}] {}.{}()", thread, className, methodName);
            } else {
                logger.debug(">>> {}.{}()", className, methodName);
            }
            if (!EXCLUDE_ARG_LOGGING.contains(methodName)) {
                String args = formatArgs(pjp.getArgs());
                if (!args.isEmpty()) {
                    logger.trace("    args: {}", args);
                }
            }
        }
        long startNs = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            if (logger.isDebugEnabled()) {
                String resultSummary = EXCLUDE_RESULT_LOGGING.contains(methodName) ? "[hidden]" : summarise(result);
                if (isAsync) {
                    logger.debug("<<< [async:{}] {}.{}() → {} ({}ms)", thread, className, methodName, resultSummary, elapsedMs);
                } else {
                    logger.debug("<<< {}.{}() → {} ({}ms)", className, methodName, resultSummary, elapsedMs);
                }
            }
            if (elapsedMs > SLOW_THRESHOLD_MS) {
                logger.warn("[SLOW] {}.{}() took {}ms (threshold {}ms) thread={}", className, methodName, elapsedMs, SLOW_THRESHOLD_MS, thread);
            }
            if (isAsync) {
                logger.info("[async-complete] {}.{}() finished in {}ms on thread={}", className, methodName, elapsedMs, thread);
            }
            return result;
        } catch (Throwable ex) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            logger.error("[exception] {}.{}() threw {} after {}ms [thread={}]: {}", className, methodName, ex.getClass().getSimpleName(), elapsedMs, thread, ex.getMessage(), ex);
            throw ex;
        }
    }
    @Before("securityLayer()")
    public void logSecurityEntry(JoinPoint jp) {
        if (!logger.isDebugEnabled()) return;
        logger.debug("[security] >>> {}.{}()", simpleClassName(jp), jp.getSignature().getName());
    }
    @AfterReturning(pointcut = "securityLayer()", returning = "result")
    public void logSecurityExit(JoinPoint jp, Object result) {
        if (!logger.isDebugEnabled()) return;
        String method = jp.getSignature().getName();
        String resultStr = EXCLUDE_RESULT_LOGGING.contains(method) ? "[hidden]" : summarise(result);
        logger.debug("[security] <<< {}.{}() → {}", simpleClassName(jp), method, resultStr);
    }
    @AfterThrowing(pointcut = "securityLayer()", throwing = "ex")
    public void logSecurityException(JoinPoint jp, Throwable ex) {
        logger.error("[security][exception] {}.{}(): {}", simpleClassName(jp), jp.getSignature().getName(), ex.getMessage(), ex);
    }
    @Before("exceptionHandlerLayer()")
    public void logExceptionHandled(JoinPoint jp) {
        String handler = simpleClassName(jp);
        String methodName = jp.getSignature().getName();
        for (Object arg : jp.getArgs()) {
            if (arg instanceof Throwable t) {
                String exType = t.getClass().getSimpleName();
                String msg = t.getMessage();
                if (EXPECTED_EXCEPTION_TYPES.contains(exType)) {
                    logger.warn("[exception-handler] {}.{}() handling {} – {}", handler, methodName, exType, msg);
                } else {
                    logger.error("[exception-handler] {}.{}() handling unexpected {} – {}", handler, methodName, exType, msg, t);
                }
                return;
            }
        }
        logger.debug("[exception-handler] >>> {}.{}()", handler, methodName);
    }
    @AfterReturning(pointcut = "exceptionHandlerLayer()", returning = "result")
    public void logExceptionHandlerExit(JoinPoint jp, Object result) {
        if (!logger.isDebugEnabled()) return;
        logger.debug("[exception-handler] <<< {}.{}() → {}", simpleClassName(jp), jp.getSignature().getName(), summarise(result));
    }
    private boolean isAsyncThread(String threadName) {
        return threadName.startsWith("analytics-") || threadName.startsWith("blog-async-") || threadName.startsWith("notify-");
    }
    private String simpleClassName(JoinPoint jp) {
        String full = jp.getTarget().getClass().getName();
        return full.substring(full.lastIndexOf('.') + 1);
    }
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    Class<?> type = arg.getClass();
                    if (SENSITIVE_TYPES.contains(type)) return "***MASKED***";
                    if (type.isArray() && type.getComponentType() == char.class) return "***PASSWORD***";
                    String s = arg.toString();
                    return s.length() > 120 ? s.substring(0, 120) + "…" : s;
                })
                .collect(Collectors.joining(", "));
    }
    private String summarise(Object result) {
        if (result == null) return "null";
        if (result instanceof String s)
            return s.length() > 200 ? s.substring(0, 200) + "… [truncated]" : s;
        if (result instanceof Iterable || result.getClass().isArray())
            return "***COLLECTION***";
        String s = result.toString();
        return s.length() > 200 ? s.substring(0, 200) + "… [truncated]" : s;
    }
    @Before("execution(* com.blog.Security.SecurityEventLogger.onAuthenticationSuccess(..))")
    public void logAuthSuccess(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length > 0 && args[0] instanceof AuthenticationSuccessEvent event) {
            String username = event.getAuthentication().getName();
            String roles = event.getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(java.util.stream.Collectors.joining(", "));
            logger.info("[SECURITY] LOGIN SUCCESS | user={} | roles=[{}] | time={} | type={}", username, roles, Instant.now(), event.getAuthentication().getClass().getSimpleName());
        }
    }
    @Before("execution(* com.blog.Security.SecurityEventLogger.onAuthenticationFailure(..))")
    public void logAuthFailure(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length > 0 && args[0] instanceof AbstractAuthenticationFailureEvent event) {
            String username = event.getAuthentication().getName();
            String reason   = event.getException().getMessage();
            boolean brute = event.getException().getClass().getSimpleName().contains("LockedException");
            if (brute) {
                logger.error("[SECURITY] BRUTE-FORCE / ACCOUNT LOCKED | user={} | reason={} | time={}", username, reason, Instant.now());
            } else {
                logger.warn("[SECURITY] LOGIN FAILED | user={} | reason={} | time={}", username, reason, Instant.now());
            }
        }
    }
    @Before("execution(* com.blog.Security.SecurityEventLogger.onLogout(..))")
    public void logLogout(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length > 0 && args[0] instanceof LogoutSuccessEvent event) {
            logger.info("[SECURITY] LOGOUT | user={} | time={}", event.getAuthentication().getName(), Instant.now());
        }
    }
}