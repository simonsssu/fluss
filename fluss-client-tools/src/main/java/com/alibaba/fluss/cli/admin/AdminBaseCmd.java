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
import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.cli.conn.ConnectionManager;
import com.alibaba.fluss.client.Connection;
import com.alibaba.fluss.client.admin.Admin;

import picocli.CommandLine;

public abstract class AdminBaseCmd extends BaseCmd<Integer> {

    @CommandLine.ParentCommand private FlussCliMain main;

    protected Connection connection() {
        return ConnectionManager.getConnection(main.getBootstrapServers());
    }

    protected Admin admin() {
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
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
