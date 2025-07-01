package expenses_tracking;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {


    public void addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (user_id, name, budget) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, category.getUserId());
            stmt.setString(2, category.getName());
            stmt.setBigDecimal(3, category.getBudget());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }


    public Category getCategoryByIdAndUserId(int categoryId, int userId) throws SQLException {
        String sql = "SELECT id, user_id, name, budget FROM categories WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        return null;
    }
    

    public Category getCategoryByNameAndUserId(String categoryName, int userId) throws SQLException {
        String sql = "SELECT id, user_id, name, budget FROM categories WHERE name = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        return null;
    }



    public List<Category> getCategoriesByUserId(int userId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "select id, user_id, name, budget from categories where user_id = ? order by name asc"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        }
        return categories;
    }

   
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, budget = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setBigDecimal(2, category.getBudget());
            stmt.setInt(3, category.getId());
            stmt.setInt(4, category.getUserId());
            stmt.executeUpdate();
        }
    }

    
    public void deleteCategory(int categoryId, int userId) throws SQLException {
       
        String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
    
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setUserId(rs.getInt("user_id"));
        category.setName(rs.getString("name"));
        category.setBudget(rs.getBigDecimal("budget"));
        return category;
    }
}
