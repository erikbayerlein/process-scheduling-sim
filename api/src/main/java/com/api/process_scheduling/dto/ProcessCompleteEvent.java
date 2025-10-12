package com.api.process_scheduling.dto;

import lombok.Builder;

/**
 * @param pid process id do processo completado
 * @param tt  turnaround time (tempo total de execução)
 * @param wt  waiting time (tempo de espera)
 */
@Builder
public record ProcessCompleteEvent(Long pid, Integer tt, Integer wt) {

}
