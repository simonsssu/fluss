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

import com.alibaba.fluss.cli.admin.group.TableGroupCmd;
import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.metadata.TablePath;

import com.google.gson.Gson;
import picocli.CommandLine;

import static com.alibaba.fluss.cli.admin.group.TableGroupCmd.splitTablePath;

@CommandLine.Command(name = "info", description = "Table info")
public class TableInfoCmd extends BaseCmd<Integer> {
    @CommandLine.ParentCommand private TableGroupCmd parent;

    @CommandLine.Parameters(index = "0", description = "Full table path (format: database.table)")
    private String tablePath;

    @Override
    public Integer call() throws Exception {
        String[] parts = splitTablePath(tablePath);
        String database = parts[0];
        String tableName = parts[1];
        TablePath path = TablePath.of(database, tableName);
        parent.admin()
                .getTableInfo(path)
                .thenAccept(
                        info -> {
                            System.out.println("Table Info:");
                            System.out.println("Database Name: " + database);
                            System.out.println("Table Name: " + tableName);
                            System.out.println(new Gson().toJson(info));
                        })
                .exceptionally(handleException("Info query failed"))
                .get();
        return 0;
    }
}
