package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source
private var shouldReturnError = false


    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Something happen while getting data")
        }
        reminders?.let { return Result.Success(it) }
        return Result.Error("Reminders not found")

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders?.find { it.id == id }
        return if (shouldReturnError) {
            Result.Error("Reminder not found!")
        } else if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    override suspend fun deleteReminder(id: String) {
        val reminder = reminders?.find { it.id == id }
        reminders?.remove(reminder)
    }


}