package com.example.apicallserver.controller

import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@RestController
class WebClientTestController(
    private val webClient: WebClient
) {



    @GetMapping("/test/webclient/blocking/{sleep}")
    fun `webclient 기반 코드(blocking)`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String?>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

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

        val resultList = Flux.merge(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
            webClient.get()
                .uri("/test/$sleep/$it")
                .retrieve()
                .bodyToMono(String::class.java)
        }).toIterable().toList()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }
}