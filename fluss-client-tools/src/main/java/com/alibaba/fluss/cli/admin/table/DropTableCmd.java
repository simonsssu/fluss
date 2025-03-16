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

import picocli.CommandLine;

import static com.alibaba.fluss.cli.utils.CmdUtils.splitTablePath;

@CommandLine.Command(name = "drop", description = "Delete table")
public class DropTableCmd extends AdminBaseCmd {
    @CommandLine.Parameters(index = "0", description = "Full table path (format: database.table)")
    private String tablePath;

    @CommandLine.Option(names = "--ignore-not-exists", description = "Ignore missing table")
    private boolean ignoreNotExists;

    @Override
    protected void validateParams() {}

    @Override
    protected int callCmd() throws Exception {
        String[] parts = splitTablePath(tablePath);
        String database = parts[0];
        String tableName = parts[1];
        TablePath path = TablePath.of(database, tableName);
        getAdmin()
                .dropTable(path, ignoreNotExists)
                .thenAccept(v -> System.out.println("Table dropped: " + tablePath))
                .exceptionally(handleException("Drop failed"))
                .get();
        return 0;
    }
}
