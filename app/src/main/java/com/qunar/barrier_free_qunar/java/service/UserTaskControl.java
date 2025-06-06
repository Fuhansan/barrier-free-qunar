package com.qunar.barrier_free_qunar.java.service;

import android.accessibilityservice.AccessibilityService;

import com.qunar.barrier_free_qunar.java.sdk.model.UserTask;

public class UserTaskControl {


    public static UserTaskService build(AccessibilityService accessibilityService) {
        return new UserTaskService(accessibilityService);
    }


    public static UserTask build(String instruction, String through) {
        return new UserTask(instruction, through);
    }



}
