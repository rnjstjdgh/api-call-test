package com.example.apicallserver.test_dummy

import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers


/***
 * https://wiki.terzeron.com/Programming/Java/Reactor_Flux%EC%9D%98_publishOn_subscribeOn%EC%9D%84_%EC%9D%B4%EC%9A%A9%ED%95%9C_%EC%8A%A4%EC%BC%80%EC%A5%B4%EB%A7%81
 */
class PlayGround {


}

fun main() {
    Flux.range(1, 3)
            .map {
                println("1st map(): " + Thread.currentThread().name + " " + it)
                it
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                println("2nd map(): " + Thread.currentThread().name + " " + it)
                it
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                println("3nd map(): " + Thread.currentThread().name + " " + it)
                it
            }
            .subscribe { println("subscribe():" + Thread.currentThread().name + " " + it) }
}