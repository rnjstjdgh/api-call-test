package com.example.apicallserver.controller

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        val resultList = Flux.merge((1..10).map {
            webClientCall(sleep, it)
        }).toIterable().toList()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    private fun webClientCall(
        sleep: Long,
        idx: Int,
    ): Mono<String?> {
        return webClient.get()
            .uri("/test/$sleep/$idx")
            .retrieve()
            .bodyToMono(String::class.java)
    }
}