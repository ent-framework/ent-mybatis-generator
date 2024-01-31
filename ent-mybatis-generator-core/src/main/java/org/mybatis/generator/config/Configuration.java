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
package org.mybatis.generator.config;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.exception.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class Configuration {

	private final List<Context> contexts;

	private final List<String> classPathEntries;

	public Configuration() {
		super();
		contexts = new ArrayList<>();
		classPathEntries = new ArrayList<>();
	}

	public void addClasspathEntry(String entry) {
		classPathEntries.add(entry);
	}

	public List<String> getClassPathEntries() {
		return classPathEntries;
	}

	/**
	 * This method does a simple validate, it makes sure that all required fields have
	 * been filled in and that all implementation classes exist and are of the proper
	 * type. It does not do any more complex operations such as: validating that database
	 * tables exist or validating that named columns exist
	 * @throws InvalidConfigurationException the invalid configuration exception
	 */
	public void validate() throws InvalidConfigurationException {
		List<String> errors = new ArrayList<>();

		for (String classPathEntry : classPathEntries) {
			if (!stringHasValue(classPathEntry)) {
				errors.add(getString("ValidationError.19")); //$NON-NLS-1$
				// only need to state this error once
				break;
			}
		}

		if (contexts.isEmpty()) {
			errors.add(getString("ValidationError.11")); //$NON-NLS-1$
		}
		else {

			for (Context context : contexts) {
				if (StringUtils.isNotBlank(context.getExtend()) && getContext(context.getExtend()).isPresent()) {
					Context extendContext = getContext(context.getExtend()).get();
					merge(context, extendContext);
				}
			}

			for (Context context : contexts) {
				context.validate(errors);
			}
		}

		if (!errors.isEmpty()) {
			throw new InvalidConfigurationException(errors);
		}
	}

	public List<Context> getContexts() {
		return contexts;
	}

	private Optional<Context> getContext(String id) {
		return contexts.stream().filter(context -> StringUtils.equals(id, context.getId())).findFirst();
	}

	public void addContext(Context context) {
		contexts.add(context);
	}

	private void merge(Context context, Context extendContext) {
		if (context.getJdbcConnectionConfiguration() == null
				&& extendContext.getJdbcConnectionConfiguration() != null) {
			context.setJdbcConnectionConfiguration(extendContext.getJdbcConnectionConfiguration());
		}
		context.getColumnGlobals().addAll(extendContext.getColumnGlobals());
		context.getTableConfigurations().addAll(extendContext.getTableConfigurations());

		context.getJoinConfig().getJoinDetailMap().putAll(extendContext.getJoinConfig().getJoinDetailMap());
	}

}
