package net.entframework.jpa.apt;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class FieldUtils {
    private final Elements elementUtils;
    private final Types typeUtils;

    public FieldUtils(Elements elementUtils, Types typeUtils) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    public boolean isPrimitiveType(TypeMirror fieldType) {
        return fieldType.getKind() == TypeKind.BOOLEAN ||
                fieldType.getKind() == TypeKind.SHORT ||
                fieldType.getKind() == TypeKind.INT ||
                fieldType.getKind() == TypeKind.LONG ||
                fieldType.getKind() == TypeKind.CHAR ||
                fieldType.getKind() == TypeKind.FLOAT ||
                fieldType.getKind() == TypeKind.DOUBLE;
    }

    public boolean isStringType(TypeMirror fieldType) {
        return typeUtils.isSameType(
                fieldType,
                elementUtils.getTypeElement("java.lang.String").asType());
    }

    public boolean isListType(TypeMirror fieldType) {
        return typeUtils.isSameType(
                typeUtils.erasure(fieldType),                 // 擦除泛型后的原始类型
                elementUtils.getTypeElement("java.util.List").asType());
    }


    /**
     * 判断字段是否属于 Number 家族（Byte/Short/Integer/Long/Float/Double/BigInteger/BigDecimal）
     */
    public boolean isNumberType(TypeMirror fieldType) {
        String qName = typeUtils.erasure(fieldType).toString();
        return qName.equals("java.lang.Byte")
                || qName.equals("java.lang.Short")
                || qName.equals("java.lang.Integer")
                || qName.equals("java.lang.Long")
                || qName.equals("java.lang.Float")
                || qName.equals("java.lang.Double")
                || qName.equals("java.math.BigInteger")
                || qName.equals("java.math.BigDecimal");
    }

    /**
     * 判断字段是否为日期/时间类型（java.util.Date / java.time.*）
     */
    public boolean isDateType(TypeMirror field) {
        TypeMirror fieldType = typeUtils.erasure(field);
        String qName = fieldType.toString();
        return qName.equals("java.util.Date")
                || qName.equals("java.time.LocalDate")
                || qName.equals("java.time.LocalTime")
                || qName.equals("java.time.LocalDateTime")
                || qName.equals("java.time.Instant")
                || qName.equals("java.time.OffsetDateTime")
                || qName.equals("java.time.ZonedDateTime");
    }
}
