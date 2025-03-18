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

import com.alibaba.fluss.cli.annotation.FlussCmdGroup;
import com.alibaba.fluss.cli.cluster.ClusterGroupCmd;
import com.alibaba.fluss.cli.database.DatabaseGroupCmd;
import com.alibaba.fluss.cli.table.TableGroupCmd;
import com.alibaba.fluss.config.ConfigOptions;
import com.alibaba.fluss.config.Configuration;
import com.alibaba.fluss.config.GlobalConfiguration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Parameters(commandDescription = "Fluss Distributed Stream Processing Platform CLI")
public class FlussCliMain {
    private static final Logger logger = LoggerFactory.getLogger(FlussCliMain.class);

    // Global Parameters
    @Parameter(
            names = {"--bootstrap-server", "-b"},
            description = "Cluster bootstrap servers (comma-separated)")
    private String bootstrapServers;

    @Parameter(
            names = {"--config", "-c"},
            description = "Path to configuration file")
    private String configFile;

    @Parameter(
            names = {"--version", "-v"},
            description = "Show version info",
            help = true)
    private boolean version;

    @Parameter(
            names = {"--help", "-h"},
            description = "Show usage help",
            help = true)
    private boolean help;

    private final JCommander jc;

    private static final List<String> keywords = new ArrayList<>();

    public static void main(String[] args) {
        FlussCliMain cli = new FlussCliMain();
        int exitCode = cli.run(args);
        System.exit(exitCode);
    }

    public FlussCliMain() {
        jc = new JCommander(this);
        jc.setProgramName("fluss");

        // Register commands
        autoRegisterCommands();
    }

    private void autoRegisterCommands() {
        try {
            Reflections reflections = new Reflections("com.alibaba.fluss.cli");
            Set<Class<?>> commands = reflections.getTypesAnnotatedWith(FlussCmdGroup.class);

            for (Class<?> clazz : commands) {
                FlussCmdGroup annotation = clazz.getAnnotation(FlussCmdGroup.class);
                keywords.add(annotation.name());
                Object commandInstance = clazz.getDeclaredConstructor().newInstance();
                jc.addCommand(annotation.name(), commandInstance);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to auto-register commands", e);
        }
    }

    private void loadConfig() {
        if (configFile != null) {
            if (!Files.exists(Paths.get(configFile))) {
                throw new ParameterException("Config file not found: " + configFile);
            }
            Configuration config = GlobalConfiguration.loadConfiguration(configFile, null);
            bootstrapServers = getBootStrapServers(config);
        }
    }

    private void validateConfig() {
        if (bootstrapServers == null) {
            throw new ParameterException(
                    "Bootstrap servers required (format: host1:port1,host2:port2,....)");
        }
        checkBootstrapServers(Arrays.asList(bootstrapServers.split(",")));
    }

    /**
     * Validates bootstrap server format.
     *
     * @param servers List of server addresses to validate
     * @throws ParameterException if any server has invalid format
     */
    private void checkBootstrapServers(List<String> servers) {
        if (servers == null || servers.isEmpty()) {
            throw new ParameterException(
                    "At least one bootstrap server required (format: host:port)");
        }

        servers.forEach(
                server -> {
                    if (!server.contains(":")) {
                        throw new ParameterException(
                                "Missing port in server: "
                                        + server
                                        + " (required format: host:port)");
                    }
                    if (!server.matches("^\\S+:\\d{1,5}$")) {
                        throw new ParameterException(
                                "Invalid server format: " + server + " (expected host:port)");
                    }
                });
    }

    /**
     * Resolves bootstrap servers from configuration with fallback logic: 1. Direct CLI parameter 2.
     * Configuration file values 3. Coordinator host+port combination
     */
    private static String getBootStrapServers(Configuration configuration) {
        Map<String, String> configMap = configuration.toMap();
        String bootstrapServers = configMap.get(ConfigOptions.BOOTSTRAP_SERVERS.key());
        if (bootstrapServers != null) {
            return bootstrapServers;
        }

        String coordinatorHost = configuration.get(ConfigOptions.COORDINATOR_HOST);
        if (coordinatorHost == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Missing coordinator configuration. Specify either %s or %s",
                            ConfigOptions.BOOTSTRAP_SERVERS.key(),
                            ConfigOptions.COORDINATOR_HOST.key()));
        }
        String coordinatorPort = configuration.get(ConfigOptions.COORDINATOR_PORT);
        return coordinatorHost + ":" + coordinatorPort;
    }

    public int run(String[] args) {
        try {
            List<String[]> splitArgs = splitByKeywords(args);
            jc.parse(splitArgs.get(0));

            if (version) {
                printVersion();
                return 0;
            }

            if (help || jc.getParsedCommand() == null) {
                printUsage();
                return 0;
            }

            loadConfig();
            validateConfig();

            return dispatchCommand(splitArgs.get(1));

        } catch (ParameterException e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Parameter error: {}", e.getMessage(), e);
            printCommandUsage();
            return 1;
        }
    }

    private static List<String[]> splitByKeywords(String[] args) {
        int splitIndex = -1;

        for (int i = 0; i < args.length; i++) {
            if (keywords.contains(args[i])) {
                splitIndex = i;
                break;
            }
        }

        if (splitIndex == -1) {
            return Arrays.asList(args.clone(), new String[0]);
        }

        String[] mainArgs = Arrays.copyOfRange(args, 0, splitIndex + 1);
        String[] subArgs = Arrays.copyOfRange(args, splitIndex + 1, args.length);

        return Arrays.asList(mainArgs, subArgs);
    }

    private int dispatchCommand(String[] subArgs) {
        String command = jc.getParsedCommand();
        switch (command) {
            case "table":
                TableGroupCmd tableCmd = new TableGroupCmd();
                tableCmd.setArgs(subArgs);
                return tableCmd.execute();
            case "cluster":
                ClusterGroupCmd clusterCmd = new ClusterGroupCmd();
                clusterCmd.setArgs(subArgs);
                return clusterCmd.execute();
            case "database":
                DatabaseGroupCmd databaseCmd = new DatabaseGroupCmd();
                databaseCmd.setArgs(subArgs);
                return databaseCmd.execute();
            default:
                printUsage();
                return 1;
        }
    }

    private void printVersion() {
        System.out.println("Fluss CLI: v0.1.0\nRuntime Version: Fluss Core 0.7.0\n");
    }

    private void printUsage() {
        jc.usage();
    }

    private void printCommandUsage() {
        String command = jc.getParsedCommand();
        if (command != null) {
            JCommander subCommander = jc.getCommands().get(command);
            if (subCommander != null) {
                subCommander.usage();
            }
        } else {
            printUsage();
        }
    }
}
