'use client';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import type { GanttSegment, Process, ProcessCompleteEvent } from '@/lib/types';

interface GanttChartProps {
  data: GanttSegment[];
  processes: Process[];
  completedProcesses?: ProcessCompleteEvent[];
}

export function GanttChart({ data, processes, completedProcesses = [] }: GanttChartProps) {
  if (data.length === 0) return null;

  const maxTime = Math.max(...data.map(d => d.end));
  const timeUnits = Array.from({ length: maxTime + 1 }, (_, i) => i);

  // Agrupar segmentos por processo
  const processIds = Array.from(new Set(data.map(d => d.processId))).sort((a, b) => a - b);

  const getProcessSegments = (pid: number) => {
    return data.filter(d => d.processId === pid);
  };

  const getProcessColor = (pid: number) => {
    const process = processes.find(p => Number(p.id) === pid);
    return process?.color || '#3b82f6';
  };

  const getProcessStats = (pid: number) => {
    return completedProcesses.find(p => p.pid === pid);
  };

  const ROW_HEIGHT = 48;
  const CELL_WIDTH = 60;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Diagrama de Gantt</CardTitle>
        <CardDescription>Visualização temporal da execução dos processos</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <div className="inline-block min-w-full">
            {/* Header com timeline */}
            <div className="flex">
              {/* Coluna de processos (header) */}
              <div className="flex-shrink-0 w-24 border-r border-border bg-muted/50">
                <div className="h-12 flex items-center justify-center font-semibold text-sm">Processo</div>
              </div>

              {/* Timeline */}
              <div className="flex flex-1">
                {timeUnits.map(time => (
                  <div
                    key={time}
                    className="flex-shrink-0 border-l border-border first:border-l-0"
                    style={{ width: `${CELL_WIDTH}px` }}>
                    <div className="h-12 flex items-center justify-center text-xs font-mono text-muted-foreground">
                      {time}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Linhas dos processos */}
            {processIds.map(processId => {
              const segments = getProcessSegments(processId);
              const processColor = getProcessColor(processId);
              const processStats = getProcessStats(processId);

              return (
                <div key={processId} className="flex border-t border-border">
                  {/* Label do processo */}
                  <TooltipProvider delayDuration={200}>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <div
                          className="flex-shrink-0 w-24 border-r border-border bg-muted/30 flex items-center justify-center gap-2 cursor-help"
                          style={{ height: `${ROW_HEIGHT}px` }}>
                          <div className="h-3 w-3 rounded-full" style={{ backgroundColor: processColor }} />
                          <span className="font-mono font-medium text-sm">P{processId}</span>
                        </div>
                      </TooltipTrigger>
                      {processStats && (
                        <TooltipContent side="left" className="font-mono">
                          <div className="space-y-1">
                            <p className="font-semibold">Processo {processId}</p>
                            <div className="text-xs space-y-0.5">
                              <p>
                                <span className="text-muted-foreground">TT:</span>{' '}
                                <span className="font-semibold">{processStats.tt}</span>
                              </p>
                              <p>
                                <span className="text-muted-foreground">WT:</span>{' '}
                                <span className="font-semibold">{processStats.wt}</span>
                              </p>
                            </div>
                          </div>
                        </TooltipContent>
                      )}
                    </Tooltip>
                  </TooltipProvider>

                  {/* Grid do tempo */}
                  <div className="flex-1 relative" style={{ height: `${ROW_HEIGHT}px` }}>
                    {/* Linhas de grid verticais */}
                    {Array.from({ length: maxTime + 2 }, (_, i) => i).map(time => (
                      <div
                        key={time}
                        className="absolute top-0 bottom-0 border-l border-border/30"
                        style={{ left: `${time * CELL_WIDTH}px` }}
                      />
                    ))}

                    {/* Barras de execução */}
                    {segments.map((segment, index) => {
                      const width = (segment.end - segment.start) * CELL_WIDTH;
                      const left = segment.start * CELL_WIDTH;

                      return (
                        <TooltipProvider key={index} delayDuration={100}>
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <div
                                className="absolute top-1 bottom-1 rounded flex items-center justify-center transition-all hover:opacity-80 cursor-pointer"
                                style={{
                                  left: `${left}px`,
                                  width: `${width}px`,
                                  backgroundColor: processColor,
                                }}>
                                <span className="text-xs font-bold text-white font-mono drop-shadow">
                                  {segment.start}-{segment.end}
                                </span>
                              </div>
                            </TooltipTrigger>
                            <TooltipContent className="font-mono">
                              <div className="space-y-1">
                                <p className="font-semibold">Processo {processId}</p>
                                <div className="text-xs space-y-0.5">
                                  <p>
                                    <span className="text-muted-foreground">Início:</span>{' '}
                                    <span className="font-semibold">{segment.start}</span>
                                  </p>
                                  <p>
                                    <span className="text-muted-foreground">Fim:</span>{' '}
                                    <span className="font-semibold">{segment.end}</span>
                                  </p>
                                  <p>
                                    <span className="text-muted-foreground">Duração:</span>{' '}
                                    <span className="font-semibold">{segment.end - segment.start}</span>
                                  </p>
                                  {processStats && (
                                    <>
                                      <div className="h-px bg-border my-1" />
                                      <p>
                                        <span className="text-muted-foreground">TT:</span>{' '}
                                        <span className="font-semibold">{processStats.tt}</span>
                                      </p>
                                      <p>
                                        <span className="text-muted-foreground">WT:</span>{' '}
                                        <span className="font-semibold">{processStats.wt}</span>
                                      </p>
                                    </>
                                  )}
                                </div>
                              </div>
                            </TooltipContent>
                          </Tooltip>
                        </TooltipProvider>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
