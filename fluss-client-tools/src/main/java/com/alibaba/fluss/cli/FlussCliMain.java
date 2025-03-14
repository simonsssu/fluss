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

import com.alibaba.fluss.cli.base.BaseCmd;
import com.alibaba.fluss.cli.base.GroupBaseCmd;
import com.alibaba.fluss.cli.conn.ConnectionManager;
import com.alibaba.fluss.client.Connection;
import com.alibaba.fluss.client.admin.Admin;
import com.alibaba.fluss.config.ConfigOptions;
import com.alibaba.fluss.config.Configuration;
import com.alibaba.fluss.config.GlobalConfiguration;
import com.alibaba.fluss.shaded.guava32.com.google.common.collect.Lists;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@CommandLine.Command(
        name = "fluss",
        mixinStandardHelpOptions = true,
        version = "0.6.0",
        description = "Fluss Command Line Interface")
public class FlussCliMain extends BaseCmd<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(FlussCliMain.class);

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

    private final Supplier<Admin> adminSupplier;

    private static final List<GroupBaseCmd> SUB_GROUP_CMD = Lists.newArrayList();

    public FlussCliMain() {
        this.adminSupplier = this::getAdmin;
        initializeGroupCmd();
    }

    private void initializeGroupCmd() {
        Reflections reflections = new Reflections("com.alibaba.fluss.cli");
        reflections
                .getSubTypesOf(GroupBaseCmd.class)
                .forEach(
                        clazz -> {
                            try {
                                GroupBaseCmd cmd = clazz.getDeclaredConstructor().newInstance();
                                cmd.setAdminSupplier(adminSupplier);
                                SUB_GROUP_CMD.add(cmd);
                            } catch (Exception e) {
                                logger.error(
                                        "Failed to initialize group command: {}", clazz.getName());
                            }
                        });
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

    public Admin getAdmin() {
        try (Connection connection = ConnectionManager.getConnection(getBootstrapServers())) {
            return connection.getAdmin();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to server", e);
        }
    }

    /** CLI entry point with proper exit code handling. */
    public static void main(String[] args) {
        try {
            CommandLine cmd = new CommandLine(new FlussCliMain());
            SUB_GROUP_CMD.forEach(cmd::addSubcommand);
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
