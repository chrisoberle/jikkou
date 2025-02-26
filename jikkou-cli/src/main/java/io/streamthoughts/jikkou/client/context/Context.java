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
package io.streamthoughts.jikkou.client.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.client.JikkouConfig;
import java.util.Collections;
import java.util.Map;

/**
 * Represent a context configuration.
 *
 * @param configFile       a configuration file in HOCON format.
 * @param configProps      additional config properties.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record Context(String configFile, Map<String, Object> configProps) {
    public Context(
            @JsonProperty("configFile") String configFile,
            @JsonProperty("configProps") Map<String, Object> configProps) {
        this.configFile = configFile;
        this.configProps = configProps;
    }

    /**
     * Static method to create and return a default context.
     * @return  the context.
     */
    public static Context defaultContext() {
        return new Context(null, Collections.emptyMap());
    }

    /**
     * Loads the configuration for this context.
     *
     * @return  the configuration.
     */
    public JikkouConfig load() {
        return configFile() != null ? JikkouConfig.load(configProps(), configFile()) : JikkouConfig.load(configProps());
    }

}
