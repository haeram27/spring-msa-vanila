package com.example.httpclient.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringLifeCycleEventListener implements InitializingBean, DisposableBean, 
        ApplicationListener<ApplicationEvent>  {
    private final ApplicationContext applicationContext;

    /*  *** Event order ***
        @PostConstruct called
        InitializingBean.afterPropertiesSet()
        ContextRefreshedEvent
        ApplicationStartedEvent
        AvailabilityChangeEvent
        ApplicationReadyEvent
        AvailabilityChangeEvent
        ContextClosedEvent
        @PreDestroy called
        DisposableBean.destroy()
     */

    /*
     * Bean construct/destroy events
     */
    @PostConstruct
    public void logPostConstruct() {
        // called per each bean(@Component) when just after bean is created
        log.trace("PostConstruct called");
    }

    @PreDestroy
    public void logPreDestroy() {
        // called per each bean(@Component) when just before bean is removed
        log.trace("PreDestroy called");
    }

    // InitializingBean interface
    @Override
    public void afterPropertiesSet() throws Exception {
        log.trace(" InitializingBean.afterPropertiesSet()");
    }

    // DisposableBean interface
    @Override
    public void destroy() throws Exception {
        log.trace(" DisposableBean.destroy()");
    }

    /*
    * Spring Application lifecycle events
    */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.trace(" onApplicationReadyEvent()");
    }

    // same as ApplicationListener interface, springboot recommended
    @EventListener
    public void onEventListener(ApplicationEvent event) {
        log.trace(applicationContext.getApplicationName() + ": onEventListener: " + event.getClass().getSimpleName());
        if (event instanceof ApplicationReadyEvent) {
        } else if (event instanceof ContextClosedEvent) {
        } 
    }

    // ApplicationListener interface
    @SuppressWarnings("null")
    @Override
    // public void onApplicationEvent(<? extends ApplicationEvent> event) {
    public void onApplicationEvent(ApplicationEvent event) {
        log.trace(applicationContext.getApplicationName() + ": onApplicationEvent: " + event.getClass().getSimpleName());
        if (event instanceof ApplicationReadyEvent) {
        } else if (event instanceof ContextClosedEvent) {
        }
    }
}