#!/usr/bin/env bash
# Stop anything listening on dev ports so `npm run dev` can restart cleanly.

free_port() {
	local port=$1
	local pids

	pids=$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)
	if [ -z "$pids" ]; then
		return 0
	fi

	echo "Stopping process on port $port (PID: $(echo "$pids" | tr '\n' ' '))"
	echo "$pids" | xargs kill -TERM 2>/dev/null || true
	sleep 0.5

	pids=$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)
	if [ -n "$pids" ]; then
		echo "$pids" | xargs kill -KILL 2>/dev/null || true
	fi
}

free_port 5173
free_port 8080
