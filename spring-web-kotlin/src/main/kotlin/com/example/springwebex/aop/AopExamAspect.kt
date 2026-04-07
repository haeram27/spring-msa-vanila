package com.example.springwebex.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.springframework.stereotype.Component
import io.github.oshai.kotlinlogging.KotlinLogging

@Aspect
@Component
class AopExamAspect {

    private val log = KotlinLogging.logger {}

    @Pointcut("execution(* com.example.springwebex.service.AopExamService.*(..))")
    fun aopExamServicePointcut() {}

    @Pointcut("@annotation(aopExam)")
    fun annotationPointcut(aopExam: AopExam) {}

    @Before("aopExamServicePointcut()")
    fun beforeServiceMethod() {
        log.info { "@Before: AOP is working! (ServiceMethod)" }
    }

    @After("aopExamServicePointcut()")
    fun afterServiceMethod() {
        log.info { "@After: Service method completed" }
    }

    @AfterReturning("aopExamServicePointcut()")
    fun afterReturningServiceMethod() {
        log.info { "@AfterReturning: Service method returned successfully" }
    }

    @AfterThrowing("aopExamServicePointcut()")
    fun afterThrowingServiceMethod() {
        log.error { "@AfterThrowing: Service method threw an exception" }
    }

    @Around("aopExamServicePointcut()")
    fun aroundServiceMethod(joinPoint: ProceedingJoinPoint): Any? {
        log.info { "@Around: Before service method execution" }
        val startTime = System.currentTimeMillis()

        return try {
            joinPoint.proceed()
        } finally {
            val endTime = System.currentTimeMillis()
            log.info { "@Around: Service method execution time: ${endTime - startTime}ms" }
        }
    }

    @Before("annotationPointcut(aopExam)")
    fun beforeAnnotatedMethod(aopExam: AopExam) {
        log.info { "@Before Annotation: Method annotated with AopExam" }
    }

    @After("annotationPointcut(aopExam)")
    fun afterAnnotatedMethod(aopExam: AopExam) {
        log.info { "@After Annotation: Annotated method completed" }
    }

    @Around("annotationPointcut(aopExam)")
    fun aroundAnnotatedMethod(joinPoint: ProceedingJoinPoint, aopExam: AopExam): Any? {
        log.info { "@Around Annotation: Executing annotated method with value: ${aopExam.value}" }
        return try {
            joinPoint.proceed()
        } catch (e: Throwable) {
            log.error(e) { "@Around Annotation: Exception in annotated method" }
            throw e
        }
    }
}
