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

import static io.streamthoughts.jikkou.api.control.ValueChange.withAfterValue;

import io.streamthoughts.jikkou.api.control.ChangeComputer;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.api.control.ConfigEntryChangeComputer;
import io.streamthoughts.jikkou.api.control.ConfigEntryReconciliationConfig;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.kafka.adapters.KafkaTopicObjectAdapter;
import io.streamthoughts.jikkou.kafka.internals.KafkaConstants;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

public class TopicChangeComputer implements ChangeComputer<V1KafkaTopicObject, String, TopicChange, KafkaTopicReconciliationConfig> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopicChange> computeChanges(@NotNull final Iterable<V1KafkaTopicObject> actualStates,
                                            @NotNull final Iterable<V1KafkaTopicObject> expectedStates,
                                            @NotNull final KafkaTopicReconciliationConfig configuration) {

        final Map<String, V1KafkaTopicObject> actualTopicResourceMapByName = Nameable.keyByName(actualStates);

        final Map<String, TopicChange> changes = new HashMap<>();

        for (final V1KafkaTopicObject expectedTopic : expectedStates) {

            final V1KafkaTopicObject actualTopic = actualTopicResourceMapByName.get(expectedTopic.getName());
            final TopicChange change = actualTopic == null ?
                    buildChangeForNewTopic(expectedTopic) :
                    buildChangeForExistingTopic(actualTopic, expectedTopic, configuration);

            changes.put(change.getName(), change);
        }

        if (configuration.isDeleteTopicOrphans()) {
            changes.putAll(buildChangesForOrphanTopics(actualTopicResourceMapByName.values(), changes.keySet(), configuration));
        }

        return new ArrayList<>(changes.values());
    }

    private static @NotNull Map<String, TopicChange> buildChangesForOrphanTopics(
            @NotNull final Collection<V1KafkaTopicObject> topics,
            @NotNull final Set<String> changes,
            @NotNull final KafkaTopicReconciliationConfig options) {
        return topics
                .stream()
                .filter(it -> !changes.contains(it.getName()))
                .filter(it -> !(KafkaTopics.isInternalTopics(it.getName()) && options.isExcludeInternalTopics()))
                .map(topic -> TopicChange.builder()
                            .withName(topic.getName())
                            .withOperation(ChangeType.DELETE)
                            .build()
                )
                .collect(Collectors.toMap(TopicChange::getName, it -> it));
    }

    private static @NotNull TopicChange buildChangeForExistingTopic(@NotNull final V1KafkaTopicObject actualState,
                                                                    @NotNull final V1KafkaTopicObject expectedState,
                                                                    @NotNull final KafkaTopicReconciliationConfig options) {

        var actualObjectTopic = new KafkaTopicObjectAdapter(actualState);
        var expectedObjectTopic = new KafkaTopicObjectAdapter(expectedState);

        ValueChange<Integer> partitions;
        // Do not compute change when described partition is equals to default.
        if (expectedObjectTopic.getPartitionsOrDefault() == KafkaConstants.NO_NUM_PARTITIONS) {
            partitions = ValueChange.none(actualObjectTopic.getPartitionsOrDefault());
        } else {
            partitions = ValueChange.with(
                    expectedObjectTopic.getPartitionsOrDefault(),
                    actualObjectTopic.getPartitionsOrDefault()
            );
        }

        ValueChange<Short> replication;
        // Do not compute change when describe replication-factor is equals to default.
        if (expectedObjectTopic.getReplicationFactorOrDefault() == KafkaConstants.NO_REPLICATION_FACTOR) {
            replication = ValueChange.none(actualObjectTopic.getReplicationFactorOrDefault());
        } else {
            replication = ValueChange.with(
                    expectedObjectTopic.getReplicationFactorOrDefault(),
                    actualObjectTopic.getReplicationFactorOrDefault()
            );
        }

        final ConfigEntryReconciliationConfig configEntryReconciliationConfig = new ConfigEntryReconciliationConfig()
                .withDeleteConfigOrphans(options.isDeleteConfigOrphans());

        var configEntryChanges = new ConfigEntryChangeComputer().computeChanges(
                        actualObjectTopic.getConfigs(),
                        expectedObjectTopic.getConfigs(),
                        configEntryReconciliationConfig
                );

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getChange() != ChangeType.NONE);

        var configOpType = hasChanged ? ChangeType.UPDATE : ChangeType.NONE;
        var partitionOpType = partitions.type();
        ChangeType op = List.of(partitionOpType, configOpType).contains(ChangeType.UPDATE) ?
                ChangeType.UPDATE :
                ChangeType.NONE;

        return TopicChange.builder()
                .withName(expectedState.getName())
                .withPartitions(partitions)
                .withReplicationFactor(replication)
                .withOperation(op)
                .withConfigs(configEntryChanges)
                .build();
    }

    private static @NotNull TopicChange buildChangeForNewTopic(@NotNull final V1KafkaTopicObject resource) {

        var topicObject = new KafkaTopicObjectAdapter(resource);

        var configEntryChanges = StreamSupport
                .stream(topicObject.getConfigs().spliterator(), false)
                .map(it -> new ConfigEntryChange(it.getName(), withAfterValue(String.valueOf(it.value()))))
                .toList();

        return TopicChange.builder()
                .withName(topicObject.getName())
                .withPartitions(withAfterValue(topicObject.getPartitionsOrDefault()))
                .withReplicationFactor(withAfterValue(topicObject.getReplicationFactorOrDefault()))
                .withOperation(ChangeType.ADD)
                .withConfigs(configEntryChanges)
                .build();
    }
}
