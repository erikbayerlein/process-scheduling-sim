package com.api.process_scheduling.controllers;

import com.api.process_scheduling.dto.ScheduleResponse;
import com.api.process_scheduling.entities.Process;

public interface ProcessSchedulerController {

    ScheduleResponse scheduleProcesses(Process process) throws InterruptedException;
}
