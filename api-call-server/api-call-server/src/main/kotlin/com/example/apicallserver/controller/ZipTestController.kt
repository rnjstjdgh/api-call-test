package com.example.apicallserver.controller

import com.example.apicallserver.fegin.FeignService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class ZipTestController(
        private val feignService: FeignService,
        private val webClient: WebClient
) {

    @GetMapping("/test/zip/blocking/{sleep}")
    fun `2개의blocking_call_zip`(
            @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val result1 = Mono.fromCallable {
            println(Thread.currentThread().name)
            feignService.testCall(3000, 1)
        }
        val result2 = Mono.fromCallable {
            println(Thread.currentThread().name)
            feignService.testCall(3000, 2)
        }

        val resultList = result1.zipWith(result2)
                .map { tuple -> listOf(tuple.t1, tuple.t2) }
                .block()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/non-blocking/zip/{sleep}")
    fun `2개의non-blocking_call_zip`(
            @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val result1 = webClientCall(3000, 1)
        val result2 = webClientCall(3000, 2)

        val resultList = result1.zipWith(result2)
                .map { tuple -> listOf(tuple.t1, tuple.t2) }
                .block()

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