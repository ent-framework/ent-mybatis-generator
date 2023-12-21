/*
 * ******************************************************************************
 *  * Copyright (c) 2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package org.mybatis.generator.api;

import java.io.File;

import org.mybatis.generator.exception.ShellException;

/**
 * This interface defines methods that a shell should support to enable the generator to
 * work. A "shell" is defined as the execution environment (i.e. an Eclipse plugin, and
 * Ant task, a NetBeans plugin, etc.)
 *
 * <p>
 * The default ShellCallback that is very low function and does not support the merging of
 * Java files. The default shell callback is appropriate for use in well controlled
 * environments where no changes made to generated Java files.
 *
 * @author Jeff Butler
 */
public interface ShellCallback {

    File getDirectory(GeneratedFile file) throws ShellException;

    /**
     * This method is called if a newly generated Java file would overwrite an existing
     * file. This method should return the merged source (formatted). The generator will
     * write the merged source as-is to the file system.
     *
     * <p>
     * A merge typically follows these steps:
     * <ol>
     * <li>Delete any methods/fields in the existing file that have the specified JavaDoc
     * tag</li>
     * <li>Add any new super interfaces from the new file into the existing file</li>
     * <li>Make sure that the existing file's super class matches the new file</li>
     * <li>Make sure that the existing file is of the same type as the existing file
     * (either interface or class)</li>
     * <li>Add any new imports from the new file into the existing file</li>
     * <li>Add all methods and fields from the new file into the existing file</li>
     * <li>Format the resulting source string</li>
     * </ol>
     *
     * <p>
     * This method is called only if you return <code>true</code> from
     * <code>isMergeSupported()</code>.
     * @param newFileSource the source of the newly generated Java file
     * @param existingFile the existing Java file
     * @param javadocTags the JavaDoc tags that denotes which methods and fields in the
     * old file to delete (if the Java element has any of these tags, the element is
     * eligible for merge)
     * @param fileEncoding the file encoding for reading existing Java files. Can be null,
     * in which case the platform default encoding will be used.
     * @return the merged source, properly formatted. The source will be saved exactly as
     * returned from this method.
     * @throws ShellException if the file cannot be merged for some reason. If this
     * exception is thrown, nothing will be saved and the existing file will remain
     * undisturbed. The generator will add the exception message to the list of warnings
     * automatically.
     */
    default String mergeJavaFile(String newFileSource, File existingFile, String[] javadocTags, String fileEncoding)
            throws ShellException {
        throw new UnsupportedOperationException();
    }

    /**
     * After all files are saved to the file system, this method is called once for each
     * unique project that was affected by the generation run. This method is useful if
     * your IDE needs to be informed that file system objects have been created or
     * updated. If you are running outside of an IDE, your implementation need not do
     * anything in this method.
     * @param project the project to be refreshed
     */
    default void refreshProject(String project) {
    }

    /**
     * Return true if the callback supports Java merging, otherwise false. The
     * <code>mergeJavaFile()</code> method will be called only if this method returns
     * <code>true</code>.
     * @return a boolean specifying whether Java merge is supported or not
     */
    default boolean isMergeSupported() {
        return false;
    }

    /**
     * Return true if the generator should overwrite an existing file if one exists. This
     * method will be called only if <code>isMergeSupported()</code> returns
     * <code>false</code> and a file exists that would be overwritten by a generated file.
     * If you return <code>true</code>, then we will log a warning specifying what file
     * was overwritten.
     * @return true if you want to overwrite existing files
     */
    boolean isOverwriteEnabled();

}
