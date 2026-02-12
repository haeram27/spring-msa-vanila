package com.example.springwebex.aspectj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


/*
 * AOP Advice is annotation as @Before, @After, @AfterRunning, @AfterThrowing, @Around.
 * Adivce tagged method is called by AOP module when Adviced timming around specific method
 * Advice uses Pointcut, PackagePattern, Jointpoint as a parameter describe target method
 * 
 * Pointcut is made with target's package path and pointcut can be referred by multiple advice easily.
 * PackagePattern is package path pattern of method
 * JointPoint is user-defined annotation tags to target method
 *  
 * @Aspect Declare this class is Aspect configuration
 */

@Component
@Aspect
public class AopExamAspect {

    /*
     * declare pointcut
     */
    @Pointcut("execution(* com.example..service.AopExamService.*(..))")
    public void executionTestPointCut() {};

    @Pointcut("within(com.example..service.AopExamService)")
    public void withinTestPointCut() {};


    /*
     * advice uses pointcut signature
     */
    @Before(value = "executionTestPointCut()")
    public void signaturePointcutBeforeAdvice() {
        System.out.println("signaturePointcutBeforeAdvice");
    }

    @After(value = "withinTestPointCut())")
    public void signaturePointcutAfterAdvice() {
        System.out.println("signaturePointcutAfterAdvice");
    }


    /*
     * advice uses pointcut pattern directly
     */
    @Before(value = "within(com.example..service.AopExamService)")
    public void patternPointcutBeforeAdvice() {
        System.out.println("patternPointcutBeforeAdvice");
    }

    @After(value = "execution(* com.example..service.AopExamService.*(..))")
    public void patternPointcutAfterAdvice() {
        System.out.println("patternPointcutAfterAdvice");
    }

    @AfterReturning(pointcut = "within(com.example..service.AopExamService)", returning = "retVal")
    public void patternPointcutAfterReturningAdvice(Object retVal) {
        System.out.println("patternPointcutAfterReturningAdvice");
    }

    @AfterThrowing(pointcut = "execution(* com.example..service.AopExamService.*(..))",
            throwing = "ex")
    public void patternPointcutAfterThrowingAdvice(Exception ex) {
        System.out.println("patternPointcutAfterThrowingAdvice");
    }

    @Around(value = "execution(* com.example..service.AopExamService.*(..))")
    public Object patternPointcutAroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("patternPointcutAroundAdvice Begin");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object retVal = pjp.proceed();
        stopWatch.stop();
        System.out.println("patternPointcutAroundAdvice::" + stopWatch.shortSummary());
        System.out.println("patternPointcutAroundAdvice End");
        return retVal;
    }


    /*
     * advice uses annotation pointcut (ex. AopExamAspectJointPoint)
     */
    @Before(value = "@annotation(AopExamAspectJointPoint)")
    public void annotationPointcutBeforeAdvice() {
        System.out.println("annotationPointcutBeforeAdvice");
    }

    @After(value = "@annotation(AopExamAspectJointPoint)")
    public void annotationPointcutAfterAdvice() {
        System.out.println("annotationPointcutAfterAdvice");
    }

    @AfterReturning(pointcut = "@annotation(AopExamAspectJointPoint)", returning = "retVal")
    public void annotationPointcutAfterReturningAdvice(Object retVal) {
        System.out.println("annotationPointcutAfterReturningAdvice");
    }

    @AfterThrowing(pointcut = "@annotation(AopExamAspectJointPoint)", throwing = "ex")
    public void annotationPointcutAfterThrowingAdvice(Exception ex) {
        System.out.println("annotationPointcutAfterThrowingAdvice");
    }

    @Around(value = "@annotation(AopExamAspectJointPoint)")
    public Object annotationPointcutAroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("annotationPointcutAroundAdvice Begin");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object retVal = pjp.proceed();
        stopWatch.stop();
        System.out.println("annotationPointcutAroundAdvice::" + stopWatch.shortSummary());
        System.out.println("annotationPointcutAroundAdvice End");
        return retVal;
    }
}
