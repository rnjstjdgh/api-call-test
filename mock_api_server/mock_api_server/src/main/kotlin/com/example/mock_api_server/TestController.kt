package com.example.mock_api_server

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    
    @GetMapping("/test/{sleep}/{idx}")
    fun test(
        @PathVariable("sleep") sleep: Long,
        @PathVariable("idx") idx: Long,
    ): ResponseEntity<String> {
        Thread.sleep(sleep)
        return ResponseEntity.ok("result${idx}")
    }
}