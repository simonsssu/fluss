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

package com.alibaba.fluss.cli.admin.database;

import com.alibaba.fluss.cli.base.AdminBaseCmd;
import com.alibaba.fluss.metadata.DatabaseDescriptor;

import picocli.CommandLine;

@CommandLine.Command(name = "create", description = "Create new database")
public class CreateDatabaseCmd extends AdminBaseCmd {

    @CommandLine.Parameters(index = "0", description = "Database name")
    private String dbName;

    @CommandLine.Option(names = "--desc", description = "Database description")
    private String description;

    @CommandLine.Option(names = "--ignore-existing", description = "Ignore if database exists")
    private boolean ignoreExisting;

    @Override
    protected void validateParams() {}

    @Override
    protected int callCmd() throws Exception {
        DatabaseDescriptor descriptor = DatabaseDescriptor.builder().comment(description).build();
        getAdmin()
                .createDatabase(dbName, descriptor, ignoreExisting)
                .thenApply(
                        v -> {
                            System.out.println("Database created: " + dbName);
                            return v;
                        })
                .exceptionally(handleException("Create failed"))
                .get();
        return 0;
    }
}
