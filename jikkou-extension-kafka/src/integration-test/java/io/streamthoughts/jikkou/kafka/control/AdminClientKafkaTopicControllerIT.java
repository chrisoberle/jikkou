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
package io.streamthoughts.jikkou.kafka.control;

import io.streamthoughts.jikkou.api.DefaultApi;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.api.testcontainer.RedpandaContainerConfig;
import io.streamthoughts.jikkou.api.testcontainer.RedpandaKafkaContainer;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
public class AdminClientKafkaTopicControllerIT {

    public static final String CLASSPATH_RESOURCE_TOPICS = "test-kafka-topics.yaml";
    public static final String CLASSPATH_RESOURCE_TOPIC_ALL_DELETE = "test-kafka-topics-with-all-delete.yaml";
    public static final String CLASSPATH_RESOURCE_TOPIC_SINGLE_DELETE = "test-kafka-topics-with-single-delete.yaml";
    public static final String TOPIC_TEST_A = "topic-test-A";
    public static final String TOPIC_TEST_B = "topic-test-B";
    public static final String TOPIC_TEST_C = "topic-test-C";

    @Container
    public RedpandaKafkaContainer kafka = new RedpandaKafkaContainer(
            new RedpandaContainerConfig()
                    .withKafkaApiFixedExposedPort(9092)
                    .withAttachContainerOutputLog(true)
                    .withTransactionEnabled(false)
    );

    private volatile JikkouApi api;

    @BeforeAll
    public static void beforeAll() {
        ResourceDeserializer.registerKind(V1KafkaTopicList.class);
    }

    @BeforeEach
    public void setUp() {
        var controller = new AdminClientKafkaTopicController(new AdminClientContext(() ->
                AdminClient.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers())))
        );

        var descriptor = new AdminClientKafkaTopicCollector(new AdminClientContext(() ->
                AdminClient.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers())))
        );

        api = DefaultApi.builder()
                .withController(controller)
                .withCollector(descriptor)
                .build();
    }


    @Test
    public void shouldReconcileKafkaTopicsGivenModeCreate() {
        // GIVEN
        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.with(Configuration.empty(), false);

        // WHEN
        V1KafkaTopicList initialTopicList = getResource();
        List<ChangeResult<Change>> results = api.apply(resources, ReconciliationMode.CREATE, context);
        V1KafkaTopicList actualTopicList = getResource();

        // THEN
        Assertions.assertEquals(
                0,
                initialTopicList.getItems().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                2, actualTopicList.getItems().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                2,
                results.size(),
                "Invalid number of changes");

        V1KafkaTopicList expectedTopicList = resources.getAllByClass(V1KafkaTopicList.class).get(0);
        Assertions.assertEquals(
                expectedTopicList.getItems().size(),
                actualTopicList.getItems().size()
        );

        Map<String, V1KafkaTopic> actualByTopicName = actualTopicList.groupByName();
        Map<String, V1KafkaTopic> expectedByTopicName = expectedTopicList.groupByName();

        expectedByTopicName.forEach((topicName, expected) -> {
            V1KafkaTopic actual = actualByTopicName.get(topicName);
            Assertions.assertEquals(expected.getSpec().getPartitions(), actual.getSpec().getPartitions());
            Assertions.assertEquals(expected.getSpec().getReplicas(), actual.getSpec().getReplicas());

            // Explicitly validate each config because Redpanda returns additional config properties.
            Map<String, ConfigValue> expectedConfigByName = Nameable.keyByName(expected.getSpec().getConfigs());
            Map<String, ConfigValue> actualConfigByName = Nameable.keyByName(actual.getSpec().getConfigs());

            expectedConfigByName.forEach((configName, expectedConfigValue) -> {
                ConfigValue actualConfigValue = actualConfigByName.get(configName);
                Assertions.assertEquals(expectedConfigValue, actualConfigValue);
            });
        });
    }

    @Test
    public void shouldReconcileKafkaTopicsForModeDeleteWithDeleteAnnotations() {

        // GIVEN
        kafka.createTopic(TOPIC_TEST_A);
        kafka.createTopic(TOPIC_TEST_B);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC_ALL_DELETE);

        var context = ReconciliationContext.with(false);

        // WHEN
        V1KafkaTopicList initialTopicList = getResource();
        List<ChangeResult<Change>> results = api.apply(resources, ReconciliationMode.DELETE, context);
        V1KafkaTopicList actualTopicList = getResource();

        // THEN
        Assertions.assertEquals(
                2,
                initialTopicList.getItems().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                0, actualTopicList.getItems().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                2,
                results.size(),
                "Invalid number of changes");

        ChangeResult<?> change = results.iterator().next();
        Assertions.assertEquals(ChangeResult.Status.CHANGED, change.status());
        Assertions.assertEquals(ChangeType.DELETE, change.resource().getChangeType());
    }

    @Test
    public void shouldReconcileKafkaTopicsForModeUpdate() {

        // GIVEN
        kafka.createTopic(TOPIC_TEST_A);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.with(false);

        // WHEN
        V1KafkaTopicList initialTopicList = getResource();
        List<ChangeResult<Change>> results = api.apply(resources, ReconciliationMode.UPDATE, context);
        V1KafkaTopicList actualTopicList = getResource();

        // THEN
        Assertions.assertEquals(
                1,
                initialTopicList.getItems().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                2, actualTopicList.getItems().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                2,
                results.size(),
                "Invalid number of changes");

        Map<String, TopicChange> changeKeyedByTopicName = results.stream()
                .map(o -> ((TopicChange) o.resource()))
                .collect(Collectors.toMap(TopicChange::getName, o -> o));

        Assertions.assertNotNull(changeKeyedByTopicName.get(TOPIC_TEST_A));
        Assertions.assertEquals(ChangeType.UPDATE, changeKeyedByTopicName.get(TOPIC_TEST_A).getChangeType());
        Assertions.assertNotNull(changeKeyedByTopicName.get(TOPIC_TEST_B));
        Assertions.assertEquals(ChangeType.ADD, changeKeyedByTopicName.get(TOPIC_TEST_B).getChangeType());
    }

    @Test
    public void shouldReconcileKafkaTopicsGivenModeApply() {

        // GIVEN
        kafka.createTopic(TOPIC_TEST_C);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.with( false);

        // WHEN
        V1KafkaTopicList initialTopicList = getResource();
        List<ChangeResult<Change>> results = api.apply(resources, ReconciliationMode.APPLY_ALL, context);
        V1KafkaTopicList actualTopicList = getResource();

        // THEN
        Assertions.assertEquals(
                1,
                initialTopicList.getItems().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                3, actualTopicList.getItems().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                2,
                results.size(),
                "Invalid number of changes");

        boolean delete = results.stream()
                .map(it -> it.resource().getChangeType())
                .anyMatch(it -> it.equals(ChangeType.DELETE));

        Assertions.assertFalse(delete);
    }

    @Test
    public void shouldReconcileKafkaTopicForModeApplyAndDeleteOrphansTrue() {

        // GIVEN
        kafka.createTopic(TOPIC_TEST_C);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC_SINGLE_DELETE);

        var context = ReconciliationContext.with( false);

        // WHEN
        V1KafkaTopicList initialTopicList = getResource();
        List<ChangeResult<Change>> results = api.apply(resources, ReconciliationMode.APPLY_ALL, context);
        V1KafkaTopicList actualTopicList = getResource();

        // THEN
        Assertions.assertEquals(
                1,
                initialTopicList.getItems().size(),
                "Invalid number of topics");
        Assertions.assertEquals(
                2, actualTopicList.getItems().size(),
                "Invalid number of topics");
        Assertions.assertEquals(
                3,
                results.size(),
                "Invalid number of changes");

        Assertions.assertEquals(1, initialTopicList.getItems().size());
        boolean delete = results.stream()
                .map(it -> it.resource().getChangeType())
                .anyMatch(it -> it.equals(ChangeType.DELETE));

        Assertions.assertTrue(delete);
    }

    private V1KafkaTopicList getResource() {
        Configuration configuration = new ConfigDescribeConfiguration()
                .withDescribeStaticBrokerConfigs(false)
                .withDescribeDynamicBrokerConfigs(false)
                .withDescribeDefaultConfigs(false)
                .asConfiguration();

        List<V1KafkaTopicList> resources = api.getResources(V1KafkaTopicList.class, configuration);
        return resources.get(0);
    }
}