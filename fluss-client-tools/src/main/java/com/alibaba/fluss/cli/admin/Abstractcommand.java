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

package com.alibaba.fluss.cli.admin;

import com.alibaba.fluss.cli.annotation.FlussCmd;
import com.alibaba.fluss.cli.base.BaseCmd;
import java.util.Set;
import java.util.stream.Stream;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public abstract class Abstractcommand extends BaseCmd<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(Abstractcommand.class);

    private final CommandLine commandLine;

    public Abstractcommand() {
        this.commandLine = new CommandLine(this);
        initializeCommands();
    }

    private void initializeCommands() {
        try {
            Reflections reflections = new Reflections("com.alibaba.fluss.cli");
            Set<Class<? extends Abstractcommand>> subTypes =
                    reflections.getSubTypesOf(Abstractcommand.class);
            subTypes.stream()
                    .map(Class::getDeclaredClasses)
                    .flatMap(Stream::of)
                    .filter(innerClass -> innerClass.isAnnotationPresent(FlussCmd.class))
                    .forEach(this::tryToAddSubcommand);
        } catch (Exception ex) {
            logger.error("Command auto-registration failed: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Command initialization failure", ex);
        }
    }

    private void tryToAddSubcommand(Class<?> innerClass) {
        try {
            Object instance = innerClass.getDeclaredConstructor().newInstance();
            commandLine.addSubcommand(instance);
            logger.info("Successfully added subcommand: {}", innerClass.getSimpleName());
        } catch (NoSuchMethodException e) {
            logger.error(
                    "No default constructor found for class: {}", innerClass.getSimpleName(), e);
        } catch (Exception e) {
            logger.error("Failed to load inner command: {}", innerClass.getSimpleName(), e);
        }
    }
}
