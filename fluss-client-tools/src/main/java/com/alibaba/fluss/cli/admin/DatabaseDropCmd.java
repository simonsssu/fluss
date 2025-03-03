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

import com.alibaba.fluss.cli.admin.group.DatabaseGroupCmd;
import com.alibaba.fluss.cli.base.BaseCmd;

import picocli.CommandLine;

@CommandLine.Command(name = "drop", description = "Delete database")
public class DatabaseDropCmd extends BaseCmd<Integer> {
    @CommandLine.ParentCommand private DatabaseGroupCmd parent;

    @CommandLine.Parameters(index = "0", description = "Database name")
    private String dbName;

    @CommandLine.Option(names = "--cascade", description = "Delete all tables in database")
    private boolean cascade;

    @CommandLine.Option(names = "--ignore-missing", description = "Ignore missing database")
    private boolean ignoreMissing;

    @Override
    public Integer call() throws Exception {
        parent.admin()
                .dropDatabase(dbName, ignoreMissing, cascade)
                .thenAccept(v -> System.out.println("Database dropped: " + dbName))
                .exceptionally(handleException("Drop failed"))
                .get();
        return 0;
    }
}
