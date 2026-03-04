#!/bin/bash
# session-save.sh - 会话记忆持久化钩子
# 在会话结束时自动归档重要发现到持久化记忆目录

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
MEMORY_DIR="$HOME/.claude/projects/-mnt-d-Personal/memory"
SESSION_NOTES="$PROJECT_ROOT/.claude/session-notes.md"

mkdir -p "$MEMORY_DIR"

if [ -f "$SESSION_NOTES" ] && [ -s "$SESSION_NOTES" ]; then
    TIMESTAMP=$(date +%Y%m%d-%H%M%S)
    if [ -f "$MEMORY_DIR/MEMORY.md" ]; then
        echo "" >> "$MEMORY_DIR/MEMORY.md"
        echo "## Session $TIMESTAMP" >> "$MEMORY_DIR/MEMORY.md"
        cat "$SESSION_NOTES" >> "$MEMORY_DIR/MEMORY.md"
    else
        echo "# Project Memory" > "$MEMORY_DIR/MEMORY.md"
        echo "" >> "$MEMORY_DIR/MEMORY.md"
        echo "## Session $TIMESTAMP" >> "$MEMORY_DIR/MEMORY.md"
        cat "$SESSION_NOTES" >> "$MEMORY_DIR/MEMORY.md"
    fi
    cp "$SESSION_NOTES" "$MEMORY_DIR/session-$TIMESTAMP.md"
    > "$SESSION_NOTES"
    echo "Session notes archived to $MEMORY_DIR"
fi
