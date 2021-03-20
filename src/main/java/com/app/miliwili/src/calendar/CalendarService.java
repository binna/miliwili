package com.app.miliwili.src.calendar;

import com.app.miliwili.config.BaseException;
import com.app.miliwili.src.calendar.dto.PostDDayReq;
import com.app.miliwili.src.calendar.dto.PostDDayRes;
import com.app.miliwili.src.calendar.dto.PostScheduleReq;
import com.app.miliwili.src.calendar.dto.PostScheduleRes;
import com.app.miliwili.src.user.UserProvider;
import com.app.miliwili.src.user.models.User;
import com.app.miliwili.utils.FirebaseCloudMessage;
import com.app.miliwili.src.calendar.models.*;
import com.app.miliwili.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.app.miliwili.config.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
public class CalendarService {
    private final ScheduleRepository scheduleRepository;
    private final DDayRepository dDayRepository;
    private final JwtService jwtService;
    private final CalendarProvider calendarProvider;
    private final UserProvider userProvider;

    /**
     * 일정 생성
     * @param PostScheduleReq parameters
     * @return PostScheduleRes
     * @throws BaseException
     * @Auther shine
     */
    public PostScheduleRes createSchedule(PostScheduleReq parameters) throws BaseException {
        User user = userProvider.retrieveUserByIdAndStatusY(jwtService.getUserId());

        Schedule newSchedule = Schedule.builder()
                .color(parameters.getColor())
                .distinction(parameters.getDistinction())
                .title(parameters.getTitle())
                .user(user)
                .build();
        setScheduleDate(parameters, newSchedule);

        if(Objects.nonNull(parameters.getRepetition())) {
            newSchedule.setRepetition(parameters.getRepetition());
        }
        if(Objects.nonNull(parameters.getPush())) {
            newSchedule.setPush(parameters.getPush());
        }
        if(Objects.nonNull(parameters.getToDoList())) {
            newSchedule.setToDoLists(calendarProvider.retrieveToDoList(parameters.getToDoList()));
        }

        if(newSchedule.getDistinction().equals("휴가")) {
            newSchedule.setScheduleVacations(
                parameters.getScheduleLeaveData().stream().map(scheduleLeaveDataRes -> {
                    return ScheduleVacation.builder()
                            .count(scheduleLeaveDataRes.getCount())
                            .schedule(newSchedule)
                            .vacationId(scheduleLeaveDataRes.getLeaveId())
                            .build();
                }).collect(Collectors.toSet()));
        }
        if(newSchedule.getPush().equals("Y")) {
            newSchedule.setPushDeviceToken(parameters.getPushDeviceToken());
        }

        try {
            Schedule savedSchedule = scheduleRepository.save(newSchedule);
            return PostScheduleRes.builder()
                    .scheduleId(savedSchedule.getId())
                    .color(savedSchedule.getColor())
                    .distinction(savedSchedule.getDistinction())
                    .title(savedSchedule.getTitle())
                    .repetition(savedSchedule.getRepetition())
                    .push(savedSchedule.getPush())
//                    .toDoList(calendarProvider.retrieveWorkRes(savedSchedule.getToDoLists()))
                    .build();
        } catch (Exception exception) {
            throw new BaseException(FAILED_TO_POST_SCHEDULE);
        }
    }

    private void setScheduleDate(PostScheduleReq parameters, Schedule schedule) {
        List<ScheduleDate> scheduleDates = new ArrayList<>();

        LocalDate startDate = LocalDate.parse(parameters.getStartDate(), DateTimeFormatter.ISO_DATE);
        LocalDate endDate = LocalDate.parse(parameters.getEndDate(), DateTimeFormatter.ISO_DATE);

        LocalDate targetDate = startDate;
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= days; i++) {
            scheduleDates.add(ScheduleDate.builder()
                    .date(targetDate)
                    .schedule(schedule)
                    .build());
            targetDate = targetDate.plusDays(Long.valueOf(1));
        }
//        schedule.setScheduleDates(scheduleDates);
    }

    /**
     * D-Day 생성
     * @param
     * @return
     * @throws BaseException
     * @Auther shine
     */
    public PostDDayRes createDDay(PostDDayReq parameters) throws BaseException {
        User user = userProvider.retrieveUserByIdAndStatusY(jwtService.getUserId());

        DDay dDay = DDay.builder()
                .distinction(parameters.getDistinction())
                .title(parameters.getTitle())
                .subtitle(parameters.getSubTitle())
                .startDay(LocalDate.parse(parameters.getStartDay(), DateTimeFormatter.ISO_DATE))
                .endDay(LocalDate.parse(parameters.getEndDay(), DateTimeFormatter.ISO_DATE))
                .placeLat(parameters.getPlaceLat())
                .placeLon(parameters.getPlaceLon())
                .user(user)
                .build();

        if (!Objects.isNull(parameters.getLink())) {
            dDay.setLink(parameters.getLink());
        }
        if (!Objects.isNull(parameters.getChoiceCalendar())) {
            dDay.setChoiceCalendar(parameters.getChoiceCalendar());
        }

        try {
            DDay savedDDay = dDayRepository.save(dDay);
            return PostDDayRes.builder()
                    .dDayId(savedDDay.getId())
                    .distinction(savedDDay.getDistinction())
                    .title(savedDDay.getTitle())
                    .subtitle(savedDDay.getSubtitle())
                    .startDay(savedDDay.getStartDay().format(DateTimeFormatter.ISO_DATE))
                    .endDay(savedDDay.getEndDay().format(DateTimeFormatter.ISO_DATE))
                    .link(savedDDay.getLink())
                    .choiceCalendar(savedDDay.getChoiceCalendar())
                    .placeLat(savedDDay.getPlaceLat())
                    .placeLon(savedDDay.getPlaceLon())
                    //.supplies()
                    .build();
        } catch (Exception exception) {
            throw new BaseException(FAILED_TO_POST_D_DAY);
        }
    }



}