/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTypeTest {

    @Test
    void should_create_given_kind_only() {
        ResourceType type = ResourceType.create("KafkaTopicList", null);
        Assertions.assertEquals("KafkaTopicList", type.getKind());
    }

    @Test
    void should_create_given_kind_and_version() {
        ResourceType type = ResourceType.create("KafkaTopicList", "kafka.jikkou.io/v1beta2");
        Assertions.assertEquals("KafkaTopicList", type.getKind());
        Assertions.assertEquals("v1beta2", type.getApiVersion());
        Assertions.assertEquals("kafka.jikkou.io", type.getGroup());
    }
}