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

package com.alibaba.fluss.cli.admin;

import com.alibaba.fluss.cli.admin.group.DatabaseGroupCmd;
import com.alibaba.fluss.cli.base.BaseCmd;
import com.google.gson.Gson;
import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "List databases")
public class DatabaseListCmd extends BaseCmd<Integer> {

    @CommandLine.ParentCommand private DatabaseGroupCmd parent;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output format: text|json",
            defaultValue = "text")
    private String outputFormat;

    @Override
    public Integer call() throws Exception {
        parent.getAdmin()
                .listDatabases()
                .thenAccept(
                        dbs -> {
                            if ("json".equalsIgnoreCase(outputFormat)) {
                                System.out.println(new Gson().toJson(dbs));
                            } else {
                                System.out.println("Databases:");
                                dbs.forEach(System.out::println);
                            }
                        })
                .exceptionally(handleException("List failed"))
                .get();
        return 0;
    }
}
