package com.example.springwebex.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import io.github.oshai.kotlinlogging.KotlinLogging
@Aspect
@Component
class AopExamAspectJointPoint {
    private val log = KotlinLogging.logger {}
    fun logJoinPoint(joinPoint: JoinPoint) {
        log.info { "========== JoinPoint Info ==========" }
        log.info { "Target: ${joinPoint.target}" }
        log.info { "This: ${joinPoint.`this`}" }
        log.info { "Method: ${joinPoint.signature.name}" }
        log.info { "Signature: ${joinPoint.signature}" }
        log.info { "DeclaringTypeName: ${joinPoint.signature.declaringTypeName}" }
        log.info { "SourceLocation: ${joinPoint.sourceLocation}" }
        log.info { "Kind: ${joinPoint.kind}" }
        
        val args = joinPoint.args
        if (args.isNotEmpty()) {
            for ((index, arg) in args.withIndex()) {
                log.info { "Arg[$index]: ${arg}" }
            }
        }
        log.info { "========== JoinPoint End ==========" }
    }
}
