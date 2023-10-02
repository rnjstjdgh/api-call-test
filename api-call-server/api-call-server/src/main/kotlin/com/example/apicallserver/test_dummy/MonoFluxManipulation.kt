package com.example.apicallserver.test_dummy

import reactor.core.publisher.Flux

/****
 * https://velog.io/@zenon8485/Reactor-Java-2.-Mono%EC%99%80-Flux%EC%9D%98-%EB%82%B4%EB%B6%80-%EB%8D%B0%EC%9D%B4%ED%84%B0%EB%A5%BC-%EC%A1%B0%EC%9E%91%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95
 */
class MonoFluxManipulation {
}

fun main() {
    val squared = Flux.range(1, 100).map { it * it }
    squared.subscribe{ ele -> println(ele) }
}