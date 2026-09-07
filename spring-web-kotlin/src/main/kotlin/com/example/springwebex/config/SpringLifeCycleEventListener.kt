package com.example.springwebex.config

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import io.github.oshai.kotlinlogging.KotlinLogging

@Component
class SpringLifeCycleEventListener(private val applicationContext: ApplicationContext) :
    InitializingBean, DisposableBean, ApplicationListener<ApplicationEvent> {

    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun logPostConstruct() {
        log.trace { "PostConstruct called" }
    }

    @PreDestroy
    fun logPreDestroy() {
        log.trace { "PreDestroy called" }
    }

    override fun afterPropertiesSet() {
        log.trace { " InitializingBean.afterPropertiesSet()" }
    }

    override fun destroy() {
        log.trace { " DisposableBean.destroy()" }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
        log.trace { " onApplicationReadyEvent()" }
    }

    @EventListener
    fun onEventListener(event: ApplicationEvent) {
        log.trace { applicationContext.applicationName + ": onEventListener: " + event::class.simpleName }
    }

    override fun onApplicationEvent(event: ApplicationEvent) {
        log.trace { applicationContext.applicationName + ": onApplicationEvent: " + event::class.simpleName }
    }
}
