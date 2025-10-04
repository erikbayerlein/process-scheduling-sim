package com.api.process_scheduling.entities;

import com.api.process_scheduling.models.ProcessStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Process {
    static int nextId = 0;

    int pid;

    int creationTime;
    int duration;
    int staticPriority;

    int remainingTime;
    ProcessStatus status;
    int startTime;
    int waitingTime;
    int turnaroundTime;

    Process(int creationTime, int duration, int staticPriority) {
        this.pid = nextId++;

        this.creationTime = creationTime;
        this.duration = duration;
        this.staticPriority = staticPriority;

        this.remainingTime = duration;
        this.status = ProcessStatus.READY;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }


}
