package me.edrakai.core.database

import androidx.room.TypeConverter
import me.edrakai.core.database.entity.ActionType
import me.edrakai.core.database.entity.SyncStatus

class EdrakTypeConverters {

    @TypeConverter
    fun syncStatusToString(status: SyncStatus): String = status.name

    @TypeConverter
    fun stringToSyncStatus(value: String): SyncStatus =
        SyncStatus.valueOf(value)

    @TypeConverter
    fun actionTypeToString(type: ActionType): String = type.name

    @TypeConverter
    fun stringToActionType(value: String): ActionType =
        ActionType.valueOf(value)
}
