package com.my.util4j.base.collection;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author michealyang
 * @version 1.0
 * @created 18/7/3
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class MyStringUtils {

    /**
     * 将一个String按照指定分隔符分隔，并将blank字符去除，字符前后的空格去除
     * @param src
     * @param regex
     * @return
     */
    public static List<String> splitAndTrim(String src, String regex) {
        if (StringUtils.isBlank(src)) {
            return Collections.emptyList();
        }
        String[] arr = src.split(regex);
        List<String> res = Lists.newArrayList();
        for (String s : arr) {
            String tmp = s.trim();
            if (StringUtils.isNotBlank(tmp)) {
                res.add(tmp);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        String test = "123,234,, 453, 65463,";
        List<String> res = MyStringUtils.splitAndTrim(test, ",");
        System.out.println(res);
    }
}
