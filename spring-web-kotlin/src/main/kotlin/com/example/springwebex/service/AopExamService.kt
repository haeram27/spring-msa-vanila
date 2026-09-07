package com.example.springwebex.service

import org.springframework.stereotype.Component
import com.example.springwebex.aop.AopExam

@Component
class AopExamService {

    fun methodNorm() {
        try {
            Thread.sleep(1000)
            println("joint point method - methodNorm")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @AopExam
    fun methodAspectDuplicated() {
        try {
            Thread.sleep(1000)
            println("joint point method - methodAspectDuplicated")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun methodExcept() {
        try {
            Thread.sleep(1000)
            println("joint point method - methodExcept")
            throw Exception("This is Error...")
        } catch (e: Exception) {
            throw e
        }
    }
}
