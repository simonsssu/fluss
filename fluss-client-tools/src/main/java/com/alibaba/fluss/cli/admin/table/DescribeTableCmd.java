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
import com.alibaba.fluss.metadata.TablePath;

import com.google.gson.Gson;
import picocli.CommandLine;

import static com.alibaba.fluss.cli.utils.CmdUtils.splitTablePath;

@CommandLine.Command(name = "describe", description = "Describe Table info")
public class DescribeTableCmd extends AdminBaseCmd {
    @CommandLine.Parameters(index = "0", description = "Full table path (format: database.table)")
    private String tablePath;

    @Override
    protected void validateParams() {}

    @Override
    protected int callCmd() throws Exception {
        String[] parts = splitTablePath(tablePath);
        String database = parts[0];
        String tableName = parts[1];
        TablePath path = TablePath.of(database, tableName);
        getAdmin()
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
