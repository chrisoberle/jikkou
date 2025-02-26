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
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.converter.ResourceConverter;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *  This class can be used to decorate an interceptor with a different name and priority.
 *
 * @see ResourceInterceptor
 */
@InterfaceStability.Evolving
public class ResourceInterceptorDecorator<E extends ResourceInterceptor, D extends ResourceInterceptorDecorator<E, D>> implements ResourceInterceptor {

    protected final E extension;
    private Integer priority;
    private String name;

    /**
     * Creates a new {@link ResourceInterceptorDecorator} instance.
     *
     * @param extension the extension; must not be null.
     */
    public ResourceInterceptorDecorator(E extension) {
        this.extension = Objects.requireNonNull(extension, "extension must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        this.extension.configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean canAccept(@NotNull ResourceType type) {
        return this.extension.canAccept(type);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceConverter<HasMetadata, HasMetadata> getResourceConverter(@NotNull HasMetadata resource) {
        return this.extension.getResourceConverter(resource);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceConverter<HasMetadata, HasMetadata> getResourceConverter(@NotNull ResourceType resource) {
        return this.extension.getResourceConverter(resource);
    }

    @SuppressWarnings("unchecked")
    public D withPriority(@Nullable Integer priority) {
        this.priority = priority;
        return (D) this;
    }

    @SuppressWarnings("unchecked")
    public D withName(@Nullable String name) {
        this.name = name;
        return (D) this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int getPriority() {
        return priority != null ? priority : extension.getPriority();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getName() {
        return name != null ? name : extension.getName();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "[" +
                "extension=" + extension +
                ", priority=" + priority +
                ", name='" + name +
                ']';
    }
}
