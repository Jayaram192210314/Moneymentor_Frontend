package com.simats.moneymentor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.simats.moneymentor.ui.screens.HomeScreen
import com.simats.moneymentor.ui.screens.LoginScreen
import com.simats.moneymentor.ui.screens.OnboardingScreen
import com.simats.moneymentor.ui.screens.SignUpScreen
import com.simats.moneymentor.ui.screens.ForgotPasswordScreen
import com.simats.moneymentor.ui.screens.VerifyOtpScreen
import com.simats.moneymentor.ui.screens.ResetPasswordScreen
import com.simats.moneymentor.ui.screens.SplashScreen
import com.simats.moneymentor.ui.screens.CreateGoalScreen
import com.simats.moneymentor.ui.screens.EditGoalScreen
import com.simats.moneymentor.ui.screens.GoalDetailsScreen
import com.simats.moneymentor.ui.screens.DailyInsightsScreen
import com.simats.moneymentor.ui.screens.ArticleDetailScreen
import com.simats.moneymentor.ui.screens.QuizScreen
import com.simats.moneymentor.ui.screens.GoldRatesScreen
import com.simats.moneymentor.ui.screens.SilverRatesScreen
import com.simats.moneymentor.data.LearningArticle
import com.simats.moneymentor.data.UserProfile
import com.simats.moneymentor.ui.theme.MoneyMentorTheme
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.simats.moneymentor.data.NotificationWorker
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy

class MainActivity : androidx.activity.ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        


        createNotificationChannel()
        
        enableEdgeToEdge()
        setContent {
            MoneyMentorTheme {
                // Initialize Repositories
                com.simats.moneymentor.data.GoalRepository.init(applicationContext)
                com.simats.moneymentor.data.LearningRepository.init(applicationContext)

                // Use a Surface container using the 'background' color from the theme
                val currentScreen = remember { mutableStateOf("splash") }

                // User Profile State with Persistence
                val context = androidx.compose.ui.platform.LocalContext.current
                val sharedPreferences = context.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)
                
                fun loadProfile(): UserProfile {
                    val fullName = sharedPreferences.getString("full_name", "Alex Johnson") ?: "Alex Johnson"
                    val email = sharedPreferences.getString("email", "alex.johnson@example.com") ?: ""
                    val phone = sharedPreferences.getString("phone", "+91 98765 43210") ?: ""
                    val dob = sharedPreferences.getString("dob", "Jan 15, 1998") ?: ""
                    val userId = sharedPreferences.getInt("user_id", 5)
                    // Sync Repository initially
                    com.simats.moneymentor.data.GoalRepository.currentUserId = userId
                    com.simats.moneymentor.data.LearningRepository.currentUserId = userId
                    return UserProfile(fullName, email, phone, dob)
                }

                fun saveProfile(profile: UserProfile) {
                    sharedPreferences.edit()
                        .putString("full_name", profile.fullName)
                        .putString("email", profile.email)
                        .putString("phone", profile.phone)
                        .putString("dob", profile.dob)
                        .putInt("user_id", com.simats.moneymentor.data.GoalRepository.currentUserId)
                        .apply()
                }

                var userProfile by remember { mutableStateOf(loadProfile()) }
                var currentArticle by remember { mutableStateOf<LearningArticle?>(null) } // State for selected article
                var currentModuleId by remember { mutableStateOf<Int?>(null) } // State for selected module

                val insightRepository = remember { com.simats.moneymentor.data.InsightRepository(com.simats.moneymentor.data.network.RetrofitClient.insightService) }
                val insightViewModel: com.simats.moneymentor.ui.viewmodels.InsightViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.simats.moneymentor.ui.viewmodels.InsightViewModel(insightRepository) as T
                        }
                    }
                )

                val mainAuthRepository = remember { com.simats.moneymentor.data.AuthRepository(com.simats.moneymentor.data.network.RetrofitClient.authService) }
                
                val goldViewModel: com.simats.moneymentor.ui.viewmodels.GoldViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.simats.moneymentor.ui.viewmodels.GoldViewModel(com.simats.moneymentor.data.network.RetrofitClient.goldRateService) as T
                        }
                    }
                )

                val silverViewModel: com.simats.moneymentor.ui.viewmodels.SilverViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.simats.moneymentor.ui.viewmodels.SilverViewModel(com.simats.moneymentor.data.network.RetrofitClient.silverRateService) as T
                        }
                    }
                )

                val notificationRepository = remember { com.simats.moneymentor.data.NotificationRepository(com.simats.moneymentor.data.network.RetrofitClient.notificationService) }
                val notificationPrefs = context.getSharedPreferences("notification_settings", android.content.Context.MODE_PRIVATE)
                val notificationViewModel: com.simats.moneymentor.ui.viewmodels.NotificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return com.simats.moneymentor.ui.viewmodels.NotificationViewModel(notificationRepository, notificationPrefs, applicationContext) as T
                        }
                    }
                )

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.simats.moneymentor.data.NotificationScheduler.scheduleReminders(applicationContext)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                androidx.compose.runtime.LaunchedEffect(com.simats.moneymentor.data.GoalRepository.currentUserId) {
                    val userId = com.simats.moneymentor.data.GoalRepository.currentUserId
                    if (userId > 0) {
                        notificationViewModel.fetchNotifications(userId)
                        val result = mainAuthRepository.getProfile(userId)
                        result.onSuccess { response ->
                             response.user?.let { user ->
                                 val rawDob = user.dob ?: userProfile.dob
                                 val formattedDob = try {
                                     val inFormat = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH)
                                     val outFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                     val date = inFormat.parse(rawDob)
                                     if (date != null) outFormat.format(date) else rawDob
                                 } catch (e: Exception) { rawDob }

                                 val updatedProfile = UserProfile(
                                     fullName = user.fullName ?: userProfile.fullName,
                                     email = user.email ?: userProfile.email,
                                     phone = user.phone ?: userProfile.phone,
                                     dob = formattedDob,
                                     id = userId
                                 )
                                 userProfile = updatedProfile
                                 saveProfile(updatedProfile)
                             }
                        }
                    }
                }

                when (currentScreen.value) {
                    "splash" -> SplashScreen {
                        currentScreen.value = "onboarding"
                    }
                    "onboarding" -> OnboardingScreen {
                        currentScreen.value = "login"
                    }
                    "login" -> {
                        var lastBackPressTime by remember { mutableLongStateOf(0L) }
                        
                        BackHandler {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastBackPressTime < 2000) {
                                (context as? android.app.Activity)?.finish()
                            } else {
                                lastBackPressTime = currentTime
                                android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        
                        val authRepository = remember { com.simats.moneymentor.data.AuthRepository(com.simats.moneymentor.data.network.RetrofitClient.authService) }
                        val authViewModel: com.simats.moneymentor.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.simats.moneymentor.ui.viewmodels.AuthViewModel(authRepository) as T
                                }
                            }
                        )

                        LoginScreen(
                            viewModel = authViewModel,
                            onSignInSuccess = { 
                                authViewModel.loginResponse?.user?.id?.let { id ->
                                    com.simats.moneymentor.data.GoalRepository.currentUserId = id
                                    com.simats.moneymentor.data.LearningRepository.currentUserId = id
                                    sharedPreferences.edit().putInt("user_id", id).apply()
                                }
                                currentScreen.value = "home" 
                            },
                            onSignUpClick = { currentScreen.value = "signup" },
                            onForgotPasswordClick = { currentScreen.value = "forgot_password" }
                        )
                    }
                    "signup" -> {
                        BackHandler { currentScreen.value = "login" }
                        val authRepository = remember { com.simats.moneymentor.data.AuthRepository(com.simats.moneymentor.data.network.RetrofitClient.authService) }
                        val authViewModel: com.simats.moneymentor.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.simats.moneymentor.ui.viewmodels.AuthViewModel(authRepository) as T
                                }
                            }
                        )

                        SignUpScreen(
                            viewModel = authViewModel,
                            onSignInClick = { currentScreen.value = "login" },
                            onBackClick = { currentScreen.value = "login" },
                            onSignUpSuccess = { currentScreen.value = "login" }
                        )
                    }
                    "forgot_password" -> {
                        BackHandler { currentScreen.value = "login" }
                        val authRepository = remember { com.simats.moneymentor.data.AuthRepository(com.simats.moneymentor.data.network.RetrofitClient.authService) }
                        val authViewModel: com.simats.moneymentor.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.simats.moneymentor.ui.viewmodels.AuthViewModel(authRepository) as T
                                }
                            }
                        )

                        ForgotPasswordScreen(
                            viewModel = authViewModel,
                            onSendOtpSuccess = { currentScreen.value = "verify_otp" },
                            onBackClick = { currentScreen.value = "login" }
                        )
                    }
                    "verify_otp" -> {
                        BackHandler { currentScreen.value = "forgot_password" }
                        val authRepository = remember { com.simats.moneymentor.data.AuthRepository(com.simats.moneymentor.data.network.RetrofitClient.authService) }
                        val authViewModel: com.simats.moneymentor.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.simats.moneymentor.ui.viewmodels.AuthViewModel(authRepository) as T
                                }
                            }
                        )

                        VerifyOtpScreen(
                            viewModel = authViewModel,
                            onVerifySuccess = { currentScreen.value = "reset_password" },
                            onBackClick = { currentScreen.value = "forgot_password" }
                        )
                    }
                    "reset_password" -> {
                        BackHandler { currentScreen.value = "verify_otp" }
                        val authRepository = remember { com.simats.moneymentor.data.AuthRepository(com.simats.moneymentor.data.network.RetrofitClient.authService) }
                        val authViewModel: com.simats.moneymentor.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.simats.moneymentor.ui.viewmodels.AuthViewModel(authRepository) as T
                                }
                            }
                        )

                        ResetPasswordScreen(
                            viewModel = authViewModel,
                            onResetSuccess = { currentScreen.value = "login" },
                            onBackClick = { currentScreen.value = "verify_otp" }
                        )
                    }
                    "create_goal" -> {
                        BackHandler { currentScreen.value = "home_goals" }
                        CreateGoalScreen(
                            onBackClick = { currentScreen.value = "home_goals" },
                            onGoalCreated = { currentScreen.value = "home_goals" }
                        )
                    }
                    "edit_goal" -> {
                        BackHandler { currentScreen.value = "home_goals" }
                        EditGoalScreen(
                            onBackClick = { currentScreen.value = "home_goals" },
                            onGoalUpdated = { currentScreen.value = "home_goals" }
                        )
                    }
                    "goal_details" -> {
                        BackHandler { currentScreen.value = "home_goals" }
                        GoalDetailsScreen(
                            onBackClick = { currentScreen.value = "home_goals" },
                            onEditGoalClick = { currentScreen.value = "edit_goal" }
                        )
                    }
                    "daily_insights" -> {
                        BackHandler { currentScreen.value = "home" }
                        DailyInsightsScreen(
                            viewModel = insightViewModel,
                            onBackClick = { currentScreen.value = "home" }
                        )
                    }
                    "profile" -> {
                        BackHandler { currentScreen.value = "home" }
                        com.simats.moneymentor.ui.screens.ProfileScreen(
                            userProfile = userProfile,
                            notificationViewModel = notificationViewModel,
                            onSaveProfile = { newProfile -> 
                                userProfile = newProfile
                                saveProfile(newProfile)
                                
                                // Call API to update backend
                                val userId = com.simats.moneymentor.data.GoalRepository.currentUserId
                                if (userId > 0) {
                                    val safeDob = newProfile.dob.ifEmpty { "2000-01-01" } // fallback
                                    val formattedDob = try {
                                        val inFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                        val outFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
                                        val date = inFormat.parse(safeDob)
                                        if (date != null) outFormat.format(date) else safeDob
                                    } catch (e: Exception) { safeDob }

                                    val request = com.simats.moneymentor.data.model.UpdateProfileRequest(
                                        fullName = newProfile.fullName,
                                        email = newProfile.email,
                                        mobile = newProfile.phone,
                                        dob = formattedDob
                                    )
                                    // Launch in a new coroutine scope since we are in a composable callback
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                        mainAuthRepository.updateProfile(userId, request)
                                    }
                                }
                            },
                            onBackClick = { currentScreen.value = "home" },
                            onLogOutClick = { currentScreen.value = "login" }
                        )
                    }
                    "quiz" -> {
                        if (currentModuleId != null) {
                            BackHandler { currentScreen.value = "home_learn" }
                            QuizScreen(
                                moduleId = currentModuleId!!,
                                onBack = { currentScreen.value = "home_learn" }
                            )
                        }
                    }
                     "article_detail" -> {
                         BackHandler { currentScreen.value = "home_learn" }
                         if (currentArticle != null && currentModuleId != null) {
                             // Use Repository
                             val currentModule = com.simats.moneymentor.data.LearningRepository.getModule(currentModuleId!!)
                             
                              if (currentModule != null) {
                                  // Filter out Quiz for navigation
                                  val articles = currentModule.articles.filter { it.type != com.simats.moneymentor.data.ArticleType.Quiz }
                                  val currentIndex = articles.indexOfFirst { it.id == currentArticle!!.id }
                                  // Re-fetch current article from repository to get latest status (isCompleted)
                                  val safeCurrentArticle = articles.find { it.id == currentArticle!!.id } ?: currentArticle!!
                                  
                                  ArticleDetailScreen(
                                      article = safeCurrentArticle,
                                      moduleName = currentModule.title,
                                      moduleId = currentModule.id,
                                      moduleColor = currentModule.color,
                                      currentIndex = currentIndex,
                                      totalArticles = articles.size,
                                      onBackClick = { currentScreen.value = "home_learn" },
                                      onNextClick = { 
                                          if (currentIndex < articles.size - 1) {
                                              currentArticle = articles[currentIndex + 1]
                                          } else {
                                              currentScreen.value = "home_learn"
                                          }
                                      },
                                      onPreviousClick = {
                                          if (currentIndex > 0) {
                                              currentArticle = articles[currentIndex - 1]
                                          }
                                      },
                                      onMarkAsReadClick = {
                                          com.simats.moneymentor.data.LearningRepository.markArticleAsCompleted(currentModule.id, safeCurrentArticle.id)
                                          // Update currentArticle state to trigger recomposition if needed
                                          currentArticle = com.simats.moneymentor.data.LearningRepository.getArticle(currentModule.id, safeCurrentArticle.id)
                                      }
                                  )
                             } else {
                                 // Fallback if module not found
                                 currentScreen.value = "home_learn"
                             }
                         } else {
                             currentScreen.value = "home_learn"
                         }
                    }
                    "gold_rates" -> {
                        BackHandler { currentScreen.value = "home" }
                        GoldRatesScreen(
                            viewModel = goldViewModel,
                            onBack = { currentScreen.value = "home" }
                        )
                    }
                    "silver_rates" -> {
                        BackHandler { currentScreen.value = "home" }
                        SilverRatesScreen(
                            viewModel = silverViewModel,
                            onBack = { currentScreen.value = "home" }
                        )
                    }
                    else -> {
                        var lastBackPressTime by remember { mutableLongStateOf(0L) }
                        
                        BackHandler {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastBackPressTime < 2000) {
                                (context as? android.app.Activity)?.finish()
                            } else {
                                lastBackPressTime = currentTime
                                android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        
                        HomeScreen(
                             userName = userProfile.fullName,
                             insightViewModel = insightViewModel,
                             silverViewModel = silverViewModel,
                             initialTab = when (currentScreen.value) {
                                 "home_goals" -> "Goals"
                                 "home_tools" -> "Tools"
                                 "home_chat" -> "Chat"
                                 "home_learn" -> "Learn"
                                 else -> "Home"
                             },
                             onCreateGoalClick = { currentScreen.value = "create_goal" },
                             onEditGoalClick = { currentScreen.value = "edit_goal" },
                             onGoalClick = { currentScreen.value = "goal_details" },
                             onDailyTipClick = { currentScreen.value = "daily_insights" },
                             onViewAllGoalsClick = { currentScreen.value = "home_goals" }, // Switch to Goals tab
                             onProfileClick = { currentScreen.value = "profile" },
                             onQuickActionClick = { action ->
                                 when (action) {
                                     "Goals" -> currentScreen.value = "home_goals"
                                     "Tools" -> currentScreen.value = "home_tools"
                                     "AI" -> currentScreen.value = "home_chat"
                                     "Learn" -> currentScreen.value = "home_learn"
                                 }
                             },
                             onArticleClick = { moduleId, article ->
                                 currentModuleId = moduleId
                                 currentArticle = article
                                 currentScreen.value = "article_detail"
                             },
                             onQuizClick = { moduleId ->
                                 currentModuleId = moduleId
                                 currentScreen.value = "quiz"
                             },
                             onGoldCardClick = { currentScreen.value = "gold_rates" },
                             onSilverCardClick = { currentScreen.value = "silver_rates" },
                             notificationViewModel = notificationViewModel
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Money Mentor Notifications"
            val descriptionText = "Notifications for Daily Tips and Savings Updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("money_mentor_notifications", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MoneyMentorTheme {
        SplashScreen {}
    }
}
