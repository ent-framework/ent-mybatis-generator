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
package net.entframework.kernel.db.generator.typescript;

import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.JavaTypeResolver;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.db.ActualTableName;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.Types;
import java.util.*;

public class TypescriptResolverDefaultImpl implements JavaTypeResolver {

    protected List<String> warnings;

    protected final Properties properties;

    protected Context context;
    protected boolean convertLongToString;
    protected final Map<Integer, JdbcTypeInformation> typeMap;

    public TypescriptResolverDefaultImpl() {
        super();
        properties = new Properties();
        typeMap = new HashMap<>();

        typeMap.put(Types.ARRAY, new JdbcTypeInformation("ARRAY", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.BIGINT, new JdbcTypeInformation("BIGINT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.BINARY, new JdbcTypeInformation("BINARY", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("byte[]"))); //$NON-NLS-1$
        typeMap.put(Types.BIT, new JdbcTypeInformation("BIT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("boolean")));
        typeMap.put(Types.BLOB, new JdbcTypeInformation("BLOB", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("byte[]"))); //$NON-NLS-1$
        typeMap.put(Types.BOOLEAN, new JdbcTypeInformation("BOOLEAN", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("boolean")));
        typeMap.put(Types.CHAR, new JdbcTypeInformation("CHAR", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.CLOB, new JdbcTypeInformation("CLOB", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.DATALINK, new JdbcTypeInformation("DATALINK", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.DATE, new JdbcTypeInformation("DATE", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("Date")));
        typeMap.put(Types.DECIMAL, new JdbcTypeInformation("DECIMAL", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.DISTINCT, new JdbcTypeInformation("DISTINCT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.DOUBLE, new JdbcTypeInformation("DOUBLE", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.FLOAT, new JdbcTypeInformation("FLOAT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.INTEGER, new JdbcTypeInformation("INTEGER", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.JAVA_OBJECT, new JdbcTypeInformation("JAVA_OBJECT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.LONGNVARCHAR, new JdbcTypeInformation("LONGNVARCHAR", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.LONGVARBINARY, new JdbcTypeInformation("LONGVARBINARY", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("byte[]"))); //$NON-NLS-1$
        typeMap.put(Types.LONGVARCHAR, new JdbcTypeInformation("LONGVARCHAR", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.NCHAR, new JdbcTypeInformation("NCHAR", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.NCLOB, new JdbcTypeInformation("NCLOB", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.NVARCHAR, new JdbcTypeInformation("NVARCHAR", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        typeMap.put(Types.NULL, new JdbcTypeInformation("NULL", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.NUMERIC, new JdbcTypeInformation("NUMERIC", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.OTHER, new JdbcTypeInformation("OTHER", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.REAL, new JdbcTypeInformation("REAL", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.REF, new JdbcTypeInformation("REF", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.SMALLINT, new JdbcTypeInformation("SMALLINT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.STRUCT, new JdbcTypeInformation("STRUCT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("any")));
        typeMap.put(Types.TIME, new JdbcTypeInformation("TIME", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("Date")));
        typeMap.put(Types.TIMESTAMP, new JdbcTypeInformation("TIMESTAMP", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("Date")));
        typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("number")));
        typeMap.put(Types.VARBINARY, new JdbcTypeInformation("VARBINARY", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("byte[]"))); //$NON-NLS-1$
        typeMap.put(Types.VARCHAR, new JdbcTypeInformation("VARCHAR", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("string")));
        // JDK 1.8 types
        typeMap.put(Types.TIME_WITH_TIMEZONE, new JdbcTypeInformation("TIME_WITH_TIMEZONE", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("java.time.OffsetTime"))); //$NON-NLS-1$
        typeMap.put(Types.TIMESTAMP_WITH_TIMEZONE, new JdbcTypeInformation("TIMESTAMP_WITH_TIMEZONE", //$NON-NLS-1$
                new FullyQualifiedTypescriptType("java.time.OffsetDateTime"))); //$NON-NLS-1$
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        convertLongToString = StringUtility
                .isTrue(properties.getProperty("convertLongToString"));
    }

    @Override
    public FullyQualifiedTypescriptType calculateJavaType(ActualTableName actualTable,
            IntrospectedColumn introspectedColumn) {
        FullyQualifiedTypescriptType answer = null;
        JdbcTypeInformation jdbcTypeInformation = typeMap.get(introspectedColumn.getJdbcType());

        if (jdbcTypeInformation != null) {
            answer = jdbcTypeInformation.getFullyQualifiedJavaType();
            answer = overrideDefaultType(introspectedColumn, answer);
        }
        return answer;
    }

    protected FullyQualifiedTypescriptType overrideDefaultType(IntrospectedColumn column,
            FullyQualifiedTypescriptType defaultType) {
        FullyQualifiedTypescriptType answer = defaultType;

        switch (column.getJdbcType()) {
            case Types.BIT:
                answer = calculateBitReplacement(column, defaultType);
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                answer = calculateBigDecimalReplacement(column, defaultType);
                break;
            case Types.BIGINT:
                answer = calculateBigintType(column, defaultType);
                break;
            default:
                break;
        }

        return answer;
    }

    protected FullyQualifiedTypescriptType calculateBigintType(IntrospectedColumn column,
                                                                  FullyQualifiedTypescriptType defaultType) {
        FullyQualifiedTypescriptType answer;

        if (convertLongToString) {
            answer = new FullyQualifiedTypescriptType("string"); //$NON-NLS-1$
        }
        else {
            answer = defaultType;
        }

        return answer;
    }

    protected FullyQualifiedTypescriptType calculateBitReplacement(IntrospectedColumn column,
            FullyQualifiedTypescriptType defaultType) {
        FullyQualifiedTypescriptType answer;

        if (column.getLength() > 1) {
            answer = new FullyQualifiedTypescriptType("byte[]"); //$NON-NLS-1$
        }
        else {
            answer = defaultType;
        }

        return answer;
    }

    protected FullyQualifiedTypescriptType calculateBigDecimalReplacement(IntrospectedColumn column,
            FullyQualifiedTypescriptType defaultType) {
        return new FullyQualifiedTypescriptType("number");
    }

    @Override
    public String calculateJdbcTypeName(IntrospectedColumn introspectedColumn) {
        String answer = null;
        JdbcTypeInformation jdbcTypeInformation = typeMap.get(introspectedColumn.getJdbcType());

        if (jdbcTypeInformation != null) {
            answer = jdbcTypeInformation.getJdbcTypeName();
        }

        return answer;
    }

    @Override
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    public static class JdbcTypeInformation {

        private final String jdbcTypeName;

        private final FullyQualifiedTypescriptType fullyQualifiedJavaType;

        public JdbcTypeInformation(String jdbcTypeName, FullyQualifiedTypescriptType fullyQualifiedJavaType) {
            this.jdbcTypeName = jdbcTypeName;
            this.fullyQualifiedJavaType = fullyQualifiedJavaType;
        }

        public String getJdbcTypeName() {
            return jdbcTypeName;
        }

        public FullyQualifiedTypescriptType getFullyQualifiedJavaType() {
            return fullyQualifiedJavaType;
        }

    }

}
