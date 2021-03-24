package com.app.miliwili.src.emotionRecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class EmotionRecordRes {
    private final Long emotionRecordId;
    private final String content;
    private final String emotion;
}