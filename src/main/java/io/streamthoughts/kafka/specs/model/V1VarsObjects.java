/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class V1VarsObjects {

    private final Map<String, Object> vars;

    /**
     * Creates a new {@link V1VarsObjects} instance.
     *
     * @param vars  the variables.
     */
    @JsonCreator
    public V1VarsObjects(@JsonProperty("vars") final Map<String, Object> vars) {
        this.vars = Optional.ofNullable(vars).orElse(Collections.emptyMap());
    }

    public Map<String, Object> vars() {
        return vars;
    }
}
