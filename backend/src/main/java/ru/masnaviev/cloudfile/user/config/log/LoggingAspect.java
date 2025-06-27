package ru.masnaviev.cloudfile.user.config.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudfile.user.dto.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Slf4j
@Component
class LoggingAspect {
    @Pointcut("execution(* ru.masnaviev.cloudfile.user.controller.*.*(..))")
    private void controllerMethods() {
    }

    @Around(value = "controllerMethods()")
    private Object logAroundControllers(ProceedingJoinPoint joinPoint) throws Throwable {
        String args = hideSensitiveData(joinPoint.getArgs());
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("Start: {}.{} with args: {}", className, methodName, args);

        try {
            Object result = joinPoint.proceed();
            log.info("Success: {}.{}", className, methodName);
            return result;
        } catch (Throwable ex) {
            log.error("Error: {}.{} with args {} | Exception = {}", className, methodName, args, ex.getMessage());
            throw ex;
        }
    }

    private String hideSensitiveData(Object[] args) {
        return Arrays.stream(args).map(arg -> {
            if (arg instanceof UserRegistrationRequest request) {
                return "UserRegistrationRequest[username=" + request.username() + ", password=***]";
            }
            if (arg instanceof UserAuthorizationRequest request) {
                return "UserAuthorizationRequest[username=" + request.username() + ", password=***]";
            }
            return arg != null ? arg.toString() : "null";
        }).collect(Collectors.joining(", "));
    }
}
