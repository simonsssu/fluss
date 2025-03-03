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

package com.alibaba.fluss.cli.cmd;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.CmdBase;

import picocli.CommandLine;

@FlussCmd(name = "table", description = "Perform table management operations")
@CommandLine.Command(
        name = "table",
        mixinStandardHelpOptions = true,
        description = "Perform table management operations")
public class CmdTable extends CmdBase<Integer> {

    @CommandLine.Parameters(index = "0", description = "Table name")
    private String tableName;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output format: text|json",
            defaultValue = "text")
    private String format;

    public Integer call() {
        return 0;
    }
}
