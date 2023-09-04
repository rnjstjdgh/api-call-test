package com.example.mock_api_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MockApiServerApplication

fun main(args: Array<String>) {
    runApplication<MockApiServerApplication>(*args)
}
