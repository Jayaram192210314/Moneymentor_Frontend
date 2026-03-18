package com.simats.moneymentor.data

object QuizRepository {
    fun getQuizByModuleId(moduleId: Int): Quiz? {
        return when (moduleId) {
            1 -> Quiz(
                questions = listOf(
                    QuizQuestion("What does the '50' represent in the 50/30/20 rule?", listOf("Wants", "Needs", "Savings", "Debt"), 1),
                    QuizQuestion("Which of these is a fixed expense?", listOf("Grocery bill", "Movie tickets", "Rent", "Dining out"), 2),
                    QuizQuestion("What is the primary purpose of an emergency fund?", listOf("Buying a car", "Unexpected expenses", "Vacation", "Investing"), 1),
                    QuizQuestion("Which should be prioritized first in a budget?", listOf("Entertainment", "Luxury goods", "Savings/Debt", "Hobbies"), 2),
                    QuizQuestion("A budget surplus occurs when:", listOf("Expenses > Income", "Income > Expenses", "Income = Expenses", "Debt = Savings"), 1),
                    QuizQuestion("Which of these is a 'Want' rather than a 'Need'?", listOf("Health Insurance", "Basic Groceries", "Designer Clothes", "Electricity"), 2),
                    QuizQuestion("The 'Pay Yourself First' principle means:", listOf("Spend on hobbies first", "Save before spending", "Buy gifts for yourself", "Pay bills last"), 1),
                    QuizQuestion("How often should you ideally track your expenses?", listOf("Yearly", "Every 6 months", "Daily/Weekly", "Never"), 2),
                    QuizQuestion("What is an example of a variable expense?", listOf("Car EMI", "Insurance Premium", "Electricity Bill", "Broadband Bill"), 2),
                    QuizQuestion("What is the first step to financial freedom?", listOf("Buying stocks", "Tracking and Budgeting", "Getting a credit card", "Taking a loan"), 1)
                )
            )
            2 -> Quiz(
                questions = listOf(
                    QuizQuestion("What is the main benefit of a Savings Account?", listOf("High risk", "Liquidity & Safety", "Stock ownership", "No interest"), 1),
                    QuizQuestion("What does FDIC/DICGC insurance protect?", listOf("Stock market loss", "Bank deposits", "Crypto theft", "Real estate"), 1),
                    QuizQuestion("Compound interest is calculated on:", listOf("Principal only", "Interest only", "Principal + Accumulated Interest", "Monthly income"), 2),
                    QuizQuestion("Which account usually offers the highest interest?", listOf("Current Account", "Savings Account", "Fixed Deposit", "Salary Account"), 2),
                    QuizQuestion("What is a 'Nominee' in a bank account?", listOf("The account holder", "Person who inherits funds", "Bank manager", "Account auditor"), 1),
                    QuizQuestion("What is the 'Rule of 72' used for?", listOf("Calculating tax", "Estimating time to double money", "Measuring inflation", "Budgeting"), 1),
                    QuizQuestion("A 'Zero Balance' account means:", listOf("No money in account", "No minimum balance requirement", "Interest is zero", "Fees are zero"), 1),
                    QuizQuestion("What is an EMI?", listOf("Existing Money Interest", "Equated Monthly Installment", "Extra Monthly Income", "Electronic Money Interface"), 1),
                    QuizQuestion("Which of these is a digital payment method?", listOf("Cash", "UPI", "Barter", "Gold"), 1),
                    QuizQuestion("Net Banking allows you to:", listOf("Visit a branch", "Transfer funds online", "Withdraw physical cash", "Print currency"), 1)
                )
            )
            3 -> Quiz(
                questions = listOf(
                    QuizQuestion("What is 'TDS'?", listOf("Total Debt Service", "Tax Deducted at Source", "Tax Deduction System", "Time Deposit Scheme"), 1),
                    QuizQuestion("Which section in India allows deductions for 80C?", listOf("Salary Tax", "Wealth Tax", "Investment Deductions", "Gifts Tax"), 2),
                    QuizQuestion("What is a Financial Year in India?", listOf("Jan to Dec", "April to March", "July to June", "Oct to Sept"), 1),
                    QuizQuestion("Income Tax is a type of:", listOf("Indirect Tax", "Direct Tax", "Service Tax", "GST"), 1),
                    QuizQuestion("What is 'Form 16'?", listOf("Passport form", "Certificate of tax deducted", "Withdrawal form", "Loan application"), 1),
                    QuizQuestion("Standard Deduction is available for:", listOf("Freelancers", "Salaried Employees", "Business owners", "Students"), 1),
                    QuizQuestion("Meaning of 'Exempt Income'?", listOf("Income with high tax", "Income not subject to tax", "Foreign income", "Illegal income"), 1),
                    QuizQuestion("PAN stands for:", listOf("Permanent Account Number", "Personal Area Network", "Primary Access Name", "Postal Address Note"), 0),
                    QuizQuestion("What is ITR?", listOf("Income Tax Revenue", "Income Tax Return", "Internal Tax Rate", "Integrated Tax Record"), 1),
                    QuizQuestion("GST stands for:", listOf("General Sales Tax", "Goods and Services Tax", "Government Service Tax", "Global Standard Tax"), 1)
                )
            )
            4 -> Quiz(
                questions = listOf(
                    QuizQuestion("The main goal of investing is to:", listOf("Spend money", "Beat inflation and grow wealth", "Keep money under mattress", "Pay more tax"), 1),
                    QuizQuestion("Risk and Return usually have a ___ relationship.", listOf("Inverse", "Direct", "Random", "No"), 1),
                    QuizQuestion("What is Asset Allocation?", listOf("Buying one stock", "Mixing different investments", "Keeping all cash", "Selling everything"), 1),
                    QuizQuestion("Which is considered a 'Safe' haven?", listOf("Crypto", "Gold/Govt Bonds", "Penny Stocks", "Derivatives"), 1),
                    QuizQuestion("What is Inflation?", listOf("Rise in purchasing power", "Decrease in price of goods", "Decline in purchasing power", "Stable prices"), 2),
                    QuizQuestion("A Portfolio is:", listOf("A single investment", "A collection of investments", "A bank account", "A physical folder"), 1),
                    QuizQuestion("Liquidity refers to:", listOf("Amount of cash", "Ease of converting to cash", "Color of money", "Bank location"), 1),
                    QuizQuestion("Which is a long-term goal?", listOf("Vacation next month", "Retirement in 20 years", "Buying a phone", "Weekend party"), 1),
                    QuizQuestion("Diversification helps to:", listOf("Increase risk", "Reduce risk", "Guarantee profit", "Complicate things"), 1),
                    QuizQuestion("What is a Bear Market?", listOf("Prices are rising", "Prices are falling", "Market is closed", "Market is stable"), 1)
                )
            )
            5 -> Quiz(
                questions = listOf(
                    QuizQuestion("Cash Flow is the movement of money ___.", listOf("In only", "Out only", "In and Out", "To the bank only"), 2),
                    QuizQuestion("Negative Cash Flow means:", listOf("Income > Expenses", "Expenses > Income", "No income", "No expenses"), 1),
                    QuizQuestion("What is 'Sinking Fund'?", listOf("Money for debt", "Savings for specific future cost", "A failing investment", "Bankrupt project"), 1),
                    QuizQuestion("Discretionary Income is:", listOf("Total salary", "Money after needs & taxes", "Tax amount", "Rent money"), 1),
                    QuizQuestion("Zero-Based Budgeting means:", listOf("Zero savings", "Every dollar has a job", "No spending", "Infinite budget"), 1),
                    QuizQuestion("What is a Debt-to-Income ratio?", listOf("Monthly debt / Gross income", "Savings / Debt", "Income - Expenses", "Loan interest rate"), 0),
                    QuizQuestion("Which is a 'Non-Essential' expense?", listOf("Electricity", "Subscription box", "Mortgage", "Health checkup"), 1),
                    QuizQuestion("An 'Appreciating Asset' is:", listOf("A car", "Clothes", "Real Estate", "Smartphone"), 2),
                    QuizQuestion("Effective cash flow management prevents:", listOf("Savings", "Debt traps", "Wealth", "Happiness"), 1),
                    QuizQuestion("Automating savings helps to:", listOf("Forget about money", "Ensure consistency", "Stop spending", "Increase bank fees"), 1)
                )
            )
            6 -> Quiz(
                questions = listOf(
                    QuizQuestion("Deductions under 80D are for:", listOf("Life Insurance", "Health Insurance", "Education Loan", "Donations"), 1),
                    QuizQuestion("HRA stands for:", listOf("Home Rent Allowance", "High Rate Account", "Housing Revenue Act", "Home Repair Amount"), 0),
                    QuizQuestion("Which is NOT a tax deduction under 80C?", listOf("PPF", "Dining out expenses", "ELSS", "Life Insurance"), 1),
                    QuizQuestion("Tax Liability is:", listOf("Your total income", "The amount you owe in tax", "Tax refund", "The tax office address"), 1),
                    QuizQuestion("Difference between Old and New Tax Regime?", listOf("Tax rates only", "Deductions availability", "Only age limit", "No difference"), 1),
                    QuizQuestion("Cess is a tax on ___.", listOf("Luxury", "Basic items", "The tax itself", "Foreigners"), 2),
                    QuizQuestion("Rebate under 87A means:", listOf("Tax increase", "Tax relief for small incomes", "Investment bonus", "Pension"), 1),
                    QuizQuestion("Agricultural Income in India is:", listOf("Taxed at 30%", "Generally Exempt", "Only for farmers", "Taxed at 10%"), 1),
                    QuizQuestion("Professional Tax is levied by:", listOf("Central Govt", "State Govt", "Municipal Corp", "Companies"), 1),
                    QuizQuestion("Capital Gains Tax is on:", listOf("Salary", "Profit from selling assets", "Gifts", "Interest"), 1)
                )
            )
            7 -> Quiz(
                questions = listOf(
                    QuizQuestion("What is a Bond?", listOf("Ownership in company", "A loan to an entity", "A saving account", "Insurance"), 1),
                    QuizQuestion("FD stands for:", listOf("Fast Deposit", "Fixed Deposit", "Final Debt", "Fund Division"), 1),
                    QuizQuestion("Which offers high safety but lower returns?", listOf("Stocks", "FDs/Govt Bonds", "Crypto", "Startups"), 1),
                    QuizQuestion("What is 'Maturity' in FD?", listOf("Account opening", "The end of the term", "Interest payment", "Withdrawal penalty"), 1),
                    QuizQuestion("Debt Funds primarily invest in:", listOf("Equity", "Fixed income instruments", "Real estate", "Gold"), 1),
                    QuizQuestion("What is 'Liquidity Risk' in Debt?", listOf("Interest rate change", "Inability to sell instantly", "Default by borrower", "Inflation"), 1),
                    QuizQuestion("Corporate FDs compared to Bank FDs:", listOf("Lower risk", "Often higher interest/risk", "Always safer", "Tax free"), 1),
                    QuizQuestion("T-Bills are issued by:", listOf("Private banks", "Central Government", "Local shops", "Tech companies"), 1),
                    QuizQuestion("Credit Rating 'AAA' signifies:", listOf("Highest risk", "Highest safety", "Average", "New company"), 1),
                    QuizQuestion("Interest on FDs is usually:", listOf("Tax-free", "Taxable", "Variable daily", "Not paid"), 1)
                )
            )
            8 -> Quiz(
                questions = listOf(
                    QuizQuestion("SIP stands for:", listOf("Simple Investment Plan", "Systematic Investment Plan", "Single Installment Payment", "Stock Interest Policy"), 1),
                    QuizQuestion("Mutual Funds are managed by:", listOf("Investors", "Professional Fund Managers", "Bank Tellers", "AI only"), 1),
                    QuizQuestion("What is NAV?", listOf("Net Asset Value", "National Asset Variety", "Net Amount Variable", "New Annual Value"), 0),
                    QuizQuestion("ELSS is best for:", listOf("Short term", "Tax saving & Equity", "Guaranteed returns", "Banking"), 1),
                    QuizQuestion("Expense Ratio refers to:", listOf("Your monthly cost", "Fees for managing the fund", "Return on investment", "Exit load"), 1),
                    QuizQuestion("Index Funds track:", listOf("Single stock", "A market index (Nifty/Sensex)", "Gold price", "Bank interest"), 1),
                    QuizQuestion("Exit Load is:", listOf("Entry fee", "Fee for selling early", "Management fee", "Tax"), 1),
                    QuizQuestion("Diversification in Mutual Funds happens because:", listOf("You buy many stocks", "The fund buys many assets", "Manager is smart", "Tax is low"), 1),
                    QuizQuestion("SIPs help in:", listOf("Timing the market", "Rupee Cost Averaging", "Avoiding tax", "Getting a loan"), 1),
                    QuizQuestion("Large Cap funds invest in:", listOf("Risky startups", "Top 100 established companies", "Small shops", "Penny stocks"), 1)
                )
            )
            9 -> Quiz(
                questions = listOf(
                    QuizQuestion("What is an 'IPO'?", listOf("Initial Public Offering", "Internal Profit Option", "International Price Order", "Instant Payment Out"), 0),
                    QuizQuestion("Meaning of 'Dividend'?", listOf("A share of profit", "A loan", "A tax penalty", "Stock price dip"), 0),
                    QuizQuestion("BSE and NSE are:", listOf("Banks", "Stock Exchanges", "Govt Departments", "Insurance firms"), 1),
                    QuizQuestion("What is 'Market Capitalization'?", listOf("Total cash of company", "Total value (Price x Shares)", "HQ location value", "Employee salaries"), 1),
                    QuizQuestion("Sensex represents which exchange?", listOf("NSE", "BSE", "NASDAQ", "London"), 1),
                    QuizQuestion("A 'Bull' in stock market is:", listOf("Optimistic/Expects rise", "Pessimistic/Expects fall", "Broker", "New investor"), 0),
                    QuizQuestion("What is a 'Blue Chip' company?", listOf("New startup", "Large, stable, reliable", "Technology only", "Foreign company"), 1),
                    QuizQuestion("Shareholders are ____ of the company.", listOf("Lenders", "Part-owners", "Customers", "Employees"), 1),
                    QuizQuestion("Nifty 50 consists of:", listOf("50 stocks in USA", "50 major stocks on NSE", "5 stocks", "All stocks in India"), 1),
                    QuizQuestion("Trading account is used to ___.", listOf("Store shares", "Buy/sell shares", "Save money", "Get a loan"), 1)
                )
            )
            10 -> Quiz(
                questions = listOf(
                    QuizQuestion("What is 'Tax Loss Harvesting'?", listOf("Farming tax", "Selling losers to offset gains", "Hiding income", "Buying more stocks"), 1),
                    QuizQuestion("Advance Tax is paid when:", listOf("Tax > 10,000", "Tax < 10,000", "Only at year end", "Only by companies"), 0),
                    QuizQuestion("LTCG stands for:", listOf("Long Term Capital Gains", "Local Tax Collection Group", "Low Tax Capital Grant", "Long Term Credit Guide"), 0),
                    QuizQuestion("STCG on stocks is usually ___ than LTCG.", listOf("Lower", "Higher", "The same", "Zero"), 1),
                    QuizQuestion("Tax Avoidance vs Tax Evasion:", listOf("Both legal", "Both illegal", "Avoidance is legal, Evasion is illegal", "Evasion is legal"), 2),
                    QuizQuestion("Gratuity is:", listOf("A tip", "Benefit for long service", "Tax penalty", "Investment return"), 1),
                    QuizQuestion("Meaning of 'Assessment Year' (AY)?", listOf("Year money was earned", "Year following earning year", "10 years ago", "Future year"), 1),
                    QuizQuestion("HUFs can be used for:", listOf("Individual tax", "Tax planning for families", "Only business", "Education"), 1),
                    QuizQuestion("Indexation benefit helps to:", listOf("Increase tax", "Adjust purchase price for inflation", "Buy more assets", "Get a loan"), 1),
                    QuizQuestion("NPS stands for:", listOf("National Pension System", "New Pension Scheme", "Net Profit Service", "National Payment System"), 0)
                )
            )
            11 -> Quiz(
                questions = listOf(
                    QuizQuestion("P/E Ratio stands for:", listOf("Price to Earnings", "Profit to Expense", "Performance to Equity", "Price to Equity"), 0),
                    QuizQuestion("A high P/E could mean:", listOf("Stock is cheap", "Stock is overvalued/High growth", "Company is failing", "No one is buying"), 1),
                    QuizQuestion("Balance Sheet shows:", listOf("Profit for year", "Assets, Liabilities, Equity", "Daily transactions", "Stock graph"), 1),
                    QuizQuestion("Revenue is:", listOf("Net profit", "Total money from sales", "Total expenses", "Owner's capital"), 1),
                    QuizQuestion("What is 'EBITDA'?", listOf("Net Income", "Earnings before interest, taxes, etc.", "Total sales", "Dividend amount"), 1),
                    QuizQuestion("Fundamental Analysis studies:", listOf("Price charts", "Business health & Financials", "Twitter trends", "Only CEO"), 1),
                    QuizQuestion("Return on Equity (ROE) measures:", listOf("Profit from debts", "Efficiency in using shareholders' funds", "Total sales", "Stock price"), 1),
                    QuizQuestion("Debt-to-Equity ratio measures:", listOf("Profitability", "Financial leverage/Risk", "Market share", "Dividend yield"), 1),
                    QuizQuestion("Working Capital is:", listOf("Fixed Assets", "Current Assets - Current Liabilities", "Total Debt", "Owner's salary"), 1),
                    QuizQuestion("Intrinsic Value is:", listOf("Current market price", "Estimated true worth of asset", "Face value", "Bonus price"), 1)
                )
            )
            12 -> Quiz(
                questions = listOf(
                    QuizQuestion("Rebalancing a portfolio means:", listOf("Buying more of everything", "Adjusting weights of assets", "Selling all stocks", "Closing the account"), 1),
                    QuizQuestion("Time Horizon refers to:", listOf("Time of day to trade", "How long you plan to invest", "Market opening hours", "Age of investor"), 1),
                    QuizQuestion("Aggressive portfolio usually has more:", listOf("Gold", "Equity/Stocks", "Bonds", "Cash"), 1),
                    QuizQuestion("Conservative portfolio usually has more:", listOf("Small caps", "Fixed Income/Bonds", "Options", "Crypto"), 1),
                    QuizQuestion("What is 'Correlated' assets?", listOf("Move in different directions", "Move in same direction", "No relation", "Not traded"), 1),
                    QuizQuestion("Benefit of low correlation?", listOf("Higher risk", "Lower overall portfolio risk", "No benefit", "Lower returns"), 1),
                    QuizQuestion("Emergency fund should be ___ from portfolio.", listOf("Included", "Separate & Liquid", "In small caps", "In real estate"), 1),
                    QuizQuestion("Investment Policy Statement (IPS) is:", listOf("A tax bill", "A plan for goals and risk", "A marketing flyer", "A stock tip"), 1),
                    QuizQuestion("Impact of high fees on portfolio?", listOf("Increases returns", "Significantly eats into long-term wealth", "No impact", "Reduces tax"), 1),
                    QuizQuestion("Standard Deviation measures:", listOf("Average return", "Volatility/Risk", "Total value", "Company size"), 1)
                )
            )
            13 -> Quiz(
                questions = listOf(
                    QuizQuestion("Goal-Based Investing prioritizes:", listOf("Highest returns only", "Specific life milestones (House, Edu)", "Beating friends", "Daily trading"), 1),
                    QuizQuestion("Short-term goal ( < 2 years) focus:", listOf("Equity", "Capital Preservation/Liquidity", "Crypto", "Commercial Land"), 1),
                    QuizQuestion("Inflation-adjusted target means:", listOf("Ignore inflation", "Account for rising costs in future", "Buy only gold", "Save more tax"), 1),
                    QuizQuestion("For a retirement goal 30 years away, use:", listOf("Savings account", "Equity/Growth assets", "Cash", "Current account"), 1),
                    QuizQuestion("SMART goals stands for:", listOf("Simple, Money, Active, Rich, Top", "Specific, Measurable, Achievable, Relevant, Time-bound", "Stocks, Mutual, Asset, Return, Tax", "Small, Medium, Active, Rapid, Today"), 1),
                    QuizQuestion("Education planning should start:", listOf("When student graduates", "As early as possible", "Only if rich", "After retirement"), 1),
                    QuizQuestion("Which is a 'Need' based goal?", listOf("Luxury Cruise", "Child's Education fund", "Designer bag", "Newest iPhone"), 1),
                    QuizQuestion("How to handle a multi-goal budget?", listOf("Save for one at a time", "Prioritize and allocate to each", "Wait for windfall", "Take a loan for all"), 1),
                    QuizQuestion("Effect of 'Delaying' a goal?", listOf("Need to save less later", "Need to save much more later", "No effect", "Market will wait"), 1),
                    QuizQuestion("Life Stage changes (e.g. Marriage) should:", listOf("Not affect goals", "Trigger a portfolio review", "Stop all investing", "Be ignored"), 1)
                )
            )
            14 -> Quiz(
                questions = listOf(
                    QuizQuestion("Value Investing involves buying stocks that are:", listOf("Expensive", "Undervalued compared to fundamentals", "Daily losers", "New tech"), 1),
                    QuizQuestion("Growth Investing focuses on companies with:", listOf("High dividends", "Rapidly rising earnings/revenue", "No debt", "Physical assets"), 1),
                    QuizQuestion("Technical Analysis uses ___ to predict price.", listOf("Balance sheets", "Historical price/volume charts", "CEO interviews", "News only"), 1),
                    QuizQuestion("What is 'Short Selling'?", listOf("Buying for long term", "Selling borrowed shares to buy cheaper", "Buying small amounts", "Selling quickly"), 1),
                    QuizQuestion("Options and Futures are:", listOf("Simple savings", "Derivatives/Advanced instruments", "Types of FDs", "Mutual funds"), 1),
                    QuizQuestion("The 'Efficient Market Hypothesis' suggests:", listOf("You can always beat the market", "All info is already in stock price", "Price is always wrong", "Invest in gold only"), 1),
                    QuizQuestion("Dollar Cost Averaging (DCA) is similar to:", listOf("Lump sum", "SIP", "Trading", "Betting"), 1),
                    QuizQuestion("Margin Trading means:", listOf("Trading with profit", "Trading with borrowed money", "Trading on holidays", "Low risk trading"), 1),
                    QuizQuestion("What is a 'Quant' fund?", listOf("Managed by humans only", "Uses mathematical/algo models", "Small fund", "Quality fund"), 1),
                    QuizQuestion("Alpha in investing represents:", listOf("Market return", "Excess return above benchmark", "First stock bought", "Total loss"), 1)
                )
            )
            15 -> Quiz(
                questions = listOf(
                    QuizQuestion("Estate Planning involves:", listOf("Buying land", "Plan for asset transfer after death", "Daily budgeting", "Tax filing only"), 1),
                    QuizQuestion("A 'Trust' is used for:", listOf("Believing others", "Managing assets for beneficiaries", "A type of bank", "Stock trading"), 1),
                    QuizQuestion("Succession planning ensures:", listOf("High returns", "Smooth transfer of wealth/business", "Zero tax", "More debt"), 1),
                    QuizQuestion("Wealth Management includes:", listOf("Only stocks", "Investment, Tax, Legal & Retirement", "Only insurance", "Finding a job"), 1),
                    QuizQuestion("Generation-Skipping transfer is:", listOf("Transfer to children", "Transfer to grandchildren", "Moving money to another country", "Avoiding tax for 1 year"), 1),
                    QuizQuestion("Philanthropy planning:", listOf("Is only for the poor", "Strategizing charitable giving", "Spending all money", "Ignoring family"), 1),
                    QuizQuestion("Tax-efficient withdrawal strategy:", listOf("Withdraw all at once", "Managing sequence of asset sales", "Stop paying tax", "Withdraw nothing"), 1),
                    QuizQuestion("Offshore accounts are used for:", listOf("Hiding money illegally", "Legal global diversification/Tax (sometimes)", "Only for tourists", "Saving on local groceries"), 1),
                    QuizQuestion("Family Office is:", listOf("A home office", "Private firm managing a family's wealth", "A big company", "A bank branch"), 1),
                    QuizQuestion("Comprehensive wealth review should be:", listOf("Once in 10 years", "Annual/Periodic", "Never", "Day of retirement"), 1)
                )
            )
            16 -> Quiz(
                questions = listOf(
                    QuizQuestion("Loss Aversion means humans feel loss ___ more than gain.", listOf("Equally", "Twice as much", "Less", "Not at all"), 1),
                    QuizQuestion("Herd Mentality leads to:", listOf("Smart decisions", "Buying because others are buying", "Doing nothing", "Detailed research"), 1),
                    QuizQuestion("Confirmation Bias is:", listOf("Checking your account", "Looking for info that supports your view", "Confirming a trade", "Being always right"), 1),
                    QuizQuestion("Recency Bias means over-weighting:", listOf("Recent events", "Old events", "Future events", "Fake news"), 0),
                    QuizQuestion("Anchoring is relying too much on:", listOf("The first piece of info offered", "Expert advice", "Charts", "Intuition"), 0),
                    QuizQuestion("Overconfidence Bias leads to:", listOf("Under-trading", "Excessive trading/High risk", "Better returns", "Detailed planning"), 1),
                    QuizQuestion("Mental Accounting is:", listOf("Doing math in your head", "Treating money differently based on source", "Saving automatically", "Hiring an accountant"), 1),
                    QuizQuestion("Fearing a market crash after a long bull run is:", listOf("Gambler's fallacy (sometimes)", "Smart planning", "Always correct", "Illegal"), 0),
                    QuizQuestion("Status Quo Bias is the preference for:", listOf("Change", "Things to stay the same", "Higher risk", "Selling assets"), 1),
                    QuizQuestion("Financial discipline is primarily about:", listOf("Math skills", "Controlling emotions/behavior", "Knowing secrets", "Having more money"), 1)
                )
            )
            else -> null
        }
    }
}
