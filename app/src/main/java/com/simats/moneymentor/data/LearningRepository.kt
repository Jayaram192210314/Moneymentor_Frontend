package com.simats.moneymentor.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import kotlinx.coroutines.*

// --- Data Classes & Enums ---

data class LearningModule(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val progress: Float,
    val status: ModuleStatus,
    val color: Color,
    val level: LearningLevel,
    val articles: List<LearningArticle> = emptyList()
)

data class LearningArticle(
    val id: Int,
    val title: String,
    val type: ArticleType,
    val duration: String,
    val isCompleted: Boolean = false,
    val content: String = "",
    val date: String = "Oct 24, 2023" // Keep for data consistency, even if not displayed
)

data class Quiz(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

enum class ArticleType {
    Article, Video, Quiz
}

enum class ModuleStatus {
    Locked, InProgress, Done
}

enum class LearningLevel(val displayName: String, val icon: ImageVector, val color: Color) {
    Beginner("Level 1: Beginner", Icons.Rounded.School, Color(0xFF6200EE)),
    Intermediate("Level 2: Intermediate", Icons.Rounded.TrendingUp, Color(0xFF2196F3)),
    Advanced("Level 3: Advanced", Icons.Rounded.Diamond, Color(0xFF43A047)), // Green
    Expert("Level 4: Expert", Icons.Rounded.WorkspacePremium, Color(0xFFE53935)) // Red
}

object LearningRepository {
    private val _modules = mutableStateListOf<LearningModule>()
    val modules: List<LearningModule> get() = _modules
    
    private var prefs: android.content.SharedPreferences? = null
    var currentUserId: Int = -1 // Default to invalid until set 
        set(value) {
            field = value
            loadProgress() // Reload when user changes
        }
    private val learningService = com.simats.moneymentor.data.network.RetrofitClient.learningService

    init {
        // Initialize with default data
        _modules.addAll(getInitialModules())
    }

    fun init(context: Context) {
        prefs = context.getSharedPreferences("learning_prefs", Context.MODE_PRIVATE)
        loadProgress()
    }

    suspend fun syncProgressWithServer() {
        if (currentUserId <= 0) return
        
        try {
            val response = learningService.getProgress(currentUserId)
            // The server returns overall progress. We might want to still rely on local status for per-article detail
            // if the server doesn't provide a list of completed article IDs.
            // For now, we'll just log it or update a global state if needed.
            // If the server provides a list of completed articles in the future, we should update _modules here.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadProgress() {
        if (currentUserId <= 0) {
            // If no user set, all articles are incomplete
             val resetModules = _modules.map { module ->
                val articles = module.articles.map { it.copy(isCompleted = false) }
                module.copy(articles = articles, progress = 0f, status = ModuleStatus.Locked)
            }
            _modules.clear()
            _modules.addAll(resetModules)
            return
        }

        val updatedModulesList = _modules.map { module ->
            val articles = module.articles.map { article ->
                val isCompleted = prefs?.getBoolean("user_${currentUserId}_module_${module.id}_article_${article.id}", false) ?: false
                article.copy(isCompleted = isCompleted)
            }
            
            // Recalculate module progress
            val contentArticles = articles.filter { it.type != ArticleType.Quiz }
            val totalContent = contentArticles.size
            val completedContent = contentArticles.count { it.isCompleted }
            val progress = if (totalContent > 0) completedContent.toFloat() / totalContent else 0f
            val status = if (progress >= 1f) ModuleStatus.Done else if (progress > 0f) ModuleStatus.InProgress else module.status
            
            module.copy(articles = articles, progress = progress, status = status)
        }
        _modules.clear()
        _modules.addAll(updatedModulesList)
    }

    // Centralized Progress Properties
    val totalArticlesCount: Int 
        get() = _modules.flatMap { it.articles }.count { it.type != ArticleType.Quiz }
    
    val completedArticlesCount: Int
        get() = _modules.flatMap { it.articles }.count { it.type != ArticleType.Quiz && it.isCompleted }
        
    val overallProgressValue: Float
        get() {
            val total = totalArticlesCount
            return if (total > 0) completedArticlesCount.toFloat() / total else 0f
        }

    fun getModule(moduleId: Int): LearningModule? {
        return _modules.find { it.id == moduleId }
    }
    
    fun getArticle(moduleId: Int, articleId: Int): LearningArticle? {
        return _modules.find { it.id == moduleId }?.articles?.find { it.id == articleId }
    }

    fun markArticleAsCompleted(moduleId: Int, articleId: Int) {
        val moduleIndex = _modules.indexOfFirst { it.id == moduleId }
        if (moduleIndex != -1) {
            val module = _modules[moduleIndex]
            val updatedArticles = module.articles.map { article ->
                if (article.id == articleId) article.copy(isCompleted = true) else article
            }

            // Save to SharedPreferences (User Specific)
            prefs?.edit()?.putBoolean("user_${currentUserId}_module_${moduleId}_article_${articleId}", true)?.apply()

            // Logic: Completion is based on content articles. Quiz is unlocked when all content is done.
            val contentArticles = updatedArticles.filter { it.type != ArticleType.Quiz }
            val totalContent = contentArticles.size
            val completedContent = contentArticles.count { it.isCompleted }
            val newProgress = if (totalContent > 0) completedContent.toFloat() / totalContent else 0f
            
            val newStatus = if (newProgress >= 1f) ModuleStatus.Done else ModuleStatus.InProgress

            // Create updated module
            val updatedModule = module.copy(
                articles = updatedArticles,
                progress = newProgress,
                status = newStatus
            )
            
            _modules[moduleIndex] = updatedModule

            if (currentUserId > 0) {
                val globalArticleNo = getGlobalArticleNo(moduleId, articleId)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        learningService.updateProgress(com.simats.moneymentor.data.model.ProgressUpdateRequest(currentUserId, globalArticleNo))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun getGlobalArticleNo(moduleId: Int, articleId: Int): Int {
        var count = 0
        for (module in _modules) {
            for (article in module.articles) {
                if (article.type != ArticleType.Quiz) {
                    count++
                    if (module.id == moduleId && article.id == articleId) {
                        return count
                    }
                }
            }
        }
        return count
    }

    fun resetProgress() {
        prefs?.edit()?.clear()?.apply()
        loadProgress()
    }

    suspend fun syncProgressFromServer() {
        if (currentUserId <= 0) return
        try {
            val response = com.simats.moneymentor.data.network.RetrofitClient.learningService.getProgress(currentUserId)
            val serverCompletedList = response.completedArticles
            
            if (serverCompletedList != null) {
                // Reset local articles for THIS user only
                val editor = prefs?.edit()
                for (module in _modules) {
                    for (article in module.articles) {
                        editor?.remove("user_${currentUserId}_module_${module.id}_article_${article.id}")
                    }
                }
                
                // Track current global count to map article_no back to moduleId and articleId
                var count = 0
                for (module in _modules) {
                    for (article in module.articles) {
                        if (article.type != ArticleType.Quiz) {
                            count++
                            if (serverCompletedList.contains(count)) {
                                editor?.putBoolean("user_${currentUserId}_module_${module.id}_article_${article.id}", true)
                            }
                        }
                    }
                }
                editor?.apply()
                // Reload progress from updated prefs
                loadProgress()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun isQuizUnlocked(moduleId: Int): Boolean {
        val module = _modules.find { it.id == moduleId } ?: return false
        // Unlock if all non-quiz articles are completed
        return module.articles.filter { it.type != ArticleType.Quiz }.all { it.isCompleted }
    }

    private fun getInitialModules(): List<LearningModule> {
        return listOf(
            // Level 1: Beginner
            LearningModule(
                1, "Budgeting Basics", "Learn how to create and stick to a budget.", Icons.Rounded.PieChart, 0f, ModuleStatus.InProgress, Color(0xFF4CAF50), LearningLevel.Beginner,
                articles = listOf(
                    LearningArticle(1, "What is budgeting?", ArticleType.Article, "3 min", false, """
Budgeting is simply making a plan for your money. It means deciding in advance how much you will spend, save, and invest each month.

Think of it like a roadmap for your salary. Without a budget, money tends to disappear on random things. With a budget, you control where every rupee goes.

**Why budget?**
• You know exactly where your money goes
• You avoid overspending
• You can save for things that matter
• You reduce money stress

**How to start:**
1. Write down your monthly income
2. List all your expenses (rent, food, transport, etc.)
3. Subtract expenses from income
4. Allocate the remaining to savings and goals

Even a simple budget on paper or a notes app works. The key is to start tracking — awareness is the first step to financial control.
                    """.trimIndent()),
                    LearningArticle(2, "Needs vs Wants", ArticleType.Article, "4 min", false, """
Every expense falls into one of two categories: a need or a want. Understanding this difference is the foundation of smart spending.

**Needs are things you must have to survive and function:**
• Rent or housing
• Food and groceries
• Electricity and water bills
• Basic clothing
• Transportation to work
• Health expenses

**Wants are things that are nice to have but not essential:**
• Eating out at restaurants
• Netflix or streaming subscriptions
• New gadgets or latest phone
• Designer clothes
• Vacations

**The tricky part:** 
Some things feel like needs but are actually wants. Do you need a ₹1,500/month gym membership, or can you exercise at home? Do you need Swiggy every day, or can you cook more?

**Quick test:** 
Ask yourself — "Can I survive without this for a month?" If yes, it's probably a want.

The goal isn't to eliminate all wants. It's to be honest about what's a need vs want, so you can make better choices with your money.
                    """.trimIndent()),
                    LearningArticle(3, "The 50/30/20 Rule", ArticleType.Article, "5 min", false, """
The 50/30/20 rule is the simplest budgeting method. It divides your income into three buckets:

**50% → Needs** (essentials you can't avoid)
**30% → Wants** (things you enjoy)
**20% → Savings & Investments**

**Example with ₹50,000 salary:**
• ₹25,000 → Needs (rent ₹12,000 + groceries ₹5,000 + bills ₹3,000 + transport ₹3,000 + insurance ₹2,000)
• ₹15,000 → Wants (eating out ₹4,000 + entertainment ₹3,000 + shopping ₹5,000 + subscriptions ₹1,000 + misc ₹2,000)
• ₹10,000 → Savings (emergency fund ₹5,000 + SIP ₹3,000 + FD ₹2,000)

**What if your needs exceed 50%?**
That's common in expensive cities. Adjust the ratio — maybe 60/20/20. The important thing is that savings should never be zero.

**Pro tip:** 
Pay yourself first. As soon as salary comes in, move 20% to savings. Then spend the rest. Don't save what's left after spending — spend what's left after saving.
                    """.trimIndent()),
                    LearningArticle(4, "Tracking Expenses", ArticleType.Article, "3 min", false, """
Tracking expenses means writing down every rupee you spend. It sounds boring, but it's the most powerful money habit you can build.

**Why track?**
Most people have no idea where 20-30% of their money goes. Small expenses like chai, auto rides, and online orders add up silently.

**Simple methods to track:**
**Method 1: Notes App**
Every night, spend 2 minutes noting what you spent that day. At month end, add it up.

**Method 2: Expense App**
Apps like Walnut, Money Manager, or even Google Sheets work great. They categorize spending automatically.

**Method 3: The Envelope System**
Withdraw cash for different categories (food, transport, fun) and put them in separate envelopes. When an envelope is empty, stop spending in that category.

**What to track:**
• Date
• Amount
• Category (food, transport, shopping, etc.)
• Was it a need or want?

The magic happens after 1 month. You'll see patterns — maybe you're spending ₹6,000 on food delivery when you thought it was ₹2,000. That awareness alone changes behavior.

Track for just 30 days. It will change how you think about money forever.
                    """.trimIndent()),
                    LearningArticle(5, "Why emergency fund is important", ArticleType.Article, "4 min", false, """
An emergency fund is money set aside for unexpected expenses — things like medical bills, job loss, car repairs, or urgent home fixes.

**Why is it important?**
Life is unpredictable. Without an emergency fund:
• You'll borrow from friends or family
• You'll use credit cards and pay high interest
• You'll break your investments (selling at a loss)
• You'll feel constant financial stress

**How much do you need?**
The standard advice is 3 to 6 months of your monthly expenses.

If your monthly expenses are ₹30,000:
• Minimum emergency fund = ₹90,000 (3 months)
• Ideal emergency fund = ₹1,80,000 (6 months)

**Where to keep it?**
Your emergency fund should be:
• Easy to access (liquid)
• Safe (no market risk)
• Separate from your regular account

**Best options:**
1. Savings account (instant access)
2. Liquid mutual fund (slightly better returns)
3. Fixed deposit with premature withdrawal option

**Important rules:**
• This money is ONLY for real emergencies
• A sale on Amazon is NOT an emergency
• Replenish it immediately after using it

Having an emergency fund gives you peace of mind. It's the foundation of all financial planning.
                    """.trimIndent()),
                    LearningArticle(6, "How to build a 3–6 month emergency fund", ArticleType.Article, "5 min", false, """
Building an emergency fund feels overwhelming, but you can do it step by step. Here's a practical plan:

**Step 1: Calculate your target**
Add up your monthly essentials: rent + food + bills + transport + insurance. Multiply by 3 (minimum) or 6 (ideal).
Example: ₹25,000 × 6 = ₹1,50,000 target

**Step 2: Start small**
Don't try to save it all at once. Start with ₹2,000-5,000 per month. Even ₹500/week adds up to ₹26,000 in a year.

**Step 3: Automate it**
Set up an auto-transfer on salary day. Money moves to your emergency fund before you can spend it.

**Step 4: Use a separate account**
Open a separate savings account just for emergencies. Out of sight, out of mind. You won't accidentally spend it.

**Step 5: Boost with windfalls**
Got a bonus? Tax refund? Cash gift? Put at least 50% into your emergency fund.

**Timeline example (₹1,50,000 target):**
• Saving ₹5,000/month → 30 months (2.5 years)
• Saving ₹8,000/month → 19 months
• Saving ₹10,000/month → 15 months

**Milestone approach:**
• First ₹10,000 → covers small emergencies
• First ₹50,000 → covers 2 months
• ₹1,50,000 → full 6-month fund ✅

Don't wait for the "right time." Start today, even if it's just ₹500. Your future self will thank you.
                    """.trimIndent()),
                    LearningArticle(7, "Take Module Quiz", ArticleType.Quiz, "3 min", false)
                )
            ),
            LearningModule(
                2, "Banking & Saving Basics", "Understand savings accounts, FDs, and banking.", Icons.Rounded.AccountBalance, 0f, ModuleStatus.Locked, Color(0xFF2196F3), LearningLevel.Beginner,
                articles = listOf(
                    LearningArticle(1, "Introduction to Banking", ArticleType.Article, "3 min", false, """
Banks offer different types of accounts for different purposes. Here are the main ones you should know:

1. Savings Account
• For everyday use — salary, expenses, small savings
• Earns 2.5% to 4% interest per year
• Easy access via ATM, UPI, net banking
• Best for: Your daily money

2. Current Account
• For businesses and frequent transactions
• Usually zero interest
• No limit on transactions
• Best for: Business owners, freelancers with high volume

3. Fixed Deposit (FD) Account
• Lock your money for a fixed period (6 months to 10 years)
• Earns higher interest (5% to 7.5%)
• Penalty for early withdrawal
• Best for: Money you won't need soon

4. Recurring Deposit (RD) Account
• Save a fixed amount every month
• Earns FD-like interest
• Good discipline tool
• Best for: Building savings habit

5. Salary Account
• Opened by your employer
• Often has zero balance requirement
• May include benefits like free insurance
• Converts to savings account if you leave the job

**Which should you have?**
Most people need: 1 savings account (daily use) + 1 savings account (emergency fund) + FD or RD (for goals). Keep it simple.
                    """.trimIndent()),
                    LearningArticle(2, "What is Fixed Deposit (FD)?", ArticleType.Article, "4 min", false, """
A Fixed Deposit (FD) is one of the safest ways to grow your money. You deposit a lump sum with a bank for a fixed period, and the bank pays you a guaranteed interest rate.

**How it works:**
1. You give the bank ₹1,00,000
2. Bank says "We'll pay you 7% per year for 1 year"
3. After 1 year, you get ₹1,07,000

**Key features:**
• Guaranteed returns — unlike stocks, you know exactly what you'll get
• Fixed period — ranges from 7 days to 10 years
• Higher interest than savings account (typically 5-7.5%)
• Safe — bank deposits up to ₹5 lakh are insured by DICGC

**Types of FD:**
• Regular FD — interest paid at maturity
• Cumulative FD — interest reinvested (compounding)
• Tax-saving FD — 5-year lock-in, saves tax under 80C
• Senior Citizen FD — extra 0.25-0.5% interest for 60+ age

**When to use FD:**
• You have a lump sum you won't need for 6+ months
• You want zero risk
• You're saving for a specific goal with a known date
• You want guaranteed returns

**Limitations:**
• Returns may not beat inflation in the long run
• Interest is taxable
• Early withdrawal has a penalty (usually 0.5-1% less interest)

FDs are perfect for beginners and for the "safe" portion of your money.
                    """.trimIndent()),
                    LearningArticle(3, "How FD interest is calculated", ArticleType.Article, "4 min", false, """
Understanding how FD interest works helps you choose the right deposit. There are two main methods:

**Simple Interest**
Interest is calculated only on your original deposit.
Formula: Interest = Principal × Rate × Time
Example: ₹1,00,000 at 7% for 2 years
= 1,00,000 × 0.07 × 2 = ₹14,000
Total: ₹1,14,000

**Compound Interest**
Interest is calculated on your deposit PLUS previously earned interest. This is more common and better for you.
Example: ₹1,00,000 at 7% compounded quarterly for 2 years
Quarter 1: ₹1,00,000 × 1.75% = ₹1,750 → Balance: ₹1,01,750
Quarter 2: ₹1,01,750 × 1.75% = ₹1,781 → Balance: ₹1,03,531
...and so on
After 2 years: ~₹1,14,888 (vs ₹1,14,000 with simple interest)

**Compounding frequency matters:**
• Quarterly compounding (most common) → better returns
• Monthly compounding → even better
• Annual compounding → lowest returns

**Quick mental math:**
At 7% compounded quarterly, your money roughly doubles in about 10 years.

**Pro tip:** Always ask the bank for the "effective annual rate" — this accounts for compounding and gives you the true return percentage.
                    """.trimIndent()),
                    LearningArticle(4, "FD vs Savings Account", ArticleType.Article, "4 min", false, """
Both keep your money safe, but they serve very different purposes. Here's a clear comparison:

**Savings Account:**
• Interest: 2.5% - 4% per year
• Access: Anytime (ATM, UPI, online)
• Minimum balance: ₹1,000 - ₹10,000 required
• Best for: Daily expenses, emergency access

**Fixed Deposit:**
• Interest: 5% - 7.5% per year
• Access: Locked for a fixed period
• Minimum amount: Usually ₹1,000 - ₹10,000
• Best for: Money you won't need soon

**When Savings Account wins:**
• You need the money anytime (emergency fund)
• You're saving for something in the next 1-3 months
• You need to pay bills and expenses regularly

**When FD wins:**
• You have extra money sitting idle in savings
• You won't need this money for 6+ months
• You want guaranteed, higher returns
• You want to avoid the temptation to spend

**Smart strategy: Use both!**
1. Keep 1-2 months expenses in savings account
2. Keep emergency fund in savings or liquid fund
3. Put everything extra into FDs

**Example:**
Salary: ₹50,000
• Savings account: ₹50,000 (monthly expenses)
• Emergency fund (separate savings): ₹1,50,000
• FD: ₹2,00,000 (long-term savings at higher interest)

Don't let large amounts sit idle in a savings account earning just 3%. Move excess to FD and earn almost double the interest.
                    """.trimIndent()),
                    LearningArticle(5, "When should you choose FD?", ArticleType.Article, "4 min", false, """
FDs aren't always the best choice. Here's when they make sense and when they don't:

**Choose FD when:**
✅ You have a specific goal with a known date (wedding in 2 years, car down payment in 1 year)
✅ You want zero risk — FDs are among the safest investments
✅ You're a beginner and not ready for market investments
✅ You need to park money for 6 months to 3 years
✅ You want to save tax (5-year tax-saving FD under Section 80C)
✅ You're retired and need predictable income

**Don't choose FD when:**
❌ You might need the money anytime (use savings account instead)
❌ Your investment horizon is 5+ years (equity/mutual funds give better returns)
❌ Inflation is higher than FD rate (your money loses real value)
❌ You're in a high tax bracket (FD interest is fully taxable)

**The inflation problem:**
If FD gives 7% and inflation is 6%, your real return is only 1%. Over long periods, this barely grows your wealth.

**Better alternatives for long-term:**
• SIP in mutual funds (10-12% average returns)
• PPF (7.1% tax-free returns)
• Debt mutual funds (more tax-efficient than FD)

**The balanced approach:**
Keep some money in FDs (for safety and short-term goals) and invest the rest in mutual funds (for long-term wealth building). Don't put ALL your money in FDs.
                    """.trimIndent()),
                    LearningArticle(6, "What is Recurring Deposit (RD)?", ArticleType.Article, "4 min", false, """
A Recurring Deposit (RD) is like a Fixed Deposit, but instead of depositing a lump sum, you save a fixed amount every month.

**How RD works:**
1. You choose a monthly amount (say ₹5,000)
2. You choose a period (say 2 years)
3. Every month, ₹5,000 is auto-debited from your account
4. After 2 years, you get your total deposits + interest

**Example:**
₹5,000/month for 2 years at 6.5% interest
• Total deposited: ₹1,20,000
• Interest earned: ~₹8,000
• You receive: ~₹1,28,000

**RD vs FD:**
• FD needs a lump sum upfront; RD needs monthly installments
• FD interest is slightly higher than RD
• RD is better for building a savings habit

**RD vs SIP (Mutual Fund):**
• RD gives guaranteed but lower returns (6-7%)
• SIP gives higher but variable returns (10-12% average)
• RD has zero risk; SIP has market risk
• For beginners, RD is safer; for long-term, SIP is better

**When RD is useful:**
• You don't have a lump sum for FD
• You want to build discipline in saving
• You're saving for a short-term goal (6 months to 3 years)
• You want guaranteed returns with no risk

**Pro tip:** Many people start with RD to build the habit of monthly saving, then graduate to SIP for better long-term returns. Both are great — the important thing is to start saving regularly.
                    """.trimIndent()),
                    LearningArticle(7, "Quiz: Banking Basics", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                3, "Introduction to Tax", "Learn basics of income tax and slabs.", Icons.Rounded.Calculate, 0f, ModuleStatus.Locked, Color(0xFFFF9800), LearningLevel.Beginner,
                articles = listOf(
                    LearningArticle(1, "What is income tax?", ArticleType.Article, "4 min", false, """
Income tax is a portion of your earnings that you pay to the government. The government uses this money to build roads, hospitals, schools, and run the country.

**Who pays income tax?**
Anyone who earns above a certain amount per year. In India, if your annual income is above ₹2.5 lakh (old regime) or ₹3 lakh (new regime), you need to pay tax.

**How does it work?**
1. You earn money (salary, business, freelancing, etc.)
2. Government says "pay X% of your income as tax"
3. The percentage increases as you earn more (this is called progressive taxation)
4. You file a tax return every year showing what you earned and paid

**Key terms to know:**
• **Financial Year (FY):** April 1 to March 31 — the year you earn
• **Assessment Year (AY):** The next year — when you file your return
• **PAN Card:** Your unique tax identity number
• **ITR:** Income Tax Return — the form you file

**Example:**
If you earn ₹6,00,000/year and your tax works out to ₹30,000, that's your income tax. Your employer usually deducts this monthly from your salary (called TDS).

Income tax isn't optional — it's a legal requirement. But there are many legal ways to reduce it, which we'll cover in later articles.
                    """.trimIndent()),
                    LearningArticle(2, "What is taxable income?", ArticleType.Article, "4 min", false, """
Taxable income is NOT the same as your total salary. It's the amount on which you actually pay tax, after removing certain deductions and exemptions.

**Total Income vs Taxable Income:**
• Total Income = Everything you earn (salary + interest + rent + other sources)
• Taxable Income = Total Income - Deductions - Exemptions

**What counts as income?**
• Salary from your job
• Interest from savings account and FDs
• Rental income from property
• Freelance or business income
• Capital gains from selling stocks/property
• Other sources (lottery, gifts above ₹50,000, etc.)

**What reduces your taxable income?**
• Standard deduction (₹50,000 for salaried in old regime, ₹75,000 in new)
• Section 80C investments (PPF, ELSS, LIC — up to ₹1.5 lakh)
• Section 80D (health insurance premium)
• HRA exemption (if you pay rent)
• Home loan interest

**Example:**
Total salary: ₹8,00,000
Standard deduction: -₹50,000
Section 80C (PPF + ELSS): -₹1,50,000
Section 80D (health insurance): -₹25,000
**Taxable income = ₹5,75,000**

You pay tax on ₹5,75,000, not ₹8,00,000. That's a big difference! Understanding this helps you legally pay less tax.
                    """.trimIndent()),
                    LearningArticle(3, "Understanding tax slabs", ArticleType.Article, "5 min", false, """
Tax slabs are like steps — the more you earn, the higher the percentage you pay. But you don't pay the highest rate on ALL your income, only on the portion that falls in that slab.

**New Tax Regime (FY 2024-25):**
• ₹0 - ₹3,00,000 → 0% (no tax)
• ₹3,00,001 - ₹7,00,000 → 5%
• ₹7,00,001 - ₹10,00,000 → 10%
• ₹10,00,001 - ₹12,00,000 → 15%
• ₹12,00,001 - ₹15,00,000 → 20%
• Above ₹15,00,000 → 30%

**Old Tax Regime:**
• ₹0 - ₹2,50,000 → 0%
• ₹2,50,001 - ₹5,00,000 → 5%
• ₹5,00,001 - ₹10,00,000 → 20%
• Above ₹10,00,000 → 30%

**How slabs work (example with ₹10,00,000 income, new regime):**
• First ₹3,00,000 → ₹0 tax
• Next ₹4,00,000 (₹3L to ₹7L) → ₹20,000 (5%)
• Next ₹3,00,000 (₹7L to ₹10L) → ₹30,000 (10%)
• **Total tax = ₹50,000**

**Common mistake:** People think "I'm in the 30% bracket so I pay 30% on everything." Wrong! You only pay 30% on the amount ABOVE ₹15 lakh. The rest is taxed at lower rates.

**Plus 4% cess:** On top of your tax, you pay 4% Health & Education Cess. So if tax is ₹50,000, cess = ₹2,000. Total = ₹52,000.
                    """.trimIndent()),
                    LearningArticle(4, "What is TDS?", ArticleType.Article, "3 min", false, """
TDS stands for Tax Deducted at Source. It means tax is cut from your income BEFORE you receive it. Think of it as advance tax payment.

**How TDS works:**
1. Your employer calculates your annual tax
2. Divides it by 12
3. Deducts that amount from your monthly salary
4. Pays it to the government on your behalf

**Example:**
Annual salary: ₹8,00,000
Estimated tax: ₹40,000
Monthly TDS: ₹40,000 ÷ 12 = ~₹3,333

So you receive ₹3,333 less in your monthly salary. But this isn't extra money taken — it's your tax being paid in advance.

**TDS isn't just on salary. It applies to:**
• Bank FD interest (if interest > ₹40,000/year) → 10% TDS
• Freelance payments → 10% TDS
• Rent payments (if rent > ₹50,000/month) → 5% TDS
• Prize money → 30% TDS

**What if too much TDS is deducted?**
File your Income Tax Return (ITR). If your actual tax is less than TDS deducted, you get a refund! The government sends the extra money back to your bank account.

**How to check your TDS:**
• **Form 16** — your employer gives this after March
• **Form 26AS** — shows all TDS deducted (check on income tax website)
• **AIS (Annual Information Statement)** — detailed view of all financial transactions
                    """.trimIndent()),
                    LearningArticle(5, "Basic idea of deductions", ArticleType.Article, "4 min", false, """
Deductions are amounts you can subtract from your total income to reduce your taxable income. Less taxable income = less tax.

Think of it this way:
If you earn ₹10,00,000 and claim ₹2,50,000 in deductions, you only pay tax on ₹7,50,000.

**Main deductions available (Old Regime):**
**Section 80C (up to ₹1,50,000):**
• PPF (Public Provident Fund)
• ELSS mutual funds
• Life insurance premium
• EPF (Employee Provident Fund — auto-deducted)
• 5-year tax-saving FD
• Children's tuition fees
• Home loan principal repayment

**Section 80D (Health Insurance):**
• Self & family premium: up to ₹25,000
• Parents' premium: additional ₹25,000 (₹50,000 if senior citizen)

**Other deductions:**
• **80E:** Education loan interest (no limit)
• **80G:** Donations to approved charities
• **HRA:** House Rent Allowance exemption
• **Home loan interest:** up to ₹2,00,000 under Section 24

**New Regime note:**
Most deductions are NOT available in the new regime. Only standard deduction of ₹75,000 applies. This is why some people choose old regime — if their deductions are high enough, old regime saves more tax.

**Quick rule:** If your total deductions exceed ₹3-4 lakh, old regime is usually better. Otherwise, new regime's lower rates win.
                    """.trimIndent()),
                    LearningArticle(6, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                4, "Introduction to Investing", "Start your investing journey here.", Icons.Rounded.RocketLaunch, 0f, ModuleStatus.Locked, Color(0xFF9C27B0), LearningLevel.Beginner,
                articles = listOf(
                    LearningArticle(1, "What is investing?", ArticleType.Article, "3 min", false, """
Investing means putting your money into something that can grow in value over time. Instead of just saving money in a bank, you make your money work for you.

**Saving vs Investing:**
• **Saving** = keeping money safe (bank account, FD)
• **Investing** = growing money over time (stocks, mutual funds, real estate)

**Why invest?**
Because of inflation. If prices rise 6% every year but your savings earn only 3-4%, you're actually losing purchasing power. ₹1,00,000 today will buy less in 10 years.

**Simple example:**
• ₹1,00,000 in savings account at 3.5% → ₹1,41,000 after 10 years
• ₹1,00,000 in mutual fund at 12% → ₹3,10,000 after 10 years

That's the power of investing — your money grows much faster.

**Types of investments:**
• **Fixed income** (safe, lower returns): FD, PPF, bonds
• **Equity** (risky, higher returns): Stocks, equity mutual funds
• **Real estate:** Property, REITs
• **Gold:** Physical gold, gold ETFs, sovereign gold bonds

**Key principle:** Start early. Even small amounts invested regularly can grow into large sums thanks to compounding. A 25-year-old investing ₹5,000/month can build more wealth than a 35-year-old investing ₹15,000/month.
                    """.trimIndent()),
                    LearningArticle(2, "Saving vs Investing", ArticleType.Article, "4 min", false, """
Saving and investing are both important, but they serve different purposes. Understanding when to save and when to invest is crucial.

**Saving:**
• **Goal:** Protect your money
• **Risk:** Almost zero
• **Returns:** Low (3-7%)
• **Access:** Easy and quick
• **Best for:** Emergency fund, short-term goals (< 2 years)
• **Examples:** Savings account, FD, RD

**Investing:**
• **Goal:** Grow your money
• **Risk:** Low to high (depends on type)
• **Returns:** Moderate to high (8-15%+)
• **Access:** May take time to withdraw
• **Best for:** Long-term goals (3+ years)
• **Examples:** Stocks, mutual funds, PPF, real estate

**When to save:**
• Building your emergency fund
• Saving for a vacation in 6 months
• Keeping money for upcoming expenses
• You need the money within 1-2 years

**When to invest:**
• Building wealth for retirement (10-30 years away)
• Saving for a house down payment (3-5 years away)
• Child's education fund (5-15 years away)
• You want your money to beat inflation

**The right approach:**
First save (emergency fund), then invest (everything else). Don't invest money you might need in the next 1-2 years — market ups and downs could hurt you short-term.

Think of saving as defense and investing as offense. You need both to win the money game.
                    """.trimIndent()),
                    LearningArticle(3, "Risk vs return", ArticleType.Article, "4 min", false, """
Risk and return are two sides of the same coin. Higher potential returns always come with higher risk. Understanding this helps you make smarter choices.

**What is risk?**
Risk means the chance that you could lose some or all of your money, or that returns could be less than expected.

**Risk-Return spectrum:**
• **Savings account** → Very low risk, very low return (3-4%)
• **Fixed Deposit** → Low risk, low-moderate return (5-7%)
• **Government bonds/PPF** → Low risk, moderate return (7-8%)
• **Debt mutual funds** → Low-moderate risk, moderate return (6-8%)
• **Equity mutual funds** → Moderate-high risk, high return (10-15%)
• **Individual stocks** → High risk, potentially very high return (variable)
• **Crypto** → Very high risk, very unpredictable return

**Key concepts:**
• **No risk = low return.** Your money is safe but grows slowly.
• **High risk ≠ guaranteed high return.** You COULD earn more, but you could also lose.
• **Time reduces risk.** Stock markets are volatile short-term but tend to grow long-term.

**Your risk capacity depends on:**
• **Age** (younger = can take more risk)
• **Income stability** (stable job = more risk capacity)
• **Financial goals** (retirement in 20 years = more risk OK)
• **Emergency fund** (have one = can take more risk)
• **Personal comfort** (can you sleep if market drops 20%?)

**Golden rule:** Never invest money you can't afford to lose in high-risk options. And never put ALL your money in one type of investment.
                    """.trimIndent()),
                    LearningArticle(4, "What is SIP?", ArticleType.Article, "4 min", false, """
SIP stands for Systematic Investment Plan. It's a way to invest a fixed amount regularly (usually monthly) into a mutual fund.

**How SIP works:**
1. You choose a mutual fund
2. You set an amount (say ₹5,000/month)
3. Every month, ₹5,000 is auto-debited and invested
4. You get units of the mutual fund at that day's price

**Why SIP is great for beginners:**
1. **No need for large amount:** Start with as little as ₹500/month. You don't need lakhs to begin investing.
2. **Rupee cost averaging:** When market is high, your ₹5,000 buys fewer units. When market is low, it buys more units. Over time, this averages out your cost — you don't need to "time the market."
3. **Power of compounding:** Your returns earn returns. ₹5,000/month at 12% for 20 years = ₹49.9 lakh (you invested only ₹12 lakh!)
4. **Discipline:** Auto-debit means you invest without thinking. It builds a habit.

**Example:**
₹5,000/month SIP at 12% average return:
• After 5 years: ₹4.12 lakh (invested ₹3 lakh)
• After 10 years: ₹11.6 lakh (invested ₹6 lakh)
• After 20 years: ₹49.9 lakh (invested ₹12 lakh)

**How to start:** Open an account on any investment app (Groww, Zerodha, Kuvera), choose a mutual fund, set your SIP amount and date. That's it!
                    """.trimIndent()),
                    LearningArticle(5, "What is a stock?", ArticleType.Article, "4 min", false, """
A stock represents a tiny piece of ownership in a company. When you buy a stock, you become a part-owner of that company.

**How stocks work:**
1. Companies need money to grow
2. They sell small pieces of ownership (shares/stocks) to the public
3. You buy these shares on the stock exchange (BSE/NSE in India)
4. If the company does well, your share's value goes up
5. You can sell it later for a profit

**Example:**
You buy 10 shares of Infosys at ₹1,500 each = ₹15,000 invested
After 2 years, the price rises to ₹2,000 per share
Your 10 shares are now worth ₹20,000 — you made ₹5,000 profit!

**How do you make money from stocks?**
• **Price appreciation:** Buy low, sell high
• **Dividends:** Some companies share their profits with shareholders regularly

**Risks of stocks:**
• Prices can go down — you could lose money
• Individual companies can fail
• Short-term volatility can be stressful
• Requires research and knowledge

**Important for beginners:**
• Don't put all your money in one stock
• Start with mutual funds before individual stocks
• Only invest money you won't need for 5+ years
• Never invest based on tips from friends or social media

Stocks can build serious wealth over time, but they require patience, knowledge, and emotional discipline.
                    """.trimIndent()),
                    LearningArticle(6, "Basic idea of mutual funds", ArticleType.Article, "5 min", false, """
A mutual fund pools money from thousands of investors and invests it in stocks, bonds, or other assets. A professional fund manager makes the investment decisions for you.

**Think of it like this:**
You and 1,000 other people each put in ₹5,000. The fund now has ₹50 lakh. A professional manager invests this across 30-50 different stocks. You own a tiny piece of all those stocks.

**Why mutual funds are great for beginners:**
1. **Professional management:** You don't need to pick stocks yourself. An expert does it.
2. **Diversification:** Your money is spread across many companies. If one stock falls, others may rise. This reduces risk.
3. **Start small:** You can start SIP with just ₹500/month.
4. **Regulated and transparent:** SEBI (Securities and Exchange Board of India) regulates all mutual funds. Your money is safe from fraud.

**Main types:**
• **Equity funds:** Invest in stocks (higher risk, higher returns ~12-15%)
• **Debt funds:** Invest in bonds/FDs (lower risk, moderate returns ~6-8%)
• **Hybrid funds:** Mix of equity + debt (balanced risk and returns)
• **Index funds:** Copy a market index like Nifty 50 (low cost, good returns)

**How to choose:**
• Long-term goals (5+ years) → Equity funds
• Short-term goals (1-3 years) → Debt funds
• Not sure → Hybrid or index funds

**Start with one simple index fund or large-cap fund via SIP. You can always add more later as you learn.**
                    """.trimIndent()),
                    LearningArticle(7, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            // Level 2: Intermediate
            LearningModule(
                5, "Advanced Budgeting & Cash Flow", "Master your cash flow and irregular income.", Icons.Rounded.QueryStats, 0f, ModuleStatus.Locked, Color(0xFF3F51B5), LearningLevel.Intermediate,
                articles = listOf(
                    LearningArticle(1, "Zero-based budgeting", ArticleType.Article, "5 min", false, """
Zero-based budgeting means giving every single rupee a job. Instead of spending first and saving what's left, you plan where EVERY rupee goes before the month starts.

**How it works:**
**Income - All planned expenses = ₹0**

This doesn't mean you spend everything. It means every rupee is assigned — including savings and investments.

**Example with ₹50,000 salary:**
• Rent: ₹15,000
• Groceries: ₹6,000
• Transport: ₹3,000
• Bills: ₹3,000
• SIP investment: ₹8,000
• Emergency fund: ₹3,000
• Eating out: ₹3,000
• Entertainment: ₹2,000
• Shopping: ₹3,000
• Miscellaneous: ₹2,000
• Buffer: ₹2,000
**Total: ₹50,000 (zero left unassigned)**

**Why it works better than 50/30/20:**
• Forces you to think about every expense
• No money "leaks" to unplanned spending
• You prioritize what matters most
• Great for people who feel 50/30/20 is too loose

**How to do it:**
1. List your income for the month
2. List every expense category
3. Assign amounts until income minus expenses = 0
4. Track actual spending against your plan
5. Adjust next month based on what you learn

**Pro tip:** Always include a "buffer" category of ₹1,000-3,000 for unexpected small expenses. Life is never perfectly predictable.
                    """.trimIndent()),
                    LearningArticle(2, "How to increase savings rate", ArticleType.Article, "4 min", false, """
Your savings rate is the percentage of income you save and invest. Increasing it is the fastest way to build wealth.

Current average: Most Indians save 15-20% of income. Top wealth builders save 30-50%.

**How to calculate yours:**
**Savings Rate = (Amount Saved + Invested) ÷ Total Income × 100**

If you earn ₹50,000 and save/invest ₹10,000, your rate is 20%.

**Strategies to increase it:**
1. **Automate first:** Increase your SIP or auto-transfer by ₹1,000. You'll adjust to the lower spending amount within a month.
2. **The 1% method:** Every month, save 1% more. Month 1: 20%, Month 2: 21%, Month 3: 22%. Small increases are painless.
3. **Save your raises:** Got a ₹5,000 raise? Put ₹3,000 into investments. You still get ₹2,000 extra to spend.
4. **Cut one big expense:** One big change beats ten small ones. Can you move to a cheaper apartment? Switch to public transport? Cook more?
5. **Audit subscriptions:** List every subscription (Netflix, Spotify, gym, apps). Cancel what you don't use weekly.
6. **The 24-hour rule:** For any purchase over ₹1,000, wait 24 hours. Most impulse buys won't happen.

**Impact of savings rate:**
• **10% savings rate** → Financial freedom in ~40 years
• **20%** → ~30 years
• **30%** → ~22 years
• **50%** → ~15 years

Even a 5% increase in savings rate can shave years off your journey to financial freedom.
                    """.trimIndent()),
                    LearningArticle(3, "Lifestyle inflation problem", ArticleType.Article, "4 min", false, """
Lifestyle inflation (or lifestyle creep) is when your spending increases every time your income increases. It's the biggest enemy of wealth building.

**How it happens:**
• You get a ₹10,000 raise → You upgrade your phone
• Next raise → You move to a fancier apartment
• Bonus → You buy a car on EMI
• **Result: You earn more but save the same (or less!)**

**Why it's dangerous:**
• Your expenses grow to match your income
• You never build wealth despite earning well
• You become dependent on a high salary
• One job loss can be devastating

**Real example:**
**Person A:** Earns ₹50,000, spends ₹40,000, saves ₹10,000
Gets raise to ₹70,000, now spends ₹60,000, saves ₹10,000
*Savings didn't improve despite 40% raise!*

**Person B:** Earns ₹50,000, spends ₹40,000, saves ₹10,000
Gets raise to ₹70,000, spends ₹45,000, saves ₹25,000
*Savings jumped 150%!*

**How to fight it:**
• Save at least 50% of every raise
• Keep your fixed costs (rent, car) stable for 2-3 years after a raise
• Upgrade experiences, not things (travel > gadgets)
• Track your spending monthly — awareness prevents creep
• Define "enough" — what lifestyle truly makes you happy?

**The mindset shift:** Rich isn't about how much you earn. It's about how much you keep. Many people earning ₹20 lakh/year have less savings than someone earning ₹8 lakh.
                    """.trimIndent()),
                    LearningArticle(4, "Budgeting for irregular income", ArticleType.Article, "5 min", false, """
If your income varies month to month (freelancers, business owners, commission-based jobs), budgeting requires a different approach.

**The challenge:**
You might earn ₹80,000 one month and ₹30,000 the next. Standard budgets based on a fixed salary don't work.

**Strategy 1: Budget on your lowest month**
Find your lowest income over the past 6-12 months. Build your budget around that number. Everything above it goes to savings.

**Strategy 2: The buffer account**
1. All income goes into a "holding" account
2. Pay yourself a fixed "salary" from this account (say ₹40,000/month)
3. Excess builds up as a buffer for lean months

**Strategy 3: Priority-based spending**
Rank your expenses by priority:
1. **Survival** (rent, food, bills) — always covered
2. **Safety** (insurance, emergency fund) — covered next
3. **Goals** (SIP, savings) — covered if income allows
4. **Wants** (entertainment, dining) — only from surplus

**Essential rules for irregular income:**
• Keep 6-9 months emergency fund (more than salaried people)
• Never commit to large EMIs based on your best month
• Pay taxes quarterly (advance tax) to avoid year-end shock
• Track income trends — know your average, not just peaks

The key insight: Irregular income requires MORE financial discipline, not less. But with the right system, you can actually save more than salaried people because your upside is unlimited.
                    """.trimIndent()),
                    LearningArticle(5, "Debt management basics", ArticleType.Article, "6 min", false, """
Debt isn't always bad, but unmanaged debt can destroy your finances. Here's how to handle it wisely.

**Good debt vs Bad debt:**
**Good debt helps you build wealth or earn more:**
• Education loan (increases earning potential)
• Home loan (builds an asset)
• Business loan (grows income)

**Bad debt costs you money with no return:**
• Credit card debt (18-42% interest!)
• Personal loan for vacations or gadgets
• Car loan (car loses value over time)
• Buy-now-pay-later for impulse purchases

**If you have debt, follow this plan:**
1. **List all debts:** Write down lender, total amount, interest rate, monthly EMI.
2. **Pay minimums on everything:** Never miss a payment — it destroys your credit score.
3. **Attack the most expensive debt first:** Pay extra on the debt with the highest interest rate (usually credit cards). This is called the "Avalanche Method."
4. **Alternative — Snowball Method:** Pay off the smallest debt first for a psychological win.
5. **Stop adding new debt:** Cut credit cards if needed. Use cash or debit card only.

**Key rules:**
• Never use credit card if you can't pay full bill monthly
• Total EMIs should be less than 30% of income
• Build emergency fund WHILE paying debt (even if small)
• Avoid debt to fund a lifestyle you can't afford

Remember: Being debt-free is one of the most powerful feelings. Every rupee of debt paid off is a rupee of freedom gained.
                    """.trimIndent()),
                    LearningArticle(6, "Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                6, "Tax Planning for Beginners", "Learn how to legally save tax and avoid common mistakes.", Icons.Rounded.AccountBalance, 0f, ModuleStatus.Locked, Color(0xFF009688), LearningLevel.Intermediate,
                articles = listOf(
                    LearningArticle(1, "Old vs New Tax Regime", ArticleType.Article, "5 min", false, """
India has two tax systems you can choose from. Understanding both helps you pick the one that saves more money.

**New Tax Regime (Default from FY 2023-24):**
• Lower tax rates
• Almost no deductions allowed
• Simpler — less paperwork
• Standard deduction: ₹75,000

**Tax slabs:**
• ₹0-3L → 0%
• ₹3-7L → 5%
• ₹7-10L → 10%
• ₹10-12L → 15%
• ₹12-15L → 20%
• Above ₹15L → 30%

**Old Tax Regime:**
• Higher tax rates
• Many deductions available (80C, 80D, HRA, etc.)
• More complex — need to plan and invest
• Standard deduction: ₹50,000

**Tax slabs:**
• ₹0-2.5L → 0%
• ₹2.5-5L → 5%
• ₹5-10L → 20%
• Above ₹10L → 30%

**Which is better for you?**
Choose NEW regime if:
• Your deductions are less than ₹3-4 lakh
• You don't pay rent (no HRA)
• You want simplicity

Choose OLD regime if:
• You claim HRA + 80C + 80D + home loan
• Total deductions exceed ₹4-5 lakh
• You're willing to plan investments

**Pro tip:** Calculate tax under both regimes using an online calculator. Choose whichever gives lower tax. You can switch every year.
                    """.trimIndent()),
                    LearningArticle(2, "Section 80C explained", ArticleType.Article, "4 min", false, """
Section 80C is the most popular tax-saving section. It lets you reduce your taxable income by up to ₹1,50,000 per year.

**How it works:**
If you invest ₹1,50,000 in 80C-eligible options and you're in the 30% tax bracket, you save ₹46,800 in tax (₹1,50,000 × 30% + 4% cess).

**Best 80C investment options:**
1. **EPF (Employee Provident Fund)**
• Auto-deducted from salary (12% of basic)
• Safe, guaranteed ~8.15% return
• You might already be using most of your 80C limit here!

2. **PPF (Public Provident Fund)**
• Government-backed, 7.1% tax-free return
• 15-year lock-in (partial withdrawal after 7 years)
• Best for: Safe, long-term, tax-free growth

3. **ELSS Mutual Funds**
• Equity mutual funds with 3-year lock-in (shortest among 80C)
• Potential returns: 10-15%
• Best for: Higher returns with moderate risk

4. **Life Insurance Premium**
• Term insurance premium qualifies
• Don't buy insurance JUST for tax saving

5. **Tax-saving FD**
• 5-year lock-in
• Returns: 6-7%
• Best for: Zero risk preference

6. **Others:**
• Children's tuition fees
• Home loan principal repayment
• NSC (National Savings Certificate)
• Sukanya Samriddhi (for girl child)

**Smart strategy:** EPF (auto) + ELSS (for growth) + PPF (for safety) = a well-balanced 80C portfolio.
                    """.trimIndent()),
                    LearningArticle(3, "80D (Health insurance deduction)", ArticleType.Article, "3 min", false, """
Section 80D lets you claim deductions for health insurance premiums. This is separate from 80C — it's additional savings!

**Deduction limits:**
• Self + spouse + children: up to ₹25,000/year
• Parents (below 60): additional ₹25,000
• Parents (above 60): additional ₹50,000
• If you're also above 60: ₹50,000 for self

**Maximum possible deduction:**
• You (below 60) + Parents (above 60) = ₹25,000 + ₹50,000 = ₹75,000

**What qualifies:**
• Health insurance premium (mediclaim)
• Preventive health check-up (up to ₹5,000, included in above limits)
• Premium for critical illness cover

**Example:**
Your health insurance: ₹18,000/year
Parents' health insurance: ₹30,000/year
Total 80D deduction: ₹48,000
If you're in 30% bracket, this saves: ₹48,000 × 31.2% = ~₹15,000 in tax!

**Why health insurance matters beyond tax:**
• One hospital visit can cost ₹2-10 lakh
• Medical inflation is 10-15% per year
• Without insurance, you'll drain your savings

**Pro tip:** Even if you have employer-provided insurance, buy a personal policy too. Employer coverage ends when you leave the job. Personal policy stays with you forever and premiums are lower when you're young.
                    """.trimIndent()),
                    LearningArticle(4, "HRA basics", ArticleType.Article, "4 min", false, """
HRA (House Rent Allowance) is a component of your salary. If you pay rent, you can claim a tax exemption on part or all of your HRA.

**HRA exemption is the LOWEST of these three:**
1. Actual HRA received from employer
2. 50% of basic salary (metro cities) or 40% (non-metro)
3. Actual rent paid minus 10% of basic salary

**Example:**
Basic salary: ₹30,000/month
HRA received: ₹15,000/month
Rent paid: ₹12,000/month
City: Bangalore (metro)

**Calculation:**
1. Actual HRA = ₹15,000
2. 50% of basic = ₹15,000
3. Rent - 10% of basic = ₹12,000 - ₹3,000 = ₹9,000
**Lowest = ₹9,000/month = ₹1,08,000/year exempt from tax**

If you're in 30% bracket: This saves ~₹33,700 in tax!

**Important rules:**
• You must actually pay rent (can be to parents too!)
• Rent above ₹1,00,000/year → you need landlord's PAN
• Keep rent receipts as proof
• If you own a house and pay rent elsewhere, you can still claim HRA

**No HRA in salary?**
If you're self-employed or your salary doesn't have HRA, you can claim deduction under Section 80GG (up to ₹5,000/month).

**Pro tip:** If you live with parents, you can pay them rent and claim HRA. They declare it as rental income (which may be tax-free if below the basic exemption limit).
                    """.trimIndent()),
                    LearningArticle(5, "How to legally reduce tax", ArticleType.Article, "5 min", false, """
There are many legal ways to reduce your tax. Here's a practical checklist:

1. **Max out Section 80C (₹1.5 lakh)**
EPF + PPF + ELSS = easy way to fill this

2. **Get health insurance (80D)**
Self + parents = up to ₹75,000 deduction

3. **Claim HRA properly**
If you pay rent, this can save ₹30,000-1,00,000+ in tax

4. **Home loan benefits**
• Principal: 80C (up to ₹1.5L)
• Interest: Section 24 (up to ₹2L)
• Total potential saving: ₹3.5 lakh deduction

5. **NPS contribution (80CCD)**
Extra ₹50,000 deduction above 80C limit
Total 80C + NPS = ₹2,00,000 deduction possible

6. **Education loan interest (80E)**
Full interest amount deductible (no upper limit)
Available for 8 years from when you start repaying

7. **Optimize salary structure**
Ask HR to restructure: more HRA, food coupons, LTA, reimbursements
These reduce taxable salary legally

8. **Choose the right regime**
Calculate tax under both old and new regime every year

9. **Invest in tax-free instruments**
• PPF interest: completely tax-free
• ELSS gains up to ₹1.25 lakh: tax-free
• Equity held 1+ year: ₹1.25 lakh gains tax-free

10. **File ITR even if not mandatory**
You might get TDS refund. Also builds a financial record for loans.

The goal isn't to avoid tax — it's to not pay MORE than you legally owe.
                    """.trimIndent()),
                    LearningArticle(6, "Common tax mistakes", ArticleType.Article, "3 min", false, """
Many people pay more tax than necessary because of these common errors:

**Mistake 1: Not investing in 80C**
Your EPF might not fill the full ₹1.5 lakh limit. The gap is tax you're overpaying. Fill it with ELSS or PPF.

**Mistake 2: Ignoring 80D**
Health insurance isn't just for tax — it's essential protection. But the tax benefit is a bonus many people miss.

**Mistake 3: Not claiming HRA**
If you pay rent but don't submit rent receipts to HR, you lose this deduction. It can be worth ₹30,000-1,00,000+ in tax savings.

**Mistake 4: Choosing wrong tax regime**
Many people stick with the default without comparing. Spend 10 minutes on a tax calculator — it could save you thousands.

**Mistake 5: Last-minute tax planning**
Rushing to invest in March leads to bad choices (expensive insurance policies, wrong funds). Plan in April when the year starts.

**Mistake 6: Buying insurance for tax saving**
Endowment and ULIP plans are sold as "tax saving + insurance." They give poor returns (4-6%) and lock your money for years. Buy term insurance (cheap) and invest separately (better returns).

**Mistake 7: Not filing ITR**
Even if no tax is due, filing ITR helps you:
• Claim TDS refunds
• Build income proof for loans
• Carry forward losses

**Mistake 8: Ignoring Form 26AS**
This shows all TDS deducted. If employer or bank deducted extra TDS, you can claim it back by filing ITR.

**Mistake 9: Not declaring all income**
FD interest, freelance income, rental income — all are taxable. Not declaring them can lead to notices and penalties.

Fix these mistakes and you could save ₹20,000-1,00,000+ per year in taxes — legally!
                    """.trimIndent()),
                    LearningArticle(7, "Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                7, "Fixed Deposits & Debt Instruments", "Safe and steady investment options.", Icons.Rounded.Shield, 0f, ModuleStatus.Locked, Color(0xFF795548), LearningLevel.Intermediate,
                articles = listOf(
                    LearningArticle(1, "FD vs Bonds", ArticleType.Article, "4 min", false, """
Both FDs and bonds are fixed-income investments, but they work differently. Here's a clear comparison:

**Fixed Deposit (FD):**
• Issued by: Banks and NBFCs
• Returns: 5-7.5% (varies by bank and tenure)
• Safety: Very safe (insured up to ₹5 lakh by DICGC)
• Liquidity: Can break early (with penalty)
• Minimum: ₹1,000-10,000
• Taxation: Interest fully taxable at your income tax slab rate

**Bonds:**
• Issued by: Government or companies
• Returns: 6-10% (depends on issuer and risk)
• Safety: Government bonds = very safe; Corporate bonds = varies
• Liquidity: Can sell on exchange (but may get less than face value)
• Minimum: ₹1,000-10,000
• Taxation: Interest taxable; some government bonds are tax-free

**Types of bonds:**
• **Government bonds (G-Secs):** Safest, 7-7.5% returns
• **Tax-free bonds:** Interest is completely tax-free (great for high earners)
• **Corporate bonds:** Higher returns (8-10%) but higher risk
• **RBI Floating Rate Bonds:** 8.05% (changes every 6 months)

**When to choose FD:**
• You want simplicity and guaranteed returns
• Short to medium term (6 months to 5 years)
• You value easy access to your money

**When to choose bonds:**
• You want higher returns than FD
• You're in a high tax bracket (tax-free bonds)
• You're comfortable with slightly less liquidity
• Long-term investment (5-10 years)

**Smart approach:** Use FDs for short-term needs and bonds for longer-term fixed-income allocation.
                    """.trimIndent()),
                    LearningArticle(2, "How FD interest is taxed", ArticleType.Article, "3 min", false, """
FD interest is fully taxable. Many people don't realize this and get surprised when their actual returns are lower than expected.

**How FD interest is taxed:**
FD interest is added to your total income and taxed at your income tax slab rate.

**Example:**
FD amount: ₹5,00,000 at 7% = ₹35,000 interest/year
Your tax bracket: 30%
Tax on FD interest: ₹35,000 × 30% = ₹10,500
**Actual return after tax: ₹24,500 (effective rate: ~4.9%)**

**TDS on FD:**
• If FD interest exceeds ₹40,000/year (₹50,000 for senior citizens), bank deducts 10% TDS
• If you don't provide PAN, TDS is 20%
• TDS is just advance tax — final tax depends on your slab

**How to handle FD tax:**
1. **Submit Form 15G/15H**
If your total income is below taxable limit, submit Form 15G (below 60) or 15H (above 60) to the bank. No TDS will be deducted.

2. **Spread FDs across banks**
₹40,000 limit is per bank. Having FDs in multiple banks can help avoid TDS (but you still owe tax on total interest).

3. **Consider cumulative FDs**
Interest is reinvested and paid at maturity. TDS applies yearly on accrued interest, but you don't receive cash — can create cash flow issues.

**Tax-efficient alternatives:**
• Debt mutual funds (taxed only when you sell, not yearly)
• PPF (interest is completely tax-free)
• Tax-free bonds (interest is tax-free)

**Key takeaway:** Always calculate your post-tax return on FDs. A 7% FD in the 30% bracket gives you only ~4.9% after tax — barely beating inflation.
                    """.trimIndent()),
                    LearningArticle(3, "Laddering strategy in FD", ArticleType.Article, "4 min", false, """
FD laddering is a smart strategy where you split your money across multiple FDs with different maturity dates instead of putting everything in one FD.

**The problem with one big FD:**
If you put ₹5,00,000 in a single 5-year FD and need ₹1,00,000 after 2 years, you have to break the entire FD and lose interest on the full amount.

**How laddering works:**
Instead of 1 FD of ₹5,00,000, create:
• FD 1: ₹1,00,000 for 1 year
• FD 2: ₹1,00,000 for 2 years
• FD 3: ₹1,00,000 for 3 years
• FD 4: ₹1,00,000 for 4 years
• FD 5: ₹1,00,000 for 5 years

**Benefits:**
• **Liquidity:** Every year, one FD matures. You can use it or renew it.
• **Better rates:** Longer FDs usually get higher interest rates.
• **Flexibility:** If rates increase, you can reinvest maturing FDs at new higher rates.
• **Partial access:** Need money? Only break the smallest/nearest FD.

**When FD 1 matures after 1 year:**
Reinvest it as a new 5-year FD. Now you have FDs maturing every year, all earning the higher 5-year rate.

**Advanced laddering:**
• Monthly ladder: 12 FDs maturing each month (for retirees needing monthly income)
• Quarterly ladder: 4 FDs maturing each quarter

**Who should use laddering?**
• Retirees who need regular income
• Anyone with a large lump sum
• People who want FD safety but also need some liquidity

It's a simple strategy that gives you the best of both worlds — high FD rates AND access to your money.
                    """.trimIndent()),
                    LearningArticle(4, "Safe investment strategies", ArticleType.Article, "5 min", false, """
Not everyone wants to take stock market risk. Here are safe investment strategies that protect your money while earning reasonable returns.

1. **FD Ladder (as discussed)**
Split money across FDs of different tenures. Safe, predictable, liquid.

2. **PPF (Public Provident Fund)**
• Government-backed, currently 7.1%
• Interest is completely tax-free
• 15-year lock-in (but partial withdrawal after 7 years)
• Max ₹1.5 lakh/year
• Best for: Long-term safe savings with tax benefits

3. **RBI Floating Rate Bonds**
• Currently 8.05% (reset every 6 months)
• 7-year tenure
• Government-guaranteed
• Best for: Higher returns than FD with government safety

4. **Debt Mutual Funds**
• Invest in bonds and government securities
• Returns: 6-8%
• More tax-efficient than FDs (taxed only on withdrawal)
• Best for: Better-than-FD returns with moderate safety

5. **Post Office Schemes**
• Senior Citizen Savings Scheme (SCSS): 8.2%
• National Savings Certificate (NSC): 7.7%
• Government-backed
• Best for: Senior citizens and very conservative investors

**The safe portfolio approach:**
• 30% in PPF (tax-free, long-term)
• 30% in FD ladder (liquidity)
• 20% in RBI bonds (higher returns)
• 20% in debt mutual funds (tax efficiency)

**Key principle:** Safe doesn't mean zero returns. A well-planned safe portfolio can earn 7-8% — much better than a savings account at 3.5%.
                    """.trimIndent()),
                    LearningArticle(5, "When FD is better than equity", ArticleType.Article, "4 min", false, """
Equity (stocks/mutual funds) usually gives better long-term returns. But there are specific situations where FDs are the smarter choice.

**FD is better when:**
1. **Short time horizon (< 3 years)**
Stock markets can drop 20-30% in a year. If you need money in 1-2 years, FD protects you from this risk.

2. **You have a specific goal with a fixed date**
Wedding in 18 months? Car down payment in 1 year? FD guarantees the exact amount you'll have.

3. **You're retired and need income**
Monthly interest from FDs provides predictable income. Stock dividends are unpredictable.

4. **Emergency fund**
Your 3-6 month emergency fund should be in FD or savings account — never in equity.

5. **Market is extremely overvalued**
When stock markets are at all-time highs and valuations are stretched, parking some money in FDs while waiting for better entry points makes sense.

6. **You can't handle volatility**
If a 20% market drop will make you panic-sell, FDs are better for your mental health AND your returns (panic selling locks in losses).

**Equity is better when:**
• Time horizon is 5+ years
• You can handle short-term drops
• You want to beat inflation significantly
• You're building long-term wealth

**The balanced approach:**
Don't choose one over the other. Use both:
• FDs for short-term goals and safety
• Equity for long-term wealth building
• Adjust the ratio based on your age and goals

**Rule of thumb:** Keep (100 minus your age)% in equity, rest in FD/debt. A 30-year-old: 70% equity, 30% FD/debt.
                    """.trimIndent()),
                    LearningArticle(6, "Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                8, "SIP & Mutual Fund Investing", "Harness the power of regular investing.", Icons.Rounded.TrendingUp, 0f, ModuleStatus.Locked, Color(0xFFE91E63), LearningLevel.Intermediate,
                articles = listOf(
                    LearningArticle(1, "How SIP works (with example)", ArticleType.Article, "5 min", false, """
SIP (Systematic Investment Plan) lets you invest a fixed amount every month into a mutual fund. Let's see exactly how it works with a real example.

**Step-by-step example:**
You start a ₹5,000/month SIP in an equity mutual fund.

**Month 1:** NAV (price per unit) = ₹50
₹5,000 ÷ ₹50 = 100 units bought

**Month 2:** Market drops, NAV = ₹40
₹5,000 ÷ ₹40 = 125 units bought (more units because price is lower!)

**Month 3:** Market recovers, NAV = ₹55
₹5,000 ÷ ₹55 = 90.9 units bought

**After 3 months:**
Total invested: ₹15,000
Total units: 315.9
Average cost per unit: ₹15,000 ÷ 315.9 = ₹47.49
Current value: 315.9 × ₹55 = ₹17,375
**Profit: ₹2,375 (15.8% return in 3 months!)**

**The magic:** When market dropped in Month 2, you bought MORE units at a cheaper price. This brought your average cost down. When market recovered, ALL your units gained value.

This is called **Rupee Cost Averaging** — you automatically buy more when cheap and less when expensive, without needing to time the market.

**SIP works best when:**
• You invest for 5+ years (longer = better)
• Markets go up AND down (volatility helps SIP!)
• You never stop during market crashes (that's when you get the best deals)

**How to start:** Choose any investment app, select a fund, set amount and date. Your bank auto-debits every month. It's that simple.
                    """.trimIndent()),
                    LearningArticle(2, "Power of compounding", ArticleType.Article, "4 min", false, """
Compounding is when your returns earn returns. It's the most powerful concept in investing — Einstein reportedly called it the "eighth wonder of the world."

**Simple example:**
You invest ₹1,00,000 at 12% per year.

**Without compounding (simple interest):**
Year 1: ₹12,000 return → Total: ₹1,12,000
Year 2: ₹12,000 return → Total: ₹1,24,000
Year 10: Total: ₹2,20,000

**With compounding:**
Year 1: ₹12,000 return → Total: ₹1,12,000
Year 2: ₹13,440 return (12% on ₹1,12,000) → Total: ₹1,25,440
Year 10: Total: ₹3,10,585
**Difference: ₹90,585 more — just from returns earning returns!**

**The real magic happens over long periods:**
₹5,000/month SIP at 12%:
• 10 years: ₹11.6 lakh (invested ₹6 lakh)
• 20 years: ₹49.9 lakh (invested ₹12 lakh)
• 30 years: ₹1.76 crore (invested ₹18 lakh)

Notice how the growth accelerates? In the last 10 years, you gained ₹1.26 crore — more than the first 20 years combined!

**Three factors that boost compounding:**
1. **Start early** — even 5 years earlier makes a huge difference
2. **Stay invested** — don't withdraw; let returns compound
3. **Higher returns** — even 2% more per year compounds dramatically

**The cost of waiting:**
Starting at 25 with ₹5,000/month = ₹1.76 crore by 55
Starting at 35 with ₹5,000/month = ₹49.9 lakh by 55

Same monthly amount, but starting 10 years earlier gives you **₹1.26 crore MORE**. Time is your biggest asset.
                    """.trimIndent()),
                    LearningArticle(3, "SIP vs Lumpsum", ArticleType.Article, "4 min", false, """
SIP and lumpsum are two ways to invest. Each has advantages depending on your situation.

**SIP (Systematic Investment Plan):**
• Invest a fixed amount every month
• Works great in volatile markets (rupee cost averaging)
• No need to time the market
• Builds discipline
• Best for: Salaried people investing from monthly income

**Lumpsum:**
• Invest a large amount at once
• Works great when market is low
• Higher risk if market drops right after investing
• Best for: When you receive a bonus, inheritance, or have idle savings

**Which gives better returns?**
It depends on market conditions:
• **Rising market:** Lumpsum wins (you bought everything at a lower price)
• **Falling market:** SIP wins (you keep buying at lower prices)
• **Volatile market:** SIP wins (averaging effect)

Historical data shows: Over long periods (10+ years), lumpsum slightly outperforms SIP because markets generally trend upward. But the difference is small.

**The practical answer:**
• Have a lump sum? Invest 50% now, rest via SIP over 3-6 months
• Have monthly income? SIP is the natural choice
• Got a bonus? Lumpsum into a good fund
• Scared of market crash? SIP gives peace of mind

**Most important rule:** Don't let the SIP vs lumpsum debate stop you from investing. Either method is infinitely better than keeping money in a savings account. Just start.
                    """.trimIndent()),
                    LearningArticle(4, "Equity vs Debt Mutual Funds", ArticleType.Article, "5 min", false, """
Mutual funds invest in different types of assets. The two main categories are equity and debt. Understanding the difference helps you choose the right fund.

**Equity Mutual Funds:**
• Invest in: Stocks of companies
• Returns: 10-15% average (long-term)
• Risk: High (can drop 20-30% in a bad year)
• Best for: Long-term goals (5+ years)
• Tax: 12.5% on gains above ₹1.25 lakh (held 1+ year)

**Types of equity funds:**
• Large-cap: Big, stable companies (lower risk)
• Mid-cap: Medium companies (moderate risk, higher growth)
• Small-cap: Small companies (highest risk, highest potential)
• Index funds: Copy Nifty 50 or Sensex (low cost, good returns)
• ELSS: Tax-saving equity fund (80C benefit)

**Debt Mutual Funds:**
• Invest in: Bonds, government securities, FDs
• Returns: 6-8% average
• Risk: Low to moderate
• Best for: Short to medium-term goals (1-3 years)
• Tax: Taxed at your income tax slab rate

**Types of debt funds:**
• Liquid funds: Very short-term (like savings account but better)
• Short-duration: 1-3 year horizon
• Corporate bond: Higher returns, slightly more risk
• Gilt funds: Government bonds (very safe)

**How to choose:**
• Goal in 1-2 years → Debt fund
• Goal in 3-5 years → Hybrid fund (mix of equity + debt)
• Goal in 5+ years → Equity fund
• Emergency fund parking → Liquid fund

**Simple starter portfolio:**
• 1 index fund (Nifty 50) for equity
• 1 short-duration debt fund for stability
• Adjust ratio based on your risk comfort
                    """.trimIndent()),
                    LearningArticle(5, "How to select a mutual fund", ArticleType.Article, "6 min", false, """
With thousands of mutual funds available, choosing can feel overwhelming. Here's a simple framework:

**Step 1: Define your goal and timeline**
• < 1 year → Liquid/ultra-short fund
• 1-3 years → Short-duration debt fund
• 3-5 years → Hybrid/balanced fund
• 5+ years → Equity fund

**Step 2: Choose the category**
For equity (5+ years):
• Beginners → Index fund (Nifty 50 or Nifty Next 50)
• Moderate risk → Large-cap or flexi-cap fund
• Higher risk → Mid-cap fund

**Step 3: Check these 5 things**
1. **Past performance (5+ years)**
Compare with category average. Consistent top-quartile performance is better than one great year.

2. **Expense ratio**
This is the annual fee. Lower is better.
• Index funds: 0.1-0.5%
• Active funds: 0.5-2.5%
Every 1% in fees reduces your final corpus significantly over 20 years.

3. **Fund manager track record**
How long has the manager been running this fund? Consistency matters.

4. **Fund size (AUM)**
Too small (< ₹500 crore) = risky. Too large (> ₹50,000 crore for mid-cap) = hard to generate high returns. Sweet spot varies by category.

5. **Consistency**
A fund that gives 12-14% every year is better than one that gives 30% one year and -10% the next.

**Beginner-friendly picks (categories, not specific funds):**
• Nifty 50 Index Fund (simplest, cheapest)
• Large-cap fund (stable, proven companies)
• Flexi-cap fund (manager picks across all sizes)

Start with **ONE** fund. You can add more later as you learn. Don't over-diversify — 2-3 funds is enough for most people.
                    """.trimIndent()),
                    LearningArticle(6, "Expense ratio & NAV explained", ArticleType.Article, "4 min", false, """
Two terms you'll see everywhere in mutual funds. Understanding them helps you make smarter choices.

**NAV (Net Asset Value):**
NAV is the price of one unit of a mutual fund. It's calculated daily.
**NAV = (Total value of all investments - Expenses) ÷ Total number of units**

Example:
Fund has ₹100 crore in stocks, ₹1 crore in expenses, and 10 crore units.
NAV = (₹100 crore - ₹1 crore) ÷ 10 crore = ₹9.9 per unit

**Common myth: "Low NAV fund is cheaper/better"**
**WRONG!** A fund with NAV ₹10 is NOT cheaper than one with NAV ₹500. NAV just tells you the current price per unit. What matters is how much the NAV grows over time (returns).

If Fund A (NAV ₹10) grows 10% → NAV becomes ₹11
If Fund B (NAV ₹500) grows 12% → NAV becomes ₹560
**Fund B gave better returns despite higher NAV!**

**Expense Ratio:**
This is the annual fee the fund charges for managing your money. It's deducted daily from the fund's NAV.

Example:
Expense ratio: 1.5%
Your investment: ₹1,00,000
Annual fee: ₹1,500 (deducted automatically, you don't pay separately)

**Why expense ratio matters:**
• 0.5% expense ratio vs 2% over 20 years on ₹10,000/month SIP:
- 0.5% fee: Final value ₹75.4 lakh
- 2.0% fee: Final value ₹59.2 lakh
- **Difference: ₹16.2 lakh lost to fees!**

**Rules of thumb:**
• Index funds: Should be below 0.5%
• Active equity funds: Below 1.5% is good
• Debt funds: Below 1% is good

**Key takeaway:** Don't choose funds based on NAV. DO pay attention to expense ratio — it directly eats into your returns. Lower fees = more money in your pocket.
                    """.trimIndent()),
                    LearningArticle(7, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                9, "Stock Market Basics", "Introduction to equity markets.", Icons.Rounded.ShowChart, 0f, ModuleStatus.Locked, Color(0xFF424242), LearningLevel.Intermediate,
                articles = listOf(
                    LearningArticle(1, "How stock market works", ArticleType.Article, "5 min", false, """
The stock market is a place where people buy and sell shares (small pieces of ownership) of companies. Think of it like a marketplace — but instead of vegetables, you're buying pieces of businesses.

**How it works:**
1. Companies list their shares on the stock exchange (IPO)
2. Investors buy and sell these shares
3. Prices go up when more people want to buy (demand)
4. Prices go down when more people want to sell (supply)

**India has two main stock exchanges:**
• **BSE (Bombay Stock Exchange):** Oldest in Asia, established 1875
• **NSE (National Stock Exchange):** Largest by volume, established 1992

**Who participates?**
• **Retail investors:** People like you and me
• **Institutional investors:** Mutual funds, insurance companies, banks
• **FIIs (Foreign Institutional Investors):** Foreign funds investing in India

**Trading hours:**
Monday to Friday, 9:15 AM to 3:30 PM (closed on weekends and market holidays)

**How prices are decided:**
Pure supply and demand. If a company reports great profits, more people want to buy → price goes up. If there's bad news, people sell → price goes down.

**Key point:** The stock market is not gambling. It's buying ownership in real businesses. Short-term prices are unpredictable, but over 10-20 years, good companies tend to grow in value. The key is patience and knowledge.
                    """.trimIndent()),
                    LearningArticle(2, "What is NIFTY & SENSEX?", ArticleType.Article, "4 min", false, """
NIFTY and SENSEX are market indices — they track the overall performance of the stock market. Think of them as the "score" of the market.

**SENSEX (BSE):**
• Tracks 30 of the largest companies on BSE
• Started in 1986 with a base value of 100
• Companies include: Reliance, TCS, HDFC Bank, Infosys, etc.
• If SENSEX goes up, it means these 30 companies are doing well overall

**NIFTY 50 (NSE):**
• Tracks 50 of the largest companies on NSE
• Started in 1996 with a base value of 1,000
• Broader than SENSEX (50 vs 30 companies)
• Most commonly used benchmark in India

**Why do they matter?**
• They tell you if the market is going up or down overall
• Fund managers compare their performance against these indices
• Index funds directly copy these indices
• News channels report these numbers daily

**Example:**
"NIFTY closed at 22,500, up 1.2% today" means the top 50 companies collectively gained 1.2% in value today.

**Historical growth:**
• NIFTY in 2004: ~2,000
• NIFTY in 2014: ~7,500
• NIFTY in 2024: ~22,000+
• Average annual return: ~12-13%

**Important:** NIFTY/SENSEX going up doesn't mean EVERY stock went up. It's an average. Some stocks may have fallen while others rose more.

**For beginners:** If you invest in a Nifty 50 index fund, you're essentially buying all 50 companies in one shot. Simple, diversified, and historically gives ~12% returns.
                    """.trimIndent()),
                    LearningArticle(3, "Demat account explained", ArticleType.Article, "3 min", false, """
To buy and sell stocks, you need two accounts: a Demat account and a Trading account. Think of them as your stock market bank accounts.

**Demat Account (Dematerialized Account):**
• Stores your shares electronically (like a bank stores your money)
• When you buy shares, they appear in your Demat account
• When you sell, shares leave your Demat account
• Managed by depositories: CDSL or NSDL

**Trading Account:**
• Used to place buy/sell orders on the stock exchange
• Connected to your Demat account and bank account
• Money flows: Bank → Trading Account → Buy shares → Demat Account

**How to open:**
1. Choose a broker (Zerodha, Groww, Angel One, Upstox, etc.)
2. Complete KYC online (PAN, Aadhaar, bank details, photo)
3. E-sign documents
4. Account opens in 1-2 days

**Costs:**
• Account opening: Free (most brokers)
• Annual maintenance: ₹0-300/year
• Brokerage per trade: ₹0-20 per order (discount brokers)

**Types of brokers:**
• **Discount brokers** (Zerodha, Groww): Low fees, online-only, good for beginners
• **Full-service brokers** (ICICI Direct, HDFC Securities): Higher fees, research support, personal advice

**For beginners:** Start with a discount broker like Zerodha or Groww. They're cheap, easy to use, and have great learning resources. You can always switch later.

**Important:** Your shares are safe even if the broker shuts down. They're held in your Demat account with CDSL/NSDL, not with the broker.
                    """.trimIndent()),
                    LearningArticle(4, "Market cap (Large/Mid/Small cap)", ArticleType.Article, "4 min", false, """
Market capitalization (market cap) is the total value of all shares of a company. It tells you how "big" a company is in the stock market.

**Formula:**
**Market Cap = Share Price × Total Number of Shares**

**Example:**
Infosys share price: ₹1,500
Total shares: 420 crore
Market cap = ₹1,500 × 420 crore = ₹6,30,000 crore (~₹6.3 lakh crore)

**Companies are classified by market cap:**
**Large-cap (above ₹20,000 crore):**
• Big, established companies (Reliance, TCS, HDFC Bank)
• More stable, less volatile
• Moderate returns (10-12%)
• Lower risk
• Best for: Beginners, conservative investors

**Mid-cap (₹5,000 - ₹20,000 crore):**
• Growing companies with proven track record
• More volatile than large-cap
• Higher potential returns (12-15%)
• Moderate risk
• Best for: Investors with 5+ year horizon

**Small-cap (below ₹5,000 crore):**
• Smaller, newer companies
• Very volatile — can double or halve quickly
• Highest potential returns (15-20%+)
• Highest risk
• Best for: Experienced investors with high risk tolerance

**Which should you invest in?**
• Beginners: Start with large-cap (or Nifty 50 index fund)
• After 1-2 years: Add some mid-cap exposure
• Experienced only: Small-cap (keep it under 15-20% of portfolio)

**Key insight:** Large-cap gives you sleep-at-night safety. Small-cap gives you excitement (and sometimes nightmares). A mix of all three, based on your risk comfort, is ideal.
                    """.trimIndent()),
                    LearningArticle(5, "Dividends explained", ArticleType.Article, "3 min", false, """
A dividend is a portion of a company's profit that it shares with its shareholders. It's like getting a bonus just for owning the stock.

**How dividends work:**
1. Company earns profit
2. Board decides to share some profit with shareholders
3. They announce a dividend (e.g., ₹10 per share)
4. If you own 100 shares, you get ₹1,000

**Example:**
You own 200 shares of ITC at ₹450 each
ITC declares dividend of ₹6.75 per share
You receive: 200 × ₹6.75 = ₹1,350 directly in your bank account

**Types of dividends:**
• **Interim dividend:** Paid during the financial year
• **Final dividend:** Paid after annual results
• **Special dividend:** One-time extra payment (rare)

**Dividend yield:**
This tells you what percentage return you get from dividends alone.
**Dividend Yield = (Annual Dividend per Share ÷ Share Price) × 100**

Example: ₹6.75 dividend on ₹450 share = 1.5% yield

**Good dividend stocks in India:**
Companies like ITC, Coal India, Power Grid, ONGC regularly pay high dividends (3-6% yield).

**Important points:**
• Not all companies pay dividends (growth companies reinvest profits instead)
• Dividends are taxable at your income tax slab rate
• Dividend is not guaranteed — company can reduce or skip it
• High dividend doesn't always mean good investment

**Two ways to make money from stocks:**
1. Price appreciation (buy at ₹100, sell at ₹150)
2. Dividends (regular income while holding)

Best stocks give you both — growing share price AND regular dividends.
                    """.trimIndent()),
                    LearningArticle(6, "Basic do’s and don’ts", ArticleType.Article, "5 min", false, """
The stock market can make you wealthy, but it can also hurt you if you're careless. Here are essential rules for beginners:

**DO's:**
✅ **Start with index funds or mutual funds** before picking individual stocks. Learn the basics first.
✅ **Invest only money you won't need for 5+ years.** Short-term money should stay in FD/savings.
✅ **Research before buying.** Understand what the company does, how it makes money, and if it's growing.
✅ **Diversify.** Don't put all money in one stock. Spread across 10-15 stocks or use mutual funds.
✅ **Think long-term.** The best investors hold for years, not days.
✅ **Keep learning.** Read annual reports, follow financial news, understand basics of valuation.

**DON'Ts:**
❌ **Don't invest based on tips.** Friends, WhatsApp groups, YouTube "experts" — most tips lose money.
❌ **Don't try to time the market.** Nobody consistently predicts when market will go up or down.
❌ **Don't panic sell during crashes.** Markets always recover. Selling during a crash locks in your loss.
❌ **Don't use borrowed money.** Never take loans to invest in stocks. The risk is too high.
❌ **Don't check prices every hour.** It creates anxiety and leads to emotional decisions.
❌ **Don't chase "hot" stocks.** By the time everyone is talking about a stock, it's usually too late.

**The golden rule:** Invest regularly, stay patient, ignore the noise. The stock market rewards discipline, not cleverness. Most millionaire investors are boring, patient people — not day traders.
                    """.trimIndent()),
                    LearningArticle(7, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            // Level 3: Advanced
            LearningModule(
                10, "Advanced Tax Strategy", "Optimizing your tax burden.", Icons.Rounded.Calculate, 0f, ModuleStatus.Locked, Color(0xFF00ACC1), LearningLevel.Advanced,
                articles = listOf(
                    LearningArticle(1, "Capital gains tax (Short vs Long term)", ArticleType.Article, "6 min", false, """
When you sell an investment for a profit, you pay capital gains tax. The rate depends on how long you held the investment.

**Short-Term Capital Gains (STCG):**
Selling within a short period.
• **Equity (stocks/equity mutual funds):** Held less than 1 year → 20% tax
• **Debt funds/FDs/property:** Held less than specified period → taxed at your slab rate

**Long-Term Capital Gains (LTCG):**
Selling after holding for a longer period.
• **Equity:** Held more than 1 year → 12.5% tax on gains above ₹1.25 lakh
• **Property:** Held more than 2 years → 12.5% tax (without indexation)

**Example — Equity:**
You bought stocks worth ₹5,00,000
Sold after 2 years for ₹8,00,000
Profit: ₹3,00,000
Tax-free: ₹1,25,000
Taxable: ₹1,75,000
**Tax: ₹1,75,000 × 12.5% = ₹21,875**

**Example — Short-term:**
Bought stocks for ₹2,00,000
Sold after 6 months for ₹2,80,000
Profit: ₹80,000
**Tax: ₹80,000 × 20% = ₹16,000**

**Key strategies:**
• Hold equity for more than 1 year to get lower LTCG rate
• Use the ₹1.25 lakh annual exemption — sell some profits each year
• Harvest losses to offset gains (sell losing stocks to reduce tax)

**Important:** Capital gains tax applies only when you SELL. If you hold and don't sell, no tax is due. This is why long-term holding is tax-efficient.
                    """.trimIndent()),
                    LearningArticle(2, "Tax on mutual funds", ArticleType.Article, "5 min", false, """
Mutual fund taxation depends on the type of fund and how long you hold it.

**Equity Mutual Funds (65%+ in stocks):**
• STCG (held < 1 year): 20%
• LTCG (held > 1 year): 12.5% on gains above ₹1.25 lakh/year

**Debt Mutual Funds:**
• All gains taxed at your income tax slab rate regardless of holding period
• No separate LTCG benefit anymore (changed in 2023)

**Hybrid Funds:**
• If equity component > 65%: Treated as equity fund for tax
• If equity component < 65%: Treated as debt fund for tax

**ELSS (Tax-saving mutual fund):**
• 3-year lock-in period
• Tax deduction under 80C (up to ₹1.5 lakh)
• LTCG after 3 years: 12.5% above ₹1.25 lakh

**SIP taxation — important!**
Each SIP installment is treated as a separate purchase. So if you've been doing SIP for 2 years and sell everything:
• Installments older than 1 year → LTCG (12.5%)
• Installments less than 1 year old → STCG (20%)

**Tax-saving tips for mutual funds:**
• Hold equity funds for 1+ year to get LTCG rate
• Use ₹1.25 lakh annual LTCG exemption — sell and rebuy to "reset" cost
• Choose growth option over dividend (dividends are taxed at slab rate)
• For short-term goals, debt funds may be less tax-efficient than FDs now

**Bottom line:** Equity mutual funds held long-term are among the most tax-efficient investments available.
                    """.trimIndent()),
                    LearningArticle(3, "Tax on stocks", ArticleType.Article, "4 min", false, """
Stock market gains are taxed similarly to equity mutual funds, but there are some additional things to know.

**Tax on stock profits:**
• STCG (sold within 1 year): 20%
• LTCG (sold after 1 year): 12.5% on gains above ₹1.25 lakh

**Tax on dividends:**
• Dividends are added to your income and taxed at your slab rate
• If you receive ₹50,000 in dividends and you're in 30% bracket: ₹15,600 tax

**Intraday trading (buy and sell same day):**
• Treated as business income
• Taxed at your slab rate
• You need to file ITR-3 (more complex)

**F&O trading (Futures & Options):**
• Also treated as business income
• Taxed at slab rate
• Requires audit if turnover exceeds limits

**Tax-loss harvesting:**
If you have stocks in loss, you can sell them to offset your gains.

Example:
• Stock A profit: ₹2,00,000
• Stock B loss: ₹80,000
• **Net taxable gain: ₹1,20,000 (below ₹1.25 lakh — no tax!)**

You can even rebuy Stock B after selling (but wait to avoid it being treated as the same transaction).

**STT (Securities Transaction Tax):**
• Automatically deducted when you buy/sell stocks
• 0.1% on delivery trades
• This is separate from income tax

**Key tip:** Keep records of all buy/sell transactions. Your broker provides a tax P&L statement — use it when filing ITR. Many people forget to declare stock market gains and get tax notices later.
                    """.trimIndent()),
                    LearningArticle(4, "Tax-saving investments", ArticleType.Article, "5 min", false, """
Certain investments help you save tax while also growing your money. Here are the best options:

1. **ELSS Mutual Funds (Section 80C)**
• Tax saving: Up to ₹1.5 lakh deduction
• Lock-in: 3 years (shortest among 80C options)
• Expected returns: 10-15%
• Best for: Tax saving + wealth creation

2. **PPF (Section 80C)**
• Tax saving: Up to ₹1.5 lakh deduction
• Returns: 7.1% (tax-free!)
• Lock-in: 15 years
• Triple tax benefit: Deduction + tax-free interest + tax-free maturity (EEE)

3. **NPS (Section 80CCD)**
• Extra ₹50,000 deduction above 80C limit
• Returns: 8-10% (market-linked)
• Lock-in: Until age 60
• Partial withdrawal allowed for specific purposes

4. **Tax-free Bonds**
• Interest is completely tax-free
• Returns: 5.5-6% (tax-free = effective 8-9% for 30% bracket)
• Available in secondary market
• Best for: High-income earners

5. **Sovereign Gold Bonds (SGB)**
• 2.5% annual interest (taxable)
• Capital gains tax-free if held till maturity (8 years)
• Linked to gold price
• Best for: Gold exposure with tax benefits

**Comparison for someone in 30% bracket:**
• FD at 7% → After tax: 4.9%
• PPF at 7.1% → After tax: 7.1% (tax-free!)
• ELSS at 12% → After tax: ~10.5%

**Strategy:** Use tax-saving investments as the foundation of your portfolio. You save tax AND build wealth simultaneously.
                    """.trimIndent()),
                    LearningArticle(5, "Salary structure optimization", ArticleType.Article, "6 min", false, """
Your salary has many components. Understanding and optimizing them can significantly reduce your tax.

**Typical salary structure:**
• **Basic Salary:** 40-50% of CTC
• **HRA:** 40-50% of Basic
• **Special Allowance:** Variable
• **PF contribution:** 12% of Basic
• **Other:** LTA, food coupons, medical, etc.

**How to optimize:**
1. **Increase HRA component**
Higher HRA = higher tax exemption if you pay rent. Ask HR to restructure.

2. **Food coupons/Meal card**
Up to ₹50 per meal (₹26,400/year) is tax-free. Many companies offer Sodexo/meal cards.

3. **Leave Travel Allowance (LTA)**
Domestic travel expenses are tax-exempt (twice in 4 years). Use it!

4. **Reimbursements**
Phone bills, internet, books, fuel — if part of salary, these are tax-free up to actual expenses.

5. **Lower Basic Salary**
Lower basic = lower PF deduction = more take-home pay. But also means lower PF savings and lower HRA.

6. **NPS employer contribution**
Up to 10% of basic contributed by employer to NPS is tax-free (not counted in 80C limit).

**Example of optimization:**
Before: Basic ₹6L, Special Allowance ₹4L = ₹10L CTC
After: Basic ₹5L, HRA ₹2.5L, LTA ₹50K, Food ₹26K, Reimbursements ₹1L, Special ₹74K = ₹10L CTC
**Tax saved: ₹40,000-80,000/year just by restructuring!**

When to ask HR: During joining or annual appraisal. Most companies allow salary restructuring if you request it.
                    """.trimIndent()),
                    LearningArticle(6, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                11, "Stock Analysis Basics", "Evaluating companies like a pro.", Icons.Rounded.Analytics, 0f, ModuleStatus.Locked, Color(0xFF5E35B1), LearningLevel.Advanced,
                articles = listOf(
                    LearningArticle(1, "P/E ratio explained", ArticleType.Article, "4 min", false, """
P/E ratio (Price to Earnings ratio) is the most popular way to check if a stock is expensive or cheap. It tells you how much investors are paying for each rupee of the company's profit.

**Formula:**
**P/E Ratio = Share Price ÷ Earnings Per Share (EPS)**

**Example:**
Infosys share price: ₹1,500
EPS (earnings per share): ₹60
P/E = 1,500 ÷ 60 = 25

This means investors are paying ₹25 for every ₹1 of Infosys's profit.

**What P/E tells you:**
• **Low P/E (below 15):** Stock might be undervalued (cheap) OR the company has problems
• **Moderate P/E (15-25):** Fairly valued for most companies
• **High P/E (above 25):** Stock might be overvalued (expensive) OR investors expect high future growth

**Important rules:**
• Compare P/E within the same industry (IT companies with IT, banks with banks)
• A high P/E isn't always bad — fast-growing companies deserve higher P/E
• A low P/E isn't always good — the company might be declining

**Industry averages:**
• Banking: 12-18
• IT: 20-30
• FMCG: 40-60
• Pharma: 20-35

**Example comparison:**
• Company A: P/E = 10, growing at 5%/year → Cheap but slow
• Company B: P/E = 30, growing at 25%/year → Expensive but fast-growing

Company B might actually be the better buy despite higher P/E, because its growth justifies the price.

**Pro tip:** Use P/E as one of many tools, not the only one. A stock can have a low P/E and still be a bad investment if the business is declining.
                    """.trimIndent()),
                    LearningArticle(2, "EPS & ROE basics", ArticleType.Article, "4 min", false, """
EPS and ROE are two fundamental numbers that tell you how profitable a company is.

**EPS (Earnings Per Share):**
How much profit the company makes for each share.
**Formula: EPS = Net Profit ÷ Total Number of Shares**

**Example:**
Company profit: ₹500 crore
Total shares: 50 crore
EPS = ₹500 crore ÷ 50 crore = ₹10 per share

**What to look for:**
• EPS should be growing year over year
• Compare EPS growth rate with share price growth
• Consistent EPS growth = reliable company

**EPS growth example:**
Year 1: ₹8 → Year 2: ₹10 → Year 3: ₹12 → Year 4: ₹15
This shows 20-25% annual growth — excellent!

**ROE (Return on Equity):**
How efficiently the company uses shareholders' money to generate profit.
**Formula: ROE = Net Profit ÷ Shareholders' Equity × 100**

**Example:**
Net profit: ₹100 crore
Shareholders' equity: ₹500 crore
ROE = 100 ÷ 500 × 100 = 20%

This means for every ₹100 of shareholders' money, the company generates ₹20 profit.

**What's a good ROE?**
• Below 10%: Poor
• 10-15%: Average
• 15-20%: Good
• Above 20%: Excellent

**Companies with consistently high ROE:** Asian Paints (~25%), TCS (~40%), HUL (~20%+)

**Why ROE matters:**
High ROE means the company is good at turning your investment into profit. Companies with ROE above 15% consistently are usually well-managed businesses worth investing in.

**Use both together:** Look for companies with growing EPS AND high ROE. That's the sign of a quality business.
                    """.trimIndent()),
                    LearningArticle(3, "Fundamental vs Technical analysis", ArticleType.Article, "6 min", false, """
There are two main approaches to analyzing stocks. Understanding both helps you make better investment decisions.

**Fundamental Analysis:**
Studying the company's business, finances, and growth potential.
**What you look at:**
• Revenue and profit growth
• P/E ratio, EPS, ROE
• Debt levels
• Industry position and competition
• Management quality
• Future growth prospects

**Best for:** Long-term investors (holding 3-10+ years)
**Philosophy:** "Buy good businesses at fair prices"

**Technical Analysis:**
Studying stock price charts and patterns to predict future price movements.
**What you look at:**
• Price charts and trends
• Moving averages (50-day, 200-day)
• Support and resistance levels
• Volume patterns
• Chart patterns (head & shoulders, triangles, etc.)

**Best for:** Short-term traders (days to months)
**Philosophy:** "The price chart tells you everything"

**Which is better?**
For beginners: **Fundamental analysis is far more important.** Here's why:
• You're investing for the long term
• Understanding the business matters more than chart patterns
• Warren Buffett, the greatest investor, uses fundamental analysis
• Technical analysis requires constant monitoring

**The practical approach:**
• Use fundamentals to decide WHAT to buy (good companies)
• Use basic technicals to decide WHEN to buy (not at all-time highs)
• Focus 90% on fundamentals, 10% on technicals

**Key insight:** Most successful long-term investors are fundamental analysts. Most day traders (technical analysts) actually lose money. Choose your approach wisely.
                    """.trimIndent()),
                    LearningArticle(4, "How to read company basics", ArticleType.Article, "5 min", false, """
You don't need to be a financial expert to evaluate a company. Here are the basic things to check before investing:

1. **What does the company do?**
Can you explain the business in one sentence? If you can't understand it, don't invest. "TCS provides IT services to global companies" — simple and clear.

2. **Is revenue growing?**
Check last 5 years of revenue. It should be increasing consistently.
• Good: ₹100 → ₹120 → ₹140 → ₹165 → ₹200 crore
• Bad: ₹100 → ₹90 → ₹110 → ₹85 → ₹95 crore

3. **Is profit growing?**
Revenue growth without profit growth is a red flag. Check net profit trend.

4. **How much debt does it have?**
Debt-to-Equity ratio: Below 1 is good, below 0.5 is great, above 2 is risky.
Some industries (banking, real estate) naturally have higher debt.

5. **Is the company a market leader?**
Leaders in their industry tend to stay profitable. Examples: Asian Paints (paints), Pidilite (adhesives), HDFC Bank (banking).

6. **Who runs the company?**
Good management = good results. Check if promoters hold a significant stake (shows confidence). Look for any fraud or governance issues.

7. **Is the stock fairly priced?**
Use P/E ratio. Compare with industry average and the company's own historical P/E.

**Where to find this information:**
• Company's annual report (free on their website)
• Screener.in (free financial data)
• Moneycontrol.com (news + data)
• Tijori Finance (visual financial data)

**The 5-minute check:** Revenue growing? Profit growing? Low debt? Good ROE? Fair P/E? If yes to all five, it's worth deeper research.
                    """.trimIndent()),
                    LearningArticle(5, "Long-term investing strategy", ArticleType.Article, "5 min", false, """
Long-term investing is the most reliable way to build wealth in the stock market. Here's a strategy that works:

**The core philosophy:**
Buy quality companies at reasonable prices and hold them for 5-10+ years. Let the business grow and your wealth compounds.

**Step 1: Build a watchlist**
Identify 20-30 quality companies across sectors. Look for:
• Consistent revenue and profit growth (5+ years)
• ROE above 15%
• Low or manageable debt
• Market leadership in their industry
• Good management with clean track record

**Step 2: Buy at fair prices**
Don't buy just because a company is good. Wait for a reasonable price.
• Use P/E ratio — buy when P/E is near or below historical average
• Market corrections (10-20% drops) are great buying opportunities
• Don't try to catch the absolute bottom — "good enough" price is fine

**Step 3: Hold patiently**
• Don't sell because of short-term price drops
• Sell only if the business fundamentals change (not because of market noise)
• Review your holdings once every 3-6 months, not daily

**Step 4: Add regularly**
• Invest more during market corrections
• Use SIP in mutual funds for automatic investing
• Reinvest dividends

**What to avoid:**
• Don't chase momentum or "hot tips"
• Don't over-diversify (15-20 stocks is enough)
• Don't check prices daily — it leads to emotional decisions
• Don't sell winners too early — let them compound

**Historical proof:**
₹1 lakh invested in Nifty 50 in 2004 = ~₹12 lakh in 2024 (12x in 20 years)
₹1 lakh in a quality stock like Asian Paints in 2004 = ~₹40-50 lakh (40-50x!)

**The secret:** Time in the market beats timing the market. Start early, buy quality, hold long. It's boring but it works.
                    """.trimIndent()),
                    LearningArticle(6, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                12, "Portfolio Building", "Diversify and manage your wealth.", Icons.Rounded.FolderCopy, 0f, ModuleStatus.Locked, Color(0xFFF4511E), LearningLevel.Advanced,
                articles = listOf(
                    LearningArticle(1, "Asset allocation strategy", ArticleType.Article, "5 min", false, """
Asset allocation is deciding how to divide your money across different types of investments. It's the most important investment decision you'll make.

**Main asset classes:**
• **Equity (stocks/mutual funds):** High growth, high risk
• **Debt (FDs/bonds/debt funds):** Stable, low risk
• **Gold:** Hedge against inflation and uncertainty
• **Real estate:** Long-term appreciation, rental income
• **Cash (savings account):** Emergency fund, immediate needs

**Why allocation matters:**
Your asset allocation determines 90% of your portfolio's performance. Which specific stocks or funds you pick matters much less than how much you put in equity vs debt.

**Age-based rule of thumb:**
**Equity allocation = 100 - Your Age**
• Age 25: 75% equity, 25% debt
• Age 35: 65% equity, 35% debt
• Age 45: 55% equity, 45% debt
• Age 55: 45% equity, 55% debt

**Risk-based allocation:**
**Aggressive (high risk tolerance):**
• 80% equity, 10% debt, 10% gold

**Moderate (balanced):**
• 60% equity, 25% debt, 10% gold, 5% cash

**Conservative (low risk tolerance):**
• 30% equity, 50% debt, 10% gold, 10% cash

**Example for a 30-year-old with ₹50,000/month to invest:**
• ₹30,000 → Equity mutual funds (SIP)
• ₹10,000 → PPF/debt funds
• ₹5,000 → Gold (SGB or gold ETF)
• ₹5,000 → Emergency fund top-up

**Key principle:** No single asset class is best all the time. Equity shines in bull markets, debt protects in crashes, gold rises during uncertainty. Having all three gives you stability in any market condition.
                    """.trimIndent()),
                    LearningArticle(2, "Diversification importance", ArticleType.Article, "4 min", false, """
Diversification means spreading your investments across different assets so that if one fails, others protect you. It's the only "free lunch" in investing.

**Why diversify?**
• No one can predict which investment will perform best
• Different assets perform well at different times
• Reduces the impact of any single bad investment
• Smoother returns over time

**Levels of diversification:**
1. **Across asset classes**
Don't put everything in stocks. Mix equity + debt + gold.

2. **Within equity**
Don't buy just one stock. Spread across:
• Different sectors (IT, banking, pharma, FMCG)
• Different sizes (large-cap, mid-cap, small-cap)
• Different styles (growth stocks, value stocks)

3. **Across geographies (advanced)**
Some investors also add international funds for global diversification.

**How many stocks/funds are enough?**
• Individual stocks: 12-15 across sectors
• Mutual funds: 3-5 funds (don't over-diversify)
• More than 20 stocks or 8 funds = over-diversification (hard to track, dilutes returns)

**Real example of diversification working:**
2020 COVID crash:
• Stocks fell 35%
• Gold rose 25%
• Debt funds gave 8%
A portfolio with 60% stocks + 20% debt + 20% gold fell only ~15% instead of 35%. Recovery was faster too.

**Common mistakes:**
• Buying 10 mutual funds that all invest in similar stocks (not real diversification)
• Putting everything in one sector because it's "hot"
• Over-diversifying until you can't track anything

**Simple diversified portfolio:**
1 Nifty 50 index fund + 1 mid-cap fund + 1 debt fund + 1 gold ETF = done. Four investments, fully diversified.
                    """.trimIndent()),
                    LearningArticle(3, "Risk profiling", ArticleType.Article, "4 min", false, """
Risk profiling means understanding how much investment risk you can handle — both financially and emotionally. It's the foundation of choosing the right portfolio.

**Two types of risk capacity:**
1. **Financial risk capacity (objective):**
Based on your actual financial situation.
• Age (younger = more capacity)
• Income stability (stable job vs freelance)
• Dependents (single vs family of 4)
• Existing savings and emergency fund
• Debt obligations
• Time horizon for goals

2. **Emotional risk tolerance (subjective):**
How you FEEL about risk.
• Can you sleep if your portfolio drops 20%?
• Will you panic-sell during a market crash?
• Do market fluctuations make you anxious?

**Risk profiles:**
**Conservative:**
• Can't handle more than 10% portfolio drop
• Prefers guaranteed returns
• Allocation: 20-30% equity, 60-70% debt, 10% gold

**Moderate:**
• Can handle 15-20% temporary drops
• Wants growth but with some stability
• Allocation: 50-60% equity, 30-40% debt, 10% gold

**Aggressive:**
• Can handle 30%+ drops without panic
• Focused on maximum long-term growth
• Allocation: 70-80% equity, 10-20% debt, 5-10% gold

**How to find your profile:**
Ask yourself: "If my ₹10 lakh portfolio dropped to ₹7 lakh in 3 months, would I:"
A) Sell everything → Conservative
B) Feel worried but hold → Moderate
C) Buy more at lower prices → Aggressive

**Important:** Be honest. Many people think they're aggressive until the first real crash. It's better to be slightly more conservative than to panic-sell at the worst time.
                    """.trimIndent()),
                    LearningArticle(4, "Rebalancing portfolio", ArticleType.Article, "5 min", false, """
Rebalancing means adjusting your portfolio back to your target allocation. Over time, some investments grow faster than others, throwing your balance off.

**Why rebalancing is needed:**
Example:
You start with: 60% equity (₹6L) + 40% debt (₹4L) = ₹10L
After 1 year, equity grows 20% and debt grows 7%:
• Equity: ₹7.2L (now 63%)
• Debt: ₹4.28L (now 37%)
• Total: ₹11.48L
Your portfolio has drifted from 60/40 to 63/37. Equity is now overweight.

**How to rebalance:**
Sell ₹34,000 of equity and move to debt. Now you're back to 60/40.
Or: Direct new investments into debt until the ratio is restored (no selling needed).

**When to rebalance:**
• **Time-based:** Once a year (simplest approach)
• **Threshold-based:** When any asset drifts more than 5% from target
• **Best time:** During market extremes (sell what's high, buy what's low)

**Benefits of rebalancing:**
• Forces you to "sell high, buy low" automatically
• Keeps risk level consistent with your plan
• Prevents portfolio from becoming too risky or too conservative
• Improves long-term returns by 0.5-1% per year

**Rebalancing in practice:**
• January each year: Check your allocation
• If equity is above target: Move excess to debt
• If equity is below target: Move from debt to equity
• Use new investments to rebalance (avoids selling and tax)

**Tax tip:** Rebalance using new investments rather than selling existing ones. This avoids capital gains tax.
                    """.trimIndent()),
                    LearningArticle(5, "Combining FD + SIP + Stocks", ArticleType.Article, "6 min", false, """
A well-built portfolio combines different investment types. Here's how to create one using FDs, SIPs, and stocks together.

**The three pillars:**
1. **FD/Debt (Safety net)**
• Purpose: Capital protection, short-term goals, emergency fund
• Allocation: 20-40% depending on age and risk profile
• Options: Bank FD, PPF, debt mutual funds, RBI bonds

2. **SIP in Mutual Funds (Growth engine)**
• Purpose: Long-term wealth building
• Allocation: 40-60%
• Options: Index funds, large-cap, mid-cap, flexi-cap

3. **Direct Stocks (Extra growth)**
• Purpose: Higher returns for experienced investors
• Allocation: 10-20% (optional for beginners)
• Options: Quality large-cap and mid-cap stocks

**Sample portfolios by age:**
**Age 25-30 (Aggressive growth):**
• 20% FD/PPF (emergency + safety)
• 50% SIP in equity mutual funds
• 20% Direct stocks
• 10% Gold (SGB/ETF)

**Age 35-40 (Balanced growth):**
• 30% FD/PPF/Debt funds
• 40% SIP in equity mutual funds
• 15% Direct stocks
• 10% Gold
• 5% Cash buffer

**Age 50+ (Capital preservation):**
• 45% FD/PPF/Debt funds
• 25% SIP in large-cap/hybrid funds
• 10% Direct stocks (blue-chips only)
• 10% Gold
• 10% Cash/liquid funds

**How to start if you're a beginner:**
Month 1-6: Build emergency fund (FD/savings)
Month 7-12: Start SIP in 1-2 index funds
Year 2: Add PPF for tax saving
Year 3+: Consider adding direct stocks if interested

**The key:** Start simple, stay consistent, and add complexity only as you learn more. A boring portfolio that you stick with beats a complex one you abandon.
                    """.trimIndent()),
                    LearningArticle(6, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                13, "Goal-Based Investing", "Invest with purpose.", Icons.Rounded.Flag, 0f, ModuleStatus.Locked, Color(0xFF2E7D32), LearningLevel.Advanced,
                articles = listOf(
                    LearningArticle(1, "Planning for house", ArticleType.Article, "5 min", false, """
Buying a house is the biggest financial goal for most people. Here's how to plan for it smartly.

**Step 1: Estimate the cost**
• Current price of the house you want: say ₹60 lakh
• If buying in 5 years, adjust for inflation (6-8%/year)
• ₹60 lakh today ≈ ₹80-88 lakh in 5 years

**Step 2: Calculate down payment**
• Banks finance 75-80% of property value
• You need 20-25% as down payment
• For ₹80 lakh house: Down payment = ₹16-20 lakh
• Plus registration, stamp duty, interiors: Add ₹5-8 lakh
• Total needed: ~₹25 lakh in 5 years

**Step 3: Choose the right investment**
Since timeline is 3-5 years:
• Aggressive approach: 60% equity mutual fund + 40% debt fund
• Conservative approach: 70% FD/debt fund + 30% equity
• Don't go 100% equity — a market crash near your buying date could delay plans

**Step 4: Calculate monthly investment**
To accumulate ₹25 lakh in 5 years:
• At 10% return: ~₹32,000/month SIP
• At 8% return: ~₹34,000/month SIP

**Step 5: Home loan planning**
• Keep EMI below 30-35% of monthly income
• Choose shorter tenure if possible (15-20 years vs 30 years)
• Compare interest rates across banks
• Consider prepaying when you get bonuses

**Additional tips:**
• Start saving for down payment at least 3-5 years before buying
• Don't rush into buying — renting is fine while you save
• Location matters more than the house itself
• Factor in maintenance costs, property tax, and society charges

**Rule of thumb:** Buy a house only when down payment is ready AND EMI is comfortable. Don't stretch beyond your means.
                    """.trimIndent()),
                    LearningArticle(2, "Planning for car", ArticleType.Article, "4 min", false, """
Planning to buy a car? Here's how to do it without hurting your finances.

**Should you buy or not?**
First, honestly assess if you NEED a car:
• Do you commute daily where public transport is poor?
• Does your family need it for regular use?
• Or is it just a status symbol?

A car is a depreciating asset — it loses value every year. A ₹10 lakh car is worth ₹6 lakh after 3 years and ₹3-4 lakh after 5 years.

**If you decide to buy:**
**Step 1: Set a budget**
• Car price should be less than 50% of your annual income
• Earning ₹8 lakh/year? Budget: ₹4 lakh car (not ₹12 lakh!)

**Step 2: Save for it (avoid loan if possible)**
• Timeline: 1-2 years
• Investment: FD or short-term debt fund (safe, since timeline is short)
• Monthly saving needed for ₹5 lakh car in 2 years: ~₹20,000/month

**Step 3: If taking a car loan**
• Make at least 20-30% down payment
• Keep EMI below 10% of monthly income
• Choose shortest tenure you can afford (3 years ideal)
• Car loan interest: 8-12% (expensive!)

**Total cost of ownership (often ignored):**
• Insurance: ₹15,000-40,000/year
• Fuel: ₹3,000-8,000/month
• Maintenance: ₹10,000-20,000/year
• Parking: ₹2,000-5,000/month (in cities)
• Depreciation: 15-20% per year

A ₹8 lakh car actually costs ₹12-15 lakh over 5 years!

**Smart alternatives:**
• Buy a reliable used car (2-3 years old) — saves 30-40%
• Use car subscriptions or rentals for occasional needs
• Delay buying until you have a solid emergency fund and investments running

**Bottom line:** A car should be a convenience, not a financial burden. Save first, buy within budget, and never skip investments to afford a car.
                    """.trimIndent()),
                    LearningArticle(3, "Retirement planning basics", ArticleType.Article, "6 min", false, """
Retirement might seem far away, but starting early makes a massive difference. Here's the basics of planning for it.

**Why plan early?**
• You'll need 20-30 years of expenses after retiring
• Inflation will make everything 3-4x more expensive
• No salary means your investments must fund your life
• Starting at 25 vs 35 can mean crores of difference

**Step 1: Estimate your retirement corpus**
Current monthly expenses: ₹40,000
Retirement age: 60
Life expectancy: 85 (plan for 25 years of retirement)
Inflation: 6%

If you're 30 now, ₹40,000/month today = ~₹1.8 lakh/month at age 60
You'll need approximately ₹3-4 crore corpus at retirement

**Step 2: Calculate monthly investment**
To build ₹3.5 crore by age 60:
• Starting at age 25: ~₹8,000/month (at 12% return)
• Starting at age 30: ~₹14,000/month
• Starting at age 35: ~₹25,000/month
• Starting at age 40: ~₹47,000/month

See the difference? Starting 10 years earlier cuts the required amount by more than half!

**Step 3: Choose investments**
• Age 25-40: 70-80% equity (SIP in index/equity funds)
• Age 40-50: 60% equity, 40% debt
• Age 50-60: 40% equity, 60% debt (gradually shift to safety)

**Key retirement tools:**
• EPF (automatic from salary)
• PPF (tax-free, safe)
• NPS (extra tax benefit)
• Equity mutual funds (growth)
• Health insurance (critical!)

Don't forget: Retirement planning isn't just about money. It's about having the freedom to live life on your terms. Start today, even if it's just ₹5,000/month.
                    """.trimIndent()),
                    LearningArticle(4, "Child education planning", ArticleType.Article, "5 min", false, """
Education costs are rising faster than almost anything else. Planning early is essential.

**The reality of education costs:**
• Good engineering college today: ₹8-15 lakh
• MBA from top college: ₹15-25 lakh
• Medical college: ₹20-50 lakh
• Study abroad: ₹30-80 lakh
• Education inflation: 10-12% per year!

If your child is 5 years old today:
Engineering in 13 years at 10% inflation:
₹10 lakh today = ₹34 lakh when they're 18

**Step 1: Estimate the cost**
Decide the type of education you want to fund. Be realistic but plan for the best.

**Step 2: Start early**
The earlier you start, the less you need to invest monthly.
For ₹34 lakh in 13 years:
• At 12% return: ~₹10,500/month SIP
• At 10% return: ~₹12,000/month SIP

For ₹34 lakh in 8 years (started late):
• At 12% return: ~₹22,000/month SIP

**Step 3: Choose the right investments**
• 10+ years away: 70-80% equity mutual funds
• 5-10 years away: 50% equity + 50% debt
• Less than 5 years: Mostly debt/FD (protect the corpus)

**Dedicated tools:**
• Sukanya Samriddhi Yojana (for girl child): 8.2% tax-free, 80C benefit
• PPF: Safe, tax-free
• Child-specific mutual funds: Designed for education goals
• Education loan: Available as backup (interest deductible under 80E)

**Important tips:**
• Open a separate account/folio for education goal
• Don't mix education savings with other goals
• Review and increase SIP amount every year
• Have a Plan B (education loan) in case of shortfall

**Key message:** ₹10,000/month started when your child is born can grow to ₹40-50 lakh by the time they need it. That's the power of starting early.
                    """.trimIndent()),
                    LearningArticle(5, "Inflation-adjusted goals", ArticleType.Article, "5 min", false, """
Inflation means prices rise every year. If you don't account for inflation, your savings won't be enough when you actually need them.

**How inflation affects your goals:**
Example: Retirement
You need ₹50,000/month today for expenses.
At 6% inflation, in 25 years you'll need: ₹2,15,000/month!
If you planned for ₹50,000/month, you'll run out of money.

Example: Child's education
College costs ₹10 lakh today.
At 10% education inflation, in 15 years: ₹42 lakh!
Planning for ₹10 lakh would cover less than 25% of the cost.

**How to inflation-adjust your goals:**
**Formula:**
**Future Cost = Current Cost × (1 + Inflation Rate)^Number of Years**

**Common inflation rates to use:**
• General expenses: 6-7%
• Education: 10-12%
• Healthcare: 10-15%
• Real estate: 6-8%

**Practical steps:**
1. **Always plan in future value**
Don't save for what things cost today. Calculate what they'll cost when you need them.

2. **Invest to beat inflation**
• FD at 7% with 6% inflation = 1% real return (barely grows)
• Equity at 12% with 6% inflation = 6% real return (actually grows wealth)

3. **Increase SIP annually**
If your SIP is ₹10,000/month, increase it by 10% every year.
Year 1: ₹10,000 → Year 2: ₹11,000 → Year 3: ₹12,100
This step-up SIP dramatically increases your final corpus.

4. **Review goals every 2-3 years**
Costs change. Update your estimates and adjust investments accordingly.

**The bottom line:** Inflation is a silent wealth destroyer. A goal that seems achievable today becomes impossible if you don't factor in rising costs. Always plan in future rupees, not today's rupees.
                    """.trimIndent()),
                    LearningArticle(6, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            // Level 4: Expert
            LearningModule(
                14, "Advanced Investing Strategy", "Sophisticated wealth management.", Icons.Rounded.Rocket, 0f, ModuleStatus.Locked, Color(0xFFC2185B), LearningLevel.Expert,
                articles = listOf(
                    LearningArticle(1, "Portfolio optimization concepts", ArticleType.Article, "6 min", false, """
Portfolio optimization means arranging your investments to get the best possible returns for your level of risk. It's about being smart, not just diversified.

**Key concepts:**
1. **Risk-adjusted returns**
Don't just look at returns. Look at returns relative to risk taken.
• Fund A: 15% return with 25% volatility
• Fund B: 12% return with 10% volatility
Fund B might be better because it gives good returns with much less risk.

2. **Correlation**
Choose investments that don't move together.
• Stocks and gold often move in opposite directions
• When stocks crash, gold usually rises
• This reduces overall portfolio volatility

3. **Core-Satellite approach**
• **Core (70-80%):** Low-cost index funds — stable, predictable
• **Satellite (20-30%):** Active funds or stocks — potential for extra returns
**Example:**
Core: Nifty 50 index fund (60%) + Debt fund (20%)
Satellite: Mid-cap fund (10%) + 2-3 quality stocks (10%)

4. **Minimize costs**
• Choose direct plans over regular plans (saves 0.5-1% per year)
• Prefer index funds (0.1-0.3% expense ratio) for core
• Avoid frequent buying/selling (saves tax and brokerage)

5. **Tax optimization**
• Hold equity for 1+ year (lower tax rate)
• Use ₹1.25 lakh LTCG exemption annually
• Harvest tax losses to offset gains
• Use tax-advantaged accounts (PPF, NPS, ELSS)

**The optimized portfolio checklist:**
✅ Right asset allocation for your age and goals
✅ Low-cost index funds as core
✅ Diversified across asset classes
✅ Tax-efficient structure
✅ Regular rebalancing
✅ Annual review and adjustment
                    """.trimIndent()),
                    LearningArticle(2, "Passive vs active investing", ArticleType.Article, "5 min", false, """
There are two main investing philosophies. Understanding both helps you choose the right approach.

**Passive Investing:**
Buy index funds that copy the market. Don't try to beat it.
**How it works:**
• Buy a Nifty 50 index fund
• It automatically holds all 50 top companies
• Your returns match the market (minus small fees)
• No fund manager picking stocks
**Advantages:**
• Very low fees (0.1-0.3%)
• No need to pick stocks or funds
• Historically beats most active fund managers
• Simple and stress-free
• Tax-efficient (low turnover)

**Active Investing:**
Try to beat the market by picking specific stocks or funds.
**How it works:**
• Fund manager researches and picks stocks
• Tries to buy undervalued stocks and avoid overvalued ones
• Charges higher fees for this expertise
**Advantages:**
• Potential to beat the market
• Can avoid bad stocks during crashes
• Skilled managers can add significant value

**The data says:**
Over 10+ years, 80-85% of active fund managers FAIL to beat the index after fees. This is why passive investing has become so popular globally.

**The practical approach for most people:**
• 70% in index funds (passive core)
• 30% in 1-2 good active funds (satellite)
• This gives you market returns plus a chance for extra returns

**Who should go fully passive?**
• Beginners
• People who don't want to research funds
• Anyone who believes in "keep it simple"

**Who might add active?**
• Experienced investors who can identify good fund managers
• People willing to monitor and switch underperforming funds

**Bottom line:** If in doubt, go passive. A Nifty 50 index fund is better than 80% of active funds.
                    """.trimIndent()),
                    LearningArticle(3, "Index fund strategy", ArticleType.Article, "4 min", false, """
Index funds are the simplest, cheapest, and most effective investment for most people. Here's why and how to use them.

**What is an index fund?**
A mutual fund that copies a market index exactly. A Nifty 50 index fund buys all 50 Nifty companies in the same proportion.

**Why index funds work:**
• You get returns of the entire market
• No risk of picking the wrong fund manager
• Extremely low fees (0.1-0.3% vs 1-2% for active funds)
• Transparent — you always know what you own
• Tax-efficient — low buying/selling within the fund

**Popular index funds in India:**
• **Nifty 50:** Top 50 companies (most popular)
• **Nifty Next 50:** Companies ranked 51-100 (slightly more growth)
• **Nifty Midcap 150:** Mid-sized companies (higher risk/reward)
• **Sensex:** Top 30 companies (similar to Nifty 50)

**How to build a portfolio with index funds:**
**Simple (1 fund):**
100% Nifty 50 index fund — done!

**Balanced (2 funds):**
70% Nifty 50 + 30% Nifty Next 50

**Diversified (3 funds):**
50% Nifty 50 + 25% Nifty Next 50 + 25% Nifty Midcap 150

**How to choose between similar index funds:**
1. Lowest expense ratio (most important)
2. Lowest tracking error (how closely it follows the index)
3. Larger fund size (better liquidity)

**The power of simplicity:**
₹10,000/month in Nifty 50 index fund at 12% for 25 years = ₹1.9 crore
You invested ₹30 lakh. The rest is compounding magic.

No stock picking. No fund manager risk. No stress. Just consistent investing in the market.
                    """.trimIndent()),
                    LearningArticle(4, "Market cycle understanding", ArticleType.Article, "6 min", false, """
Markets don't go up in a straight line. They move in cycles of optimism and pessimism. Understanding these cycles helps you make better decisions.

**The four phases of a market cycle:**
1. **Accumulation (Bottom)**
• Market has crashed, pessimism is high
• Smart investors start buying quietly
• News is negative, everyone is scared
• Best time to invest (but hardest emotionally)

2. **Markup (Bull market)**
• Prices start rising
• More investors join in
• Positive news, growing optimism
• Good time to stay invested

3. **Distribution (Top)**
• Market is at highs, euphoria everywhere
• "Everyone" is making money and talking about stocks
• Smart investors start selling quietly
• Risky time to invest heavily

4. **Markdown (Bear market)**
• Prices fall sharply
• Panic selling, negative news everywhere
• People swear off stocks forever
• Cycle repeats — this becomes the next accumulation phase

**How to use this knowledge:**
Don't try to time perfectly. Nobody can consistently predict tops and bottoms.
Instead:
• Continue SIP through all phases (automatic buying)
• Invest extra during crashes (accumulation phase)
• Be cautious during euphoria (distribution phase)
• Never panic sell during markdown

**Signs of market euphoria (be cautious):**
• Your auto driver gives stock tips
• Everyone on social media is showing profits
• New investors are making "easy money"
• IPOs are oversubscribed 50-100x

**Signs of market fear (opportunity):**
• News channels predict doom
• People are selling everything
• Nobody wants to talk about stocks
• Mutual fund SIP cancellations increase

**Warren Buffett's advice:** "Be fearful when others are greedy, and greedy when others are fearful." This one sentence captures the entire strategy.
                    """.trimIndent()),
                    LearningArticle(5, "Long-term wealth formula", ArticleType.Article, "5 min", false, """
Building long-term wealth isn't complicated. It follows a simple formula that anyone can apply.

**The Wealth Formula:**
**Wealth = Income × Savings Rate × Returns × Time**

Each factor matters:
1. **Income (earn more)**
• Invest in your skills and career
• Multiple income sources help
• But income alone doesn't build wealth — saving and investing does

2. **Savings Rate (keep more)**
• Save at least 20-30% of income
• Increase savings rate with every raise
• Avoid lifestyle inflation
• Automate savings on salary day

3. **Returns (grow faster)**
• Invest in equity for long-term goals (12%+ returns)
• Don't keep large amounts in savings account (3.5%)
• Keep costs low (index funds, direct plans)
• Avoid speculation and get-rich-quick schemes

4. **Time (start early)**
• This is the most powerful factor
• ₹5,000/month from age 25 to 55 at 12% = ₹1.76 crore
• Same amount from age 35 to 55 at 12% = ₹49.9 lakh
• 10 extra years = ₹1.26 crore more!

**The complete wealth-building system:**
1. Build emergency fund (6 months expenses)
2. Get term insurance and health insurance
3. Start SIP in index funds (at least 20% of income)
4. Max out tax-saving investments (80C, 80D)
5. Increase SIP by 10% every year
6. Never stop during market crashes
7. Review and rebalance annually
8. Be patient — wealth builds slowly, then suddenly

**What NOT to do:**
• Don't chase quick returns
• Don't invest in things you don't understand
• Don't borrow to invest
• Don't check your portfolio daily
• Don't compare with others

**The truth:** Most wealthy people got there through decades of consistent saving and investing, not through one lucky bet. It's boring, but it works. Every. Single. Time.
                    """.trimIndent()),
                    LearningArticle(6, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                15, "Advanced Tax & Wealth Management", "Preserve and pass on your wealth.", Icons.Rounded.VpnKey, 0f, ModuleStatus.Locked, Color(0xFF1976D2), LearningLevel.Expert,
                articles = listOf(
                    LearningArticle(1, "Tax-efficient investing", ArticleType.Article, "6 min", false, """
Tax-efficient investing means structuring your investments to keep more of your returns by minimizing taxes — all legally.

**Key strategies:**
1. **Hold equity for 1+ year**
• Short-term: 20% tax on gains
• Long-term: 12.5% tax (and first ₹1.25 lakh is tax-free!)
• Simply holding longer saves you 7.5% in tax

2. **Use the ₹1.25 lakh LTCG exemption**
Every year, you can book ₹1.25 lakh in long-term equity gains tax-free.
**Strategy:** Sell and rebuy to "reset" your purchase price. This is called tax harvesting.

3. **Choose growth over dividend**
• Dividends are taxed at your slab rate (up to 30%+)
• Growth option: No tax until you sell
• Growth is almost always more tax-efficient

4. **Use tax-free instruments**
• PPF: Interest completely tax-free
• EPF: Tax-free if held 5+ years
• Sovereign Gold Bonds: Capital gains tax-free at maturity
• ELSS: Tax deduction + relatively low LTCG

5. **Debt fund vs FD**
Both are now taxed at slab rate, but:
• FD: TDS deducted yearly (even if you don't withdraw)
• Debt fund: Tax only when you sell (you control timing)

6. **Direct plans over regular plans**
Direct plans have 0.5-1% lower expense ratio. Over 20 years, this saves lakhs.

7. **Harvest tax losses**
Sell investments that are in loss to offset your gains. Net gain is lower = less tax.

**Example of tax-efficient portfolio:**
• PPF: ₹1.5 lakh/year (tax-free returns + 80C deduction)
• ELSS: ₹50,000/year (80C deduction + equity growth)
• Index fund SIP: ₹10,000/month (hold long-term for LTCG benefit)
• NPS: ₹50,000/year (extra deduction under 80CCD)

**Result:** You save tax on investment AND pay less tax on returns. Double benefit!
                    """.trimIndent()),
                    LearningArticle(2, "Estate planning basics", ArticleType.Article, "5 min", false, """
Estate planning ensures your assets go to the right people after you're gone. It's not just for the wealthy — everyone with any assets should plan.

**Why estate planning matters:**
• Without a plan, legal disputes can tie up your assets for years
• Your family may not even know about all your investments
• Government rules decide distribution if you don't (may not match your wishes)
• Reduces stress for your family during an already difficult time

**Key components:**
1. **Will**
A legal document stating who gets what after your death.
• Can be handwritten or typed
• Must be signed by you and 2 witnesses
• Should be registered (not mandatory but recommended)
• Can be changed anytime during your lifetime

2. **Nomination**
Naming a person to receive specific assets.
• Bank accounts: Nominate online or at branch
• Mutual funds: Nominate through AMC or app
• Insurance: Nominee receives the payout
• Stocks/Demat: Nominate through broker
**Important:** Nominee is NOT the same as legal heir. Nominee is a custodian — legal heirs (as per will or succession law) are the actual owners.

3. **Asset inventory**
Create a list of ALL your assets:
• Bank accounts (with account numbers)
• FDs, PPF, NPS
• Mutual funds and stocks
• Insurance policies
• Property documents
• Loans and liabilities
• Digital assets (crypto, online accounts)

Share this list with your spouse or trusted family member.

4. **Insurance**
• **Term insurance:** Covers your family's financial needs if you pass away
• **Health insurance:** Prevents medical bills from draining family savings

**When to start:** Now. Even if you're young. Accidents don't check your age. A simple will and updated nominations take just 1-2 hours but save your family enormous pain.
                    """.trimIndent()),
                    LearningArticle(3, "Nomination & will basics", ArticleType.Article, "5 min", false, """
Nomination and will are two different things. Understanding both is crucial for protecting your family's financial future.

**Nomination:**
• Names a person to receive a specific asset (bank account, mutual fund, insurance)
• Nominee acts as a TRUSTEE — holds the asset until legal heirs claim it
• Easy to set up (online for most investments)
• Can be changed anytime

**Will:**
• Legal document that distributes ALL your assets
• Overrides nomination in case of conflict
• Specifies exact shares for each person
• Can include conditions (e.g., "property to wife, but she can't sell it")

**Why you need BOTH:**
• Nomination ensures quick access to funds (family doesn't wait for legal process)
• Will ensures final distribution matches your wishes

**How to write a simple will:**
1. List all assets and their current value
2. Decide who gets what
3. Name an executor (person who carries out the will)
4. Write it clearly (no legal jargon needed)
5. Sign with 2 witnesses present
6. Register at sub-registrar office (recommended)

**Common mistakes:**
• Not updating nominee after marriage/children
• Having different nominees and will beneficiaries (creates confusion)
• Not telling family where the will is kept
• Not listing all assets (some get forgotten)

**Digital assets checklist:**
• Email account access
• Investment app passwords (or recovery info)
• Crypto wallet keys
• Online banking credentials
• Social media accounts

**Pro tip:** Use a password manager and share the master password with your spouse. Keep a physical copy of important documents in a safe place that your family knows about.
                    """.trimIndent()),
                    LearningArticle(4, "High-income tax strategy", ArticleType.Article, "6 min", false, """
If you earn well (₹15 lakh+ per year), you're in the highest tax brackets. Here are advanced strategies to optimize your taxes.

**The challenge:**
At 30% tax bracket + 4% cess, you pay 31.2% on every additional rupee earned. On ₹20 lakh income, you might pay ₹3-4 lakh in tax.

**Strategy 1: Maximize all deductions (Old Regime)**
• 80C: ₹1,50,000 (EPF + PPF + ELSS)
• 80CCD: ₹50,000 (NPS — above 80C limit)
• 80D: ₹75,000 (health insurance for self + parents)
• HRA: ₹1,00,000-2,00,000 (if paying rent)
• Home loan interest: ₹2,00,000 (Section 24)
• **Total possible deductions: ₹5-6 lakh!**

**Strategy 2: Salary restructuring**
Work with HR to optimize:
• Higher HRA (if you pay rent)
• Food coupons: ₹26,400/year tax-free
• LTA: Domestic travel exemption
• Reimbursements: Phone, internet, fuel
• NPS employer contribution: 10% of basic (tax-free)

**Strategy 3: Invest in tax-efficient instruments**
• PPF: Tax-free returns (EEE benefit)
• ELSS: 80C deduction + equity growth
• NPS: Extra deduction + partial tax-free withdrawal
• Sovereign Gold Bonds: Tax-free capital gains at maturity

**Strategy 4: Capital gains management**
• Hold investments long-term (lower tax rates)
• Use ₹1.25 lakh LTCG exemption annually
• Harvest losses to offset gains
• Time your selling across financial years

**Strategy 5: Family tax planning**
• Gift money to spouse/parents for investing (their lower tax bracket)
• Pay rent to parents (claim HRA, they may have no tax)
• Invest in children's name for education

**The goal:** Legally reduce your effective tax rate from 30%+ to 15-20% through smart planning. The savings can be ₹1-3 lakh per year — money that compounds into serious wealth over time.
                    """.trimIndent()),
                    LearningArticle(5, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            ),
            LearningModule(
                16, "Behavioral Finance", "Master the psychology of money.", Icons.Rounded.Psychology, 0f, ModuleStatus.Locked, Color(0xFFFFA000), LearningLevel.Expert,
                articles = listOf(
                    LearningArticle(1, "Emotional investing mistakes", ArticleType.Article, "5 min", false, """
The biggest threat to your investments isn't the market — it's your own emotions. Here are the most common emotional mistakes investors make.

1. **Panic selling**
Market drops 20% → You sell everything → Market recovers → You missed the recovery.
This is the #1 wealth destroyer. Every major crash has been followed by recovery and new highs.

2. **Greed buying**
Market is at all-time high → Everyone is making money → You invest your entire savings → Market corrects → You're stuck at high prices.

3. **Herd mentality**
"Everyone is buying this stock/crypto/IPO, so it must be good."
By the time everyone is buying, it's usually too late. The smart money already bought earlier.

4. **Anchoring**
"I bought this stock at ₹500, I'll sell only when it goes back to ₹500."
The stock doesn't care what price you bought it at. If the business is bad, it may never reach ₹500 again.

5. **Loss aversion**
Losing ₹10,000 feels twice as painful as gaining ₹10,000 feels good. This makes people hold losing stocks too long (hoping to break even) and sell winning stocks too early (locking in small gains).

6. **Recency bias**
If markets went up last year, you assume they'll go up this year too. If they crashed recently, you assume they'll keep crashing.

**How to protect yourself:**
• Automate investments (SIP removes emotion)
• Write down your investment plan and follow it
• Don't check portfolio more than once a month
• Remember: Volatility is normal, not dangerous
• Have a long-term perspective (5-10+ years)

The best investors aren't the smartest — they're the most emotionally disciplined.
                    """.trimIndent()),
                    LearningArticle(2, "Fear & greed cycle", ArticleType.Article, "5 min", false, """
Markets swing between two extremes: fear and greed. Understanding this cycle helps you avoid buying high and selling low.

**The cycle:**
**Phase 1: Optimism**
Market starts rising. People feel good. "This time it's different."

**Phase 2: Excitement**
Returns are great. More people invest. Media coverage increases.

**Phase 3: Euphoria (GREED PEAK)**
"Everyone" is making money. Your neighbor, auto driver, and cousin are giving stock tips. New investors pour in. Prices are sky-high.
→ This is the WORST time to invest heavily. But it FEELS like the best time.

**Phase 4: Anxiety**
Market starts falling. "It's just a correction, it'll bounce back."

**Phase 5: Denial**
Market falls more. "I'll hold, it'll recover."

**Phase 6: Panic (FEAR PEAK)**
Market crashes 30-40%. News channels predict doom. People sell everything at huge losses. "I'm never investing again."
→ This is the BEST time to invest. But it FEELS like the worst time.

**Phase 7: Depression**
Market is at bottom. Nobody wants to talk about investing. SIPs are cancelled.

**Phase 8: Hope**
Market slowly starts recovering. Smart investors who bought during panic see gains.

Then the cycle repeats.

**How to break the cycle:**
• Never invest a lump sum during euphoria
• Never sell during panic
• Continue SIP through ALL phases
• Keep 6-12 months expenses in cash (so you never NEED to sell during crashes)
• Remember: Every crash in history has been followed by recovery

**Historical proof:**
• **2008 crash:** Nifty fell from 6,300 to 2,500. Recovered to 6,300 by 2010.
• **2020 crash:** Nifty fell from 12,400 to 7,500. Recovered to 12,400 in 5 months.
• Those who stayed invested (or bought more) made excellent returns.
                    """.trimIndent()),
                    LearningArticle(3, "Overconfidence bias", ArticleType.Article, "4 min", false, """
Overconfidence bias is when you believe you're better at investing than you actually are. It's one of the most dangerous biases because it leads to excessive risk-taking.

**How overconfidence shows up:**
1. **"I can pick winning stocks"**
After a few lucky picks, you think you have a special talent. Reality: In a bull market, almost everything goes up. Your "skill" might just be luck.

2. **"I can time the market"**
You think you know when to buy and sell. Reality: Even professional fund managers can't consistently time the market. Studies show that missing just the 10 best days in a decade can halve your returns.

3. **"This time I'm sure"**
You concentrate your portfolio in one stock or sector because you're "confident." Reality: Concentration increases risk dramatically. Even great companies can have bad years.

4. **"I don't need to diversify"**
You think your picks are so good that diversification is unnecessary. Reality: Even Warren Buffett diversifies. No one is right 100% of the time.

**The data on overconfidence:**
• Overconfident investors trade 45% more than average
• More trading = more costs = lower returns
• Men tend to be more overconfident than women in investing
• Overconfident investors underperform by 2-3% per year

**How to fight overconfidence:**
• Keep a trading journal — write down WHY you buy/sell
• Review your past decisions honestly (including losses)
• Compare your returns against a simple index fund
• Remember: If beating the market were easy, everyone would be rich
• Use index funds for your core portfolio (humility in action)

**The paradox:** The best investors are often the most humble. They know what they don't know, and they build portfolios that don't depend on being right all the time.
                    """.trimIndent()),
                    LearningArticle(4, "How to stay disciplined", ArticleType.Article, "6 min", false, """
Staying disciplined is the hardest part of investing. Here are practical strategies to maintain discipline through market ups and downs.

1. **Automate everything**
• Set up SIP auto-debit on salary day
• Auto-transfer to emergency fund
• Auto-pay insurance premiums
• When investing is automatic, emotions can't interfere

2. **Write an Investment Policy Statement (IPS)**
A simple document that states:
• Your goals and timeline
• Your asset allocation (e.g., 60% equity, 30% debt, 10% gold)
• When you'll rebalance (annually)
• Rules for buying more (e.g., "invest extra if market drops 15%+")
• Rules for selling (e.g., "only sell if fundamentals change, never because of price drop")

When emotions hit, read your IPS. It's your rational self guiding your emotional self.

3. **Limit information consumption**
• Don't watch business news channels daily
• Unfollow "stock tip" accounts on social media
• Check your portfolio once a month, not daily
• News creates urgency. Investing requires patience.

4. **Focus on process, not outcomes**
• Bad month doesn't mean bad strategy
• Good month doesn't mean you're a genius
• Judge your process: "Am I following my plan?" not "Am I making money this month?"

5. **Find an accountability partner**
• A spouse, friend, or financial advisor who keeps you honest
• Someone to talk you out of panic selling or greed buying

6. **Remember the big picture**
• Market drops are temporary. Your goals are long-term.
• In 20 years, today's "crash" will be a tiny blip on the chart
• Every successful investor has sat through multiple crashes

7. **Celebrate milestones**
• First ₹1 lakh invested
• First ₹10 lakh portfolio
• 5 years of unbroken SIP
• These milestones reinforce good behavior

**The ultimate truth:** Investing success is 10% knowledge and 90% behavior. You already know enough to build wealth. The challenge is doing it consistently for decades. Discipline is your superpower.
                    """.trimIndent()),
                    LearningArticle(5, "Take Module Quiz", ArticleType.Quiz, "5 questions", false)
                )
            )
        )
    }
}
