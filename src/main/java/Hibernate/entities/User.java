package Hibernate.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "login", length = 32, nullable = false)
    private String login;

    @Column(name = "password", length = 32, nullable = false)
    private String password;

    @Column(name = "name", length = 32)
    private String name;

    @Column(name = "isAdmin", nullable = false)
    private int isAdmin;

    // Getters и setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getIsAdmin() { return isAdmin; }
    public void setIsAdmin(int isAdmin) { this.isAdmin = isAdmin; }
}
