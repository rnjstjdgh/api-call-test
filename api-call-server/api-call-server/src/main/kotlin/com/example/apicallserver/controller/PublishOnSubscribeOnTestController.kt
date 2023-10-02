package com.example.apicallserver.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@RestController
class PublishOnSubscribeOnTestController(
        private val webClient: WebClient
) {

    @GetMapping("/test/webclient/publishOn")
    fun `publishOn`() {
        Flux.range(1, 3)
                .flatMap {
                    println("1st map(): " + Thread.currentThread().name + " " + it)
                    webClientCall(1000,1)
                }
                .publishOn(Schedulers.boundedElastic())
                .flatMap {
                    println("2nd map(): " + Thread.currentThread().name + " " + it)
                    webClientCall(1000,2)
                }
//                .publishOn(Schedulers.boundedElastic())
                .flatMap {
                    println("3nd map(): " + Thread.currentThread().name + " " + it)
                    webClientCall(1000,1)
                }
                .subscribe { println("subscribe():" + Thread.currentThread().name + " " + it) }
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