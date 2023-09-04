package com.example.apicallserver.async

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
    
    @Bean
    @Primary
    fun cherryTaskExecutor(): Executor {
        return ThreadPoolTaskExecutor()
            .apply {
                corePoolSize = 1
                maxPoolSize = 1
                queueCapacity = 10
                setThreadNamePrefix("taskExecutor")
            }
    }
}
