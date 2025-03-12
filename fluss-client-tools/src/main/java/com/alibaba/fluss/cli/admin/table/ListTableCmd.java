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

package com.alibaba.fluss.cli.admin.table;

import com.alibaba.fluss.cli.base.AdminBaseCmd;

import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "List tables")
public class ListTableCmd extends AdminBaseCmd {
    @CommandLine.Option(
            names = {"-d", "--database"},
            required = true,
            description = "Database name")
    public String database;

    @Override
    public Integer call() throws Exception {
        getAdmin()
                .listTables(database)
                .thenAccept(
                        tables -> {
                            System.out.println("Tables in " + database + ":");
                            tables.forEach(
                                    table -> {
                                        System.out.println(" - " + table);
                                    });
                        })
                .exceptionally(handleException("List failed"))
                .get();
        return 0;
    }
}
