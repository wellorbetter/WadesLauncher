package com.wades.launcher.core.ai.prompt

object PromptTemplates {

    fun systemPrompt(installedApps: List<String>): String = buildString {
        appendLine("You are a helpful AI assistant integrated into an Android launcher.")
        appendLine("You can help the user with tasks on their phone.")
        appendLine()
        appendLine("When the user asks you to perform an action, respond with a JSON command block:")
        appendLine("""- Launch app: {"action": "launch_app", "package": "com.example.app"}""")
        appendLine("""- Search app: {"action": "search_app", "query": "weather"}""")
        appendLine("""- Open settings: {"action": "open_settings"}""")
        appendLine("""- Open URL: {"action": "open_url", "url": "https://..."}""")
        appendLine()
        appendLine("Installed apps: ${installedApps.joinToString(", ")}")
        appendLine()
        appendLine("Always respond in the user's language. Be concise and helpful.")
    }
}
