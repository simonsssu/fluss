/*
 * Copyright (c) 2024 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.fluss.cli.admin.cluster;

import com.alibaba.fluss.cli.base.AdminBaseCmd;

import com.google.gson.Gson;
import picocli.CommandLine;

@CommandLine.Command(name = "nodes", description = "List cluster nodes")
public class ClusterNodesCmd extends AdminBaseCmd {

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output format: text|json",
            defaultValue = "text")
    private String outputFormat;

    @Override
    protected void validateParams() {}

    @Override
    protected int callCmd() throws Exception {
        getAdmin()
                .getServerNodes()
                .thenAccept(
                        nodes -> {
                            if ("json".equalsIgnoreCase(outputFormat)) {
                                System.out.println(new Gson().toJson(nodes));
                            } else {
                                System.out.println("Cluster Nodes:");
                                nodes.forEach(
                                        node ->
                                                System.out.printf(
                                                        " - %s:%d%n", node.host(), node.port()));
                            }
                        })
                .exceptionally(handleException("Nodes query failed"))
                .get();
        return 0;
    }
}
