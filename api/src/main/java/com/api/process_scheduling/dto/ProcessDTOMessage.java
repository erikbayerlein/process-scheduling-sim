package com.api.process_scheduling.dto;

/**
 * @param creationTime   tempo de criação
 * @param duration       tempo de duração
 * @param staticPriority prioridade estática
 */
public record ProcessDTOMessage(int creationTime, int duration, int staticPriority) {

}