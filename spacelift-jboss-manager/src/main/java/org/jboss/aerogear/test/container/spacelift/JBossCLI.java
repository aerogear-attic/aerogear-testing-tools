/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.test.container.spacelift;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.tool.Tool;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JBossCLI extends Tool<Object, ProcessResult> {

    private Map<String, String> environment;

    String user;

    String password;

    File file;

    String controller;

    boolean connect = false;

    List<String> command = new ArrayList<String>();

    public JBossCLI() {
        environment = new HashMap<String, String>();
        environment.put("JBOSS_HOME", System.getenv("JBOSS_HOME"));
    }

    public JBossCLI environment(String key, String value) {
        if (key != null) {
            environment.put(key, value);
        }
        return this;
    }

    public JBossCLI environment(Map<String, String> environment) {
        if (environment != null) {
            environment.putAll(environment);
        }
        return this;
    }

    public JBossCLI user(String user) {
        this.user = user;
        return this;
    }

    public JBossCLI password(String password) {
        this.password = password;
        return this;
    }

    public JBossCLI file(String file) {
        this.file = new File(file);
        return this;
    }

    public JBossCLI file(File file) {
        this.file = file;
        return this;
    }

    public JBossCLI controller(String controller) {
        this.controller = controller;
        return this;
    }

    public JBossCLI connect() {
        this.connect = true;
        return this;
    }

    public JBossCLI cliCommand(String... command) {
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    @Override
    protected Collection<String> aliases() {
        return Arrays.asList("jbosscli_binary");
    }

    @Override
    protected ProcessResult process(Object input) throws Exception {

        final CommandTool jbossCliTool = getJBossCliTool();

        if (controller != null) {
            jbossCliTool.parameter("--controller=" + controller);
        }

        if (connect) {
            jbossCliTool.parameter("--connect");
        }

        if (file != null) {
            jbossCliTool.parameter("--file=" + file.getAbsolutePath());
        }

        if (this.command.size() == 1) {
            jbossCliTool.parameter("--command=" + this.command.get(0));
        }

        if (this.command.size() > 1) {
            jbossCliTool.parameter("--commands=" + getCommands());
        }

        if (user != null) {
            jbossCliTool.parameter("--user=" + user);
        }

        if (password != null) {
            jbossCliTool.parameter("--password=" + password);
        }

        final ProcessResult processResult = jbossCliTool.execute().await();

        return processResult;
    }

    private String getCommands() {
        StringBuilder sb = new StringBuilder();
        String delim = "";

        for (String command : this.command) {
            sb.append(delim).append(command);
            delim = ",";
        }

        return sb.toString();
    }

    private CommandTool getJBossCliTool() {

        if (SystemUtils.IS_OS_WINDOWS) {
            return Tasks.prepare(CommandTool.class)
                .command(new CommandBuilder("cmd.exe"))
                .parameters("/C", new File(environment.get("JBOSS_HOME"), "/bin/jboss-cli.bat").getAbsolutePath())
                .addEnvironment(environment);
        } else {
            return Tasks.prepare(CommandTool.class)
                .command(new CommandBuilder(new File(environment.get("JBOSS_HOME"), "/bin/jboss-cli.sh").getAbsolutePath()))
                .addEnvironment(environment);
        }
    }

}
