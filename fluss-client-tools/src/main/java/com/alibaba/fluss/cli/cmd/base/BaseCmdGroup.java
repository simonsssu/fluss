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

package com.alibaba.fluss.cli.cmd.base;

import com.alibaba.fluss.cli.utils.CmdUtils;

import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class BaseCmdGroup extends BaseCmd {

    protected String[] args;

    public BaseCmdGroup() {
        super();
        CmdUtils.registerCommand(cmdJc, parentCmdPredicate());
    }

    @Override
    protected String fullNamePrefix() {
        return "fluss";
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public int execute() {
        try {
            cmdJc.parse(args);
            if (Objects.isNull(cmdJc.getParsedCommand())) {
                System.err.println(
                        StringUtils.capitalize(getCmdName())
                                + " Command Error: No sub command specified!");
                cmdJc.usage();
                return 1;
            }
            BaseCliCmd<?> cmd = getCliCmd(cmdJc.getParsedCommand());
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

    private Predicate<Class<?>> parentCmdPredicate() {
        return cliClazz -> {
            Type type = cliClazz.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type[] actualTypeArgs = pt.getActualTypeArguments();
                return actualTypeArgs.length > 0
                        && actualTypeArgs[0].getTypeName().equals(this.getClass().getName());
            } else {
                return false;
            }
        };
    }

    public BaseCliCmd<?> getCliCmd(String cmdName) {
        List<Object> subCmds = cmdJc.getCommands().get(cmdName).getObjects();
        return (BaseCliCmd<?>) subCmds.get(0);
    }
}
