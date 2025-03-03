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
import com.alibaba.fluss.cli.admin.ClusterNodesCmd;
import com.alibaba.fluss.cli.annotation.FlussCmd;

import picocli.CommandLine;

@FlussCmd(name = "cluster")
@CommandLine.Command(
        name = "cluster",
        description = "Cluster operations",
        subcommands = {ClusterNodesCmd.class, CommandLine.HelpCommand.class})
public class ClusterGroupCmd extends AdminBaseCmd {}
