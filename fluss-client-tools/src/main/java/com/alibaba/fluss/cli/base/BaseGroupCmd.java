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

package com.alibaba.fluss.cli.base;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

public abstract class BaseGroupCmd extends BaseCmd {

    protected String[] args;

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public int execute() {
        try {
            cmdJc.parse(args);
            if (cmdJc.getParsedCommand() == null) {
                System.err.println(
                        StringUtils.capitalize(getCmdName())
                                + " Command Error: No sub command specified!");
                cmdJc.usage();
                return 1;
            }

            BaseCliCmd cmd = getCliCmd(cmdJc.getParsedCommand());
            if (cmd != null) {
                return cmd.execute();
            } else {
                throw new ParameterException("Unknown command: " + cmdJc.getParsedCommand());
            }
        } catch (ParameterException e) {
            System.err.println(StringUtils.capitalize(getCmdName()) + " Error: " + e.getMessage());
            if (e instanceof MissingCommandException) {
                cmdJc.usage();
            } else {
                getCliCmd(cmdJc.getParsedCommand()).printUsage();
            }
            return 1;
        }
    }

    public void initSubCommand() {
        Reflections reflections = new Reflections("com.alibaba.fluss.cli");
        List<Class<?>> commands =
                new ArrayList<>(reflections.getTypesAnnotatedWith(FlussCmd.class));
        commands.sort(Comparator.comparing(Class::getName));
        commands.stream()
                .filter(
                        cmd ->
                                getParentCmdName(cmd).equals(this.getClass().getName())
                                        && !cmd.getAnnotation(FlussCmd.class).isGroup())
                .forEach(
                        cmd -> {
                            try {
                                BaseCliCmd commandInstance =
                                        (BaseCliCmd) cmd.getDeclaredConstructor().newInstance();
                                commandInstance.setAdminSupplier(adminSupplier);
                                commandInstance.attachParentCmd(cmdJc);
                                cmdJc.addCommand(getCmdName(cmd), commandInstance);
                            } catch (Exception e) {
                                System.err.println(
                                        "Initialize sub command failed: " + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
    }

    public BaseCliCmd getCliCmd(String cmdName) {
        List<Object> subCmds = cmdJc.getCommands().get(cmdName).getObjects();
        return (BaseCliCmd) subCmds.get(0);
    }
}
