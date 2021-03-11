package com.app.miliwili.src.calendar;

import com.app.miliwili.src.calendar.models.Schedule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends CrudRepository<Schedule, Long> {
    // TODO
}