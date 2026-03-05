package com.simats.moneymentor.data

object QuizRepository {
    fun getQuizByModuleId(moduleId: Int): Quiz? {
        return when (moduleId) {
            1 -> Quiz(
                questions = listOf(
                    QuizQuestion("What is the primary purpose of a budget?", listOf("To limit your happiness", "To track income and plan expenses", "To save for 50 years later", "To pay more taxes"), 1),
                    QuizQuestion("What does the '50' represent in the 50/30/20 rule?", listOf("Wants", "Savings", "Needs", "Investments"), 2),
                    QuizQuestion("Which of these is considered a 'Need'?", listOf("Netflix subscription", "Newest iPhone", "Rent/Housing", "Dining at fancy restaurants"), 2),
                    QuizQuestion("Why is an emergency fund important?", listOf("To buy a new car on impulse", "To cover unexpected expenses without debt", "To gamble in the stock market", "To show off to friends"), 1),
                    QuizQuestion("How much should a basic emergency fund ideally cover?", listOf("1 week of expenses", "1 month of expenses", "3-6 months of expenses", "10 years of expenses"), 2)
                )
            )
            2 -> Quiz(
                questions = listOf(
                    QuizQuestion("Which type of interest is calculated on both principal and accumulated interest?", listOf("Simple Interest", "Compound Interest", "Nominal Interest", "Fixed Interest"), 1),
                    QuizQuestion("The 'Rule of 72' helps you estimate:", listOf("How many stocks to buy", "When to retire", "How long it takes to double your money", "Your credit score"), 2),
                    QuizQuestion("Inflation generally means your money's purchasing power:", listOf("Increases", "Stays the same", "Decreases", "Varies with the moon"), 2),
                    QuizQuestion("What is the main advantage of starting an SIP early?", listOf("Higher monthly payments", "More time for compounding to work", "Less risk of losing money", "Exemption from all taxes"), 1),
                    QuizQuestion("If inflation is 6% and your bank gives 3% interest, your real wealth is:", listOf("Growing", "Staying the same", "Shrinking", "Doubling"), 2)
                )
            )
            3 -> Quiz(
                questions = listOf(
                    QuizQuestion("Which life insurance is purely for protection with no maturity benefit?", listOf("Endowment Plan", "ULIP", "Term Insurance", "Money Back Policy"), 2),
                    QuizQuestion("Health insurance should ideally be bought when you are:", listOf("Old and sick", "Young and healthy", "Only for children", "Not needed if you have a job"), 1),
                    QuizQuestion("In insurance, 'Premium' refers to:", listOf("The payout after an accident", "The interest earned", "The periodic payment for coverage", "The claim processing fee"), 2),
                    QuizQuestion("Critical illness cover is designed to provide:", listOf("Daily hospital cash", "Lumpsum payment upon diagnosis of serious disease", "Coverage for common cold", "Free gym membership"), 1),
                    QuizQuestion("A 'Top-up' health insurance policy is used to:", listOf("Increase basic coverage at a lower cost", "Pay for grocery bills", "Insurance for your car", "Cover only dental treatment"), 0)
                )
            )
            4 -> Quiz(
                questions = listOf(
                    QuizQuestion("What does a share represent?", listOf("A loan to a company", "Partial ownership of a company", "A promise of fixed interest", "A government bond"), 1),
                    QuizQuestion("What is a Mutual Fund?", listOf("A single stock", "A pool of money from investors managed by professionals", "A type of bank account", "An insurance policy"), 1),
                    QuizQuestion("Higher risk usually comes with the potential for:", listOf("Lower returns", "Guaranteed returns", "Higher returns", "No returns"), 2),
                    QuizQuestion("Diversification helps to:", listOf("Increase risk", "Predict the future", "Reduce overall portfolio risk", "Avoid all taxes"), 2),
                    QuizQuestion("An Index Fund primarily aims to:", listOf("Beat the market significantly", "Match the performance of a specific market index", "Invest only in small startups", "Lend money to the government"), 1)
                )
            )
            5 -> Quiz(
                questions = listOf(
                    QuizQuestion("Zero-based budgeting means:", listOf("Saving zero money", "Income minus expenses equals zero", "Having zero debt", "Spending nothing all month"), 1),
                    QuizQuestion("Lifestyle inflation is when:", listOf("Prices of groceries rise", "Spending increases as income increases", "The stock market goes up", "Inflation hits 0%"), 1),
                    QuizQuestion("Your savings rate is calculated as:", listOf("Expenses / Income", "Investments / Expenses", "Savings & Investments / Total Income", "Debt / Assets"), 2),
                    QuizQuestion("For irregular income, it's best to budget based on:", listOf("Your highest month", "Your average month", "Your lowest month", "Next month's expected bonus"), 2),
                    QuizQuestion("The 'Avalanche Method' focuses on paying off debt with:", listOf("The smallest balance first", "The highest interest rate first", "The friendliest lender first", "The oldest debt first"), 1)
                )
            )
            6 -> Quiz(
                questions = listOf(
                    QuizQuestion("The New Tax Regime is known for:", listOf("Higher rates and more deductions", "Lower rates and fewer deductions", "No tax on any income", "Only for senior citizens"), 1),
                    QuizQuestion("Section 80C allows a maximum deduction of:", listOf("₹50,000", "₹1,00,000", "₹1,50,000", "₹5,00,000"), 2),
                    QuizQuestion("Which of these is an 80C eligible investment?", listOf("Credit card bill", "ELSS Mutual Fund", "Personal Loan EMI", "Gold jewelry"), 1),
                    QuizQuestion("Section 80D is for deductions related to:", listOf("Education Loam", "Home Loan", "Health Insurance Premium", "Charity donations"), 2),
                    QuizQuestion("Standard Deduction for salaried individuals is currently (FY 24-25):", listOf("₹25,000", "₹50,000", "₹75,000", "₹1,00,000"), 2)
                )
            )
            7 -> Quiz(
                questions = listOf(
                    QuizQuestion("Fixed Deposit (FD) interest is:", listOf("Completely tax-free", "Taxable at your income slab rate", "Only taxable if above ₹10 lakh", "Exempt under 80C"), 1),
                    QuizQuestion("DICGC insures bank deposits up to:", listOf("₹1 lakh", "₹2 lakh", "₹5 lakh", "Total deposit amount"), 2),
                    QuizQuestion("FD Laddering helps to manage:", listOf("Stock market risk", "Liquidity and interest rate risk", "Tax evasion", "Inflation in real estate"), 1),
                    QuizQuestion("PPF stands for:", listOf("Private Provident Fund", "Public Provident Fund", "Personal Pension Fund", "Professional Payment Fund"), 1),
                    QuizQuestion("The tenure of RBI Floating Rate Bonds is:", listOf("3 years", "5 years", "7 years", "15 years"), 2)
                )
            )
            8 -> Quiz(
                questions = listOf(
                    QuizQuestion("SIP stands for:", listOf("Simple Investment Plan", "Systematic Investment Plan", "Stock Income Program", "Secure Interest Plan"), 1),
                    QuizQuestion("Rupee Cost Averaging means you buy:", listOf("More units when prices are high", "Fewer units when prices are low", "More units when prices are low", "Same units every month regardless of price"), 2),
                    QuizQuestion("Compounding works best over:", listOf("1 year", "3 years", "Short term", "Long term"), 3),
                    QuizQuestion("Expense ratio in mutual funds is:", listOf("One-time entry fee", "Annual management fee", "Tax on profit", "Bonus for the investor"), 1),
                    QuizQuestion("Which fund tracks big companies like Nifty 50?", listOf("Small-cap fund", "Index fund", "Sector fund", "Penny stock fund"), 1)
                )
            )
            9 -> Quiz(
                questions = listOf(
                    QuizQuestion("The Nifty 50 tracks how many companies?", listOf("30", "50", "100", "500"), 1),
                    QuizQuestion("BSE stands for:", listOf("Bharat Stock Exchange", "Bombay Stock Exchange", "Basic Stock Exchange", "Banker's Stock Exchange"), 1),
                    QuizQuestion("A Demat account is used to:", listOf("Withdraw cash", "Store shares electronically", "Place trade orders", "Calculate taxes"), 1),
                    QuizQuestion("Market Capitalization is calculated as:", listOf("Net Profit x PE", "Share Price x Total Shares", "Assets - Liabilities", "Revenue / Employees"), 1),
                    QuizQuestion("Dividends are paid from:", listOf("Company's debt", "Company's revenue", "Company's profit", "Investments from shareholders"), 2)
                )
            )
            10 -> Quiz(
                questions = listOf(
                    QuizQuestion("Long-term Capital Gains (LTCG) on equity applies after holding for:", listOf("6 months", "1 year", "2 years", "3 years"), 1),
                    QuizQuestion("LTCG tax rate on equity gains above ₹1.25L is:", listOf("5%", "10%", "12.5%", "20%"), 2),
                    QuizQuestion("Tax harvesting involves selling and rebuying to:", listOf("Avoid all taxes", "Reset the cost basis and utilize exemptions", "Cheat the government", "Lose money intentionally"), 1),
                    QuizQuestion("Gains from debt mutual funds are now taxed at:", listOf("10%", "20% with indexation", "Your income tax slab rate", "Wait 3 years for tax-free"), 2),
                    QuizQuestion("Short-term Capital Gains (STCG) on equity is currently:", listOf("10%", "15%", "20%", "30%"), 2)
                )
            )
            11 -> Quiz(
                questions = listOf(
                    QuizQuestion("P/E ratio helps compare a stock's price with its:", listOf("Revenue", "Earnings per share", "Debt", "Employee count"), 1),
                    QuizQuestion("ROE stands for:", listOf("Return on Equity", "Return on Expenses", "Rate of Earnings", "Revenue over Equity"), 0),
                    QuizQuestion("Fundamental Analysis focuses on:", listOf("Price charts", "Company's financial health and business", "Market rumors", "What celebrities buy"), 1),
                    QuizQuestion("A Debt-to-Equity ratio above 2 is generally considered:", listOf("Very safe", "Risky", "Irrelevant", "Ideal for beginners"), 1),
                    QuizQuestion("Technical Analysis primarily uses:", listOf("Balance sheets", "Historical price and volume charts", "Newspaper articles", "Company's MISSION statement"), 1)
                )
            )
            12 -> Quiz(
                questions = listOf(
                    QuizQuestion("Asset Allocation is deciding to split money across:", listOf("Different banks", "Different family members", "Different asset classes like equity/debt/gold", "Different stores"), 2),
                    QuizQuestion("The age-based rule suggests your equity % should be:", listOf("Exactly your age", "100 minus your age", "100 plus your age", "Random"), 1),
                    QuizQuestion("Rebalancing your portfolio helps to:", listOf("Maximize taxes", "Maintain your desired risk level", "Avoid investing", "Follow the herd"), 1),
                    QuizQuestion("Diversification is often called:", listOf("Concentrated risk", "The only free lunch in investing", "A way to lose money", "Optional for experts"), 1),
                    QuizQuestion("Correlation measures how two investments:", listOf("Look on a chart", "Move in relation to each other", "Cost in fees", "Pay dividends"), 1)
                )
            )
            13 -> Quiz(
                questions = listOf(
                    QuizQuestion("When planning for a goal, you must adjust the cost for:", listOf("GDP growth", "Inflation", "Weather", "Bank holidays"), 1),
                    QuizQuestion("For a goal 1-2 years away, you should invest in:", listOf("Small-cap stocks", "Long-term real estate", "Safe options like FDs or Liquid Funds", "Bitcoin"), 2),
                    QuizQuestion("The corpus for retirement must account for:", listOf("Last year's expenses only", "Expected lifespan after retirement and inflation", "Nothing, government pays everything", "Daily lottery tickets"), 1),
                    QuizQuestion("Education inflation is typically _______ than general inflation.", listOf("Lower", "The same", "Higher", "Zero"), 2),
                    QuizQuestion("A Home Loan EMI should ideally be within ______ of your income.", listOf("10%", "30-35%", "70%", "90%"), 1)
                )
            )
            14 -> Quiz(
                questions = listOf(
                    QuizQuestion("Passive investing typically involves:", listOf("Picking individual stocks", "Buying index funds", "Day trading", "Listening to news constantly"), 1),
                    QuizQuestion("Tracking Error in index funds should be:", listOf("High", "As low as possible", "Irrelevant", "Above 5%"), 1),
                    QuizQuestion("The 'Accumulation' phase of a market cycle is characterized by:", listOf("Extreme euphoria", "Peak prices", "Pessimism and low prices", "Everyone buying"), 2),
                    QuizQuestion("A 'Core-Satellite' approach suggests keeping most of your money in:", listOf("Risky startups", "Stable core investments like index funds", "Cash", "Gold only"), 1),
                    QuizQuestion("Active fund managers charge ____ fees compared to index funds.", listOf("Lower", "Same", "Higher", "No"), 2)
                )
            )
            15 -> Quiz(
                questions = listOf(
                    QuizQuestion("A 'Will' is a legal document that:", listOf("Guarantees stock profits", "Distributes your assets after death", "Appoints a manager for your job", "Pays your taxes"), 1),
                    QuizQuestion("A Nominee is essentially a:", listOf("Final owner of the asset", "Trustee/Custodian for the asset", "Tax collector", "Lawyer"), 1),
                    QuizQuestion("Estate planning is important for:", listOf("Only the mega-rich", "Only for old people", "Everyone with any assets", "None of the above"), 2),
                    QuizQuestion("Tax-efficient investing aims to:", listOf("Evade taxes illegally", "Maximize after-tax returns legally", "Stop investing", "Pay the highest tax possible"), 1),
                    QuizQuestion("Sovereign Gold Bonds are tax-free if:", listOf("Held for 1 year", "Held till maturity (8 years)", "Bought from a friend", "Sold during a crash"), 1)
                )
            )
            16 -> Quiz(
                questions = listOf(
                    QuizQuestion("Loss Aversion means the pain of losing is ______ than the joy of winning the same amount.", listOf("Lower", "The same", "Greater", "Zero"), 2),
                    QuizQuestion("Herd Mentality leads investors to:", listOf("Think independently", "Follow what everyone else is doing", "Avoid stocks", "Save in gold"), 1),
                    QuizQuestion("Recency Bias is assuming the future will look like:", listOf("The distant past", "The immediate recent past", "A random guess", "Scientific predictions"), 1),
                    QuizQuestion("Automating investments (SIP) helps to remove:", listOf("Money from bank", "Emotional decision making", "Taxes", "Bank fees"), 1),
                    QuizQuestion("Confirmation Bias is seeking information that:", listOf("Challenges your beliefs", "Confirms your existing beliefs", "Is completely new", "Is written in German"), 1)
                )
            )
            else -> null
        }
    }
}
