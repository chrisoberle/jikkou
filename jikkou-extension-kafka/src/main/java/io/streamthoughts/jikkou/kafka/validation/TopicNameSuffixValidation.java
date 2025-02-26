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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ExtensionEnabled(value = false)
public class TopicNameSuffixValidation extends TopicValidation {

    public static final ConfigProperty<List<String>> VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG = ConfigProperty
            .ofList("topicNameSuffixes");

    private List<String> suffixes;

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        super.configure(config);
        suffixes = VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.getOptional(config)
               .orElseThrow(() -> new ConfigException(
                       String.format("The '%s' configuration property is required for %s",
                               VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.key(),
                               TopicNameSuffixValidation.class.getSimpleName()
                       )
               ));
    }

    /** {@inheritDoc} */
    @Override
    public void validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        final boolean matched = suffixes.stream()
                .filter(prefix -> resource.getMetadata().getName().endsWith(prefix))
                .findAny()
                .isEmpty();
        if (matched) {
            throw new ValidationException(String.format(
                    "Name for topic '%s' does not end with one of the configured suffixes: %s",
                    resource.getMetadata().getName(),
                    suffixes
            ), this);
        }
    }
}
