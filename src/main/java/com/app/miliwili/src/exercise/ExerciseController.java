package com.app.miliwili.src.exercise;

import com.app.miliwili.config.BaseException;
import com.app.miliwili.config.BaseResponse;
import com.app.miliwili.src.exercise.dto.*;
import com.app.miliwili.utils.Validation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.app.miliwili.config.BaseResponseStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/exercises")
public class ExerciseController {
    private final ExerciseProvider exerciseProvider;
    private final ExerciseService exerciseService;


    /**
     * 처음 운동 탭 입장시 call
     * 목표 체중, 현재 체중 입력 --> 입력 안해도 되지만 안할시에는 체중 기록 사용 불가
     * @return BaseResponse<Long>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PostMapping("/first-weights")
    public BaseResponse<Long> postFirstWeight(@RequestHeader("X-ACCESS-TOKEN") String token, @RequestBody PostExerciseFirstWeightReq param){
        try{
            Long exerciseId = exerciseService.createFistWeight(param);
            return new BaseResponse<>(SUCCESS,exerciseId);
        }catch (BaseException e){
            e.printStackTrace();
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * daily 체중 입력
     * @return BaseResponse<String>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PostMapping("/{exerciseId}/weights")
    public BaseResponse<String> postDailyWeight(@RequestHeader("X-ACCESS-TOKEN") String token, @RequestBody PostExerciseWeightReq param,
                                                @PathVariable Long exerciseId){
        //TODO: 하루에 한번만 입력되도록 검증
        if(param.getDayWeight() == null)
            return new BaseResponse<>(EMPTY_WEIGHT);

        try{
            String returnStr = exerciseService.createDayilyWeight(param,exerciseId);
            return new BaseResponse<>(SUCCESS,returnStr);
        }catch (BaseException e){
            e.printStackTrace();
            return new BaseResponse<>(FAILED_POST_DAILY_WEIGHT);
        }
    }

    /**
     * daily 체중 수정
     * @return BaseResponse<String>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PatchMapping("/{exerciseId}/weights")
    public BaseResponse<String> patchDailyWeight(@RequestHeader("X-ACCESS-TOKEN") String token, @RequestBody PatchExerciseDailyWeightReq param,
                                                 @PathVariable Long exerciseId){
        if(!Validation.isRegexDate(param.getDayDate())){
            return new BaseResponse<>(INVALID_MODIFY_DATE);
        }

        try{
            String resultStr = exerciseService.modifyDailyWeight(param,exerciseId);
            return new BaseResponse<>(SUCCESS,resultStr);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 목표체중 수정
     * @return BaseResponse<String>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PatchMapping("/{exerciseId}/goal-weights")
    public BaseResponse<String> patchGoalWeight(@RequestHeader("X-ACCESS-TOKEN") String token, @RequestBody PatchExerciseGoalWeight param,
                                                @PathVariable Long exerciseId){
        if(param.getGoalWeight() == null)
            return new BaseResponse<>(EMPTY_WEIGHT);

        try{
            String returnStr = exerciseService.modifyGoalWeight(param,exerciseId);
            return new BaseResponse<>(SUCCESS,returnStr);
        }catch (BaseException e){            e.printStackTrace();
            return new BaseResponse<>(FAILED_PATCH_GOAL_WEIGHT);
        }
    }

    /**
     * 일별 체중 기록 조회
     * @return BaseResponse<GetExerciseDailyWeight>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/daily-weights")
    public BaseResponse<GetExerciseDailyWeightRes> getDailyWeight(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId){
        try{
            GetExerciseDailyWeightRes exerciseDailyWeightRes= exerciseProvider.retrieveExerciseDailyWeight(exerciseId);
            return new BaseResponse<>(SUCCESS, exerciseDailyWeightRes);
        }catch (BaseException e){
            e.printStackTrace();
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 체중 기록 조회
     * @return BaseResponse<String>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/weight-records")
    public BaseResponse<GetExerciseWeightRecordRes> getWeightRecords(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                             @RequestParam Integer viewMonth, @RequestParam Integer viewYear){

        LocalDate now = LocalDate.now();
        if(viewYear > now.getYear() || (viewYear==now.getYear() && viewMonth>now.getMonthValue()))
            return new BaseResponse<>(INVALID_VIEW_DATE);
        try{
            GetExerciseWeightRecordRes result = exerciseProvider.retrieveExerciseWeightRecord(viewMonth, viewYear,exerciseId);
            System.out.println("outout again");
            return new BaseResponse<>(SUCCESS,result);
        }catch (BaseException e){
            e.printStackTrace();
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 루틴 생성
     * @return BaseResponse<String>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PostMapping("/{exerciseId}/routines")
    public BaseResponse<Long> postRoutines(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                           @RequestBody PostExerciseRoutineReq param){
        try{
            Long resultLong = exerciseService.createRoutine(param,exerciseId);
            System.out.println(resultLong);
            return new BaseResponse<>(SUCCESS,resultLong);
        }catch (BaseException e){
            e.printStackTrace();
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 루틴 수정
     * @return BaseResponse<String>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PatchMapping("/{exerciseId}/routines/{routineId}")
    public BaseResponse<String> patchRoutines(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,@PathVariable Long routineId,
                                           @RequestBody PostExerciseRoutineReq param){
        try{
            String resultStr = exerciseService.modifyRoutine(param,exerciseId,routineId);
            return new BaseResponse<>(SUCCESS,resultStr);
        }catch (BaseException e){
            e.printStackTrace();
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 전체 루틴 조회
     * @return BaseResponse<MyRoutineInfo>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/all-routines")
    public BaseResponse<List<MyRoutineInfo>> getMyAllRoutines(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId){
        try{
            List<MyRoutineInfo> myAllRoutineList = exerciseProvider.retrieveAllRoutineList(exerciseId);
            return new BaseResponse<>(SUCCESS, myAllRoutineList);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 루틴 삭제
     * @return BaseResponse<MyRoutineInfo>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @DeleteMapping("/{exerciseId}/routines/{routineId}")
    public BaseResponse<String> deleteMyAllRoutines(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                                              @PathVariable Long routineId){
        try{
            String resultStr = exerciseService.deleteRoutine(exerciseId,routineId);
            return new BaseResponse<>(SUCCESS, resultStr);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 특정 날짜의 루틴 조회
     * @return BaseResponse<MyRoutineInfo>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/routines")
    public BaseResponse<List<RoutineInfo>> getMyDateRoutines(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                                       @RequestParam String targetDate){
        if(!Validation.isRegexDate(targetDate)){
            return new BaseResponse<>(INVALID_MODIFY_DATE);
        }
        try{
            List<RoutineInfo> resultList = exerciseProvider.retrieveDateRoutine(exerciseId, targetDate);
            return new BaseResponse<>(SUCCESS, resultList);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 루틴 상세정보 조회 -> 루틴 수정을 위한
     * @return BaseResponse<GetExerciseRoutineRes>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/routines/{routineId}/detail-exercises")
    public BaseResponse<GetExerciseRoutineRes> getRoutineDetailForPatchRoutine(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                                                               @PathVariable Long routineId){
        try{
            GetExerciseRoutineRes resultRoutineRes = exerciseProvider.retrieveRoutineDetailForPatchRoutine(exerciseId,routineId);
            return new BaseResponse<>(SUCCESS,resultRoutineRes);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());

        }
    }

    /**
     * 루틴 상세정보 조회 -> 운동 시작을 위한
     * @return BaseResponse<GetStartExerciseRes>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/routines/{routineId}/start-exercises")
    public BaseResponse<GetStartExerciseRes> getRoutineDetailForStartExercise(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                                                               @PathVariable Long routineId){
        try{
            GetStartExerciseRes resultRoutineRes = exerciseProvider.retrieveRoutineInfoForStartExercise(exerciseId,routineId);
            return new BaseResponse<>(SUCCESS,resultRoutineRes);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());

        }
    }

    /**
     * 운동 리포트 생성
     * @return BaseResponse<Long>
     * @RequestHeader X-ACCESS-TOKEN
     * @Auther vivi
     */
    @ResponseBody
    @PostMapping("/{exerciseId}/routines/{routineId}/reports")
    public BaseResponse<Long> postExerciseReport(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId, @PathVariable Long routineId,
                                                                  @RequestBody PostExerciseReportReq param){
        //TODO : 검증더해야함
        if(param.getExerciseStatus().length() == 0 || param.getExerciseStatus() == null)
            return new BaseResponse<>(EMPTY_EXERCISESTATUS);
        if(param.getTotalTime().length() == 0 || param.getTotalTime() == null)
            return new BaseResponse<>(EMPTY_TOTALTIME);

        try{
            Long reportId = exerciseService.createExerciseReport(exerciseId,routineId,param);
            return new BaseResponse<>(SUCCESS, reportId);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 운동리포트 조회
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/routines/{routineId}/reports")
    public BaseResponse<GetExerciseReportRes> getExerciseReport(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId, @PathVariable Long routineId,
                                                                @RequestParam String reportDate){
        if(!Validation.isRegexDate(reportDate))
            return new BaseResponse<>(INVALID_DATE);

        try{
            GetExerciseReportRes reportRes = exerciseProvider.retrieveExerciseReport(exerciseId,routineId,reportDate);
            return new BaseResponse<>(SUCCESS,reportRes);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }

    /**
     * 운동 리포트 삭제
     */
    @ResponseBody
    @DeleteMapping("/{exerciseId}/routines/{routineId}/reports")
    public BaseResponse<String> delteRoutineExerciseReport(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId, @PathVariable Long routineId,
                                                           @RequestParam String reportDate){
        if(!Validation.isRegexDate(reportDate))
            return new BaseResponse<>(INVALID_DATE);

        try{
            String reportRes = exerciseService.deleteExerciseReport(exerciseId,routineId,reportDate);
            return new BaseResponse<>(SUCCESS,reportRes);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 운동 리포트 수정
     */
    @ResponseBody
    @PatchMapping("/{exerciseId}/routines/{routineId}/reports")
    public BaseResponse<String> patchExerciseReport(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId, @PathVariable Long routineId,
                                                           @RequestBody PatchExerciseReportReq param){
        if(!Validation.isRegexDate(param.getReportDate()))
            return new BaseResponse<>(INVALID_DATE);

        if(param.getReportText().length()>300)
            return new BaseResponse<>(FULL_REPORT_TEXT);

        try{
            String reportRes = exerciseService.modifyExerciseReport(exerciseId,routineId,param);
            return new BaseResponse<>(SUCCESS,reportRes);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 운동리포트 달력 조회
     */
    @ResponseBody
    @GetMapping("/{exerciseId}/reports")
    public BaseResponse<List<String>> getCalendarReports(@RequestHeader("X-ACCESS-TOKEN") String token, @PathVariable Long exerciseId,
                                                    @RequestParam Integer viewYear, @RequestParam Integer viewMonth){

        if(viewMonth<=0 || viewMonth>12)
            return new BaseResponse<>(INVALIED_VIEW_MONTH);

        try{
            List<String> reportRes = exerciseProvider.retrieveCalendarReport(exerciseId,viewYear,viewMonth);
            return new BaseResponse<>(SUCCESS,reportRes);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

}
