package com.app.miliwili.src.calendar.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
public class ScheduleVacationReq {
    private Long vacationId;
    private Integer count;
}