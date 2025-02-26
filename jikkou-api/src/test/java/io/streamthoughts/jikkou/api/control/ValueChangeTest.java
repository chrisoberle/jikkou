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
package io.streamthoughts.jikkou.api.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueChangeTest {

    public static final String AFTER_VALUE = "foo";
    public static final String BEFORE_VALUE = "bar";

    @Test
    void shouldReturnNoneForEqualsValues() {
        // Given
        ValueChange<String> change = ValueChange.with(AFTER_VALUE, AFTER_VALUE);

        // When
        ChangeType type = change.type();

        // Then
        Assertions.assertEquals(ChangeType.NONE, type);
    }

    @Test
    void shouldReturnUpdateForNotEqualsValues() {
        // Given
        ValueChange<String> change = ValueChange.with(AFTER_VALUE, BEFORE_VALUE);

        // When
        ChangeType type = change.type();

        // Then
        Assertions.assertEquals(BEFORE_VALUE, change.getBefore());
        Assertions.assertEquals(AFTER_VALUE, change.getAfter());
        Assertions.assertEquals(ChangeType.UPDATE, type);
    }

    @Test
    void shouldReturnAddForBeforeNull() {
        // Given
        ValueChange<String> change = ValueChange.with(AFTER_VALUE, null);

        // When
        ChangeType type = change.type();

        // Then
        assertNull(change.getBefore());
        Assertions.assertEquals(AFTER_VALUE, change.getAfter());
        Assertions.assertEquals(ChangeType.ADD, type);
    }

    @Test
    void shouldReturnAddForAfterNull() {
        // Given
        ValueChange<String> change = ValueChange.with(null, BEFORE_VALUE);

        // When
        ChangeType type = change.type();

        // Then
        assertNull(change.getAfter());
        Assertions.assertEquals(BEFORE_VALUE, change.getBefore());
        Assertions.assertEquals(ChangeType.DELETE, type);
    }
}