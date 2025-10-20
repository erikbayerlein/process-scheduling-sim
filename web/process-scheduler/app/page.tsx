"use client"

import { useState, useCallback } from "react"
import { AlgorithmSelector } from "@/components/algorithm-selector"
import { ProcessTable } from "@/components/process-table"
import { SimulationResults } from "@/components/simulation-results"
import { GanttChart } from "@/components/gantt-chart"
import { ThemeToggle } from "@/components/theme-toggle"
import { Button } from "@/components/ui/button"
import { Play, Wifi, WifiOff } from "lucide-react"
import { useWebSocket, type WebSocketMessage } from "@/hooks/use-websocket"
import { useToast } from "@/hooks/use-toast"

export type Algorithm =
  | "fcfs"
  | "round-robin"
  | "round-robin-aging"
  | "srtf"
  | "sjf"
  | "priority-preemptive"
  | "priority-non-preemptive"

export interface Process {
  id: string
  arrivalTime: number
  duration: number
  priority: number
  color: string
}

export interface SimulationConfig {
  algorithm: Algorithm
  quantum?: number
  aging?: number
}

export interface SimulationState {
  currentProcess: string | null
  waitQueue: string[]
  iteration: number
}

export interface ProcessMetrics {
  processId: string
  turnaroundTime: number
  waitingTime: number
}

export interface SimulationMetrics {
  averageTurnaroundTime: number
  averageWaitingTime: number
  contextSwitches: number
}

export default function Home() {
  const [algorithm, setAlgorithm] = useState<Algorithm>("fcfs")
  const [quantum, setQuantum] = useState<number>(2)
  const [aging, setAging] = useState<number>(1)
  const [processes, setProcesses] = useState<Process[]>([])
  const [isSimulating, setIsSimulating] = useState(false)
  const [simulationState, setSimulationState] = useState<SimulationState | null>(null)
  const [ganttData, setGanttData] = useState<Array<{ processId: string; start: number; end: number; color: string }>>(
    [],
  )
  const [metrics, setMetrics] = useState<SimulationMetrics | null>(null)
  const { toast } = useToast()

  const handleWebSocketMessage = useCallback(
    (message: WebSocketMessage) => {
      switch (message.type) {
        case "state":
          setSimulationState(message.data)
          break

        case "gantt":
          setGanttData((prev) => [...prev, message.data])
          break

        case "metrics":
          setMetrics(message.data)
          break

        case "complete":
          setIsSimulating(false)
          toast({
            title: "Simulação concluída",
            description: "Os resultados estão disponíveis abaixo.",
          })
          break

        case "error":
          setIsSimulating(false)
          toast({
            title: "Erro na simulação",
            description: message.data.message || "Ocorreu um erro durante a simulação.",
            variant: "destructive",
          })
          break

        default:
          console.warn("[v0] Tipo de mensagem desconhecido:", message.type)
      }
    },
    [toast],
  )

  const { isConnected, isConnecting, connect, disconnect, sendMessage } = useWebSocket({
    url: process.env.NEXT_PUBLIC_WEBSOCKET_URL || "ws://localhost:8000/ws",
    onMessage: handleWebSocketMessage,
    onError: () => {
      toast({
        title: "Erro de conexão",
        description: "Não foi possível conectar ao servidor de simulação.",
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

    setSimulationState(null)
    setGanttData([])
    setMetrics(null)
    setIsSimulating(true)

    const config: SimulationConfig = {
      algorithm,
      ...(algorithm === "round-robin" || algorithm === "round-robin-aging" ? { quantum } : {}),
      ...(algorithm === "round-robin-aging" ? { aging } : {}),
    }

    const payload = {
      type: "start_simulation",
      config,
      processes: processes.map((p) => ({
        id: p.id,
        arrivalTime: p.arrivalTime,
        duration: p.duration,
        priority: p.priority,
        color: p.color,
      })),
    }

    const success = sendMessage(payload)

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

        {simulationState && (
          <div className="rounded-lg border border-border bg-card p-4 animate-in fade-in slide-in-from-bottom-2">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Iteração {simulationState.iteration}</p>
                <p className="font-medium">
                  Processo atual:{" "}
                  <span className="font-mono text-primary">{simulationState.currentProcess || "Nenhum"}</span>
                </p>
              </div>
              <div className="space-y-1 text-right">
                <p className="text-sm text-muted-foreground">Fila de espera</p>
                <p className="font-mono text-sm">
                  {simulationState.waitQueue.length > 0 ? simulationState.waitQueue.join(", ") : "Vazia"}
                </p>
              </div>
            </div>
          </div>
        )}

        {metrics && (
          <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="h-px bg-border" />

            <SimulationResults metrics={metrics} />

            <GanttChart data={ganttData} />
          </div>
        )}
      </div>
    </main>
  )
}
