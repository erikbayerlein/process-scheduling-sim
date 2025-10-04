package com.api.process_scheduling.controllers;

import com.api.process_scheduling.dto.SimulationConfigMessage;

public interface ProcessSchedulerController {

  void startSimulation(SimulationConfigMessage request);
}
