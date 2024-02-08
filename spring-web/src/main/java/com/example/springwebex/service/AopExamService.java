package com.example.springwebex.service;

import org.springframework.stereotype.Component;
import com.example.springwebex.aop.AopExamAspectJointPoint;


@Component
public class AopExamService {
    public void methodNorm() {
        try {
            Thread.sleep(1000);
            System.out.println("joint point method - methodNorm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AopExamAspectJointPoint
    public void methodAspectDuplicated() {
        try {
            Thread.sleep(1000);
            System.out.println("joint point method - methodAspectDuplicated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void methodExcept() throws Exception {
        Thread.sleep(1000);
        System.out.println("joint point method - methodExcept");
        throw new Exception("This is Error...");
    }

}
