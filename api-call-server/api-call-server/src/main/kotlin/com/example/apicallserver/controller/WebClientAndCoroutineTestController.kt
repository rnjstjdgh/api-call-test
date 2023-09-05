package com.example.apicallserver.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class WebClientAndCoroutineTestController(
    private val webClient: WebClient
) {

    @GetMapping("/test/webclient/non-blocking/coroutine/{sleep}")
    fun `webclient_coroutine 기반 코드(non-blocking)`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String?>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = runBlocking(newFixedThreadPoolContext(1, "coroutine-thread")) {
            (1..10).map {
                async { webClientCall(sleep, it) }
            }.awaitAll()
        }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    private suspend fun webClientCall(
        sleep: Long,
        idx: Int,
    ): String? {
        return webClient.get()
            .uri("/test/$sleep/$idx")
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
    }

}