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
package org.mybatis.generator.config.xml;

import static org.mybatis.generator.config.xml.PlusXmlConfig.*;
import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.StringUtility;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class parses configuration files into the new Configuration API.
 *
 * @author Jeff Butler
 */
public class MyBatisGeneratorConfigurationParser {

	private final Properties extraProperties;

	private final Properties configurationProperties;

	public MyBatisGeneratorConfigurationParser(Properties extraProperties) {
		super();
		if (extraProperties == null) {
			this.extraProperties = new Properties();
		}
		else {
			this.extraProperties = extraProperties;
		}
		configurationProperties = new Properties();
	}

	public Configuration parseConfiguration(Element rootNode) throws XMLParserException {

		Configuration configuration = new Configuration();

		NodeList nodeList = rootNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("properties".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperties(childNode);
			}
			else if ("classPathEntry".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseClassPathEntry(configuration, childNode);
			}
			else if ("context".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseContext(configuration, childNode);
			}
		}

		return configuration;
	}

	protected void parseProperties(Node node) throws XMLParserException {
		Properties attributes = parseAttributes(node);
		String resource = attributes.getProperty("resource"); //$NON-NLS-1$
		String url = attributes.getProperty("url"); //$NON-NLS-1$

		if (!stringHasValue(resource) && !stringHasValue(url)) {
			throw new XMLParserException(getString("RuntimeError.14")); //$NON-NLS-1$
		}

		if (stringHasValue(resource) && stringHasValue(url)) {
			throw new XMLParserException(getString("RuntimeError.14")); //$NON-NLS-1$
		}

		URL resourceUrl;

		try {
			if (stringHasValue(resource)) {
				resourceUrl = ObjectFactory.getResource(resource);
				if (resourceUrl == null) {
					throw new XMLParserException(getString("RuntimeError.15", resource)); //$NON-NLS-1$
				}
			}
			else {
				resourceUrl = new URL(url);
			}

			InputStream inputStream = resourceUrl.openConnection().getInputStream();

			configurationProperties.load(inputStream);
			inputStream.close();
		}
		catch (IOException e) {
			if (stringHasValue(resource)) {
				throw new XMLParserException(getString("RuntimeError.16", resource)); //$NON-NLS-1$
			}
			else {
				throw new XMLParserException(getString("RuntimeError.17", url)); //$NON-NLS-1$
			}
		}
	}

	private void parseContext(Configuration configuration, Node node) {

		Properties attributes = parseAttributes(node);
		String defaultModelType = attributes.getProperty("defaultModelType"); //$NON-NLS-1$
		String targetRuntime = attributes.getProperty("targetRuntime"); //$NON-NLS-1$
		String introspectedColumnImpl = attributes.getProperty("introspectedColumnImpl"); //$NON-NLS-1$
		String id = attributes.getProperty("id"); //$NON-NLS-1$
		String extend = attributes.getProperty("extend");
		ModelType mt = defaultModelType == null ? null : ModelType.getModelType(defaultModelType);

		Context context = new Context(mt);
		context.setId(id);
		context.setExtend(extend);
		if (stringHasValue(introspectedColumnImpl)) {
			context.setIntrospectedColumnImpl(introspectedColumnImpl);
		}
		if (stringHasValue(targetRuntime)) {
			context.setTargetRuntime(targetRuntime);
		}

		configuration.addContext(context);

		NodeList nodeList = node.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(context, childNode);
			}
		}

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			else if ("plugin".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parsePlugin(context, childNode);
			}
			else if ("commentGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseCommentGenerator(context, childNode);
			}
			else if ("jdbcConnection".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseJdbcConnection(context, childNode);
			}
			else if ("connectionFactory".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseConnectionFactory(context, childNode);
			}
			else if ("javaModelGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseJavaModelGenerator(context, childNode);
			}
			else if ("javaTypeResolver".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseJavaTypeResolver(context, childNode);
			}
			else if ("sqlMapGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseSqlMapGenerator(context, childNode);
			}
			else if ("javaClientGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseJavaClientGenerator(context, childNode);
			}
			else if ("table".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseTable(context, childNode);
			}
			else if (JOIN_ENTRY_ARG.equals(childNode.getNodeName())) {
				parseJoinEntry(context, childNode);
			}
			else if ("columnOverride".equals(childNode.getNodeName())) {
				context.addColumnGlobal(parseColumnOverride(childNode));
			}
		}
	}

	protected void parseSqlMapGenerator(Context context, Node node) {
		SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();

		context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String targetPackage = parsePropertyTokens(context, attributes.getProperty("targetPackage")); //$NON-NLS-1$
		String targetProject = attributes.getProperty("targetProject"); //$NON-NLS-1$

		sqlMapGeneratorConfiguration.setTargetPackage(targetPackage);
		sqlMapGeneratorConfiguration.setTargetProject(targetProject);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(context, sqlMapGeneratorConfiguration, childNode);
			}
		}
	}

	protected void parseTable(Context context, Node node) {
		TableConfiguration tc = new TableConfiguration(context);
		context.addTableConfiguration(tc);

		Properties attributes = parseAttributes(node);

		String catalog = attributes.getProperty("catalog"); //$NON-NLS-1$
		if (stringHasValue(catalog)) {
			tc.setCatalog(catalog);
		}

		String schema = attributes.getProperty("schema"); //$NON-NLS-1$
		if (stringHasValue(schema)) {
			tc.setSchema(schema);
		}

		String tableName = attributes.getProperty("tableName"); //$NON-NLS-1$
		if (stringHasValue(tableName)) {
			tc.setTableName(tableName);
		}

		String domainObjectName = attributes.getProperty("domainObjectName"); //$NON-NLS-1$
		if (stringHasValue(domainObjectName)) {
			tc.setDomainObjectName(domainObjectName);
		}

		String alias = attributes.getProperty("alias"); //$NON-NLS-1$
		if (stringHasValue(alias)) {
			tc.setAlias(alias);
		}

		String enableInsert = attributes.getProperty("enableInsert"); //$NON-NLS-1$
		if (stringHasValue(enableInsert)) {
			tc.setInsertStatementEnabled(isTrue(enableInsert));
		}

		String enableSelectByPrimaryKey = attributes.getProperty("enableSelectByPrimaryKey"); //$NON-NLS-1$
		if (stringHasValue(enableSelectByPrimaryKey)) {
			tc.setSelectByPrimaryKeyStatementEnabled(isTrue(enableSelectByPrimaryKey));
		}

		String enableSelectByExample = attributes.getProperty("enableSelectByExample"); //$NON-NLS-1$
		if (stringHasValue(enableSelectByExample)) {
			tc.setSelectByExampleStatementEnabled(isTrue(enableSelectByExample));
		}

		String enableUpdateByPrimaryKey = attributes.getProperty("enableUpdateByPrimaryKey"); //$NON-NLS-1$
		if (stringHasValue(enableUpdateByPrimaryKey)) {
			tc.setUpdateByPrimaryKeyStatementEnabled(isTrue(enableUpdateByPrimaryKey));
		}

		String enableDeleteByPrimaryKey = attributes.getProperty("enableDeleteByPrimaryKey"); //$NON-NLS-1$
		if (stringHasValue(enableDeleteByPrimaryKey)) {
			tc.setDeleteByPrimaryKeyStatementEnabled(isTrue(enableDeleteByPrimaryKey));
		}

		String enableDeleteByExample = attributes.getProperty("enableDeleteByExample"); //$NON-NLS-1$
		if (stringHasValue(enableDeleteByExample)) {
			tc.setDeleteByExampleStatementEnabled(isTrue(enableDeleteByExample));
		}

		String enableCountByExample = attributes.getProperty("enableCountByExample"); //$NON-NLS-1$
		if (stringHasValue(enableCountByExample)) {
			tc.setCountByExampleStatementEnabled(isTrue(enableCountByExample));
		}

		String enableUpdateByExample = attributes.getProperty("enableUpdateByExample"); //$NON-NLS-1$
		if (stringHasValue(enableUpdateByExample)) {
			tc.setUpdateByExampleStatementEnabled(isTrue(enableUpdateByExample));
		}

		String selectByPrimaryKeyQueryId = attributes.getProperty("selectByPrimaryKeyQueryId"); //$NON-NLS-1$
		if (stringHasValue(selectByPrimaryKeyQueryId)) {
			tc.setSelectByPrimaryKeyQueryId(selectByPrimaryKeyQueryId);
		}

		String selectByExampleQueryId = attributes.getProperty("selectByExampleQueryId"); //$NON-NLS-1$
		if (stringHasValue(selectByExampleQueryId)) {
			tc.setSelectByExampleQueryId(selectByExampleQueryId);
		}

		String modelType = attributes.getProperty("modelType"); //$NON-NLS-1$
		if (stringHasValue(modelType)) {
			tc.setConfiguredModelType(modelType);
		}

		String escapeWildcards = attributes.getProperty("escapeWildcards"); //$NON-NLS-1$
		if (stringHasValue(escapeWildcards)) {
			tc.setWildcardEscapingEnabled(isTrue(escapeWildcards));
		}

		String delimitIdentifiers = attributes.getProperty("delimitIdentifiers"); //$NON-NLS-1$
		if (stringHasValue(delimitIdentifiers)) {
			tc.setDelimitIdentifiers(isTrue(delimitIdentifiers));
		}

		String delimitAllColumns = attributes.getProperty("delimitAllColumns"); //$NON-NLS-1$
		if (stringHasValue(delimitAllColumns)) {
			tc.setAllColumnDelimitingEnabled(isTrue(delimitAllColumns));
		}

		String mapperName = attributes.getProperty("mapperName"); //$NON-NLS-1$
		if (stringHasValue(mapperName)) {
			tc.setMapperName(mapperName);
		}

		String sqlProviderName = attributes.getProperty("sqlProviderName"); //$NON-NLS-1$
		if (stringHasValue(sqlProviderName)) {
			tc.setSqlProviderName(sqlProviderName);
		}

		String logicDeleteColumn = attributes.getProperty("logicDeleteColumn"); //$NON-NLS-1$
		if (stringHasValue(logicDeleteColumn)) {
			tc.setLogicDeleteColumn(logicDeleteColumn);
		}

		String tenantColumn = attributes.getProperty("tenantColumn"); //$NON-NLS-1$
		if (stringHasValue(tenantColumn)) {
			tc.setTenantColumn(tenantColumn);
		}

		String versionColumn = attributes.getProperty("versionColumn"); //$NON-NLS-1$
		if (stringHasValue(versionColumn)) {
			tc.setVersionColumn(versionColumn);
		}

		String parentTable = attributes.getProperty("parentTable"); //$NON-NLS-1$
		if (stringHasValue(parentTable)) {
			tc.setParentTable(parentTable);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(context, tc, childNode);
			}
			else if ("columnOverride".equals(childNode.getNodeName())) { //$NON-NLS-1$
				tc.addColumnOverride(parseColumnOverride(childNode));
			}
			else if ("ignoreColumn".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseIgnoreColumn(tc, childNode);
			}
			else if ("ignoreColumnsByRegex".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseIgnoreColumnByRegex(tc, childNode);
			}
			else if ("generatedKey".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseGeneratedKey(tc, childNode);
			}
			else if ("domainObjectRenamingRule".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseDomainObjectRenamingRule(tc, childNode);
			}
			else if ("columnRenamingRule".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseColumnRenamingRule(tc, childNode);
			} else if ("ui".equals(childNode.getNodeName())) {
				UIConfig uiConfig = parseUIConfig(childNode);
				tc.setUiConfig(uiConfig);
			}
		}
	}

	private ColumnOverride parseColumnOverride(Node node) {
		Properties attributes = parseAttributes(node);
		String column = attributes.getProperty("column"); //$NON-NLS-1$

		ColumnOverride co = new ColumnOverride(column);

		String property = attributes.getProperty("property"); //$NON-NLS-1$
		if (stringHasValue(property)) {
			co.setJavaProperty(property);
		}

		String javaType = attributes.getProperty("javaType"); //$NON-NLS-1$
		if (stringHasValue(javaType)) {
			co.setJavaType(javaType);
		}

		String jdbcType = attributes.getProperty("jdbcType"); //$NON-NLS-1$
		if (stringHasValue(jdbcType)) {
			co.setJdbcType(jdbcType);
		}

		String typeHandler = attributes.getProperty("typeHandler"); //$NON-NLS-1$
		if (stringHasValue(typeHandler)) {
			co.setTypeHandler(typeHandler);
		}

		String delimitedColumnName = attributes.getProperty("delimitedColumnName"); //$NON-NLS-1$
		if (stringHasValue(delimitedColumnName)) {
			co.setColumnNameDelimited(isTrue(delimitedColumnName));
		}

		String isGeneratedAlways = attributes.getProperty("isGeneratedAlways"); //$NON-NLS-1$
		if (stringHasValue(isGeneratedAlways)) {
			co.setGeneratedAlways(Boolean.parseBoolean(isGeneratedAlways));
		}

		String defaultValue = attributes.getProperty("defaultValue");
		if (stringHasValue(defaultValue)) {
			co.setDefaultValue(defaultValue);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(co, childNode);
			}

			if (CUSTOM_GENERIC_TYPE_ARG.equals(childNode.getNodeName())) { // $NON-NLS-1$
				Properties childAttributes = parseAttributes(childNode);
				String elementType = childAttributes.getProperty(CUSTOM_JAVA_TYPE_ARG);
				co.addGenericType(elementType);
			}
		}
		return co;
	}

	private void parseGeneratedKey(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);

		String column = attributes.getProperty("column"); //$NON-NLS-1$
		boolean identity = isTrue(attributes.getProperty("identity")); //$NON-NLS-1$
		String sqlStatement = attributes.getProperty("sqlStatement"); //$NON-NLS-1$
		String type = attributes.getProperty("type"); //$NON-NLS-1$

		GeneratedKey gk = new GeneratedKey(column, sqlStatement, identity, type);

		tc.setGeneratedKey(gk);
	}

	private void parseIgnoreColumn(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String column = attributes.getProperty("column"); //$NON-NLS-1$
		String delimitedColumnName = attributes.getProperty("delimitedColumnName"); //$NON-NLS-1$

		IgnoredColumn ic = new IgnoredColumn(column);

		if (stringHasValue(delimitedColumnName)) {
			ic.setColumnNameDelimited(isTrue(delimitedColumnName));
		}

		tc.addIgnoredColumn(ic);
	}

	private void parseIgnoreColumnByRegex(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String pattern = attributes.getProperty("pattern"); //$NON-NLS-1$

		IgnoredColumnPattern icPattern = new IgnoredColumnPattern(pattern);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("except".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseException(icPattern, childNode);
			}
		}

		tc.addIgnoredColumnPattern(icPattern);
	}

	private void parseException(IgnoredColumnPattern icPattern, Node node) {
		Properties attributes = parseAttributes(node);
		String column = attributes.getProperty("column"); //$NON-NLS-1$
		String delimitedColumnName = attributes.getProperty("delimitedColumnName"); //$NON-NLS-1$

		IgnoredColumnException exception = new IgnoredColumnException(column);

		if (stringHasValue(delimitedColumnName)) {
			exception.setColumnNameDelimited(isTrue(delimitedColumnName));
		}

		icPattern.addException(exception);
	}

	private void parseDomainObjectRenamingRule(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String searchString = attributes.getProperty("searchString"); //$NON-NLS-1$
		String replaceString = attributes.getProperty("replaceString"); //$NON-NLS-1$

		DomainObjectRenamingRule dorr = new DomainObjectRenamingRule();

		dorr.setSearchString(searchString);

		if (stringHasValue(replaceString)) {
			dorr.setReplaceString(replaceString);
		}

		tc.setDomainObjectRenamingRule(dorr);
	}

	private void parseColumnRenamingRule(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String searchString = attributes.getProperty("searchString"); //$NON-NLS-1$
		String replaceString = attributes.getProperty("replaceString"); //$NON-NLS-1$

		ColumnRenamingRule crr = new ColumnRenamingRule();

		crr.setSearchString(searchString);

		if (stringHasValue(replaceString)) {
			crr.setReplaceString(replaceString);
		}

		tc.setColumnRenamingRule(crr);
	}

	protected void parseJavaTypeResolver(Context context, Node node) {
		JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();

		context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type"); //$NON-NLS-1$

		if (stringHasValue(type)) {
			javaTypeResolverConfiguration.setConfigurationType(type);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(context, javaTypeResolverConfiguration, childNode);
			}
		}
	}

	private void parsePlugin(Context context, Node node) {
		PluginConfiguration pluginConfiguration = new PluginConfiguration();

		context.addPluginConfiguration(pluginConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type"); //$NON-NLS-1$

		pluginConfiguration.setConfigurationType(type);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(context, pluginConfiguration, childNode);
			}
		}
	}

	protected void parseJavaModelGenerator(Context context, Node node) {
		JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();

		context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String targetPackage = parsePropertyTokens(context, attributes.getProperty("targetPackage")); //$NON-NLS-1$
		String targetProject = attributes.getProperty("targetProject"); //$NON-NLS-1$

		javaModelGeneratorConfiguration.setTargetPackage(targetPackage);
		javaModelGeneratorConfiguration.setTargetProject(targetProject);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(javaModelGeneratorConfiguration, childNode);
			}
		}
	}

	private void parseJavaClientGenerator(Context context, Node node) {
		JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();

		context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type"); //$NON-NLS-1$
		String targetPackage = parsePropertyTokens(context, attributes.getProperty("targetPackage")); //$NON-NLS-1$
		String targetProject = attributes.getProperty("targetProject"); //$NON-NLS-1$

		javaClientGeneratorConfiguration.setConfigurationType(type);
		javaClientGeneratorConfiguration.setTargetPackage(targetPackage);
		javaClientGeneratorConfiguration.setTargetProject(targetProject);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(javaClientGeneratorConfiguration, childNode);
			}
		}
	}

	protected void parseJdbcConnection(Context context, Node node) {
		JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();

		context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

		Properties attributes = parseAttributes(node);
		String driverClass = attributes.getProperty("driverClass"); //$NON-NLS-1$
		String connectionURL = attributes.getProperty("connectionURL"); //$NON-NLS-1$

		jdbcConnectionConfiguration.setDriverClass(driverClass);
		jdbcConnectionConfiguration.setConnectionURL(connectionURL);

		String userId = attributes.getProperty("userId"); //$NON-NLS-1$
		if (stringHasValue(userId)) {
			jdbcConnectionConfiguration.setUserId(userId);
		}

		String password = attributes.getProperty("password"); //$NON-NLS-1$
		if (stringHasValue(password)) {
			jdbcConnectionConfiguration.setPassword(password);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(jdbcConnectionConfiguration, childNode);
			}
		}
	}

	protected void parseClassPathEntry(Configuration configuration, Node node) {
		Properties attributes = parseAttributes(node);

		configuration.addClasspathEntry(attributes.getProperty("location")); //$NON-NLS-1$
	}

	protected void parseProperty(Context context, PropertyHolder propertyHolder, Node node) {
		Properties attributes = parseAttributes(node);

		String name = attributes.getProperty("name"); //$NON-NLS-1$
		String value = parsePropertyTokens(context, attributes.getProperty("value")); //$NON-NLS-1$

		propertyHolder.addProperty(name, value);
	}

	protected void parseProperty(PropertyHolder propertyHolder, Node node) {
		parseProperty(null, propertyHolder, node);
	}

	protected Properties parseAttributes(Node node) {
		Properties attributes = new Properties();
		NamedNodeMap nnm = node.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Node attribute = nnm.item(i);
			String value = parsePropertyTokens(attribute.getNodeValue());
			attributes.put(attribute.getNodeName(), value);
		}

		return attributes;
	}

	String parsePropertyTokens(String s) {
		return parsePropertyTokens(null, s);
	}

	String parsePropertyTokens(Context context, String s) {
		final String OPEN = "${"; //$NON-NLS-1$
		final String CLOSE = "}"; //$NON-NLS-1$
		int currentIndex = 0;

		List<String> answer = new ArrayList<>();

		int markerStartIndex = s.indexOf(OPEN);
		if (markerStartIndex < 0) {
			// no parameter markers
			answer.add(s);
			currentIndex = s.length();
		}

		while (markerStartIndex > -1) {
			if (markerStartIndex > currentIndex) {
				// add the characters before the next parameter marker
				answer.add(s.substring(currentIndex, markerStartIndex));
				currentIndex = markerStartIndex;
			}

			int markerEndIndex = s.indexOf(CLOSE, currentIndex);
			int nestedStartIndex = s.indexOf(OPEN, markerStartIndex + OPEN.length());
			while (nestedStartIndex > -1 && markerEndIndex > -1 && nestedStartIndex < markerEndIndex) {
				nestedStartIndex = s.indexOf(OPEN, nestedStartIndex + OPEN.length());
				markerEndIndex = s.indexOf(CLOSE, markerEndIndex + CLOSE.length());
			}

			if (markerEndIndex < 0) {
				// no closing delimiter, just move to the end of the string
				answer.add(s.substring(markerStartIndex));
				currentIndex = s.length();
				break;
			}

			// we have a valid property marker...
			String property = s.substring(markerStartIndex + OPEN.length(), markerEndIndex);
			String propertyValue = resolveProperty(context, parsePropertyTokens(property));
			if (propertyValue == null) {
				// add the property marker back into the stream
				answer.add(s.substring(markerStartIndex, markerEndIndex + 1));
			}
			else {
				answer.add(propertyValue);
			}

			currentIndex = markerEndIndex + CLOSE.length();
			markerStartIndex = s.indexOf(OPEN, currentIndex);
		}

		if (currentIndex < s.length()) {
			answer.add(s.substring(currentIndex));
		}

		return String.join("", answer);
	}

	protected void parseCommentGenerator(Context context, Node node) {
		CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();

		context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type"); //$NON-NLS-1$

		if (stringHasValue(type)) {
			commentGeneratorConfiguration.setConfigurationType(type);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(commentGeneratorConfiguration, childNode);
			}
		}
	}

	protected void parseConnectionFactory(Context context, Node node) {
		ConnectionFactoryConfiguration connectionFactoryConfiguration = new ConnectionFactoryConfiguration();

		context.setConnectionFactoryConfiguration(connectionFactoryConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type"); //$NON-NLS-1$

		if (stringHasValue(type)) {
			connectionFactoryConfiguration.setConfigurationType(type);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
				parseProperty(connectionFactoryConfiguration, childNode);
			}
		}
	}

	/**
	 * This method resolve a property from one of the three sources: system properties,
	 * properties loaded from the &lt;properties&gt; configuration element, and "extra"
	 * properties that may be supplied by the Maven or Ant environments.
	 *
	 * <p>
	 * If there is a name collision, system properties take precedence, followed by
	 * configuration properties, followed by extra properties.
	 * @param key property key
	 * @return the resolved property. This method will return null if the property is
	 * undefined in any of the sources.
	 */
	private String resolveProperty(Context context, String key) {
		String property = System.getProperty(key);

		if (property == null) {
			property = configurationProperties.getProperty(key);
		}

		if (property == null) {
			property = extraProperties.getProperty(key);
		}

		if (property == null && context != null) {
			property = context.getProperty(key);
		}

		return property;
	}

	public void parseJoinEntry(Context context, Node node) {
		Map<String, JoinEntry> joinEntries = parseJoinDetail(node);
		context.getJoinConfig().getJoinDetailMap().putAll(joinEntries);
	}

	private Map<String, JoinEntry> parseJoinDetail(Node node) {
		Properties attributes = parseAttributes(node);
		String leftTable = attributes.getProperty(JOIN_LEFT_TABLE_ARG); // $NON-NLS-1$
		NodeList nodeList = node.getChildNodes();
		List<Pair<String, JoinTarget>> details = new ArrayList<>();
		List<JoinTable> joinTables = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if (JOIN_TARGET_ARG.equals(childNode.getNodeName())) { // $NON-NLS-1$
				details.add(parseJoinTarget(childNode));
			}
			if (JOIN_TABLE_ARG.equals(childNode.getNodeName())) { // $NON-NLS-1$
				joinTables.add(parseJoinTable(childNode));
			}
		}
		JoinEntry detail = new JoinEntry(leftTable);
		detail.getDetails().addAll(details);
		detail.getJoinTables().addAll(joinTables);
		return Collections.singletonMap(leftTable, detail);
	}

	private JoinTable parseJoinTable(Node node) {
		Properties attributes = parseAttributes(node);
		String middleTable = attributes.getProperty("middleTable"); // $NON-NLS-1$
		String rightTable = attributes.getProperty("rightTable"); // $NON-NLS-1$
		String property = attributes.getProperty("property"); // $NON-NLS-1$
		JoinTable joinTable = new JoinTable();
		joinTable.setMiddleTable(middleTable);
		joinTable.setRightTable(rightTable);
		joinTable.setProperty(property);
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("joinColumn".equals(childNode.getNodeName())) { // $NON-NLS-1$
				joinTable.setJoinColumn(parseJoinColumn(childNode));
			}
			else if ("inverseJoinColumn".equals(childNode.getNodeName())) {
				joinTable.setInverseJoinColumn(parseJoinColumn(childNode));
			}
		}
		return joinTable;
	}

	private UIConfig parseUIConfig(Node node) {
		Properties attributes = parseAttributes(node);
		String viewType = attributes.getProperty("viewType"); // $NON-NLS-1$
		String displayField = attributes.getProperty("displayField"); // $NON-NLS-1$
		UIConfig uiConfig = new UIConfig();
		if (StringUtility.stringHasValue(viewType)) {
			uiConfig.setViewType(viewType);
		}
		if (StringUtility.stringHasValue(displayField)) {
			uiConfig.setDisplayField(displayField);
		}
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if ("searchable".equals(childNode.getNodeName())) { // $NON-NLS-1$
				uiConfig.setSearchable(parseLimitDisplayField(childNode));
			}
			else if ("criteria".equals(childNode.getNodeName())) {
				uiConfig.setCriteria(parseLimitDisplayField(childNode));
			}
			else if ("listFields".equals(childNode.getNodeName())) {
				uiConfig.setListFields(parseLimitDisplayField(childNode));
			}
			else if ("inputFields".equals(childNode.getNodeName())) {
				uiConfig.setInputFields(parseLimitDisplayField(childNode));
			}
		}
		return uiConfig;
	}

	private JoinColumn parseJoinColumn(Node childNode) {
		JoinColumn joinColumn = new JoinColumn();
		Properties attributes = parseAttributes(childNode);
		joinColumn.setColumnName(attributes.getProperty("columnName"));
		joinColumn.setReferencedColumnName(attributes.getProperty("referencedColumnName"));
		return joinColumn;
	}

	private LimitDisplayField parseLimitDisplayField(Node childNode) {
		LimitDisplayField limitDisplayField = new LimitDisplayField();
		Properties attributes = parseAttributes(childNode);
		String fields = attributes.getProperty("fields");
		String ignored = attributes.getProperty("ignored");
		if (StringUtility.stringHasValue(fields)) {
			limitDisplayField.getFields().addAll(Arrays.asList(StringUtils.split(fields, ",")));
		}
		if (StringUtility.stringHasValue(ignored)) {
			limitDisplayField.getIgnored().addAll(Arrays.asList(StringUtils.split(ignored, ",")));
		}
		return limitDisplayField;
	}

	private Pair<String, JoinTarget> parseJoinTarget(Node node) {
		Properties attributes = parseAttributes(node);
		String leftTableColumn = attributes.getProperty(JOIN_LEFT_COLUMN_ARG);
		String rightTable = attributes.getProperty(JOIN_RIGHT_TABLE_ARG);
		JoinTarget.JoinType joinType = JoinTarget.JoinType.valueOf(attributes.getProperty(JOIN_TYPE_ARG));
		String fieldName = attributes.getProperty(JOIN_JAVA_PROPERTY_ARG);
		String rightTableColumn = attributes.getProperty(JOIN_RIGHT_COLUMN_ARG);
		return Pair.of(leftTableColumn, new JoinTarget(rightTable, fieldName, rightTableColumn, joinType));
	}

}
