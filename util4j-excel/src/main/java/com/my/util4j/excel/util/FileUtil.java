package com.my.util4j.excel.util;

import java.net.URL;

/**
 * @author michealyang
 * @version 1.0
 * @created 18/6/11
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class FileUtil {

    public static String getFilePath(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (url == null) {
            return null;
        }
        return url.getPath();
    }
}