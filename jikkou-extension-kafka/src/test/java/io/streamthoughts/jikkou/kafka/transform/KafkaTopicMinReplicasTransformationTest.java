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
package io.streamthoughts.jikkou.kafka.transform;

import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicMinReplicasTransformationTest {

    @Test
    void shouldNotEnforceConstraintForValidValue() {
        // Given
        KafkaTopicMinReplicasTransformation transformation = new KafkaTopicMinReplicasTransformation();
        transformation.configure(KafkaTopicMinReplicasTransformation.MIN_REPLICATION_FACTOR_CONFIG.asConfiguration(3));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withReplicas((short) 6)
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals((short) 6, transformed.getSpec().getReplicas());
    }

    @Test
    void shouldEnforceConstraintForInvalidValue() {
        // Given
        KafkaTopicMinReplicasTransformation transformation = new KafkaTopicMinReplicasTransformation();
        transformation.configure(KafkaTopicMinReplicasTransformation.MIN_REPLICATION_FACTOR_CONFIG.asConfiguration(3));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withReplicas((short) 1)
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals((short) 3, transformed.getSpec().getReplicas());
    }

}