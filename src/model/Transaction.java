package model;

import java.time.LocalDate;

/**
 * Transaction model representing a financial transaction.
 */
public class Transaction {

    private int id;
    private double amount;
    private String category;
    private String type;       // "INCOME" or "EXPENSE"
    private LocalDate date;
    private String description;

    // ─── Constructor WITHOUT id (for new inserts) ───────────────────────────
    public Transaction(double amount, String category, String type,
                       LocalDate date, String description) {
        this.amount      = amount;
        this.category    = category;
        this.type        = type;
        this.date        = date;
        this.description = description;
    }

    // ─── Constructor WITH id (for DB-fetched records) ───────────────────────
    public Transaction(int id, double amount, String category, String type,
                       LocalDate date, String description) {
        this.id          = id;
        this.amount      = amount;
        this.category    = category;
        this.type        = type;
        this.date        = date;
        this.description = description;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────
    public int getId()            { return id; }
    public double getAmount()     { return amount; }
    public String getCategory()   { return category; }
    public String getType()       { return type; }
    public LocalDate getDate()    { return date; }
    public String getDescription(){ return description; }

    // ─── Setters ─────────────────────────────────────────────────────────────
    public void setId(int id)                  { this.id = id; }
    public void setAmount(double amount)        { this.amount = amount; }
    public void setCategory(String category)    { this.category = category; }
    public void setType(String type)            { this.type = type; }
    public void setDate(LocalDate date)         { this.date = date; }
    public void setDescription(String description) { this.description = description; }

    // ─── toString ────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Transaction{id=%d, amount=%.2f, category='%s', type='%s', date=%s, description='%s'}",
            id, amount, category, type, date, description
        );
    }
}
