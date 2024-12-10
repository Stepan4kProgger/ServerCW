package Hibernate.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "on_verify")
public class OnVerify {
    @Id
    @Column(name = "login", length = 32, nullable = false)
    private String login;

    @Column(name = "password", length = 32, nullable = false)
    private String password;

    // Getters и setters

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
