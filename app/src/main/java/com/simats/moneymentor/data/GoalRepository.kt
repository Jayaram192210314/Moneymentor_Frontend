package com.simats.moneymentor.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import android.util.Log
import com.simats.moneymentor.data.network.RetrofitClient
import com.simats.moneymentor.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class GoalStatus {
    OnTrack,
    Attention,
    Done,
    Failed
}

data class Goal(
    val id: Int,
    var name: String,
    var currentAmount: Double,
    var targetAmount: Double,
    var dueDate: String,
    var color: Color,
    var status: GoalStatus = GoalStatus.OnTrack
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
        
    val formattedCurrentAmount: String
        get() = formatAmount(currentAmount)
        
    val formattedTargetAmount: String
        get() = formatAmount(targetAmount)

    val formattedDueDate: String
        get() = try {
            if (dueDate.isBlank()) {
                "No deadline"
            } else if (dueDate.contains(" 00:00:00")) {
                dueDate.substringBefore(" 00:00:00")
            } else if (dueDate.contains(":")) {
                val unwanted = listOf("GMT", "UTC", "Z")
                dueDate.split(" ")
                    .filter { word -> !word.contains(":") && unwanted.none { it.equals(word, ignoreCase = true) } }
                    .joinToString(" ")
                    .trim()
            } else {
                dueDate
            }
        } catch (e: Exception) {
            dueDate
        }

    val isOverdue: Boolean
        get() {
            if (status == GoalStatus.Done || dueDate.isBlank() || dueDate == "No deadline") return false
            return try {
                val formats = listOf(
                    "EEE, dd MMM yyyy HH:mm:ss zzz",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd",
                    "dd MMM yyyy"
                )
                val parsedDate = formats.firstNotNullOfOrNull { formatStr ->
                    try { java.text.SimpleDateFormat(formatStr, java.util.Locale.ENGLISH).parse(dueDate.trim()) } 
                    catch(e: Exception) { null }
                }
                
                if (parsedDate != null) {
                    val current = java.util.Calendar.getInstance()
                    
                    // Clear time components for pure date comparison
                    current.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    current.set(java.util.Calendar.MINUTE, 0)
                    current.set(java.util.Calendar.SECOND, 0)
                    current.set(java.util.Calendar.MILLISECOND, 0)
                    
                    val goalDate = java.util.Calendar.getInstance()
                    goalDate.time = parsedDate
                    goalDate.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    goalDate.set(java.util.Calendar.MINUTE, 0)
                    goalDate.set(java.util.Calendar.SECOND, 0)
                    goalDate.set(java.util.Calendar.MILLISECOND, 0)
                    
                    // Failed if current date is strictly after deadline date
                    current.after(goalDate)
                } else false
            } catch (e: Exception) {
                false
            }
        }

    private fun formatAmount(amount: Double): String {
        return when {
            amount >= 100000 -> "₹%.2fL".format(amount / 100000).replace(".00", "")
            amount >= 1000 -> "₹%.0fk".format(amount / 1000)
            else -> "₹%.0f".format(amount)
        }
    }
}

object GoalRepository {
    var currentGoalIndex: Int = 0
    var currentUserId: Int = 5

    // Observable list of goals (Persistent for session)
    val goals = mutableStateListOf<Goal>()

    suspend fun fetchGoals(userId: Int): Result<List<Goal>> {
        return try {
            val response = RetrofitClient.goalService.getGoals(userId)
            if (response.status == "success") {
                val cardColors = listOf(
                    Color(0xFFE91E63), // Pink
                    Color(0xFF2196F3), // Blue
                    Color(0xFF009688), // Teal
                    Color(0xFFFFB300), // Amber
                    Color(0xFF9C27B0)  // Purple
                )
                val mappedGoals = response.goals.mapIndexed { index, item ->
                    val tempGoal = Goal(
                        id = item.id,
                        name = item.goalName,
                        currentAmount = item.alreadySavedAmount,
                        targetAmount = item.targetAmount,
                        dueDate = item.deadline,
                        color = cardColors[index % cardColors.size]
                    )
                    
                    tempGoal.status = when {
                        tempGoal.currentAmount >= tempGoal.targetAmount -> GoalStatus.Done
                        tempGoal.isOverdue -> GoalStatus.Failed
                        tempGoal.progress < 0.2f -> GoalStatus.Attention
                        else -> GoalStatus.OnTrack
                    }
                    tempGoal
                }
                goals.clear()
                goals.addAll(mappedGoals)
                Result.success(mappedGoals)
            } else {
                Result.failure(Exception("Failed to fetch goals: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error fetching goals", e)
            Result.failure(e)
        }
    }

    suspend fun fetchGoalById(goalId: Int): Result<Goal> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.goalService.getGoalById(goalId)
            if (response.status == "success") {
                val item = response.goal
                val fetchedGoal = Goal(
                    id = goalId, // Use the requested ID, since backend might not return it
                    name = item.goalName,
                    currentAmount = item.alreadySavedAmount,
                    targetAmount = item.targetAmount,
                    dueDate = item.deadline,
                    color = Color(0xFFE91E63)
                )
                fetchedGoal.status = when {
                    fetchedGoal.currentAmount >= fetchedGoal.targetAmount -> GoalStatus.Done
                    fetchedGoal.isOverdue -> GoalStatus.Failed
                    fetchedGoal.progress < 0.2f -> GoalStatus.Attention
                    else -> GoalStatus.OnTrack
                }
                
                // Optional: Update it in the local list if it exists
                val index = goals.indexOfFirst { it.id == goalId }
                if (index != -1) {
                    goals[index] = fetchedGoal
                }
                
                Result.success(fetchedGoal)
            } else {
                Result.failure(Exception("Failed to fetch goal"))
            }
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error fetching individual goal", e)
            Result.failure(e)
        }
    }

    suspend fun createGoal(request: AddGoalRequest): Result<GenericResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.goalService.addGoal(request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error creating goal", e)
            Result.failure(e)
        }
    }

    suspend fun updateExtra(goalId: Int, extraAmount: Double): Result<UpdateExtraResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateExtraRequest(extraAmount)
            val response = RetrofitClient.goalService.updateExtraAmount(goalId, request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error updating extra", e)
            Result.failure(e)
        }
    }

    fun init(context: android.content.Context) {
        val prefs = context.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", 5)
    }

    fun getSelectedGoal(): Goal {
        if (currentGoalIndex in goals.indices) {
            return goals[currentGoalIndex]
        }
        return goals.firstOrNull() ?: Goal(id = 0, name = "New Goal", currentAmount = 0.0, targetAmount = 1000.0, dueDate = "Dec 2025", color = Color.Gray)
    }

    fun addGoal(goal: Goal) {
        val newId = (goals.maxByOrNull { it.id }?.id ?: 0) + 1
        goals.add(goal.copy(id = newId))
    }

    suspend fun updateGoal(goalId: Int, name: String?, target: Double?, deadline: String?, current: Double?): Result<com.simats.moneymentor.data.model.GenericResponse> = withContext(Dispatchers.IO) {
        val index = goals.indexOfFirst { it.id == goalId }
        if (index != -1) {
            val goal = goals[index]
            val actualName = name ?: goal.name
            val actualTarget = target ?: goal.targetAmount
            val actualCurrent = current ?: goal.currentAmount
            val actualDeadline = deadline ?: goal.dueDate ?: ""

            try {
                val request = com.simats.moneymentor.data.model.UpdateGoalRequest(
                    goalName = actualName,
                    targetAmount = actualTarget,
                    alreadySavedAmount = actualCurrent,
                    deadline = actualDeadline
                )
                val response = RetrofitClient.goalService.updateGoalDetails(goalId, request)
                if (response.status == "success") {
                    // Update status locally
                    goal.name = actualName
                    goal.targetAmount = actualTarget
                    goal.dueDate = actualDeadline
                    goal.currentAmount = actualCurrent
                    
                    val newStatus = when {
                        actualCurrent >= actualTarget -> GoalStatus.Done 
                        goal.isOverdue -> GoalStatus.Failed
                        (actualTarget > 0 && (actualCurrent / actualTarget) < 0.2) -> GoalStatus.Attention
                        else -> GoalStatus.OnTrack
                    }
                    
                    // Create a new copy to trigger Compose state refresh
                    goals[index] = goal.copy(
                        name = actualName,
                        targetAmount = actualTarget,
                        dueDate = actualDeadline,
                        currentAmount = actualCurrent,
                        status = newStatus
                    )
                    
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    if (errorBody != null) org.json.JSONObject(errorBody).getString("message") else "HTTP ${e.code()}"
                } catch (ex: Exception) {
                    "HTTP Error"
                }
                Log.e("GoalRepository", "HTTP Error updating goal: $errorBody", e)
                Result.failure(Exception(message))
            } catch (e: Exception) {
                Log.e("GoalRepository", "Error updating goal", e)
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Goal not found locally"))
        }
    }


    suspend fun withdrawMoney(goalId: Int, amount: Double): Result<com.simats.moneymentor.data.model.WithdrawResponse> = withContext(Dispatchers.IO) {
        try {
            val request = com.simats.moneymentor.data.model.WithdrawRequest(amount)
            val response = RetrofitClient.goalService.withdrawAmount(goalId, request)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error withdrawing money", e)
            Result.failure(e)
        }
    }

    suspend fun deleteGoal(goalId: Int): Result<com.simats.moneymentor.data.model.GenericResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.goalService.deleteGoal(goalId)
            if (response.status == "success") {
                // Remove from local list immediately
                goals.removeAll { it.id == goalId }
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error deleting goal", e)
            Result.failure(e)
        }
    }
}
