package com.skyd.anivu.model.bean

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A [group] contains many [feeds].
 */
data class GroupWithFeedBean(
    @Embedded
    var group: GroupBean,
    @Relation(
        parentColumn = GroupBean.GROUP_ID_COLUMN,
        entityColumn = FeedBean.GROUP_ID_COLUMN,
    )
    var feeds: List<FeedViewBean>,
)
