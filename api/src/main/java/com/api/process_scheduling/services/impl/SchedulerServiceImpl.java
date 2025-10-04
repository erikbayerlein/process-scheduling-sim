package com.api.process_scheduling.services.impl;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.SchedulerService;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Override
    public String schedule(Process process) {
       return "Scheduled Process: " + process.getPid();
    }
}
