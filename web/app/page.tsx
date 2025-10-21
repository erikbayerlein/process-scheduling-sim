"use client"

import { useState, useCallback } from "react"
import { AlgorithmSelector } from "@/components/algorithm-selector"
import { ProcessTable } from "@/components/process-table"
import { SimulationResults } from "@/components/simulation-results"
import { GanttChart } from "@/components/gantt-chart"
import { ThemeToggle } from "@/components/theme-toggle"
import { Button } from "@/components/ui/button"
import { Play, Wifi, WifiOff } from "lucide-react"
import { useStompWebSocket } from "@/hooks/use-stomp-websocket"
import { useToast } from "@/hooks/use-toast"
import {
  ALGORITHM_MAP,
  type Process,
  type StatusUpdateEvent,
  type ProcessCompleteEvent,
  type SimulationCompletedEvent,
  type GanttSegment,
} from "@/lib/types"

export type Algorithm =
  | "fcfs"
  | "round-robin"
  | "round-robin-aging"
  | "srtf"
  | "sjf"
  | "priority-preemptive"
  | "priority-non-preemptive"

export default function Home() {
  const [algorithm, setAlgorithm] = useState<Algorithm>("fcfs")
  const [quantum, setQuantum] = useState<number>(2)
  const [aging, setAging] = useState<number>(1)
  const [processes, setProcesses] = useState<Process[]>([])
  const [isSimulating, setIsSimulating] = useState(false)
  const [currentStatus, setCurrentStatus] = useState<StatusUpdateEvent | null>(null)
  const [ganttData, setGanttData] = useState<GanttSegment[]>([])
  const [completedProcesses, setCompletedProcesses] = useState<ProcessCompleteEvent[]>([])
  const [metrics, setMetrics] = useState<SimulationCompletedEvent | null>(null)
  const { toast } = useToast()

  const handleStatusUpdate = useCallback(
    (event: StatusUpdateEvent) => {
      console.log("[v0] Atualizando status:", event)
      setCurrentStatus(event)

      // Atualizar diagrama de Gantt se houver processo em execução
      if (event.cpuRunningPid !== null) {
        setGanttData((prev) => {
          const lastSegment = prev[prev.length - 1]
          const processColor = processes.find((p) => Number(p.id) === event.cpuRunningPid)?.color || "#3b82f6"

          // Se o último segmento é do mesmo processo, estender
          if (lastSegment && lastSegment.processId === event.cpuRunningPid) {
            return [...prev.slice(0, -1), { ...lastSegment, end: event.time }]
          }

          // Caso contrário, adicionar novo segmento
          return [
            ...prev,
            {
              processId: event.cpuRunningPid,
              start: event.time - 1,
              end: event.time,
              color: processColor,
            },
          ]
        })
      }
    },
    [processes],
  )

  const handleProcessComplete = useCallback(
    (event: ProcessCompleteEvent) => {
      console.log("[v0] Processo concluído:", event)
      setCompletedProcesses((prev) => [...prev, event])
      toast({
        title: "Processo concluído",
        description: `Processo ${event.pid} finalizado. TT: ${event.tt}, WT: ${event.wt}`,
      })
    },
    [toast],
  )

  const handleSimulationComplete = useCallback(
    (event: SimulationCompletedEvent) => {
      console.log("[v0] Simulação concluída:", event)
      setMetrics(event)
      setIsSimulating(false)
      toast({
        title: "Simulação concluída",
        description: "Todos os processos foram executados. Confira os resultados abaixo.",
      })
    },
    [toast],
  )

  const { isConnected, isConnecting, connect, disconnect, startSimulation } = useStompWebSocket({
    url: process.env.NEXT_PUBLIC_WEBSOCKET_URL || "ws://localhost:8080/ws",
    onStatusUpdate: handleStatusUpdate,
    onProcessComplete: handleProcessComplete,
    onSimulationComplete: handleSimulationComplete,
    onError: (error) => {
      toast({
        title: "Erro",
        description: error,
        variant: "destructive",
      })
    },
    onOpen: () => {
      toast({
        title: "Conectado",
        description: "Conexão estabelecida com o servidor de simulação.",
      })
    },
    onClose: () => {
      if (isSimulating) {
        setIsSimulating(false)
        toast({
          title: "Desconectado",
          description: "Conexão perdida com o servidor de simulação.",
          variant: "destructive",
        })
      }
    },
  })

  const handleSimulate = () => {
    if (!isConnected) {
      toast({
        title: "Não conectado",
        description: "Conecte-se ao servidor antes de iniciar a simulação.",
        variant: "destructive",
      })
      return
    }

    if (processes.length === 0) {
      toast({
        title: "Nenhum processo",
        description: "Adicione pelo menos um processo antes de simular.",
        variant: "destructive",
      })
      return
    }

    setCurrentStatus(null)
    setGanttData([])
    setCompletedProcesses([])
    setMetrics(null)
    setIsSimulating(true)

    const config = {
      algorithm: ALGORITHM_MAP[algorithm],
      processes: processes.map((p) => ({
        creationTime: p.arrivalTime,
        duration: p.duration,
        staticPriority: p.priority,
      })),
      config: {
        quantum: algorithm === "round-robin" || algorithm === "round-robin-aging" ? quantum : undefined,
        aging: algorithm === "round-robin-aging" ? aging : undefined,
      },
    }

    console.log("[v0] Iniciando simulação com config:", config)
    const success = startSimulation(config)

    if (!success) {
      setIsSimulating(false)
      toast({
        title: "Erro ao enviar",
        description: "Não foi possível enviar os dados da simulação.",
        variant: "destructive",
      })
    }
  }

  const hasProcesses = processes.length > 0

  return (
    <main className="min-h-screen bg-background p-6">
      <div className="mx-auto max-w-7xl space-y-8">
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <div className="space-y-2">
              <h1 className="text-4xl font-bold tracking-tight text-balance">
                Simulador de Escalonamento de Processos
              </h1>
              <p className="text-muted-foreground text-lg">
                Configure o algoritmo e os processos para simular o escalonamento
              </p>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-md border border-border bg-card">
                {isConnected ? (
                  <>
                    <Wifi className="h-4 w-4 text-green-500" />
                    <span className="text-sm font-medium">Conectado</span>
                    <Button variant="ghost" size="sm" onClick={disconnect} className="h-7 px-2 ml-1">
                      Desconectar
                    </Button>
                  </>
                ) : (
                  <>
                    <WifiOff className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm font-medium text-muted-foreground">
                      {isConnecting ? "Conectando..." : "Desconectado"}
                    </span>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={connect}
                      disabled={isConnecting}
                      className="h-7 px-2 ml-1"
                    >
                      {isConnecting ? "Conectando..." : "Conectar"}
                    </Button>
                  </>
                )}
              </div>
              <ThemeToggle />
            </div>
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <AlgorithmSelector
            algorithm={algorithm}
            quantum={quantum}
            aging={aging}
            onAlgorithmChange={setAlgorithm}
            onQuantumChange={setQuantum}
            onAgingChange={setAging}
          />

          <ProcessTable processes={processes} onProcessesChange={setProcesses} />
        </div>

        <div className="flex justify-center">
          <Button
            size="lg"
            onClick={handleSimulate}
            disabled={!hasProcesses || isSimulating || !isConnected}
            className="min-w-48 text-lg h-12"
          >
            <Play className="mr-2 h-5 w-5" />
            {isSimulating ? "Simulando..." : "Simular"}
          </Button>
        </div>

        {currentStatus && (
          <div className="rounded-lg border border-border bg-card p-4 animate-in fade-in slide-in-from-bottom-2">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Tempo: {currentStatus.time}</p>
                <p className="font-medium">
                  CPU:{" "}
                  <span className="font-mono text-primary">
                    {currentStatus.cpuRunningPid !== null ? `P${currentStatus.cpuRunningPid}` : "Ocioso"}
                  </span>
                </p>
              </div>
              <div className="space-y-1 text-right">
                <p className="text-sm text-muted-foreground">Fila de Prontos</p>
                <p className="font-mono text-sm">
                  {currentStatus.readyQueueState.length > 0
                    ? currentStatus.readyQueueState.map((p) => `P${p.pid}`).join(", ")
                    : "Vazia"}
                </p>
              </div>
            </div>
            {currentStatus.readyQueueState.length > 0 && (
              <div className="mt-3 pt-3 border-t border-border">
                <p className="text-xs text-muted-foreground mb-2">Detalhes da fila:</p>
                <div className="flex flex-wrap gap-2">
                  {currentStatus.readyQueueState.map((proc) => (
                    <div
                      key={proc.pid}
                      className="text-xs bg-muted px-2 py-1 rounded font-mono flex items-center gap-2"
                    >
                      <span className="font-semibold">P{proc.pid}</span>
                      <span className="text-muted-foreground">Restante: {proc.remainingTime}</span>
                      <span className="text-muted-foreground">Prior: {proc.dynamicPriority}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {metrics && (
          <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="h-px bg-border" />

            <SimulationResults
              metrics={{
                averageTurnaroundTime: metrics.averageTurnaroundTime,
                averageWaitingTime: metrics.averageWaitingTime,
                contextSwitches: metrics.totalContextSwitches,
              }}
            />

            <GanttChart data={ganttData} processes={processes} />
          </div>
        )}
      </div>
    </main>
  )
}
