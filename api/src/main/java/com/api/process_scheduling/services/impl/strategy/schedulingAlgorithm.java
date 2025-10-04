package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;

import java.util.List;

public interface schedulingAlgorithm {
    Result<Process> selectNextProcess(List<Process> readyQueue, Process currentProcess);
}
