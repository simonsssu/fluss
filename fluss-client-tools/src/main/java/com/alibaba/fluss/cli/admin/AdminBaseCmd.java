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

package com.alibaba.fluss.cli.admin;

import com.alibaba.fluss.cli.FlussCliMain;
import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.cli.conn.ConnectionManager;
import com.alibaba.fluss.client.Connection;
import com.alibaba.fluss.client.admin.Admin;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import picocli.CommandLine;

public abstract class AdminBaseCmd extends BaseCmd<Integer> {

    @CommandLine.ParentCommand private FlussCliMain main;

    public AdminBaseCmd() {
        attachSubCommand();
    }

    protected Connection connection() {
        return ConnectionManager.getConnection(main.getBootstrapServers());
    }

    public Admin getAdmin() {
        return connection().getAdmin();
    }

    public static String[] splitTablePath(String tablePath) {
        int dotIndex = tablePath.indexOf('.');
        if (dotIndex == -1 || dotIndex == 0 || dotIndex == tablePath.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid table path format. Expected: database.table");
        }
        return new String[] {tablePath.substring(0, dotIndex), tablePath.substring(dotIndex + 1)};
    }

    @Override
    public void attachSubCommand() {
        Class<?>[] declaredClasses = this.getClass().getDeclaredClasses();
        Arrays.stream(declaredClasses)
                .filter(
                        clazz ->
                                clazz.isAnnotationPresent(FlussCmd.class)
                                        && !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(
                        clazz -> {
                            FlussCmd cmdAnnotation = clazz.getAnnotation(FlussCmd.class);
                            String cmdName = cmdAnnotation.name();
                            try {
                                getCli().addSubcommand(
                                                cmdName,
                                                clazz.getConstructor(this.getClass())
                                                        .newInstance(this));
                            } catch (Exception e) {
                                System.err.println("Failed to create subcommand: " + cmdName);
                                throw new RuntimeException(e);
                            }
                        });
    }

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
