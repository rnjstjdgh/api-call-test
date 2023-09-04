package com.example.apicallserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class ApiCallServerApplication

fun main(args: Array<String>) {
    runApplication<ApiCallServerApplication>(*args)
}
