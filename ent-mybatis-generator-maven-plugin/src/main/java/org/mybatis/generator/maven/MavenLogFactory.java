/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.maven;

import org.apache.maven.plugin.Mojo;
import org.mybatis.generator.logging.AbstractLogFactory;
import org.mybatis.generator.logging.Log;

public class MavenLogFactory implements AbstractLogFactory {

    private final MavenLogImpl logImplementation;

    MavenLogFactory(Mojo mojo) {
        logImplementation = new MavenLogImpl(mojo.getLog());
    }

    @Override
    public Log getLog(Class<?> targetClass) {
        return logImplementation;
    }

}