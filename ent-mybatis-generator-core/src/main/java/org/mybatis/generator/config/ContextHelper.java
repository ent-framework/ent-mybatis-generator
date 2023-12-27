/*
 * ******************************************************************************
 *  * Copyright (c) 2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ContextHelper {

    public static boolean containsSqlWildcard(String s) {
        if (s == null) {
            return false;
        }
        return s.indexOf('%') != -1;
    }

    public static boolean isMatch(String str, String pattern) {
        int s = 0, p = 0, match = 0, starIdx = -1;
        while (s < str.length()) {
            if (p < pattern.length() && (pattern.charAt(p) == '?' || str.charAt(s) == pattern.charAt(p))) {
                s++;
                p++;
            }
            else if (p < pattern.length() && pattern.charAt(p) == '%') {
                starIdx = p;
                match = s;
                p++;
            }
            else if (starIdx != -1) {
                p = starIdx + 1;
                match++;
                s = match;
            }
            else {
                return false;
            }
        }
        while (p < pattern.length() && pattern.charAt(p) == '%') {
            p++;
        }

        return p == pattern.length();
    }

    public static void main(String[] args) {
        System.out.println(isMatch("exam_class_grade", "exam%"));
        System.out.println(isMatch("exa1class_grade", "exam%"));
        System.out.println(isMatch("exam_class_grade", "%exam%"));
        System.out.println(isMatch("exam_class_grade", "%exam"));
        System.out.println(isMatch("exam_class_grade", "%exam"));
    }

    public static List<TableConfiguration> mergeTableConfiguration(ArrayList<TableConfiguration> tableConfigurations) {
        List<TableConfiguration> sorted = tableConfigurations.stream().sorted((o1, o2) -> {
            boolean o1Wildcard = containsSqlWildcard(o1.getTableName());
            boolean o2Wildcard = containsSqlWildcard(o2.getTableName());
            if (o1Wildcard && !o2Wildcard) {
                return -1;
            }
            if (o2Wildcard && !o1Wildcard) {
                return 1;
            }
            return 0;
        }).collect(Collectors.toList());

        List<TableConfiguration> wildcardMatched = sorted.stream().filter(tableConfiguration -> {
            String tableName = tableConfiguration.getTableName();
            return containsSqlWildcard(tableName);
        }).collect(Collectors.toList());

        if (wildcardMatched.size() == 0) {
            return sorted;
        }

        sorted.forEach(tableConfiguration -> {
            String tableName = tableConfiguration.getTableName();
            if (!containsSqlWildcard(tableName)) {
                Optional<TableConfiguration> parentLikeConfig = findParentLikeConfig(wildcardMatched, tableName);
                parentLikeConfig.ifPresent(tableConfiguration::merge);
            }
        });

        return sorted;
    }

    private static Optional<TableConfiguration> findParentLikeConfig(List<TableConfiguration> wildcardMatched,
            String tableName) {
        return wildcardMatched.stream()
                .filter(tableConfiguration -> isMatch(tableName, tableConfiguration.getTableName())).findFirst();
    }

}
