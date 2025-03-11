/*
 * Copyright (c) 2025 Alibaba Group Holding Ltd.
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

import static com.alibaba.fluss.cli.admin.group.TableGroupCmd.splitTablePath;

import com.alibaba.fluss.cli.admin.group.TableGroupCmd;
import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.metadata.TableDescriptor;
import com.alibaba.fluss.metadata.TablePath;
import jakarta.validation.constraints.Min;
import picocli.CommandLine;

@CommandLine.Command(name = "create", description = "Create new table")
public class TableCreateCmd extends BaseCmd<Integer> {

    @CommandLine.ParentCommand private TableGroupCmd parent;

    @CommandLine.Parameters(index = "0", description = "Full table path (format: database.table)")
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

        parent.admin()
                .createTable(path, descriptor, ignoreExisting)
                .thenAccept(v -> System.out.println("Table created: " + tablePath))
                .exceptionally(handleException("Create failed"))
                .get();
        return 0;
    }
}
