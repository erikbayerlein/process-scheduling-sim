"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import type { GanttSegment, Process } from "@/lib/types"

interface GanttChartProps {
  data: GanttSegment[]
  processes: Process[]
}

export function GanttChart({ data, processes }: GanttChartProps) {
  if (data.length === 0) return null

  const maxTime = Math.max(...data.map((d) => d.end))
  const timeUnits = Array.from({ length: maxTime + 1 }, (_, i) => i)

  const getProcessColor = (pid: number) => {
    const process = processes.find((p) => Number(p.id) === pid)
    return process?.color || "#3b82f6"
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Diagrama de Gantt</CardTitle>
        <CardDescription>Visualização temporal da execução dos processos</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Timeline */}
          <div className="relative">
            <div className="flex border-b border-border pb-2 mb-4">
              {timeUnits.map((time) => (
                <div
                  key={time}
                  className="flex-1 text-center text-xs text-muted-foreground font-mono"
                  style={{ minWidth: "60px" }}
                >
                  {time}
                </div>
              ))}
            </div>

            {/* Gantt bars */}
            <div className="relative h-16 bg-muted/30 rounded-lg overflow-hidden">
              {data.map((item, index) => {
                const width = ((item.end - item.start) / maxTime) * 100
                const left = (item.start / maxTime) * 100
                const processLabel = `P${item.processId}`

                return (
                  <div
                    key={index}
                    className="absolute h-full flex items-center justify-center transition-all hover:opacity-80 cursor-pointer group"
                    style={{
                      left: `${left}%`,
                      width: `${width}%`,
                      backgroundColor: item.color,
                    }}
                    title={`${processLabel}: ${item.start} → ${item.end}`}
                  >
                    <span className="text-sm font-bold text-white font-mono drop-shadow-lg">{processLabel}</span>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Legend */}
          <div className="flex flex-wrap gap-4 pt-4 border-t border-border">
            {Array.from(new Set(data.map((d) => d.processId))).map((processId) => {
              const segment = data.find((d) => d.processId === processId)
              const processLabel = `P${processId}`

              return (
                <div key={processId} className="flex items-center gap-2">
                  <div
                    className="h-3 w-3 rounded-full"
                    style={{ backgroundColor: segment?.color || getProcessColor(processId) }}
                  />
                  <span className="text-sm font-mono font-medium">{processLabel}</span>
                </div>
              )
            })}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
