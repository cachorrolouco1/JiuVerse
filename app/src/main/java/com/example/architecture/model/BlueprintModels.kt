package com.example.architecture.model

data class PrismaField(
    val name: String,
    val type: String,
    val isId: Boolean = false,
    val isRelation: Boolean = false,
    val description: String
)

data class PrismaModel(
    val name: String,
    val fields: List<PrismaField>,
    val relations: List<String>,
    val rawCode: String,
    val purpose: String
)

data class FolderItem(
    val name: String,
    val description: String,
    val isFile: Boolean,
    val children: List<FolderItem> = emptyList(),
    val sampleCode: String = ""
)

data class RoadmapPhase(
    val id: String,
    val title: String,
    val subtitle: String,
    val duration: String,
    val team: String,
    val tasks: List<String>,
    val deliverables: List<String>,
    val risks: List<String>
)

data class PlaybookIncident(
    val id: String,
    val title: String,
    val attackType: String,
    val mitigationTech: String,
    val description: String,
    val simulationLogs: List<String>
)

data class ChatMessage(
    val sender: String, // "user", "ai"
    val content: String,
    val timestamp: String
)
