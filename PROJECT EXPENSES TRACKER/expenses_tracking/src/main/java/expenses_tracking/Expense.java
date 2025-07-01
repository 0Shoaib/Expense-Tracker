package expenses_tracking;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private int id;
    private int userId;
    private int categoryId; 
    private String title; 
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String paymentMethod; 
    private String categoryName; 

   
    public Expense() {
    }

    public Expense(int userId, int categoryId, String title, BigDecimal amount, LocalDate date, String description, String paymentMethod) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.paymentMethod = paymentMethod;
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
