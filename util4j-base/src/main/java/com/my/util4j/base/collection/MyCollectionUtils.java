package com.my.util4j.base.collection;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author michealyang
 * @version 1.0
 * @created 18/7/3
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class MyCollectionUtils {
    /**
     * 将一个大的list分隔成大小最大为size的多个list
     * @param list
     * @param size
     * @param <T>
     * @return
     */
    public static  <T> List<List<T>> splitList(List<T> list, final int size) {
        List<List<T>> parts = Lists.newArrayList();
        final int n = list.size();
        for (int i = 0; i < n; i += size) {
            parts.add(Lists.newArrayList(list.subList(i, Math.min(n, i + size))));
        }
        return parts;
    }

    public static void main(String[] args) {
        List<Integer> test = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> res = MyCollectionUtils.splitList(test, 3);
        System.out.println(res);
    }
}
