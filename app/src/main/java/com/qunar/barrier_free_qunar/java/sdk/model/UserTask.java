package com.qunar.barrier_free_qunar.java.sdk.model;

import androidx.annotation.NonNull;

/**
 * 用户任务数据
 */
public class UserTask {
    /**
     * 任务状态true表示执行完成
     */
    private boolean status;
    /**
     * 步骤执行限制次数
     */
    private int limitStep;

    /**
     * 当前执行步骤
     */
    private int currentStep;
    /**
     * 用户指令
     */
    private String instruction;


    /**
     * LLM 构思
     */
    private String through;

    public UserTask(String instruction) {
        this.status = false;
        this.limitStep = 5;
        this.currentStep = 0;
        this.instruction = instruction;
    }

    public UserTask(String instruction, String through) {
        this.instruction = instruction;
        this.through = through;
        this.limitStep = 5;
        this.currentStep = 0;
    }

    public UserTask(boolean status, int limitStep, int currentStep, String instruction) {
        this.status = status;
        this.limitStep = limitStep;
        this.currentStep = currentStep;
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public boolean
    isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getLimitStep() {
        return limitStep;
    }

    public void setLimitStep(int limitStep) {
        this.limitStep = limitStep;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getThrough() {
        return through;
    }

    public void setThrough(String through) {
        this.through = through;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserTask{" +
                "status=" + status +
                ", limitStep=" + limitStep +
                ", currentStep=" + currentStep +
                ", instruction='" + instruction + '\'' +
                '}';
    }

    public void checkArgs() {
        // 检查指令是否为空
        if (instruction == null || instruction.isEmpty()) {
            throw new BarrierFreeException("user instruction is empty");
        }
        
        // 最大次数不能超过50次
        if (limitStep > 50) {
            throw new BarrierFreeException("limit step is too large");
        }

        // 当前次数不能超过最大次数
        if (currentStep > limitStep) {
            throw new BarrierFreeException("current step is too large");
        }

        // 当前次数不能小于0
        if (currentStep < 0) {
            throw new BarrierFreeException("current step is too small");
        }

    }
}
