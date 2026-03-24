package com.phaneendra.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Event-Driven Messaging System
 * Author: Phaneendra Gunji
 *
 * Scalable messaging pipeline using Spring Boot and Apache Kafka.
 * Supports async order processing with idempotent consumers
 * and 99.9% message consistency guarantees.
 */
@SpringBootApplication
public class MessagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingApplication.class, args);
    }
}
