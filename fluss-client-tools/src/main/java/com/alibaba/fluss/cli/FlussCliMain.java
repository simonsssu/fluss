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
import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.config.ConfigOptions;
import com.alibaba.fluss.config.Configuration;
import com.alibaba.fluss.config.GlobalConfiguration;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        name = "fluss",
        mixinStandardHelpOptions = true,
        version = "0.6.0",
        description = "Fluss Command Line Interface",
        subcommands = {CommandLine.HelpCommand.class})
public class FlussCliMain extends BaseCmd<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(FlussCliMain.class);

    // List to hold dynamically discovered subcommands
    private static final List<Object> SUB_COMMANDS =
            Collections.synchronizedList(new ArrayList<>());

    @CommandLine.Option(
            names = {"-c", "--configDir"},
            scope = CommandLine.ScopeType.INHERIT,
            paramLabel = "configuration directory",
            description = "Directory containing server.yaml configuration file.")
    public String configDir;

    @CommandLine.Option(
            names = {"-b", "--bootstrap-servers"},
            required = true,
            scope = CommandLine.ScopeType.INHERIT,
            description = "Cluster connection endpoints (format: host1:port1,host2:port2)",
            paramLabel = "HOST:PORT")
    public String bootstrapServers;

    public FlussCliMain() {
        initializeCommands();
    }

    /** Initializes subcommands during class loading. */
    private void initializeCommands() {
        try {
            Reflections reflections = new Reflections("com.alibaba.fluss.cli.admin.group");
            reflections.getTypesAnnotatedWith(FlussCmd.class).stream()
                    .sorted(Comparator.comparing(c -> c.getAnnotation(FlussCmd.class).name()))
                    .filter(c -> c.getAnnotation(FlussCmd.class).baseSuit())
                    .forEach(
                            clazz -> {
                                try {
                                    SUB_COMMANDS.add(clazz.getDeclaredConstructor().newInstance());
                                } catch (Exception ex) {
                                    handleCommandInitializationError(clazz, ex);
                                }
                            });

            if (SUB_COMMANDS.isEmpty()) {
                logger.warn("No subcommands detected in classpath");
            }
        } catch (Exception ex) {
            logger.error("Command auto-registration failed: {}", ex.getMessage());
            throw new IllegalStateException("Command initialization failure", ex);
        }
    }

    /** Handles errors during command initialization. */
    private static void handleCommandInitializationError(Class<?> clazz, Exception ex) {
        String errorMsg = String.format("Failed to load command: %s", clazz.getSimpleName());
        logger.error(errorMsg, ex);
        System.err.println(errorMsg);
    }

    /** Validates configuration directory existence. */
    protected void checkConfigPath(String path) {
        if (!Files.exists(Paths.get(path))) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this), "Config file not found: " + path);
        }
    }

    /**
     * Validates bootstrap server format.
     *
     * @param servers List of server addresses to validate
     * @throws CommandLine.ParameterException if any server has invalid format
     */
    public void checkBootstrapServers(List<String> servers) {
        if (servers == null || servers.isEmpty()) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "At least one bootstrap server required (format: host:port)");
        }

        servers.forEach(
                server -> {
                    if (!server.contains(":")) {
                        throw new CommandLine.ParameterException(
                                new CommandLine(this),
                                "Missing port in server: "
                                        + server
                                        + " (required format: host:port)");
                    }
                    if (!server.matches("^\\S+:\\d{1,5}$")) {
                        throw new CommandLine.ParameterException(
                                new CommandLine(this),
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

    public void validateConfiguration() {
        validateConfigurationSources();
        validateBootstrapServers();
    }

    private void validateConfigurationSources() {
        if (configDir == null && (bootstrapServers == null || bootstrapServers.isEmpty())) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this), "Required: --configDir or --bootstrap-servers");
        }
    }

    private void validateBootstrapServers() {
        if (configDir != null) {
            checkConfigPath(configDir);
            Configuration config = GlobalConfiguration.loadConfiguration(configDir, null);
            this.bootstrapServers = getBootStrapServers(config);
        }
        checkBootstrapServers(Arrays.asList(this.bootstrapServers.split(",")));
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /**
     * Main command execution logic.
     *
     * @return CLI exit code
     */
    @Override
    public Integer call() {
        validateConfiguration();
        CommandLine.usage(this, System.out);
        return 0;
    }

    /** CLI entry point with proper exit code handling. */
    public static void main(String[] args) {
        try {
            CommandLine cmd = new CommandLine(new FlussCliMain());
            SUB_COMMANDS.forEach(cmd::addSubcommand);
            System.exit(cmd.execute(args));
        } catch (CommandLine.ParameterException ex) {
            ex.getCommandLine().usage(System.err);
            System.exit(2); // Invalid parameter exit code
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Critical execution failure: {}", ex.getMessage());
            System.exit(1); // General error exit code
        }
    }
}
