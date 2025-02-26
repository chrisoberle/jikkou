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
package io.streamthoughts.jikkou.kafka.control.change;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaEntity;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class QuotaChange implements Change {

    private final ChangeType operation;

    private final KafkaClientQuotaType type;

    private final V1KafkaClientQuotaEntity entity;

    private final List<ConfigEntryChange> configs;

    /**
     * Creates a new {@link QuotaChange} instance.
     *
     * @param operation     the type of the change.
     * @param type          the quota type.
     * @param entity        the quota entity.
     * @param configs       the quota configuration.
     */
    public QuotaChange(@NotNull final ChangeType operation,
                       @NotNull final KafkaClientQuotaType type,
                       @NotNull final V1KafkaClientQuotaEntity entity,
                       @NotNull final List<ConfigEntryChange> configs) {
        this.operation = Objects.requireNonNull(operation, "operation cannot be null");
        this.entity = Objects.requireNonNull(entity, "entity cannot be null");;
        this.configs = Objects.requireNonNull(configs, "configs cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    public ChangeType getChangeType() {
        return operation;
    }

    @JsonProperty
    public V1KafkaClientQuotaEntity getEntity() {
        return entity;
    }

    @JsonProperty
    public List<ConfigEntryChange> getConfigs() {
        return configs;
    }

    @JsonProperty
    public KafkaClientQuotaType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuotaChange that)) return false;
        return Objects.equals(entity, that.entity) &&
                Objects.equals(configs, that.configs) &&
                operation == that.operation &&
                type == that.type;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(entity, configs, operation, type);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "QuotaChange{" +
                "entity=" + entity +
                ", configs=" + configs +
                ", operation=" + operation +
                ", type=" + type +
                '}';
    }
}
