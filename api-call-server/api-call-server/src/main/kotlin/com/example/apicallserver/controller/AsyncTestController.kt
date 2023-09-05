package com.example.apicallserver.controller

import com.example.apicallserver.fegin.FeignService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@RestController
class AsyncTestController(
    private val feignService: FeignService,
    private val executor: Executor,
) {

    @GetMapping("/test/task_executor/{sleep}")
    fun `TaskExecutor 기반 비동기코드`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = (1..10).map {
            CompletableFuture.supplyAsync({ feignService.testCall(sleep, it.toLong()) }, executor)
        }.map { it.get() }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }
}