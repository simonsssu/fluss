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

package com.alibaba.fluss.cli.base;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.client.admin.Admin;
import com.beust.jcommander.JCommander;
import java.util.function.Supplier;

public abstract class BaseCmd {

    protected Supplier<Admin> adminSupplier;

    protected JCommander cmdJc;

    protected abstract int execute();

    public void printUsage() {
        cmdJc.usage();
    }

    public void setAdminSupplier(Supplier<Admin> adminSupplier) {
        this.adminSupplier = adminSupplier;
    }

    public Admin getAdmin() {
        return adminSupplier.get();
    }

    public void setArgs(String[] args) {}

    protected String getCmdName() {
        return getCmdName(this.getClass());
    }

    protected String getCmdName(Class<?> clazz) {
        return clazz.getAnnotation(FlussCmd.class).name();
    }

    protected String getParentCmdName(Class<?> clazz) {
        return clazz.getAnnotation(FlussCmd.class).parentCmd().getName();
    }
}
