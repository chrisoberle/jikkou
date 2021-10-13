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
package io.streamthoughts.kafka.specs.template;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import io.streamthoughts.kafka.specs.GlobalSpecsContext;
import io.streamthoughts.kafka.specs.error.KafkaSpecsException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for manipulating template using Mustache.
 */
public class Template {

    public static final String SCOPE_LABELS = "labels";
    public static final String SCOPE_SYSTEM = "system";
    public static final String SCOPE_SYSTEM_ENV = "env";
    public static final String SCOPE_SYSTEM_PROPS = "props";

    public static String compile(final String template,
                                 final GlobalSpecsContext context) {

        JinjavaConfig config = JinjavaConfig.newBuilder()
                .withCharset(StandardCharsets.UTF_8)
                .withFailOnUnknownTokens(true)
                .build();

        Jinjava jinjava = new Jinjava(config);

        HashMap<String, Object> bindings = new HashMap<>();
        bindings.put(SCOPE_LABELS, context.getLabels());
        bindings.put(SCOPE_SYSTEM, Map.of(
                SCOPE_SYSTEM_ENV, context.getSystemEnv(),
                SCOPE_SYSTEM_PROPS, context.getSystemProps())
        );

        RenderResult result = jinjava.renderForResult(template, bindings);

        List<TemplateError> errors = result.getErrors();
        if (!errors.isEmpty()) {
            TemplateError error = errors.get(0);
            throw new KafkaSpecsException(
                    String.format(
                            "%s: line %d, %s",
                            formatErrorReason(error.getReason().name()),
                            error.getLineno(),
                            error.getMessage())
            );
        }

        return result.getOutput();
    }

    /**
     * @return the input string to camel-case.
     */
    private static String formatErrorReason(final String s) {
        String formatted = Pattern.compile("_([a-z])")
                .matcher(s.toLowerCase())
                .replaceAll(m -> m.group(1).toUpperCase());
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
