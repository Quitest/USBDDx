package ru.pel.usbdda.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pel.usbdda.controller.SystemInfoController;

@Aspect
@Component
public class SystemInfoControllerAspect {
    private Logger logger = LoggerFactory.getLogger(SystemInfoController.class);

    @Before("postSystemInfo()")
    public void newPostInfo() {
        logger.info("Прилетел новый systemInfo");
    }

    @AfterReturning("postSystemInfo()")
    public void postDoneInfo(JoinPoint jp) {
        logger.info("SystemInfo сохранен: {}", jp.getArgs());
    }

    @Pointcut("execution(* ru.pel.usbdda.controller.SystemInfoController.postSystemInfo(..))")
    public void postSystemInfo() {
    }
}
