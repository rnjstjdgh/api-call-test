package com.example.apicallserver.controller

import com.example.apicallserver.fegin.FeignService
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.netty.resources.ConnectionProvider
import reactor.netty.resources.LoopResources
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.*


@RestController
class TestController(
    private val feignService: FeignService,
    private val executor: Executor,
) {

    companion object {
        val THREADS = 10
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

    @GetMapping("/test/normal/{sleep}")
    fun `일반적인동기코드`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
            feignService.testCall(sleep, it.toLong())
        }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/task_executor/{sleep}")
    fun `TaskExecutor 기반 비동기코드`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
            CompletableFuture.supplyAsync({ feignService.testCall(sleep, it.toLong()) }, executor)
        }.map { it.get() }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/coroutine/{sleep}")
    fun `Coroutine 기반 비동기코드`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = runBlocking(newFixedThreadPoolContext(1, "coroutine-thread")) {
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
                async { feignService.testCall(sleep, it.toLong()) }
            }.awaitAll()
        }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/webclient/blocking/{sleep}")
    fun `webclient 기반 코드(blocking)`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String?>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val webClient = buildWebClient()
        val resultList = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
            webClient.get()
                .uri("/test/$sleep/$it")
                .retrieve()
                .bodyToMono(String::class.java).block()
        }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/webclient/non-blocking/{sleep}")
    fun `webclient 기반 코드(non-blocking)`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String?>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val webClient = buildWebClient()
        val resultList = Flux.merge(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
            webClient.get()
                .uri("/test/$sleep/$it")
                .retrieve()
                .bodyToMono(String::class.java)
        }).toIterable().toList()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    private fun buildWebClient(): WebClient {
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