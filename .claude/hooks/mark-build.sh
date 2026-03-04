#!/bin/bash
# mark-build.sh - PostToolUse hook for Edit/Write
# 当编辑了 WadesLauncher 下的文件时，标记需要构建

# 从 stdin 读取 hook input JSON
INPUT=$(cat)

# 提取文件路径
FILE_PATH=$(echo "$INPUT" | node -e "
let d='';
process.stdin.on('data',c=>d+=c);
process.stdin.on('end',()=>{
  try {
    const i=JSON.parse(d);
    console.log(i.tool_input?.file_path || i.tool_input?.path || '');
  } catch { console.log(''); }
});
" 2>/dev/null)

# 如果编辑的是 WadesLauncher 下的源码文件，创建标记
if echo "$FILE_PATH" | grep -q "WadesLauncher/.*\.\(kt\|xml\|kts\|java\)$"; then
    touch /tmp/.claude-needs-build
fi

# 必须原样输出 input（不阻塞工具执行）
echo "$INPUT"
