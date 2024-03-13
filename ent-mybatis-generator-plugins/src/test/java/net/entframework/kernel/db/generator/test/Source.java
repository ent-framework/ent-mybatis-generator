package net.entframework.kernel.db.generator.test;

import net.entframework.kernel.core.enums.SupperEnum;

public enum Source implements SupperEnum<String> {


    Shanghai("sh", "上海")

    ;

    Source(String label, String value) {
        this.label = label;
        this.value = value;
    }

    private String label;
    private String value;

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getValue() {
        return value;
    }
}
