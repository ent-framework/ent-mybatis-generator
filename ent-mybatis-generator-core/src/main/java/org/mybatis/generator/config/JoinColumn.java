package org.mybatis.generator.config;

public class JoinColumn {
    private String left;

    private String right;

    public String getLeft() {
        return left;
    }

    public JoinColumn setLeft(String left) {
        this.left = left;
        return this;
    }

    public String getRight() {
        return right;
    }

    public JoinColumn setRight(String right) {
        this.right = right;
        return this;
    }
}
