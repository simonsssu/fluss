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

import com.alibaba.fluss.cli.annotation.FlussCmdGroup;
import com.alibaba.fluss.cli.base.GroupCmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

// Table Command Group
@FlussCmdGroup(name = "table", description = "Table management commands")
@Parameters(commandDescription = "Table management operations")
public class TableGroupCmd implements GroupCmd {
    private JCommander cmdJc;
    private String[] args;

    public TableGroupCmd() {
        cmdJc = new JCommander(this);
        cmdJc.setProgramName("table");
        cmdJc.addCommand("list", new ListCommand());
        cmdJc.addCommand("create", new CreateCommand());
        cmdJc.addCommand("drop", new DropCommand());
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public int execute() {
        try {
            cmdJc.parse(args);
            return dispatchSubCommand();
        } catch (ParameterException e) {
            System.err.println("Table Error: " + e.getMessage());
            cmdJc.usage();
            return 1;
        }
    }

    private int dispatchSubCommand() {
        if (cmdJc.getParsedCommand() == null) {
            System.err.println("Table Error: No command specified!");
            cmdJc.usage();
            return 1;
        }

        switch (cmdJc.getParsedCommand()) {
            case "list":
                return new ListCommand().run();
            case "create":
                return new CreateCommand().run();
            case "drop":
                return new DropCommand().run();
            default:
                return 1;
        }
    }
}
