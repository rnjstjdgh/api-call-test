package com.example.apicallserver.controller

import com.example.apicallserver.fegin.FeignService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class CoroutineTestController(
    private val feignService: FeignService,
) {

    @GetMapping("/test/coroutine/{sleep}")
    fun `Coroutine 기반 비동기코드`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = runBlocking(newFixedThreadPoolContext(1, "coroutine-thread")) {
            (1..10).map {
                async { feignService.testCall(sleep, it.toLong()) }
            }.awaitAll()
        }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }
}