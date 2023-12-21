/*
 *    Copyright 2006-2021 the original author or authors.
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
package org.mybatis.generator.runtime.dynamic.sql.elements;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.Optional;

public class Utils {

    public static boolean generateDeleteByPrimaryKey(IntrospectedTable introspectedTable) {
        return introspectedTable.hasPrimaryKeyColumns();
    }

    public static boolean generateMultipleRowInsert(IntrospectedTable introspectedTable) {
        // multi row inserts work if we don't expect generated keys, or of the generated
        // keys are
        // JDBC standard.
        return introspectedTable.getGeneratedKey().map(GeneratedKey::isJdbcStandard).orElse(true);
    }

    public static boolean canRetrieveMultiRowGeneratedKeys(IntrospectedTable introspectedTable) {
        // if the generated keys are JDBC standard, then we can retrieve them
        // if no generated keys, or not JDBC, then we cannot retrieve them
        return introspectedTable.getGeneratedKey().map(GeneratedKey::isJdbcStandard).orElse(false);
    }

    public static boolean generateSelectByPrimaryKey(IntrospectedTable introspectedTable) {
        return introspectedTable.hasPrimaryKeyColumns()
                && (introspectedTable.hasBaseColumns() || introspectedTable.hasBLOBColumns());
    }

    public static boolean generateUpdateByPrimaryKey(IntrospectedTable introspectedTable) {
        if (ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()).isEmpty()) {
            return false;
        }

        return introspectedTable.hasPrimaryKeyColumns()
                && (introspectedTable.hasBLOBColumns() || introspectedTable.hasBaseColumns());
    }

    public static Optional<IntrospectedColumn> getVersionColumn(IntrospectedTable introspectedTable) {
        String versionColumn = introspectedTable.getTableConfiguration().getVersionColumn();
        if (StringUtils.isNotEmpty(versionColumn)) {
            return introspectedTable.getNonPrimaryKeyColumns().stream()
                    .filter(column -> StringUtils.equals(versionColumn, column.getActualColumnName())).findFirst();
        }
        return Optional.empty();
    }

    public static String getSetVersionValue(IntrospectedColumn column, String tableFieldName,
            MethodAndImports.Builder builder) {
        String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
        String methodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(),
                column.getFullyQualifiedJavaType());
        String fqn = column.getFullyQualifiedJavaType().getFullyQualifiedName();
        if (StringUtils.equals(fqn, Integer.class.getName()) || StringUtils.equals(fqn, Long.class.getName())) {
            builder.withStaticImport("org.mybatis.dynamic.sql.SqlBuilder.add")
                    .withStaticImport("org.mybatis.dynamic.sql.SqlBuilder.constant");
            return String.format("    .set(%s).equalTo(add(%s, constant(\"1\")))", fieldName, fieldName);
        }
        return null;
    }

    public static String setVersionWhereClauseForUpdate(IntrospectedColumn column, String tableFieldName) {
        String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
        String methodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(),
                column.getFullyQualifiedJavaType());
        String fqn = column.getFullyQualifiedJavaType().getFullyQualifiedName();
        if (StringUtils.equals(fqn, Integer.class.getName()) || StringUtils.equals(fqn, Long.class.getName())) {
            return String.format("    .and(%s, isEqualTo(row::%s))", fieldName, methodName);
        }
        return null;
    }

}
