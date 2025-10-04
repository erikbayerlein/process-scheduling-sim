package com.api.process_scheduling.controllers.impl;

import com.api.process_scheduling.controllers.ProcessSchedulerController;
import com.api.process_scheduling.dto.ScheduleResponse;
import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProcessSchedulerControllerImpl implements ProcessSchedulerController {

    private final SchedulerService schedulerService;

    @MessageMapping("/schedule") // Maps messages sent to /app/schedule
    @SendTo("/process-scheduler/results") // Sends results to /process-scheduling/results
    public ScheduleResponse scheduleProcesses(Process process) throws InterruptedException {
        Thread.sleep(1000);
        return new ScheduleResponse(schedulerService.schedule(process));
    }

}
