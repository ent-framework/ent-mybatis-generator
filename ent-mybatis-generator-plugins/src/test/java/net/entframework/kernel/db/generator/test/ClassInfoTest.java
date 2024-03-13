package net.entframework.kernel.db.generator.test;

import net.entframework.kernel.db.generator.utils.ClassInfo;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelEnumeration;

public class ClassInfoTest {
    public static void main(String[] args) {
        ClassInfo classInfo = ClassInfo.getInstance(Vendor.class.getName());
        System.out.println(classInfo.isEnum());
        if (classInfo.isEnum()) {
            TopLevelEnumeration topLevelEnumeration = classInfo.toTopLevelEnumeration("enum", "enum." + "Vendor", "@/");
            for (Field field : topLevelEnumeration.getFields()) {
                System.out.println(field.getName() + ":" + field.getType());
            }
        }

        ClassInfo classInfo2 = ClassInfo.getInstance(Source.class.getName());
        System.out.println(classInfo2.isEnum());
        if (classInfo2.isEnum()) {
            TopLevelEnumeration topLevelEnumeration = classInfo.toTopLevelEnumeration("enum", "enum." + "Vendor", "@/");
            for (Field field : topLevelEnumeration.getFields()) {
                System.out.println(field.getName() + ":" + field.getType());
            }

        }
    }
}
