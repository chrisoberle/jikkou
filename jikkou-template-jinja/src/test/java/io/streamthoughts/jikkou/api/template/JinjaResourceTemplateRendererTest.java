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
package io.streamthoughts.jikkou.api.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderOptions;
import io.streamthoughts.jikkou.api.io.readers.TemplateResourceReader;
import io.streamthoughts.jikkou.api.model.GenericResource;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.NamedValue;
import io.streamthoughts.jikkou.common.utils.Classes;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JinjaResourceTemplateRendererTest {

    private static final ObjectMapper OBJECT_MAPPER = Jackson.YAML_OBJECT_MAPPER;

    @Test
    @SuppressWarnings("unchecked")
    void should_load_template_resource_given_values_file() throws IOException {

        // Given
        ClassLoader classLoader = Classes.getClassLoader();
        InputStream template = classLoader.getResourceAsStream("datasets/resource-template.yaml");
        InputStream values = classLoader.getResourceAsStream("datasets/resource-values.yaml");

        Map<String, Object> mapValues = OBJECT_MAPPER.readValue(values, Map.class);
        ResourceReaderOptions options = new ResourceReaderOptions()
                .withValues(NamedValue.setOf(mapValues));

        try (var reader = new TemplateResourceReader(
                new JinjaResourceTemplateRenderer(),
                () -> template,
                OBJECT_MAPPER
        )) {

            // When
            List<HasMetadata> results = reader.readAllResources(options);

            // Then
            Assertions.assertNotNull(results);
            Assertions.assertEquals(1, results.size());
            GenericResource resource = (GenericResource) results.get(0);
            Map<String, Object> spec = (LinkedHashMap) resource.getAdditionalProperties().get("spec");
            Object topics = spec.get("topics");
            Assertions.assertNotNull(topics);
            Assertions.assertEquals(5, ((List) topics).size());
        }
    }
}