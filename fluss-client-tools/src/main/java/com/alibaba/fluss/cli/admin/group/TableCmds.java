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

package com.alibaba.fluss.cli.admin.group;

import com.alibaba.fluss.cli.admin.AdminBaseCmd;
import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.metadata.TableDescriptor;
import com.alibaba.fluss.metadata.TablePath;
import com.google.gson.Gson;
import jakarta.validation.constraints.Min;
import picocli.CommandLine;

@FlussCmd(name = "table", baseSuit = true)
@CommandLine.Command(name = "table", description = "Table operations")
public class TableCmds extends AdminBaseCmd {

    @FlussCmd(name = "create")
    @CommandLine.Command(name = "create", description = "Create new table")
    public class Create extends BaseCmd<Integer> {

        @CommandLine.Parameters(
                index = "0",
                description = "Full table path (format: database.table)")
        private String tablePath;

        @CommandLine.Option(names = "--buckets", required = true, description = "Number of buckets")
        @Min(1)
        private int buckets;

        @CommandLine.Option(
                names = "--replicas",
                defaultValue = "1",
                description = "Replication factor")
        @Min(1)
        private int replicas;

        @CommandLine.Option(names = "--ignore-existing", description = "Ignore existing table")
        private boolean ignoreExisting;

        @Override
        public Integer call() throws Exception {
            String[] parts = splitTablePath(tablePath);
            String database = parts[0];
            String tableName = parts[1];
            TablePath path = TablePath.of(database, tableName);
            TableDescriptor descriptor = TableDescriptor.builder().build();

            getAdmin()
                    .createTable(path, descriptor, ignoreExisting)
                    .thenAccept(v -> System.out.println("Table created: " + tablePath))
                    .exceptionally(handleException("Create failed"))
                    .get();
            return 0;
        }
    }

    @FlussCmd(name = "drop")
    @CommandLine.Command(name = "drop", description = "Delete table")
    public class Drop extends BaseCmd<Integer> {

        @CommandLine.Parameters(
                index = "0",
                description = "Full table path (format: database.table)")
        private String tablePath;

        @CommandLine.Option(names = "--ignore-not-exists", description = "Ignore missing table")
        private boolean ignoreNotExists;

        @Override
        public Integer call() throws Exception {
            String[] parts = splitTablePath(tablePath);
            String database = parts[0];
            String tableName = parts[1];
            TablePath path = TablePath.of(database, tableName);
            getAdmin()
                    .dropTable(path, ignoreNotExists)
                    .thenAccept(v -> System.out.println("Table dropped: " + tablePath))
                    .exceptionally(handleException("Drop failed"))
                    .get();
            return 0;
        }
    }

    @FlussCmd(name = "info")
    @CommandLine.Command(name = "info", description = "Table info")
    public class Info extends BaseCmd<Integer> {

        @CommandLine.Parameters(
                index = "0",
                description = "Full table path (format: database.table)")
        private String tablePath;

        @Override
        public Integer call() throws Exception {
            String[] parts = splitTablePath(tablePath);
            String database = parts[0];
            String tableName = parts[1];
            TablePath path = TablePath.of(database, tableName);
            getAdmin()
                    .getTableInfo(path)
                    .thenAccept(
                            info -> {
                                System.out.println("Table Info:");
                                System.out.println("Database Name: " + database);
                                System.out.println("Table Name: " + tableName);
                                System.out.println(new Gson().toJson(info));
                            })
                    .exceptionally(handleException("Info query failed"))
                    .get();
            return 0;
        }
    }

    @FlussCmd(name = "list")
    @CommandLine.Command(name = "list", description = "List tables")
    public class List extends BaseCmd<Integer> {

        @CommandLine.Option(
                names = {"-d", "--database"},
                required = true,
                description = "Database name")
        public String database;

        @Override
        public Integer call() throws Exception {
            getAdmin()
                    .listTables(database)
                    .thenAccept(
                            tables -> {
                                System.out.println("Tables in " + database + ":");
                                tables.forEach(
                                        table -> {
                                            System.out.println(" - " + table);
                                        });
                            })
                    .exceptionally(handleException("List failed"))
                    .get();
            return 0;
        }
    }
}
