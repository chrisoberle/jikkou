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
package io.streamthoughts.jikkou.client.command.quotas.subcommands;

import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.client.command.quotas.QuotasCommand;
import io.streamthoughts.jikkou.kafka.control.change.KafkaQuotaReconciliationConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "alter",
         description = "Update the client quotas on the cluster as describe in the specification file."
)
public class Alter extends QuotasCommand.Base {

    @CommandLine.Option(
            names = "--delete-orphans",
            defaultValue = "false",
            description = "Delete config entries overridden on the cluster but absent from the specification file"
    )
    Boolean deleteOrphans;

    /** {@inheritDoc} */
    @Override
    public ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.UPDATE_ONLY;
    }

    /** {@inheritDoc} */
    @Override
    public Configuration getReconciliationConfiguration() {
        return new KafkaQuotaReconciliationConfig()
                .withDeleteConfigOrphans(deleteOrphans)
                .withDeleteQuotaOrphans(false)
                .asConfiguration();
    }

}
