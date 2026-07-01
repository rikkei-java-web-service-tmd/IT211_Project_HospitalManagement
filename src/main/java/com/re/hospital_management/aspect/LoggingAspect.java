package com.re.hospital_management.aspect;

import com.re.hospital_management.entity.AuditLog;
import com.re.hospital_management.repository.AuditLogRepository;
import com.re.hospital_management.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuditLogRepository auditLogRepository;

    public LoggingAspect(@Lazy AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {
    }

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void servicePointcut() {
    }

    @Around("controllerPointcut() || servicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        boolean isController = joinPoint.getSignature().getDeclaringType()
                .isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class);

        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - start;
            if (timeTaken > 2000) {
                log.warn("Performance Warning: {}.{}() took {} ms (Slow Response)", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), timeTaken);
            } else {
                log.info("Response Time: {}.{}() took {} ms", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), timeTaken);
            }
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result);
            }

            // Save audit log to DB for controller methods only
            if (isController) {
                saveAuditLog(joinPoint, "SUCCESS", timeTaken);
            }

            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            if (isController) {
                saveAuditLog(joinPoint, "ILLEGAL_ARGUMENT", System.currentTimeMillis() - start);
            }
            throw e;
        }
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
    }

    private void saveAuditLog(ProceedingJoinPoint joinPoint, String status, long executionTimeMs) {
        try {
            Long userId = null;
            String username = "anonymous";

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                userId = userDetails.getId();
                username = userDetails.getUsername();
            }

            String httpMethod = null;
            String endpoint = null;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                httpMethod = request.getMethod();
                endpoint = request.getRequestURI();
            }

            String action = joinPoint.getSignature().getDeclaringType().getSimpleName()
                    + "." + joinPoint.getSignature().getName() + "()";

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .action(action)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .status(status)
                    .executionTimeMs(executionTimeMs)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.warn("Failed to save audit log: {}", ex.getMessage());
        }
    }
}
