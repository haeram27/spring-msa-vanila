package com.example.springwebex.aop

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AopExam(
    val value: String = ""
)
