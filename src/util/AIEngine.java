package util;

import model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rule-based AI Engine — no ML libraries.
 *
 * Responsibilities:
 *  1. Auto-categorize a transaction from its description
 *  2. Generate textual insights from historical data
 *  3. Predict next-month spending per category
 */
public class AIEngine {

    // ── 1. AUTO-CATEGORIZER ───────────────────────────────────────────────
    /**
     * Maps a free-text description to a category using keyword rules.
     *
     * @param description raw description entered by user
     * @return matched category string
     */
    public static String autoCategorize(String description) {
        if (description == null || description.isBlank()) return "Other";
        String lower = description.toLowerCase();

        if (matches(lower, "pizza", "burger", "swiggy", "zomato", "restaurant",
                    "cafe", "food", "lunch", "dinner", "breakfast", "snack",
                    "dominos", "kfc", "mcdonalds", "biryani", "sushi"))
            return "Food";

        if (matches(lower, "uber", "ola", "petrol", "fuel", "metro", "bus",
                    "taxi", "flight", "train", "irctc", "toll", "parking",
                    "travel", "trip", "rapido", "auto"))
            return "Travel";

        if (matches(lower, "amazon", "flipkart", "myntra", "mall", "shop",
                    "store", "purchase", "buy", "order", "meesho", "ajio",
                    "nykaa", "blinkit", "zepto", "shopping"))
            return "Shopping";

        if (matches(lower, "rent", "electricity", "wifi", "bill", "recharge",
                    "phone", "water", "gas", "dth", "broadband", "emi",
                    "insurance", "subscription", "utility"))
            return "Bills";

        if (matches(lower, "netflix", "spotify", "movie", "game", "cinema",
                    "prime", "hotstar", "disney", "youtube", "entertainment",
                    "concert", "show", "ott", "steam", "playstation"))
            return "Entertainment";

        return "Other";
    }

    private static boolean matches(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }

    // ── 2. GENERATE INSIGHTS ──────────────────────────────────────────────
    /**
     * Produces a list of human-readable insight strings from all transactions.
     *
     * Rules applied:
     *  - Warn if current-month category spending > 40% above 3-month average
     *  - Alert if Travel > ₹5000 this month
     *  - Celebrate income increase > 15%
     *  - Tip on top expense category this month
     *  - Tip if no savings this month
     */
    public static List<String> generateInsights(List<Transaction> all) {
        List<String> insights = new ArrayList<>();
        if (all == null || all.isEmpty()) {
            insights.add("💡 No transactions yet. Add some to see insights!");
            return insights;
        }

        YearMonth thisMonth  = YearMonth.now();
        YearMonth lastMonth  = thisMonth.minusMonths(1);

        // Split by current month vs previous 3 months (baseline)
        List<Transaction> currentExpenses = filterExpensesByMonth(all, thisMonth);
        List<Transaction> lastExpenses    = filterExpensesByMonth(all, lastMonth);

        Map<String, Double> currentByCategory  = sumByCategory(currentExpenses);
        Map<String, Double> baselineByCategory = avgOfLast3Months(all, thisMonth);

        // ── Rule: Category spending 40% above 3-month average ────────────
        for (Map.Entry<String, Double> entry : currentByCategory.entrySet()) {
            String cat = entry.getKey();
            double current = entry.getValue();
            double baseline = baselineByCategory.getOrDefault(cat, 0.0);
            if (baseline > 0 && current > baseline * 1.40) {
                double pct = ((current - baseline) / baseline) * 100;
                insights.add(String.format(
                    "⚠️ %s spending is %.0f%% above your 3-month average (₹%.0f vs avg ₹%.0f)",
                    cat, pct, current, baseline));
            }
        }

        // ── Rule: Travel > ₹5000 this month ──────────────────────────────
        double travelSpend = currentByCategory.getOrDefault("Travel", 0.0);
        if (travelSpend > 5000) {
            insights.add(String.format(
                "🚨 You've spent ₹%.0f on Travel this month — exceeding the ₹5,000 threshold!", travelSpend));
        }

        // ── Rule: Income increased 15% vs last month ──────────────────────
        double incomeThis = sumIncome(filterIncomeByMonth(all, thisMonth));
        double incomeLast = sumIncome(filterIncomeByMonth(all, lastMonth));
        if (incomeLast > 0 && incomeThis > incomeLast * 1.15) {
            double pct = ((incomeThis - incomeLast) / incomeLast) * 100;
            insights.add(String.format("✅ Income increased by %.0f%% compared to last month (₹%.0f → ₹%.0f)",
                pct, incomeLast, incomeThis));
        }

        // ── Tip: Top expense category ─────────────────────────────────────
        if (!currentByCategory.isEmpty()) {
            String topCat = currentByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("Unknown");
            double topAmt = currentByCategory.get(topCat);
            insights.add(String.format("💡 Tip: Your top expense this month is %s (₹%.0f)", topCat, topAmt));
        }

        // ── Rule: No savings this month ───────────────────────────────────
        double totalExpense = currentByCategory.values().stream().mapToDouble(Double::doubleValue).sum();
        if (incomeThis > 0 && totalExpense >= incomeThis) {
            insights.add("🚨 Warning: You have spent equal to or more than your income this month!");
        } else if (incomeThis > 0 && totalExpense > 0) {
            double savingRate = ((incomeThis - totalExpense) / incomeThis) * 100;
            if (savingRate < 20) {
                insights.add(String.format("⚠️ Your saving rate this month is only %.0f%%. Aim for 20%%+", savingRate));
            } else {
                insights.add(String.format("✅ Great job! You saved %.0f%% of your income this month.", savingRate));
            }
        }

        // ── Rule: High spending day ───────────────────────────────────────
        Map<LocalDate, Double> byDay = all.stream()
            .filter(t -> "EXPENSE".equals(t.getType())
                && YearMonth.from(t.getDate()).equals(thisMonth))
            .collect(Collectors.groupingBy(Transaction::getDate,
                Collectors.summingDouble(Transaction::getAmount)));
        byDay.entrySet().stream()
            .filter(e -> e.getValue() > 3000)
            .max(Map.Entry.comparingByValue())
            .ifPresent(e -> insights.add(String.format(
                "📅 Your highest spending day this month was %s (₹%.0f)", e.getKey(), e.getValue())));

        if (insights.isEmpty()) {
            insights.add("✅ Everything looks balanced this month. Keep it up!");
        }

        return insights;
    }

    // ── 3. PREDICT NEXT MONTH ─────────────────────────────────────────────
    /**
     * Predicts next month's spending per category as the average of the last 3 months.
     *
     * @param all all transactions
     * @return Map: category → predicted amount
     */
    public static Map<String, Double> predictNextMonth(List<Transaction> all) {
        if (all == null || all.isEmpty()) return Collections.emptyMap();
        return avgOfLast3Months(all, YearMonth.now());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private static List<Transaction> filterExpensesByMonth(List<Transaction> all, YearMonth ym) {
        return all.stream()
            .filter(t -> "EXPENSE".equals(t.getType()) && YearMonth.from(t.getDate()).equals(ym))
            .collect(Collectors.toList());
    }

    private static List<Transaction> filterIncomeByMonth(List<Transaction> all, YearMonth ym) {
        return all.stream()
            .filter(t -> "INCOME".equals(t.getType()) && YearMonth.from(t.getDate()).equals(ym))
            .collect(Collectors.toList());
    }

    private static Map<String, Double> sumByCategory(List<Transaction> txns) {
        return txns.stream()
            .collect(Collectors.groupingBy(Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmount)));
    }

    private static double sumIncome(List<Transaction> txns) {
        return txns.stream().mapToDouble(Transaction::getAmount).sum();
    }

    /**
     * Computes per-category average monthly spending over the 3 months
     * immediately before `currentMonth`.
     */
    private static Map<String, Double> avgOfLast3Months(List<Transaction> all, YearMonth currentMonth) {
        Map<String, List<Double>> monthlyTotals = new HashMap<>();

        for (int i = 1; i <= 3; i++) {
            YearMonth ym = currentMonth.minusMonths(i);
            Map<String, Double> totals = sumByCategory(filterExpensesByMonth(all, ym));
            for (Map.Entry<String, Double> e : totals.entrySet()) {
                monthlyTotals.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());
            }
        }

        Map<String, Double> avg = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> e : monthlyTotals.entrySet()) {
            double sum = e.getValue().stream().mapToDouble(Double::doubleValue).sum();
            avg.put(e.getKey(), sum / 3.0);   // average over 3 months (missing months count as 0)
        }
        return avg;
    }
}
