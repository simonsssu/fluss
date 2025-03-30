/*
 *  Copyright (c) 2025 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.fluss.cli.cmd.table;

import com.alibaba.fluss.cli.cmd.base.BaseCliCmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "list", commandDescription = "List tables in a database")
public class ListTableCmd extends BaseCliCmd<TableCmdGroup> {
    @Parameter(
            names = {"--db", "-d"},
            description = "Database name",
            required = true)
    private String database;

    @Parameter(names = "--format", description = "Output format (text/json)")
    private String format = "text";

    @Override
    public int execute() {
        getAdmin().listTables(database);
        System.out.printf("Listing tables in %s (format: %s)\n", database, format);
        return 0;
    }
}
