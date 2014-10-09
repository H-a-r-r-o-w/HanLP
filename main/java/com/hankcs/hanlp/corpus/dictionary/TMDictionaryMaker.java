/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/9 20:00</create-date>
 *
 * <copyright file="NGramDictionaryMaker.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 转移矩阵词典制作工具
 * @author hankcs
 */
public class TMDictionaryMaker implements ISaveAble
{
    static Logger logger = LoggerFactory.getLogger(TMDictionaryMaker.class);
    Map<String, Map<String, Integer>> transferMatrix;

    public TMDictionaryMaker()
    {
        transferMatrix = new TreeMap<>();
    }

    /**
     * 添加一个转移例子，会在内部完成统计
     * @param first
     * @param second
     */
    public void addPair(String first, String second)
    {
        Map<String, Integer> firstMatrix = transferMatrix.get(first);
        if (firstMatrix == null)
        {
            firstMatrix = new TreeMap<>();
            transferMatrix.put(first, firstMatrix);
        }
        Integer frequency = firstMatrix.get(second);
        if (frequency == null) frequency = 0;
        firstMatrix.put(second, frequency + 1);
    }

    @Override
    public String toString()
    {
        Set<String> labelSet = new TreeSet<>();
        for (Map.Entry<String, Map<String, Integer>> first : transferMatrix.entrySet())
        {
            labelSet.add(first.getKey());
            labelSet.addAll(first.getValue().keySet());
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(' ');
        for (String key : labelSet)
        {
            sb.append(',');
            sb.append(key);
        }
        sb.append('\n');
        for (String first : labelSet)
        {
            Map<String, Integer> firstMatrix = transferMatrix.get(first);
            if (firstMatrix == null) firstMatrix = new TreeMap<>();
            sb.append(first);
            for (String second : labelSet)
            {
                sb.append(',');
                Integer frequency = firstMatrix.get(second);
                if (frequency == null) frequency = 0;
                sb.append(frequency);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
            bw.write(toString());
            bw.close();
        }
        catch (Exception e)
        {
            logger.warn("在保存转移矩阵词典到{}时发生异常", path, e);
            return false;
        }
        return true;
    }
}
