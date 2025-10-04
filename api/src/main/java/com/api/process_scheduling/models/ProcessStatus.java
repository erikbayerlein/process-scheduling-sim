package com.api.process_scheduling.models;


/**
 * Enum para os estados de um processo no sistema de escalonamento.
 * apenas 3 estados pois nao será considerado estados intermediarios, como novo, esperando ou bloqueado
 */
public enum ProcessStatus {
    READY,
    RUNNING,
    TERMINATED
}
