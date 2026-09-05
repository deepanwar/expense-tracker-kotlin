package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.entity.GroupEntity
import com.example.expensetracker.data.local.entity.GroupWithMemberCount
import com.example.expensetracker.data.local.entity.GroupWithMemberEntities
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.GroupWithMembers

internal fun GroupEntity.toDomain(): Group {
    return Group(
        id = id,
        name = name,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt
    )
}

internal fun Group.toEntity(): GroupEntity {
    return GroupEntity(
        id = id,
        name = name,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt
    )
}

internal fun GroupWithMemberCount.toSummary(): GroupSummary {
    return GroupSummary(
        group = group.toDomain(),
        memberCount = memberCount
    )
}

internal fun GroupWithMemberEntities.toDomain(): GroupWithMembers {
    return GroupWithMembers(
        group = group.toDomain(),
        members = members.map { it.toDomain() }
    )
}
