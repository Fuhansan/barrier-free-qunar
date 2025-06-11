package com.qunar.barrier_free_qunar.java.sdk.action;

import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionCommand;

import java.util.List;

/**
 * 动作解析器接口
 * 负责从文本内容中解析出动作指令
 */
public interface ActionParser {
    
    /**
     * 解析文本内容中的动作指令
     * 
     * @param content 待解析的文本内容
     * @return 解析出的动作指令列表
     */
    List<ActionCommand> parseActions(String content);
    
    /**
     * 检查文本内容是否包含动作指令open_app
     * 
     * @param content 待检查的文本内容
     * @return 是否包含动作指令
     */
    boolean containsActions(String content);
}