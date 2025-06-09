package com.qunar.barrier_free_qunar.java.sdk.util;

import java.util.regex.Pattern;

/**
 * Markdown工具类
 * 用于检测和处理Markdown格式的文本
 */
public class MarkdownUtil {
    
    // Markdown语法检测的正则表达式
    private static final Pattern[] MARKDOWN_PATTERNS = {
        // 标题 (# ## ### 等)
        Pattern.compile("^#{1,6}\\s+.+$", Pattern.MULTILINE),
        // 粗体 (**text** 或 __text__)
        Pattern.compile("(\\*\\*|__)(?=\\S)(.+?)(?<=\\S)\\1"),
        // 斜体 (*text* 或 _text_)
        Pattern.compile("(\\*|_)(?=\\S)(.+?)(?<=\\S)\\1"),
        // 删除线 (~~text~~)
        Pattern.compile("~~(?=\\S)(.+?)(?<=\\S)~~"),
        // 代码块 (```code``` 或 `code`)
        Pattern.compile("(`{1,3})([^`]+?)\\1"),
        // 链接 ([text](url))
        Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)"),
        // 图片 (![alt](url))
        Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)"),
        // 列表 (- item 或 * item 或 + item)
        Pattern.compile("^\\s*[-*+]\\s+.+$", Pattern.MULTILINE),
        // 有序列表 (1. item)
        Pattern.compile("^\\s*\\d+\\.\\s+.+$", Pattern.MULTILINE),
        // 引用 (> text)
        Pattern.compile("^\\s*>\\s*.+$", Pattern.MULTILINE),
        // 水平线 (--- 或 ***)
        Pattern.compile("^\\s*([-*_]){3,}\\s*$", Pattern.MULTILINE),
        // 表格分隔符 (|---|---|
        Pattern.compile("\\|\\s*:?-+:?\\s*\\|"),
        // 表格行 (|cell|cell|)
        Pattern.compile("\\|.+\\|")
    };
    
    /**
     * 检测文本是否包含Markdown语法
     * 
     * @param text 要检测的文本
     * @return 如果包含Markdown语法返回true，否则返回false
     */
    public static boolean containsMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否匹配任何Markdown模式
        for (Pattern pattern : MARKDOWN_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检测文本是否可能是Markdown格式
     * 这个方法比containsMarkdown更宽松，用于流式文本的检测
     * 
     * @param text 要检测的文本
     * @return 如果可能是Markdown格式返回true，否则返回false
     */
    public static boolean mightBeMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // 检查一些常见的Markdown起始标记
        String trimmed = text.trim();
        
        // 标题
        if (trimmed.startsWith("#")) {
            return true;
        }
        
        // 列表
        if (trimmed.matches("^\\s*[-*+]\\s+.*") || trimmed.matches("^\\s*\\d+\\.\\s+.*")) {
            return true;
        }
        
        // 引用
        if (trimmed.startsWith(">")) {
            return true;
        }
        
        // 代码块
        if (trimmed.startsWith("```") || trimmed.contains("`")) {
            return true;
        }
        
        // 包含常见的Markdown语法
        if (trimmed.contains("**") || trimmed.contains("__") || 
            trimmed.contains("~~") || trimmed.contains("[")||trimmed.contains("]")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 清理Markdown语法，返回纯文本
     * 用于在不支持Markdown渲染的地方显示文本
     * 
     * @param markdownText Markdown格式的文本
     * @return 清理后的纯文本
     */
    public static String stripMarkdown(String markdownText) {
        if (markdownText == null) {
            return "";
        }
        
        String result = markdownText;
        
        // 移除标题标记
        result = result.replaceAll("^#{1,6}\\s+", "");
        
        // 移除粗体和斜体标记
        result = result.replaceAll("(\\*\\*|__)(.+?)\\1", "$2");
        result = result.replaceAll("(\\*|_)(.+?)\\1", "$2");
        
        // 移除删除线
        result = result.replaceAll("~~(.+?)~~", "$1");
        
        // 移除代码标记
        result = result.replaceAll("`(.+?)`", "$1");
        result = result.replaceAll("```[\\s\\S]*?```", "");
        
        // 移除链接，保留文本
        result = result.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");
        
        // 移除图片
        result = result.replaceAll("!\\[([^\\]]*)\\]\\([^\\)]+\\)", "$1");
        
        // 移除列表标记
        result = result.replaceAll("^\\s*[-*+]\\s+", "");
        result = result.replaceAll("^\\s*\\d+\\.\\s+", "");
        
        // 移除引用标记
        result = result.replaceAll("^\\s*>\\s*", "");
        
        return result.trim();
    }
}