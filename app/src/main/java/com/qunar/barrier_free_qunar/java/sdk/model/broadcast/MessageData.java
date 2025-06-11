package com.qunar.barrier_free_qunar.java.sdk.model.broadcast;

import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;

import java.util.HashMap;
import java.util.Map;

public  class MessageData {

        public final String broadcastKey;

        public final String originalMessage;
        public final String reply;
        public final String senderName;
        public final String senderId;
        public final String messageType;
        public final String sessionId;
        public final String conversationId;
        public final long timestamp;
        
        // 多媒体字段
        public final String imageData;
        public final String videoData;
        public final String audioData;
        public final String multimediaType;
        public final String multimediaData;
        
        // 额外参数
        public final Map<String, Object> extras;

        public MessageData(
                String broadcastKey,
                String originalMessage, String reply, String senderName,
                          String senderId, String messageType, String sessionId, String conversationId, long timestamp,
                          String imageData, String videoData, String audioData, String multimediaType, String multimediaData,
                          Map<String, Object> extras) {
            this.broadcastKey = broadcastKey;
            this.originalMessage = originalMessage;
            this.reply = reply;
            this.senderName = senderName;
            this.senderId = senderId;
            this.messageType = messageType;
            this.sessionId = sessionId;
            this.conversationId = conversationId;
            this.timestamp = timestamp;
            this.imageData = imageData;
            this.videoData = videoData;
            this.audioData = audioData;
            this.multimediaType = multimediaType;
            this.multimediaData = multimediaData;
            this.extras = extras != null ? extras : new HashMap<>();
        }
        
        public boolean isStreamChunk() {
            return BroadcastConst.MessageType.STREAM_CHUNK.equals(messageType);
        }
        
        public boolean isError() {
            return BroadcastConst.MessageType.ERROR.equals(messageType);
        }
        
        public boolean isSystemMessage() {
            return BroadcastConst.MessageType.SYSTEM.equals(messageType);
        }
        
        public boolean isTextMessage() {
            return BroadcastConst.MessageType.TEXT.equals(messageType);
        }
        
        public boolean isImageMessage() {
            return BroadcastConst.MessageType.IMAGE.equals(messageType);
        }
        
        public boolean isVideoMessage() {
            return BroadcastConst.MessageType.VIDEO.equals(messageType);
        }
        
        public boolean isAudioMessage() {
            return BroadcastConst.MessageType.AUDIO.equals(messageType);
        }
        
        public boolean isMultimediaMessage() {
            return BroadcastConst.MessageType.MULTIMEDIA.equals(messageType);
        }
        
        public boolean hasMultimediaContent() {
            return imageData != null || videoData != null || audioData != null || multimediaData != null;
        }

        public boolean isUIControl(){
            return BroadcastConst.MessageType.UI_CONTROL.equals(messageType);
        }
    }