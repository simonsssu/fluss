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
import com.alibaba.fluss.shaded.guava32.com.google.common.collect.Maps;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class BaseGroupCmd implements IBaseCmd {

    private static final Map<String, BaseCliCmd> CMD_MAP = Maps.newHashMap();

    protected JCommander cmdJc;

    public BaseGroupCmd() {
        this.cmdJc = new JCommander(this);
    }

    protected String[] args;

    protected String bootstrapServers;

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public void setBootStrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @Override
    public int execute() {
        try {
            cmdJc.parse(args);
            if (cmdJc.getParsedCommand() == null) {
                System.err.println(
                        StringUtils.capitalize(getCmdName()) + " Error: No command specified!");
                cmdJc.usage();
                return 1;
            }
            return getCmd(cmdJc.getParsedCommand()).execute();
        } catch (ParameterException e) {
            System.err.println(StringUtils.capitalize(getCmdName()) + " Error: " + e.getMessage());
            cmdJc.usage();
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
                                cmdJc.addCommand(getCmdName(cmd), commandInstance);
                                CMD_MAP.put(getCmdName(cmd), commandInstance);
                            } catch (Exception e) {
                                System.err.println(
                                        "Initialize sub command failed: " + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public BaseCliCmd getCmd(String cmdName) {
        return CMD_MAP.get(cmdName);
    }

    private String getCmdName() {
        return getCmdName(this.getClass());
    }

    private String getCmdName(Class<?> clazz) {
        return clazz.getAnnotation(FlussCmd.class).name();
    }

    private String getParentCmdName(Class<?> clazz) {
        return clazz.getAnnotation(FlussCmd.class).parentCmd().getName();
    }
}
