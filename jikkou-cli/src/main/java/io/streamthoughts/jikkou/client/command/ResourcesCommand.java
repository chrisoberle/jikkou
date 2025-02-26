/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.client.command;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.api.ResourceContext;
import io.streamthoughts.jikkou.api.ResourceDescriptor;
import io.streamthoughts.jikkou.client.ClientContext;
import java.util.Comparator;
import java.util.List;
import picocli.CommandLine.Command;

@Command(name = "resources", description = "List supported resources")
public class ResourcesCommand implements Runnable {

    /** {@inheritDoc} **/
    @Override
    public void run() {

        ClientContext clientContext = ClientContext.get();

        ResourceContext resourceContext = clientContext.getResourceContext();
        List<ResourceDescriptor> descriptors = resourceContext
                .getAllResourceDescriptors()
                .stream()
                .sorted(Comparator.comparing(ResourceDescriptor::kind))
                .toList();

        String[][] data = descriptors
                .stream()
                .map(descriptor -> new String[]{
                        descriptor.kind(),
                        descriptor.group(),
                        descriptor.singularName(),
                        String.join(", ", descriptor.shortNames()),
                        descriptor.pluralName().orElse(""),
                })
                .toArray(String[][]::new);

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{

                        new Column().header("KIND").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("APIGROUP").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("SHORT NAMES").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("PLURAL").dataAlign(HorizontalAlign.LEFT),
                },
                data);

        System.out.println(table);
    }
}
