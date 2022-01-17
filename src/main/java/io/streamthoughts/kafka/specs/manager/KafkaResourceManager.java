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
package io.streamthoughts.kafka.specs.manager;

import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.ChangeComputer;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.config.Configurable;
import io.streamthoughts.kafka.specs.extensions.Extension;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;

import java.util.Collection;
import java.util.List;

/**
 * The top-level interface for managing resources that live on a kafka cluster, e.g., Topics, Quotas, ACLs.
 *
 * @param <R>   type of the resource handle by the manager.
 * @param <C>   type of change to be generated by the manager.
 * @param <OP>  type of {@link  ChangeComputer.Options}.
 * @param <OD>  type of {@link DescribeOptions}.
 */
public interface KafkaResourceManager<R, C extends Change<?>, OP extends ChangeComputer.Options, OD extends DescribeOptions>
        extends Extension,
                Configurable {

    enum UpdateMode {
        /**
         * Create only new resources.
         */
        CREATE_ONLY,
        /**
         * Alter only existing resources.
         */
        ALTER_ONLY,
        /**
         * Delete only orphan resources.
         */
        DELETE_ONLY,
        /**
         * Apply all resources changes (i.e., create, alter and delete).
         */
        APPLY;
    }

    /**
     * Updates the Kafka cluster resources using the given {@link V1SpecsObject} list.
     *
     * @param mode          the update-mode to execute.
     * @param objects       the {@link V1SpecsObject} list.
     * @param context       the {@link KafkaResourceUpdateContext}.
     * @return              the list of {@link ChangeResult}.
     */
    Collection<ChangeResult<C>> update(UpdateMode mode,
                                       List<V1SpecsObject> objects,
                                       KafkaResourceUpdateContext<OP> context);


    /**
     * Describes the objects that exist on a remote Kafka cluster.
     *
     * @param options   the options to be used for describing resources.
     *
     * @return          the list of resources.
     */
    List<R> describe(OD options);


}
