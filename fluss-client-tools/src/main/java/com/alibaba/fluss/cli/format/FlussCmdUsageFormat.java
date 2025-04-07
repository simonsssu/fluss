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

package com.alibaba.fluss.cli.format;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.ProgramName;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.UnixStyleUsageFormatter;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class FlussCmdUsageFormat extends UnixStyleUsageFormatter {

    private final JCommander commander;

    public FlussCmdUsageFormat(JCommander commander) {
        super(commander);
        this.commander = commander;
    }

    @Override
    public void appendCommands(
            StringBuilder out, int indentCount, int descriptionIndent, String indent) {
        List<Entry<ProgramName, JCommander>> visibleCommands =
                commander.getRawCommands().entrySet().stream()
                        .filter(
                                entry -> {
                                    Object arg = entry.getValue().getObjects().get(0);
                                    Parameters p = arg.getClass().getAnnotation(Parameters.class);
                                    return Objects.isNull(p) || !p.hidden();
                                })
                        .collect(Collectors.toList());
        if (visibleCommands.isEmpty()) {
            return;
        }
        int maxDispNameLength =
                visibleCommands.stream()
                        .map(entry -> entry.getKey().getDisplayName())
                        .mapToInt(String::length)
                        .max()
                        .orElse(0);
        int spaceBetweenNameAndDesc = 8;
        for (Entry<ProgramName, JCommander> entry : visibleCommands) {
            JCommander.ProgramName progName = entry.getKey();
            String dispName = progName.getDisplayName();
            String description = getCommandDescription(progName.getName());
            String line =
                    indent
                            + s(4)
                            + dispName
                            + s(maxDispNameLength - dispName.length() + spaceBetweenNameAndDesc)
                            + description;
            wrapDescription(out, indentCount + descriptionIndent, line);
            out.append("\n");
        }
    }
}
