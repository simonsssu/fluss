/*
 * Copyright (c) 2025 Alibaba Group Holding Ltd.
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

package com.alibaba.fluss.cli.admin;

import com.alibaba.fluss.cli.admin.group.DatabaseGroupCmd;
import com.alibaba.fluss.cli.base.BaseCmd;

import com.google.gson.Gson;
import picocli.CommandLine;

@CommandLine.Command(name = "info", description = "Show database details")
public class DatabaseInfoCmd extends BaseCmd<Integer> {
    @CommandLine.ParentCommand private DatabaseGroupCmd parent;

    @CommandLine.Parameters(index = "0", description = "Database name")
    private String dbName;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output format: text|json",
            defaultValue = "json")
    private String outputFormat;

    @Override
    public Integer call() throws Exception {
        parent.admin()
                .getDatabaseInfo(dbName)
                .thenAccept(
                        info -> {
                            if ("json".equalsIgnoreCase(outputFormat)) {
                                System.out.println(new Gson().toJson(info));
                            } else {
                                System.out.println("Database Name: " + info.getDatabaseName());
                                System.out.println("Created At: " + info.getCreatedTime());
                                System.out.println(
                                        "Database Descriptor: " + info.getDatabaseDescriptor());
                                System.out.println("Modified Time: " + info.getModifiedTime());
                            }
                        })
                .exceptionally(handleException("Info query failed"))
                .get();
        return 0;
    }
}
