package com.example.expensetracker.model

data class Group(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val archivedAt: Long? = null
)

data class GroupSummary(
    val group: Group,
    val memberCount: Int
)

data class GroupWithMembers(
    val group: Group,
    val members: List<Person>
)
