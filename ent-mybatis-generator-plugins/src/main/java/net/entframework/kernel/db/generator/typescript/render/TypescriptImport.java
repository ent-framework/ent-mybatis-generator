package net.entframework.kernel.db.generator.typescript.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypescriptImport {
    private Set<String> objects = new HashSet<>();

    private String path;

    private boolean isInterface;

    public TypescriptImport(String path, boolean isInterface) {
        this.path = path;
        this.isInterface = isInterface;
    }

    public Set<String> getObjects() {
        return objects;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isInterface() {
        return isInterface;
    }
}
