package expenses_tracking;

public class User {
    private int id;
    private String username;
    private String passwordHash; 
    private String email;

    
    public User() {
    }

    
    public User(String username, String plainPassword, String email) {
        this.username = username;
        this.passwordHash = plainPassword; 
        this.email = email;
    }


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}