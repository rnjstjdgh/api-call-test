package com.example.apicallserver.controller

import com.example.apicallserver.fegin.FeignService
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
class MapFlatMapTestController(
        private val feignService: FeignService,
        private val webClient: WebClient
) {

    @GetMapping("/test/feign/map/{sleep}")
    fun `feign_map`(
            @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = Flux.range(1,10)
                .map {
                    feignService.testCall(sleep, it.toLong())
                }
                .collectList().block()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/feign/flatmap/{sleep}")
    fun `feign_flatmap`(
            @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = Flux.range(1,10)
                .flatMap {
                    Mono.just(feignService.testCall(sleep, it.toLong()))
                }
                .collectList().block()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/webclient/map/{sleep}")
    fun `webclient_map`(
            @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String?>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        val resultList = Flux.range(1,10)
                .map {
                    webClientCall(sleep, it).block()
                }
                .collectList().block()

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }

    @GetMapping("/test/webclient/flatmap/{sleep}")
    fun `webclient_flatmap`(
            @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String?>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = Flux.range(1,10)
                .flatMap {
                    webClientCall(sleep, it)
                }
                .collectList().block()

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