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

package com.alibaba.fluss.cli.table;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.BaseCliCmd;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@FlussCmd(name = "create", parentCmd = TableGroupCmd.class)
@Parameters(commandDescription = "Create a new table")
public class CreateCommand extends BaseCliCmd {
    @Parameter(description = "<table-path>", required = true)
    private String tablePath;

    @Parameter(names = "--schema", description = "Schema definition file")
    private String schemaFile;

    @Parameter(names = "--shards", description = "Number of shards")
    private int shards = 1;

    @Override
    public int execute() {
        System.out.printf("Creating table %s with %d shards\n", tablePath, shards);
        if (schemaFile != null) {
            System.out.println("Using schema: " + schemaFile);
        }
        return 0;
    }
}
