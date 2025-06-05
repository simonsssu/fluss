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

package com.alibaba.fluss.cli.cmd.database;

import com.alibaba.fluss.cli.cmd.base.BaseCliCmd;
import com.alibaba.fluss.cli.format.CliTable;
import com.alibaba.fluss.cli.format.CliTable.CliOutputStyle;
import com.alibaba.fluss.cli.format.CliTable.CliTableBuilder;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** List databases sub command. */
@Parameters(commandNames = "list", commandDescription = "List databases")
public class ListDatabaseCmd extends BaseCliCmd<DatabaseParentCmd> {

    @Parameter(names = "--format", description = "Output format (text/table)")
    private String format = "table";

    @Override
    protected int execute() throws Exception {
        CompletableFuture<List<String>> dbs = getAdmin().listDatabases();
        System.out.printf("Listing databases in fluss cluster.(format: %s)\n", format);
        CliTableBuilder builder =
                CliTable.builder().style(CliOutputStyle.of(format)).addHeader("Table");
        dbs.get().forEach(builder::addRow);
        builder.build().print();
        return 0;
    }
}
