package com.michealyang.util4j.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Excel存在多个Sheet
 *
 * @author michealyang
 * @version 1.0
 * @created 18/6/11
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcelSheetField {
    String name() default "";
}