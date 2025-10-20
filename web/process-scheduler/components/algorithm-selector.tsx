"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import type { Algorithm } from "@/app/page"

interface AlgorithmSelectorProps {
  algorithm: Algorithm
  quantum: number
  aging: number
  onAlgorithmChange: (algorithm: Algorithm) => void
  onQuantumChange: (quantum: number) => void
  onAgingChange: (aging: number) => void
}

const algorithms = [
  { value: "fcfs", label: "First Come, First Served (FCFS)" },
  { value: "round-robin", label: "Round-Robin" },
  { value: "round-robin-aging", label: "Round-Robin + Aging" },
  { value: "srtf", label: "Shortest Remaining Time First (SRTF)" },
  { value: "sjf", label: "Shortest Job First (SJF)" },
  { value: "priority-preemptive", label: "Priority Preemptive" },
  { value: "priority-non-preemptive", label: "Priority Non Preemptive" },
] as const

const preemptiveAlgorithms = ["round-robin", "round-robin-aging", "srtf", "priority-preemptive"]

export function AlgorithmSelector({
  algorithm,
  quantum,
  aging,
  onAlgorithmChange,
  onQuantumChange,
  onAgingChange,
}: AlgorithmSelectorProps) {
  const isPreemptive = preemptiveAlgorithms.includes(algorithm)
  const hasAging = algorithm === "round-robin-aging"

  return (
    <Card>
      <CardHeader>
        <CardTitle>Configuração do Algoritmo</CardTitle>
        <CardDescription>Selecione o algoritmo de escalonamento e seus parâmetros</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-2">
          <Label htmlFor="algorithm">Algoritmo</Label>
          <Select value={algorithm} onValueChange={(value) => onAlgorithmChange(value as Algorithm)}>
            <SelectTrigger id="algorithm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {algorithms.map((algo) => (
                <SelectItem key={algo.value} value={algo.value}>
                  {algo.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {isPreemptive && (
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="quantum">Quantum</Label>
              <Input
                id="quantum"
                type="number"
                min="1"
                value={quantum}
                onChange={(e) => onQuantumChange(Number(e.target.value))}
                className="font-mono"
              />
            </div>

            {hasAging && (
              <div className="space-y-2">
                <Label htmlFor="aging">Aging</Label>
                <Input
                  id="aging"
                  type="number"
                  min="1"
                  value={aging}
                  onChange={(e) => onAgingChange(Number(e.target.value))}
                  className="font-mono"
                />
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
