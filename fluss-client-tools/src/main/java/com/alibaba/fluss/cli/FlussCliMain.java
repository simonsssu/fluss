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

package com.alibaba.fluss.cli;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.CmdBase;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CommandLine.Command(
        name = "fluss",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Fluss Command Line Interface",
        subcommands = {CommandLine.HelpCommand.class})
public class FlussCliMain extends CmdBase<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(FlussCliMain.class);

    private static final List<Object> SUB_COMMANDS = new ArrayList<>();

    protected static final String FLUSS_HOME =
            System.getProperty("fluss.home", System.getenv("FLUSS_HOME"));

    @CommandLine.Option(
            names = {"--config"},
            description = "Config file path",
            defaultValue = "${FLUSS_HOME}/conf/fluss.conf")
    protected String configPath;

    @CommandLine.Option(
            names = {"--bootstrap-servers"},
            required = true,
            split = ",",
            description =
                    "Host:port pairs for cluster connection (format: host1:port1,host2:port2)",
            paramLabel = "HOST:PORT")
    protected List<String> bootstrapServers;

    static {
        initSubCommands();
    }

    private static void initSubCommands() {
        try {
            Reflections reflections = new Reflections("com.alibaba.fluss.cli.cmd");
            Set<Class<?>> commands = reflections.getTypesAnnotatedWith(FlussCmd.class);

            commands.stream()
                    .sorted(Comparator.comparing(c -> c.getAnnotation(FlussCmd.class).name()))
                    .forEach(
                            clazz -> {
                                try {
                                    SUB_COMMANDS.add(clazz.getDeclaredConstructor().newInstance());
                                } catch (Exception ex) {
                                    System.err.println(
                                            "Failed to load command: " + clazz.getSimpleName());
                                    logger.error(
                                            "Failed to load command: " + clazz.getSimpleName(), ex);
                                }
                            });
        } catch (Exception ex) {
            logger.error("Command auto-registration failed", ex);
        }
    }

    protected void checkConfigPath() {
        if (!Files.exists(Paths.get(configPath))) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this), "Config file not found: " + configPath);
        }
    }

    public void checkBootstrapServers() {
        Optional.ofNullable(bootstrapServers).filter(servers -> !servers.isEmpty())
                .orElseThrow(
                        () ->
                                new CommandLine.ParameterException(
                                        new CommandLine(this),
                                        "At least one bootstrap server is required"))
                .stream()
                .filter(server -> !server.matches(".+:\\d+"))
                .findFirst()
                .ifPresent(
                        server -> {
                            throw new CommandLine.ParameterException(
                                    new CommandLine(this), "Invalid server format: " + server);
                        });
    }

    @Override
    public Integer call() {
        // print usage
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String[] args) {
        try {
            CommandLine cmd = new CommandLine(new FlussCliMain());
            SUB_COMMANDS.forEach(cmd::addSubcommand);
            System.exit(cmd.execute(args));
        } catch (CommandLine.ParameterException ex) {
            ex.getCommandLine().usage(System.err);
            System.exit(2);
        } catch (Exception ex) {
            logger.error("Command execution failed", ex);
            System.exit(1);
        }
    }

    @Override
    protected void checkRequiredArgs() {
        checkBootstrapServers();
        checkConfigPath();
    }
}
