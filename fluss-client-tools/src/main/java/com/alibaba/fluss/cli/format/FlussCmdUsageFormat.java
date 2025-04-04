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
import com.beust.jcommander.Parameters;
import com.beust.jcommander.UnixStyleUsageFormatter;

import java.util.Map;
import java.util.Objects;

public class FlussCmdUsageFormat extends UnixStyleUsageFormatter {

    private final JCommander commander;

    public FlussCmdUsageFormat(JCommander commander) {
        super(commander);
        this.commander = commander;
    }

    @Override
    public void appendCommands(
            StringBuilder out, int indentCount, int descriptionIndent, String indent) {
        boolean hasOnlyHiddenCommands = true;
        for (Map.Entry<JCommander.ProgramName, JCommander> commands :
                commander.getRawCommands().entrySet()) {
            Object arg = commands.getValue().getObjects().get(0);
            Parameters p = arg.getClass().getAnnotation(Parameters.class);
            if (p == null || !p.hidden()) {
                hasOnlyHiddenCommands = false;
            }
        }

        if (!hasOnlyHiddenCommands) {
            out.append(indent + "  Commands:\n");
            int dispNamePrefixIndent = 0;
            for (Map.Entry<JCommander.ProgramName, JCommander> commands :
                    commander.getRawCommands().entrySet()) {
                Object arg = commands.getValue().getObjects().get(0);
                Parameters p = arg.getClass().getAnnotation(Parameters.class);
                if (Objects.isNull(p) || !p.hidden()) {
                    JCommander.ProgramName programName = commands.getKey();
                    String dispName = programName.getDisplayName();
                    if (dispName.length() > dispNamePrefixIndent) {
                        dispNamePrefixIndent = dispName.length();
                    }
                }
            }
            // The magic value 3 is the number of spaces between the name of the option and its
            // description
            for (Map.Entry<JCommander.ProgramName, JCommander> commands :
                    commander.getRawCommands().entrySet()) {
                Object arg = commands.getValue().getObjects().get(0);
                Parameters p = arg.getClass().getAnnotation(Parameters.class);

                if (p == null || !p.hidden()) {
                    JCommander.ProgramName progName = commands.getKey();
                    String dispName = progName.getDisplayName();
                    String description =
                            indent
                                    + s(4)
                                    + dispName
                                    + s(dispNamePrefixIndent - dispName.length() + 8)
                                    + getCommandDescription(progName.getName());
                    wrapDescription(out, indentCount + descriptionIndent, description);
                    out.append("\n");
                }
            }
        }
    }
}
