package expenses_tracking;

import java.math.BigDecimal;

public class Category {
    private int id;
    private int userId;
    private String name;
    private BigDecimal budget;

    public Category() {
    }

   
    public Category(int userId, String name, BigDecimal budget) {
        this.userId = userId;
        this.name = name;
        this.budget = budget;
    }
    
        public Category(int id, int userId, String name, BigDecimal budget) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.budget = budget;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    @Override
    public String toString() {
     
        return name;
    }
}
