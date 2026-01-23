package com.example.empty.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SpringLifeCycleEventListener implements InitializingBean, DisposableBean, 
        ApplicationListener<ApplicationEvent>  {
    private final ApplicationContext applicationContext;

    @Autowired
    public SpringLifeCycleEventListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

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
        log.debug("PostConstruct called");
    }

    @PreDestroy
    public void logPreDestroy() {
        // called per each bean(@Component) when just before bean is removed
        log.debug("PreDestroy called");
    }

    // InitializingBean interface
    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug(" InitializingBean.afterPropertiesSet()");
    }

    // DisposableBean interface
    @Override
    public void destroy() throws Exception {
        log.debug(" DisposableBean.destroy()");
    }

    /*
    * Spring Application lifecycle events
    */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.debug(" onApplicationReadyEvent()");
    }

    // same as ApplicationListener interface, springboot recommended
    @EventListener
    public void onEventListener(ApplicationEvent event) {
        log.debug(applicationContext.getApplicationName() + ": onEventListener: " + event.getClass().getSimpleName());
        if (event instanceof ApplicationReadyEvent) {
        } else if (event instanceof ContextClosedEvent) {
        } 
    }

    // ApplicationListener interface
    @SuppressWarnings("null")
    @Override
    // public void onApplicationEvent(<? extends ApplicationEvent> event) {
    public void onApplicationEvent(ApplicationEvent event) {
        log.debug(applicationContext.getApplicationName() + ": onApplicationEvent: " + event.getClass().getSimpleName());
        if (event instanceof ApplicationReadyEvent) {
        } else if (event instanceof ContextClosedEvent) {
        }
    }
}