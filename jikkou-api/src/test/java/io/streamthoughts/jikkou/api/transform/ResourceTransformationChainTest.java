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
package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.api.TestResource;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTransformationChainTest {

    @Test
    void shouldRunTransformationsInPriorityOrder() {

        List<Integer> calls = new ArrayList<>();

        // Given
        ResourceTransformationChain chain = new ResourceTransformationChain(List.of(
                newTransformation(3, () -> calls.add(3)),
                newTransformation(0, () -> calls.add(0)),
                newTransformation(2, () -> calls.add(2)),
                newTransformation(1, () -> calls.add(1))
        ));
        // When
        chain.transform(new TestResource(), new GenericResourceListObject(Collections.emptyList()));

        // Then
        Assertions.assertEquals(4, calls.size());
        for (int i = 0; i < calls.size(); i++) {
            Assertions.assertEquals(i, calls.get(i));
        }
    }

    private ResourceTransformation<HasMetadata> newTransformation(int priority, Runnable onTransformation) {
        return new ResourceTransformation<>() {

            @Override
            public boolean canAccept(@NotNull ResourceType type) {
                return true;
            }
            
            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform,
                                                            @NotNull HasItems resources) {
                onTransformation.run();
                return Optional.of(toTransform);
            }
        };
    }
}