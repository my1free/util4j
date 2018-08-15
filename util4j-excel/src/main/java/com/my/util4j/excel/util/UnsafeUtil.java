package com.my.util4j.excel.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 获取Unsafe类
 *
 * @author michealyang
 * @version 1.0
 * @created 18/6/11
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class UnsafeUtil {
    public static Unsafe getUnsafe() {
        Field f = null;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("获取Unsafe对象失败");
        }
        f.setAccessible(true);
        try {
            Unsafe unsafe = (Unsafe) f.get(null);
            return unsafe;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("获取Unsafe对象失败");
        }
    }
}