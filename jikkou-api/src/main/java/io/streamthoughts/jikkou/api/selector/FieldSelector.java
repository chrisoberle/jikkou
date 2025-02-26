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
package io.streamthoughts.jikkou.api.selector;

import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.selector.internal.PropertyAccessors;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldSelector extends ExpressionResourceSelector {

    /**
     * Creates a new {@link FieldSelector} instance.
     */
    public FieldSelector() {
        super(new PathExpressionKeyValueExtractor());
    }

    static class PathExpressionKeyValueExtractor implements ExpressionKeyValueExtractor {

        /** {@inheritDoc} **/
        @Override
        public String getKeyValue(@NotNull HasMetadata resource, @Nullable String key) {
            if (key == null) {
                throw new IllegalArgumentException("Cannot apply extractor with empty key");
            }
            Object value = new PropertyAccessors().readPropertyValue(resource, key);
            return Optional.ofNullable(value).map(Object::toString).orElse(null);
        }

        /** {@inheritDoc} **/
        @Override
        public boolean isKeyExists(@NotNull HasMetadata resource, @Nullable String key) {
            if (key == null) {
                throw new IllegalArgumentException("Cannot apply extractor with empty key");
            }
            return getKeyValue(resource, key) != null;
        }
    }
}
