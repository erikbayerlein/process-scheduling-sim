package com.api.process_scheduling.dto;

/**
 * @param creationTime  tempo de criação
 * @param duration tempo de duração
 * @param staticPriority prioridade estática
 */
public record ProcessDTOMessage(Long creationTime, int duration, int staticPriority) {

}