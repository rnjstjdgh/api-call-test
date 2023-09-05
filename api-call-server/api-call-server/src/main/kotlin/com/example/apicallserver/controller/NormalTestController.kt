package com.example.apicallserver.controller

import com.example.apicallserver.fegin.FeignService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class NormalTestController(
    private val feignService: FeignService,
) {

    @GetMapping("/test/normal/{sleep}")
    fun `일반적인동기코드`(
        @PathVariable("sleep") sleep: Long,
    ): ResponseEntity<List<String>> {
        println("호출전: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        val resultList = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map {
            feignService.testCall(sleep, it.toLong())
        }

        println("호출후: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        return ResponseEntity.ok(resultList)
    }
}