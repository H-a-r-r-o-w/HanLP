/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/18 19:47</create-date>
 *
 * <copyright file="NatureDictionaryMaker.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.dictionary;

import com.hankcs.hanlp.corpus.document.CorpusLoader;
import com.hankcs.hanlp.corpus.document.Document;
import com.hankcs.hanlp.corpus.document.sentence.word.Word;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.util.Precompiler;
import com.hankcs.hanlp.utility.Predefine;
import com.hankcs.hanlp.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author hankcs
 */
public class NatureDictionaryMaker extends CommonDictionaryMaker
{
    static Logger logger = LoggerFactory.getLogger(NatureDictionaryMaker.class);

    public NatureDictionaryMaker(EasyDictionary dictionary)
    {
        super(dictionary);
    }

    @Override
    protected void addToDictionary(List<List<Word>> sentenceList)
    {
        logger.trace("开始制作词典");
        // 制作NGram词典
        for (List<Word> wordList : sentenceList)
        {
            Word pre = null;
            for (Word word : wordList)
            {
                if (pre != null)
                {
                    nGramDictionaryMaker.addPair(pre, word);
                }
                pre = word;
            }
        }
    }

    @Override
    protected void roleTag(List<List<Word>> sentenceList)
    {
        logger.trace("开始标注");
        int i = 0;
        for (List<Word> wordList : sentenceList)
        {
            logger.trace("{} / {}", ++i, sentenceList.size());
            for (Word word : wordList)
            {
                Precompiler.compile(word);  // 编译为等效字符串
            }
            LinkedList<Word> wordLinkedList = (LinkedList<Word>) wordList;
            wordLinkedList.addFirst(new Word(Predefine.TAG_BIGIN, Nature.begin.toString()));
            wordLinkedList.addLast(new Word(Predefine.TAG_END, Nature.end.toString()));
        }
    }

    /**
     * 指定语料库文件夹，制作一份词频词典
     * @return
     */
    static boolean makeCoreDictionary(String inPath, String outPath)
    {
        final DictionaryMaker dictionaryMaker = new DictionaryMaker();
        final TreeSet<String> labelSet = new TreeSet<>();

        CorpusLoader.walk(inPath, new CorpusLoader.Handler()
        {
            @Override
            public void handle(Document document)
            {
                for (List<Word> sentence : document.getSimpleSentenceList(true))
                {
                    for (Word word : sentence)
                    {
                        if (shouldInclude(word))
                            dictionaryMaker.add(word);
                    }
                }
//                for (List<Word> sentence : document.getSimpleSentenceList(false))
//                {
//                    for (Word word : sentence)
//                    {
//                        if (shouldInclude(word))
//                            dictionaryMaker.add(word);
//                    }
//                }
            }

            /**
             * 是否应当计算这个词语
             * @param word
             * @return
             */
            boolean shouldInclude(Word word)
            {
                switch (word.label)
                {
                    case "m":
                    case "mq":
                    case "w":
                    case "t":
                        if (!Utility.isAllChinese(word.value)) return false;
                    case "nr":
                        return false;
                }

                return true;
            }
        });
        if (outPath != null)
        return dictionaryMaker.saveTxtTo(outPath);
        return false;
    }

    public static void main(String[] args)
    {
//        makeCoreDictionary("D:\\JavaProjects\\CorpusToolBox\\data\\2014", "data/dictionary/CoreNatureDictionary.txt");
        EasyDictionary dictionary = EasyDictionary.create("data/dictionary/CoreNatureDictionary.txt");
        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker(dictionary);
        CorpusLoader.walk("D:\\JavaProjects\\CorpusToolBox\\data\\2014\\", new CorpusLoader.Handler()
        {
            @Override
            public void handle(Document document)
            {
                dictionaryMaker.compute(document.getSimpleSentenceList(false)); // 再打一遍不拆分的
                dictionaryMaker.compute(document.getSimpleSentenceList(true));  // 先打一遍拆分的
            }
        });
        dictionaryMaker.saveTxtTo("D:\\JavaProjects\\HanLP\\data\\dictionary\\CoreNatureDictionary");
    }
}
