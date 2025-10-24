'use client';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Plus, Trash2 } from 'lucide-react';
import type { Process } from '@/lib/types';

interface ProcessTableProps {
  processes: Process[];
  onProcessesChange: (processes: Process[]) => void;
}

const generateColor = () => {
  const colors = [
    '#8b5cf6', // purple
    '#06b6d4', // cyan
    '#10b981', // green
    '#f59e0b', // amber
    '#ef4444', // red
    '#ec4899', // pink
    '#6366f1', // indigo
    '#14b8a6', // teal
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

export function ProcessTable({ processes, onProcessesChange }: ProcessTableProps) {
  const addProcess = () => {
    const nextId = processes.length > 0 ? Math.max(...processes.map(p => Number(p.id))) + 1 : 0;

    const newProcess: Process = {
      id: String(nextId),
      arrivalTime: 0,
      duration: 1,
      priority: 1,
      color: generateColor(),
    };
    onProcessesChange([...processes, newProcess]);
  };

  const removeProcess = (id: string) => {
    onProcessesChange(processes.filter(p => p.id !== id));
  };

  const updateProcess = (id: string, field: keyof Process, value: number) => {
    onProcessesChange(processes.map(p => (p.id === id ? { ...p, [field]: value } : p)));
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Processos</CardTitle>
        <CardDescription>Adicione e configure os processos para simulação</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="rounded-lg border border-border overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border bg-muted/50">
                  <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Processo</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Instante de Criação</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Duração</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Prioridade</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Ações</th>
                </tr>
              </thead>
              <tbody>
                {processes.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-8 text-center text-muted-foreground">
                      Nenhum processo adicionado. Clique em "Adicionar Processo" para começar.
                    </td>
                  </tr>
                ) : (
                  processes.map(process => (
                    <tr
                      key={process.id}
                      className="border-b border-border last:border-0 hover:bg-accent/50 transition-colors">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <div className="h-3 w-3 rounded-full" style={{ backgroundColor: process.color }} />
                          <span className="font-mono font-medium">P{process.id}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <Input
                          type="number"
                          min="0"
                          value={process.arrivalTime}
                          onChange={e => updateProcess(process.id, 'arrivalTime', Number(e.target.value))}
                          className="w-24 h-8 font-mono"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <Input
                          type="number"
                          min="1"
                          value={process.duration}
                          onChange={e => updateProcess(process.id, 'duration', Number(e.target.value))}
                          className="w-24 h-8 font-mono"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <Input
                          type="number"
                          min="1"
                          value={process.priority}
                          onChange={e => updateProcess(process.id, 'priority', Number(e.target.value))}
                          className="w-24 h-8 font-mono"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => removeProcess(process.id)}
                          className="h-8 w-8 text-muted-foreground hover:text-destructive">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <Button onClick={addProcess} variant="outline" className="w-full bg-transparent">
          <Plus className="mr-2 h-4 w-4" />
          Adicionar Processo
        </Button>
      </CardContent>
    </Card>
  );
}
