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

package com.alibaba.fluss.cli.cluster;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.BaseGroupCmd;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

// Cluster Command Group
@FlussCmd(name = "cluster", isGroup = true, description = "Cluster management commands")
@Parameters(commandDescription = "Cluster management operations")
public class ClusterGroupCmd extends BaseGroupCmd {

    public ClusterGroupCmd(JCommander cmdJc) {
        super(cmdJc);
    }
}
