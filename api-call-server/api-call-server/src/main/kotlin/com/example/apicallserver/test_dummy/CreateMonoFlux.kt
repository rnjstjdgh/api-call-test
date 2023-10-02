package com.example.apicallserver.test_dummy

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.time.Duration

/***
 * 아래 링크를 따라해보기위함
 * https://velog.io/@zenon8485/Reactor-Java-1.-Mono%EC%99%80-Flux%EB%A5%BC-%EC%83%9D%EC%84%B1%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95
 */
class CreateMonoFlux {

    companion object {
        fun getAnyInteger(): Int? {
            throw RuntimeException("An error as occured for no reason.")
        }
    }

}


fun main() {
    val integerFlux = Flux.just(1,2,3)
    val stringFlux = Flux.just("hello")


    val intervalFlux = Flux.interval(Duration.ofMillis(100))
    intervalFlux.map {
        println(it)
    }.collectList().block()

    val fromCallable = Mono.fromCallable { "hello" }
    val deffer = Mono.defer { Mono.just("fdsf") }

    val monoCreate = Mono.create { callback: MonoSink<Any?> ->
        try {
            callback.success(CreateMonoFlux.getAnyInteger())
        } catch (e: Exception) {
            callback.error(e)
        }
    }

    Mono.just("Hello World.").subscribe(
            { successValue: String? -> println(successValue) },
            { error: Throwable -> println(error.message) }
    ) { println("Mono consumed.") }
}