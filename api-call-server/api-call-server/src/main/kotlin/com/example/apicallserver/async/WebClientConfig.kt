package com.example.apicallserver.async

import com.example.apicallserver.controller.WebClientTestController
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.resources.LoopResources
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {

    companion object {
        val THREADS = 1
        val THREADFACTORY: BasicThreadFactory = BasicThreadFactory.Builder()
            .namingPattern("HttpThread-%d")
            .daemon(true)
            .priority(Thread.MAX_PRIORITY)
            .build()

        val EXECUTOR = ThreadPoolExecutor(
            THREADS,
            THREADS,
            0L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(),
            THREADFACTORY,
            ThreadPoolExecutor.AbortPolicy()
        )

        val RESOURCE = NioEventLoopGroup(THREADS, EXECUTOR)
    }

    @Bean(name = ["webClient"])
    fun buildWebClient(): WebClient {
        val provider = ConnectionProvider.builder("custom-provider")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(58))
            .maxLifeTime(Duration.ofSeconds(58))
            .pendingAcquireTimeout(Duration.ofMillis(5000))
            .pendingAcquireMaxCount(-1)
            .evictInBackground(Duration.ofSeconds(30))
            .lifo()
            .metrics(false)
            .build()

        val reactorResourceFactory = ReactorResourceFactory().apply {
            connectionProvider = provider
            loopResources = LoopResources { RESOURCE }
            isUseGlobalResources = false
        }

        return WebClient.builder()
            .baseUrl("http://localhost:8081")
            .clientConnector(ReactorClientHttpConnector(reactorResourceFactory) { httpClient ->
                httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .responseTimeout(Duration.ofMillis(5000))
                    .doOnConnected { conn ->
                        conn.addHandlerLast(ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                            .addHandlerLast(WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                    }
            })
            .build()
    }
}