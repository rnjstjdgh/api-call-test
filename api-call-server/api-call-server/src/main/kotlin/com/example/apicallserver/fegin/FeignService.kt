package com.example.apicallserver.fegin

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Service
@FeignClient(
    contextId = "testFeignService",
    name = "test",
    url = "\${test.api.url}",
    configuration = [FeignConfig::class]
)
interface FeignService {

    @GetMapping("/test/{sleep}/{idx}")
    fun testCall(
        @PathVariable("sleep") sleep: Long,
        @PathVariable("idx") idx: Long,
    ): String
}