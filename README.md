# 💰 Personal Finance Analyzer

A professional offline desktop finance app built with Java 17, Swing (dark theme), MySQL, JFreeChart, and a rule-based AI engine — zero external ML libraries.

---

## 🖥️ Features

| Feature | Details |
|---------|---------|
| **Transactions** | Add / Edit / Delete with full validation |
| **Auto-Categorize** | AI engine maps description → category (Food, Travel, Shopping, Bills, Entertainment) |
| **Dashboard** | Live income/expense/balance cards + Pie chart + Bar chart (last 6 months) |
| **AI Insights** | Colored alert cards: overspending warnings, income trends, saving rate |
| **Predictions** | 3-month rolling average to predict next month's spending per category |
| **Dark Theme** | Full Catppuccin-inspired dark palette throughout |

---

## 🗂️ Project Structure

```
finance-app/
├── src/
│   ├── Main.java                    ← Entry point
│   ├── model/Transaction.java       ← Data model
│   ├── db/DBConnection.java         ← MySQL singleton
│   ├── dao/TransactionDAO.java      ← All SQL operations
│   ├── ui/
│   │   ├── MainFrame.java           ← JFrame, sidebar, CardLayout
│   │   ├── TransactionForm.java     ← Add/Edit/Delete form
│   │   ├── TransactionTable.java    ← Styled JTable
│   │   ├── DashboardPanel.java      ← Charts + summary cards
│   │   └── AIInsightsPanel.java     ← AI alerts + predictions
│   └── util/AIEngine.java           ← Rule-based AI
├── lib/
│   ├── mysql-connector-j-8.0.33.jar
│   └── jfreechart-1.5.4.jar
└── README.md
```

---

## ⚙️ Setup (IntelliJ IDEA)

### Prerequisites
- Java 17 or later (JDK)
- MySQL 8.x server running locally
- IntelliJ IDEA (Community or Ultimate)

### 1. Clone / Open Project
```
File → Open → select the finance-app/ folder
```

### 2. Add JARs to Classpath
```
File → Project Structure (Ctrl+Alt+Shift+S)
  → Modules → Dependencies tab → + → JARs or Directories
  → Navigate to finance-app/lib/
  → Select both JARs → OK → Apply
```

### 3. Set Source Root
```
File → Project Structure → Modules → Sources tab
  → Right-click src/ → "Mark as: Sources Root"
```

### 4. Run Configuration
```
Run → Edit Configurations → + → Application
  Name:       Finance App
  Main class: Main          ← (in src/ with no package)
  JDK:        Java 17+
→ Apply → Run
```

### 5. MySQL Setup
Make sure MySQL is running. The app **auto-creates** the `finance_app` database and `transactions` table on first launch.

If you need to create it manually:
```sql
CREATE DATABASE IF NOT EXISTS finance_app;
USE finance_app;
CREATE TABLE IF NOT EXISTS transactions (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  amount      DOUBLE       NOT NULL,
  category    VARCHAR(50)  NOT NULL,
  type        VARCHAR(10)  NOT NULL,
  date        DATE         NOT NULL,
  description VARCHAR(255)
);
```

### 6. Credentials
Edit `src/db/DBConnection.java` lines 12–13 if your MySQL credentials differ:
```java
private static final String USER     = "root";
private static final String PASSWORD = "your_password_here";
```

---

## 🔄 Data Flow

```
[UI Panel]
    │
    ▼
[TransactionDAO]   ← insert / fetchAll / update / delete / fetchByCategory / fetchByMonth
    │
    ▼
[DBConnection.getInstance().getConnection()]   ← Singleton JDBC connection
    │
    ▼
[MySQL: finance_app.transactions]
```

---

## 🤖 AI Engine Rules

| Rule | Trigger |
|------|---------|
| ⚠️ Category overspend | Current month > 140% of 3-month average |
| 🚨 Travel alert | Travel > ₹5,000 this month |
| ✅ Income increase | This month > 115% of last month |
| 💡 Top expense tip | Always shown for highest-spend category |
| ⚠️ Low saving rate | < 20% of income saved |
| 🔮 Prediction | Simple 3-month rolling average per category |

---

## 📦 Dependencies

| Library | Version | Source |
|---------|---------|--------|
| MySQL Connector/J | 8.0.33 | Maven Central |
| JFreeChart | 1.5.4 | Maven Central (jcommon bundled) |

---

## 🌐 Deployment for Resume

See the deployment guide provided separately after confirming local run.
The app can be exported as an executable JAR and deployed as a web-accessible demo.

---

## 📄 License

MIT License — free to use and modify.
