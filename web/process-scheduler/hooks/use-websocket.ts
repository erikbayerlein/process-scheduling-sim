"use client"

import { useEffect, useRef, useState, useCallback } from "react"

export interface WebSocketMessage {
  type: "state" | "gantt" | "metrics" | "error" | "complete"
  data: any
}

interface UseWebSocketOptions {
  url: string
  onMessage?: (message: WebSocketMessage) => void
  onError?: (error: Event) => void
  onOpen?: () => void
  onClose?: () => void
}

export function useWebSocket({ url, onMessage, onError, onOpen, onClose }: UseWebSocketOptions) {
  const [isConnected, setIsConnected] = useState(false)
  const [isConnecting, setIsConnecting] = useState(false)
  const wsRef = useRef<WebSocket | null>(null)
  const reconnectTimeoutRef = useRef<NodeJS.Timeout>()
  const reconnectAttemptsRef = useRef(0)
  const maxReconnectAttempts = 5

  const connect = useCallback(() => {
    if (wsRef.current?.readyState === WebSocket.OPEN || wsRef.current?.readyState === WebSocket.CONNECTING) {
      return
    }

    setIsConnecting(true)

    try {
      const ws = new WebSocket(url)

      ws.onopen = () => {
        console.log("[v0] WebSocket conectado")
        setIsConnected(true)
        setIsConnecting(false)
        reconnectAttemptsRef.current = 0
        onOpen?.()
      }

      ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data)
          console.log("[v0] Mensagem recebida:", message)
          onMessage?.(message)
        } catch (error) {
          console.error("[v0] Erro ao parsear mensagem:", error)
        }
      }

      ws.onerror = (error) => {
        console.error("[v0] Erro no WebSocket:", error)
        onError?.(error)
      }

      ws.onclose = () => {
        console.log("[v0] WebSocket desconectado")
        setIsConnected(false)
        setIsConnecting(false)
        wsRef.current = null
        onClose?.()

        // Tentar reconectar automaticamente
        if (reconnectAttemptsRef.current < maxReconnectAttempts) {
          reconnectAttemptsRef.current++
          const delay = Math.min(1000 * Math.pow(2, reconnectAttemptsRef.current), 10000)
          console.log(`[v0] Tentando reconectar em ${delay}ms (tentativa ${reconnectAttemptsRef.current})`)

          reconnectTimeoutRef.current = setTimeout(() => {
            connect()
          }, delay)
        } else {
          console.error("[v0] Número máximo de tentativas de reconexão atingido")
        }
      }

      wsRef.current = ws
    } catch (error) {
      console.error("[v0] Erro ao criar WebSocket:", error)
      setIsConnecting(false)
    }
  }, [url, onMessage, onError, onOpen, onClose])

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
    }

    if (wsRef.current) {
      wsRef.current.close()
      wsRef.current = null
    }

    setIsConnected(false)
    setIsConnecting(false)
    reconnectAttemptsRef.current = 0
  }, [])

  const sendMessage = useCallback((message: any) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      const messageStr = typeof message === "string" ? message : JSON.stringify(message)
      console.log("[v0] Enviando mensagem:", message)
      wsRef.current.send(messageStr)
      return true
    } else {
      console.warn("[v0] WebSocket não está conectado. Mensagem não enviada.")
      return false
    }
  }, [])

  useEffect(() => {
    return () => {
      disconnect()
    }
  }, [disconnect])

  return {
    isConnected,
    isConnecting,
    connect,
    disconnect,
    sendMessage,
  }
}
