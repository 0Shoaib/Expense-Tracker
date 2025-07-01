package expenses_tracking;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {


    public void addExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (user_id, category_id, title, amount, expense_date, description, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, expense.getUserId());
            stmt.setInt(2, expense.getCategoryId());
            stmt.setString(3, expense.getTitle()); 
            stmt.setBigDecimal(4, expense.getAmount());
            stmt.setDate(5, Date.valueOf(expense.getDate()));
            stmt.setString(6, expense.getDescription());
            stmt.setString(7, expense.getPaymentMethod());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating expense failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    expense.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating expense failed, no ID obtained.");
                }
            }
        }
    }

  
    public Expense getExpenseById(int expenseId, int userId) throws SQLException {
        String sql = "SELECT e.id, e.user_id, e.category_id, e.title, e.amount, e.expense_date, e.description, e.payment_method, c.name as category_name " +
                     "FROM expenses e JOIN categories c ON e.category_id = c.id " +
                     "WHERE e.id = ? AND e.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExpense(rs);
                }
            }
        }
        return null;
    }


    public List<Expense> getExpensesByUserId(int userId) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.id, e.user_id, e.category_id, e.title, e.amount, e.expense_date, e.description, e.payment_method, c.name as category_name " +
                     "FROM expenses e JOIN categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? ORDER BY e.expense_date DESC, e.id DESC"; // Recent first
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
        }
        return expenses;
    }


    public List<Expense> getExpensesByCategoryId(int userId, int categoryId) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.id, e.user_id, e.category_id, e.title, e.amount, e.expense_date, e.description, e.payment_method, c.name as category_name " +
                     "FROM expenses e JOIN categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? AND e.category_id = ? ORDER BY e.expense_date DESC, e.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
        }
        return expenses;
    }
    

    public BigDecimal getTotalExpensesByCategoryId(int userId, int categoryId) throws SQLException {
        String sql = "SELECT SUM(amount) AS total_spent FROM expenses WHERE user_id = ? AND category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total_spent");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }


    
    public void updateExpense(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET category_id = ?, title = ?, amount = ?, expense_date = ?, description = ?, payment_method = ? " +
                     "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expense.getCategoryId());
            stmt.setString(2, expense.getTitle());
            stmt.setBigDecimal(3, expense.getAmount());
            stmt.setDate(4, Date.valueOf(expense.getDate()));
            stmt.setString(5, expense.getDescription());
            stmt.setString(6, expense.getPaymentMethod());
            stmt.setInt(7, expense.getId());
            stmt.setInt(8, expense.getUserId()); 
            stmt.executeUpdate();
        }
    }

   
    public void deleteExpense(int expenseId, int userId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.setInt(2, userId); 
            stmt.executeUpdate();
        }
    }

   
    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setUserId(rs.getInt("user_id"));
        expense.setCategoryId(rs.getInt("category_id"));
        expense.setTitle(rs.getString("title"));
        expense.setAmount(rs.getBigDecimal("amount"));
        Date sqlDate = rs.getDate("expense_date");
        if (sqlDate != null) {
            expense.setDate(sqlDate.toLocalDate());
        }
        expense.setDescription(rs.getString("description"));
        expense.setPaymentMethod(rs.getString("payment_method"));
        expense.setCategoryName(rs.getString("category_name")); 
        return expense;
    }

    public List<Expense> getExpensesByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.id, e.user_id, e.category_id, e.title, e.amount, e.expense_date, e.description, e.payment_method, c.name as category_name " +
                     "FROM expenses e JOIN categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? AND e.expense_date BETWEEN ? AND ? " +
                     "ORDER BY e.expense_date DESC, e.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
        }
        return expenses;
    }
}
