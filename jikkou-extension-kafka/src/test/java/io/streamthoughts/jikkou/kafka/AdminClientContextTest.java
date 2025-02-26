/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka;

import static io.streamthoughts.jikkou.kafka.AdminClientContext.KAFKA_BROKERS_WAIT_FOR_ENABLED;
import static io.streamthoughts.jikkou.kafka.AdminClientContext.KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE;
import static io.streamthoughts.jikkou.kafka.AdminClientContext.KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS;
import static io.streamthoughts.jikkou.kafka.AdminClientContext.KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS;

import io.streamthoughts.jikkou.api.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AdminClientContextTest {

    @Test
    void shouldOverrideOptionsForWaitFalse() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_ENABLED.asConfiguration(false);
        try(AdminClientContext context = new AdminClientContext(configuration)) {
            // When
            boolean result = context.isWaitForKafkaBrokersEnabled();

            // Then
            Assertions.assertFalse(result);
        }
    }


    @Test
    void shouldOverrideOptionsForWaitMinAvailable() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE.asConfiguration(42);
        try(AdminClientContext context = new AdminClientContext(configuration)) {
            // When
            int result = context.getOptions().minAvailableBrokers();

            // Then
            Assertions.assertEquals(42, result);
        }
    }

    @Test
    void shouldOverrideOptionsForWaitRetryBackMs() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS.asConfiguration(42L);
        try(AdminClientContext context = new AdminClientContext(configuration)) {
            // When
            long result = context.getOptions().retryBackoffMs();

            // Then
            Assertions.assertEquals(42L, result);
        }
    }

    @Test
    void shouldOverrideOptionsForWaitRetryTimeoutMs() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS.asConfiguration(42L);
        try(AdminClientContext context = new AdminClientContext(configuration)) {
            // When
            long result = context.getOptions().timeoutMs();

            // Then
            Assertions.assertEquals(42L, result);
        }
    }

}