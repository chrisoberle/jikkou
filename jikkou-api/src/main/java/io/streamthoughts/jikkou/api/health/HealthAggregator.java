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
package io.streamthoughts.jikkou.api.health;

import java.util.Collection;
import java.util.Optional;

/**
 * The {@link HealthAggregator} aggregates multiple {@link Health} instances into a single one.
 */
public final class HealthAggregator {

    private final StatusAggregator statusAggregator;

    public HealthAggregator() {
        this(new DefaultStatusAggregator());
    }

    public HealthAggregator(final StatusAggregator statusAggregator) {
        this.statusAggregator = statusAggregator;
    }

    /**
     * Aggregates the specified {@link Health} instances to a single one.
     *
     * @param healths the list of {@link Health} to aggregate.
     * @return the aggregated {@link Health}.
     */
    public Health aggregate(final Collection<Health> healths) {
        return aggregate(null, healths);
    }

    /**
     * Aggregates the specified {@link Health} instances to a single one.
     *
     * @param name    the aggregate health name.
     * @param healths the list of {@link Health} to aggregate.
     * @return the aggregated {@link Health}.
     */
    public Health aggregate(final String name, final Collection<Health> healths) {
        final Health.Builder builder = new Health.Builder().up();
        Optional.ofNullable(name).ifPresent(builder::withName);
        if (!healths.isEmpty()) {
            healths.forEach(h -> {
                if (h.getName() == null || h.getName().isBlank()) {
                    throw new IllegalArgumentException("Cannot aggregate metric with empty name");
                }
                builder.withDetails(
                                h.getName(),
                                // avoid redundancy by removing the health name
                                new Health.Builder()
                                        .withStatus(h.getStatus())
                                        .withDetails(h.getDetails())
                                        .build()
                        );
                    }
            );
            final Status status = statusAggregator.aggregateStatus(StatusAggregator.getAllStatus(healths));
            builder.withStatus(status);
        }
        return builder.build();
    }

}