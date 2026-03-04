#!/bin/bash
# build-install.sh - 代码修改后自动 build + adb install
# 作为 Stop hook 运行：每次 Claude 完成响应后检查是否有代码变更，有则构建安装

PROJECT_DIR="/mnt/d/Personal/WadesLauncher"
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
MARKER="/tmp/.claude-needs-build"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# 检查是否有标记文件（由 PostToolUse hook 创建）
if [ ! -f "$MARKER" ]; then
    exit 0
fi

rm -f "$MARKER"

echo "[Hook] 检测到代码变更，开始构建..."

cd "$PROJECT_DIR" || exit 0

# Build
if ./gradlew :app:assembleDebug --quiet 2>&1; then
    echo "[Hook] ✅ Build 成功"

    # Install
    if command -v adb &> /dev/null; then
        if adb devices | grep -q "device$"; then
            if adb install -r "$APK_PATH" 2>&1; then
                echo "[Hook] ✅ 已安装到设备"
            else
                echo "[Hook] ❌ adb install 失败"
            fi
        else
            echo "[Hook] ⚠️ 未检测到已连接设备"
        fi
    else
        echo "[Hook] ⚠️ adb 未找到"
    fi
else
    echo "[Hook] ❌ Build 失败"
fi
