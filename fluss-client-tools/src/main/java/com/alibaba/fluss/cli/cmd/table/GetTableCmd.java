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

package com.alibaba.fluss.cli.cmd.table;

import com.alibaba.fluss.cli.cmd.base.BaseCliCmd;
import com.alibaba.fluss.cli.format.CliTable;
import com.alibaba.fluss.cli.format.CliTable.CliTableBuilder;
import com.alibaba.fluss.metadata.PartitionInfo;
import com.alibaba.fluss.metadata.TableInfo;
import com.alibaba.fluss.metadata.TablePath;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.Strings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;

@Parameters(commandNames = "get", commandDescription = "get table info.")
public class GetTableCmd extends BaseCliCmd<TableParentCmd> {

    @Parameter(
            names = {"--db", "-d"},
            description = "Database name",
            required = true)
    private String database;

    @Parameter(
            names = {"--table", "-tb"},
            description = "table name",
            required = true)
    private String table;

    @Parameter(
            names = {"--partition-info"},
            description = "get partition info.",
            required = false)
    private boolean getPartitionInfo = false;

    @Parameter(
            names = {"--table-schema"},
            description = "get partition info.",
            required = false)
    private boolean getTableSchema = true;

    @Override
    protected int execute() throws Exception {
        CompletableFuture<TableInfo> tableInfo =
                getAdmin().getTableInfo(TablePath.of(database, table));
        TableInfo ti = tableInfo.get();
        System.out.printf("Get table (%s) info from database (%s).\n", table, database);
        printTableMetadata(ti);
        printTableProperty(ti);
        if (getTableSchema) {
            printTableSchema(ti);
        }
        if (getPartitionInfo) {
            printTablePartitionInfo(ti);
        }
        return 0;
    }

    private void printTablePartitionInfo(TableInfo ti) {
        System.out.println("Table Partition info");
        CompletableFuture<List<PartitionInfo>> partitionInfoFeature =
                getAdmin().listPartitionInfos(ti.getTablePath());
        try {
            List<PartitionInfo> partitionInfos = partitionInfoFeature.get();
            CliTableBuilder partitionInfoBuilder = CliTable.builder();
            partitionInfoBuilder.addHeader("Partition Id", "Partition Spec");
            partitionInfos.forEach(
                    partitionInfo -> {
                        partitionInfoBuilder.addRow(
                                String.valueOf(partitionInfo.getPartitionId()),
                                partitionInfo.getPartitionSpec().toString());
                    });
            partitionInfoBuilder.build().print();
        } catch (Exception e) {
            System.err.println("Error when get partition info. Error msg: " + e.getMessage());
        }
    }

    private void printTableSchema(TableInfo ti) {
        System.out.println("Table schema.");
        CliTableBuilder schemaBuilder = CliTable.builder();
        schemaBuilder.addHeader("Table Column Name", "Column Type");
        ti.getSchema()
                .getColumns()
                .forEach(
                        column ->
                                schemaBuilder.addRow(
                                        column.getName(), column.getDataType().asSummaryString()));
        schemaBuilder.build().print();
    }

    private void printTableMetadata(TableInfo ti) {
        System.out.println("Table metadata");
        CliTableBuilder cliTableBasicInfoBuilder = CliTable.builder();
        cliTableBasicInfoBuilder
                .addHeader("Table Basic Info", "Content")
                .addRow("tablePath", ti.getTablePath().toString())
                .addRow("schemaId", String.valueOf(ti.getSchemaId()))
                .addRow("numBuckets", String.valueOf(ti.getNumBuckets()))
                .addRow("createdTime", String.valueOf(ti.getCreatedTime()))
                .addRow("modifiedTime", String.valueOf(ti.getModifiedTime()))
                .addRow("primaryKeys", Strings.join(",", ti.getPrimaryKeys()))
                .addRow("physicalPrimaryKeys", Strings.join(",", ti.getPhysicalPrimaryKeys()))
                .addRow("bucketKeys", Strings.join(",", ti.getBucketKeys()))
                .addRow("partitionKeys", Strings.join(",", ti.getPartitionKeys()))
                .addRow("comment", ti.getComment().orElse(StringUtils.EMPTY));
        cliTableBasicInfoBuilder.build().print();
    }

    private void printTableProperty(TableInfo ti) {
        CliTableBuilder cliTablePropertiesBuilder = CliTable.builder();
        cliTablePropertiesBuilder.addHeader("Table Property", "Value");
        Map<String, String> props = new HashMap<>(ti.getProperties().toMap());
        props.putAll(ti.getCustomProperties().toMap());
        props.forEach(cliTablePropertiesBuilder::addRow);
        System.out.println("Table properties");
        cliTablePropertiesBuilder.build().print();
    }
}
