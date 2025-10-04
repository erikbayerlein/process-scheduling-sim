package com.api.process_scheduling.entities;

import com.api.process_scheduling.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Process {

  static Long nextId = 0L;

  Long pid;

  int creationTime;
  int duration;
  int staticPriority;

  int remainingTime;
  ProcessStatus status;
  int startTime;
  int waitingTime;
  int turnaroundTime;

  public Process(int creationTime, int duration, int staticPriority) {
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
