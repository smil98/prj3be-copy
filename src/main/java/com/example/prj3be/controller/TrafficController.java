package com.example.prj3be.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
//
//@Slf4j
//@RestController
//public class TrafficController {
//    private List<String> list = new ArrayList<>();
//    @GetMapping("cpu")
//    public String cpu() {
//        log.info("cpu");
//        long value = 0;
//        for (long i = 0; i < 10000000000000L; i++){
//            value++;
//        }
//        return "ok value = " + value;
//    }
//    @GetMapping("/jvm")
//    public String jvm() {
//        log.info("jvm");
//        for (int i = 0; i < 1000000; i++) {
//            list.add("hello jvm!" + i);
//        }
//        return "ok";
//    }
//}
