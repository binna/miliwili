package com.app.miliwili.src.exercise;

import com.app.miliwili.config.BaseException;
import com.app.miliwili.src.exercise.dto.*;
import com.app.miliwili.src.exercise.model.ExerciseInfo;
import com.app.miliwili.src.exercise.model.ExerciseRoutine;
import com.app.miliwili.src.exercise.model.ExerciseWeightRecord;
import com.app.miliwili.src.user.UserRepository;
import com.app.miliwili.utils.JwtService;
import io.jsonwebtoken.Jwt;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static com.app.miliwili.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseProvider {
    private final ExerciseSelectRepository exerciseSelectRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseWeightRepository exerciseWeightRepository;
    private final JwtService jwtService;



    /**
     * 일별 체중 조회
     */
    @Transactional
    public GetExerciseDailyWeightRes retrieveExerciseDailyWeight(long exerciseId) throws BaseException{
        ExerciseInfo exerciseInfo = getExerciseInfo(exerciseId);
        List<ExerciseWeightRecord> exerciseDailyWeightList = null;


        if(exerciseInfo.getUser().getId() != jwtService.getUserId()){
            throw new BaseException(INVALID_USER);
        }

        try{
            exerciseDailyWeightList = exerciseWeightRepository.findTop5ByExerciseInfo_IdAndStatusOrderByDateCreatedDesc(exerciseId,"Y");
        }catch (Exception e){
            e.printStackTrace();
            throw new BaseException(FAILED_GET_DAILY_WEIGHT);
        }

        GetExerciseDailyWeightRes dailyWeightRes = GetExerciseDailyWeightRes.builder()
                .goalWeight(exerciseInfo.getGoalWeight())
                .dailyWeightList(getDailyWeightTodailyWeightList(exerciseDailyWeightList))
                .weightDayList(getDailyWeightTodailyDaytList(exerciseDailyWeightList))
                .build();

        return dailyWeightRes;
    }


    /**
     * for Service createFistWeight
     * 이미 ExerciseInfo가지는 회원이면 true
     * 없는 회원이라면 false
     * 없어야만 첫 체중, 목표체중 등록 가능
     */
    public Boolean isExerciseInfoUser(long userId) throws BaseException{
        List<Long> exerciseIdList = null;
        try{
            exerciseIdList = exerciseSelectRepository.getExerciseInfoByUserId(userId);
        }catch (Exception e){
            e.printStackTrace();
            throw new BaseException(FAILED_TO_GET_USER);
        }

        if(exerciseIdList.size() == 0)
            return false;
        else
            return true;
    }

    /**
     * 체중 기록 조회
     */
    @Transactional
    public GetExerciseWeightRecordRes retrieveExerciseWeightRecord(Integer viewMonth, Integer viewYear, Long exerciseId) throws BaseException{
        ExerciseInfo exerciseInfo = getExerciseInfo(exerciseId);
        List<ExerciseWeightRecord> allRecordList = exerciseInfo.getWeightRecords();
        List<ExerciseWeightRecord> exerciseWeightList = new ArrayList<>();


        if (exerciseInfo.getUser().getId() != jwtService.getUserId()) {
            throw new BaseException(INVALID_USER);
        }

        //생성 날짜 오름차순 정렬
        Collections.sort(allRecordList, new Comparator<ExerciseWeightRecord>() {
            @Override
            public int compare(ExerciseWeightRecord o1, ExerciseWeightRecord o2) {
                return o1.getDateCreated().compareTo(o2.getDateCreated());
            }
        });

        //지정한 의 모든 몸무게 정보 가져오기
        for(int i=0;i<allRecordList.size();i++){
            ExerciseWeightRecord record = allRecordList.get(i);
            if(record.getDateCreated().getYear() == viewYear && record.getDateCreated().getMonthValue() == viewMonth){
                exerciseWeightList.add(record);
            }
        }

        //최근 5개월 가져와서 평균내기
        //month에 들어갈애들
        List<String> monthWeightMonth = new ArrayList<>();
        List<Double> monthWeight =new ArrayList<>();

        int idx=1;
        int continueIndx=1;
        int nowMonth = LocalDate.now().getMonthValue();
        int nowYear = LocalDate.now().getYear();

        int wantMonth = nowMonth - 1;
        int wantYear = nowYear;

        int lastIdx=0;
        while(idx <= 5 && continueIndx <=3) {
            List<ExerciseWeightRecord> monthWeightList = new ArrayList<>();
            double sum = 0.0;

            if (wantMonth == 0) {
                wantYear--;
                wantMonth = 12;
            }

            for (int i = 0; i < allRecordList.size(); i++) {
                ExerciseWeightRecord record = allRecordList.get(i);
                if (record.getDateCreated().getYear() == wantYear && record.getDateCreated().getMonthValue() == wantMonth) {
                    monthWeightList.add(record);
                }

            }
            if (monthWeightList.size() == 0) {
                wantMonth--;
                continueIndx++;
                continue;
            }

            monthWeightMonth.add(wantMonth + "월");

            for (int k = 0; k < monthWeightList.size(); k++) {
                sum += monthWeightList.get(k).getWeight();
            }
            double avg = sum / (monthWeightList.size());
            monthWeight.add(Math.round(avg * 100) / 100.0);
            System.out.println(idx);
            idx++;
            wantMonth--;

        }
        for(int i=0;i<monthWeight.size();i++){
            System.out.println(monthWeight.get(i));
        }
        for(int i=0;i<monthWeight.size();i++){
            System.out.println(monthWeightMonth.get(i));
        }

        GetExerciseWeightRecordRes getExerciseWeightRecordRes = GetExerciseWeightRecordRes.builder()
                .goalWeight(exerciseInfo.getGoalWeight())
                .monthWeight(monthWeight)
                .monthWeightMonth(monthWeightMonth)
                .dayWeightDay(dayWeightDayList(viewYear,viewMonth))
                .dayWeight(dayWeightListWeight(dayweightList(viewYear, viewMonth,exerciseWeightList)))
                .dayDif(dayWeightListDif(exerciseInfo.getGoalWeight(),dayweightList(viewYear, viewMonth,exerciseWeightList)))
                .build();

        System.out.println("making done");
        return getExerciseWeightRecordRes;
    }

    //몇월 몇일인지 출력
    public List<String> dayWeightDayList( int year, int month){
        List<String> dayList= new ArrayList<>();
        System.out.println("dayList");
        LocalDate standardMonth = LocalDate.of(year,month,1);
        int moveDay = 1;
        int monthInt = standardMonth.getMonthValue();
        LocalDate moveMonth = standardMonth;

        while(moveMonth.getMonthValue() == standardMonth.getMonthValue()){
            dayList.add(monthInt+"월"+moveMonth.getDayOfMonth()+"일");
            try {
                moveMonth = LocalDate.of(year, month, ++moveDay);
            }catch (Exception e){
                break;
            }
        }
        return dayList;
    }

    //몇월 몇일에 몸무게가 얼마였는지 출력  --> int형 --> 이후 차이 계산을 위해  --> 얘는 그대로 쓰이는데 없음
    public List<Double> dayweightList(int year, int month, List<ExerciseWeightRecord> recordList){
        System.out.println("dayWeightList");

        List<Double> dayWeightList = new ArrayList<>();
        int index=0;
        int moveDay = 1;
        boolean isEndIndx=false;
        LocalDate standardMonth = LocalDate.of(year,month,1);
        LocalDate moveMonth = standardMonth;

        while(moveMonth.getMonthValue() == standardMonth.getMonthValue()){
            if(isEndIndx == true) {
                dayWeightList.add(0.0);
                try {
                    moveMonth = LocalDate.of(year, month, ++moveDay);
                }catch (Exception e){
                    break;
                }
                continue;
            }
            if(moveMonth.isEqual(recordList.get(index).getDateCreated().toLocalDate())){
                dayWeightList.add(recordList.get(index).getWeight());
                if(index == recordList.size()-1) {
                    isEndIndx = true;
                }else{
                    index++;
                }
            }else{
                dayWeightList.add(0.0);
            }

            try {
                moveMonth = LocalDate.of(year, month, ++moveDay);
            }catch (Exception e){
                break;
            }
        }



        return dayWeightList;
    }
    //dayWeight변환
    public List<String> dayWeightListWeight(List<Double> weightList){
        System.out.println("ListWeight");

        List<String> changedList = new ArrayList<>();
        for(int i=0;i<weightList.size();i++){
            if(weightList.get(i) == 0.0){
                changedList.add("정보 없음");
            }else {
                changedList.add((weightList.get(i)).toString());
            }
        }
        return changedList;
    }
    //차이 변환
    public List<Double> dayWeightListDif(double goalWeight,List<Double> weightList){
        System.out.println("WeightDif");

        List<Double> changedList = new ArrayList<>();
        for(int i=0;i<weightList.size();i++){
            if(weightList.get(i) == 0.0){
                changedList.add(0.0);
            }else {
                changedList.add(Math.round((goalWeight-weightList.get(i)) * 100) / 100.0);
            }
        }
        return changedList;
    }


    /**
     * 사용자의 전체 루틴 조회
     */
    public List<MyRoutineInfo> retrieveAllRoutineList(long exerciseId) throws BaseException{
        ExerciseInfo exerciseInfo = getExerciseInfo(exerciseId);

        if (exerciseInfo.getUser().getId() != jwtService.getUserId()) {
            throw new BaseException(INVALID_USER);
        }

        List<ExerciseRoutine> routineList= exerciseInfo.getExerciseRoutines();
        List<MyRoutineInfo> myAllRoutines = new ArrayList<>();
        for(int i=0; i< routineList.size(); i++){
            String[] repeatDay = routineList.get(i).getRepeaDay().split("#");
            String repeatDayStr = "";
            for(int j=0;j<repeatDay.length;j++){
                switch (repeatDay[j]){
                    case "1":
                        repeatDayStr+="매일,";
                        break;
                    case "2":
                        repeatDayStr+="월,";
                        break;
                    case "3":
                        repeatDayStr+="화,";
                        break;
                    case "4":
                        repeatDayStr+="수,";
                        break;
                    case "5":
                        repeatDayStr+="목,";
                        break;
                    case "6":
                        repeatDayStr+="금,";
                        break;
                    case "7":
                        repeatDayStr+="토,";
                        break;
                    case "8":
                        repeatDayStr+="일,";
                        break;
                }
            }
            MyRoutineInfo myRoutineInfo = MyRoutineInfo.builder()
                    .routineName(routineList.get(i).getName())
                    .routineRepeatDay(repeatDayStr.substring(0,repeatDayStr.length()-1))
                    .routineId(routineList.get(i).getId())
                    .build();
            myAllRoutines.add(myRoutineInfo);
        }

        return myAllRoutines;
    }


    /**
         * ExerciseId로 ExerciseInfo Return
         */
    public ExerciseInfo getExerciseInfo(long exerciseId) throws BaseException{
        return exerciseRepository.findByIdAndStatus(exerciseId, "Y")
            .orElseThrow(() -> new BaseException(NOT_FOUND_EXERCISEINFO));
    }

    /**
     * 생성 날짜로 exerciseWeightRecord찾기
     */
    public ExerciseWeightRecord getExerciseWiehgtRecord(long exerciseId, LocalDateTime targetDate, LocalDateTime targetNextDate) throws BaseException{
       return exerciseWeightRepository.findExerciseWeightRecordsByExerciseInfo_IdAndStatusAndDateCreatedBetween
               (exerciseId, "Y", targetDate, targetNextDate)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_EXERCISE_WEIGHT_RECORD));
    }

    /**
     * GetExerciseDailywWeightRes에 들어가는 List들 만들기
     */

    public List<String> getDailyWeightTodailyWeightList(List<ExerciseWeightRecord> dailyWeight){
        List<String> changedList = dailyWeight.stream().map(ExerciseWeightRecord -> {
            double weight = ExerciseWeightRecord.getWeight();
            return Double.toString(weight);
        }).collect(Collectors.toList());

        return changedList;
    }

    public List<String> getDailyWeightTodailyDaytList(List<ExerciseWeightRecord>  dailyWeight){
        List<String> changedList = dailyWeight.stream().map(ExerciseWeightRecord -> {
            LocalDate day = ExerciseWeightRecord.getDateCreated().toLocalDate();
            int monthValue = day.getMonthValue();
            int dayValue = day.getDayOfMonth();

            String monthStr = (monthValue < 10) ? ("0"+monthValue) : Integer.toString(monthValue);
            String dayStr = (dayValue < 10) ? ("0"+dayValue) : Integer.toString(dayValue);
            return monthStr+"/"+dayStr;
        }).collect(Collectors.toList());

        return changedList;
    }

}



