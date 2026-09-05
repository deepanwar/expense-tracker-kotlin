package com.example.expensetracker.data.local.entity

import androidx.room.Embedded

data class GroupWithMemberCount(
    @Embedded val group: GroupEntity,
    val memberCount: Int
)
