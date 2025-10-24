'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Clock, Timer, ArrowLeftRight } from 'lucide-react';
import type { SimulationMetrics } from '@/app/page';

interface SimulationResultsProps {
  metrics: SimulationMetrics;
}

export function SimulationResults({ metrics }: SimulationResultsProps) {
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Resultados da Simulação</h2>
        <p className="text-muted-foreground">Métricas de desempenho do escalonamento</p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Turnaround Time Médio</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold font-mono">{metrics.averageTurnaroundTime.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground mt-1">unidades de tempo</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Tempo Médio de Espera</CardTitle>
            <Timer className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold font-mono">{metrics.averageWaitingTime.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground mt-1">unidades de tempo</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Trocas de Contexto</CardTitle>
            <ArrowLeftRight className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold font-mono">{metrics.contextSwitches}</div>
            <p className="text-xs text-muted-foreground mt-1">trocas realizadas</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
