package com.api.process_scheduling.services;

import com.api.process_scheduling.entities.Process;

public interface SchedulerService {

    String schedule(Process process);
}
