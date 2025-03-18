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

@FlussCmd(name = "drop", parentCmd = TableGroupCmd.class)
@Parameters(commandDescription = "Drop a table")
public class DropCommand extends BaseCliCmd {
    @Parameter(description = "<table-path>", required = true)
    private String tablePath;

    @Parameter(names = "--force", description = "Skip confirmation")
    private boolean force;

    @Override
    public int execute() {
        System.out.printf("Dropping table %s (force: %b)\n", tablePath, force);
        return 0;
    }
}
