package com.auraos.launcher.ai

class ConversationMemory(private val maxTurns: Int = 10) {

    private val history = mutableListOf<Pair<String, String>>()

    fun addUserMessage(message: String) {
        history.add("user" to message)
        trimHistory()
    }

    fun addModelResponse(response: String) {
        history.add("model" to response)
        trimHistory()
    }

    fun getHistory(): List<Pair<String, String>> = history.toList()

    fun clear() = history.clear()

    private fun trimHistory() {
        while (history.size > maxTurns * 2) {
            history.removeAt(0)
        }
    }
}
