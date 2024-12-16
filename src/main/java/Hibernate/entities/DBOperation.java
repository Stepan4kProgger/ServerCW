package Hibernate.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "operations")
public class DBOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_login", length = 64, nullable = false)
    private String authorLogin;

    @Column(name = "author_name", length = 64)
    private String authorName;

    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(name = "target", length = 64, nullable = false)
    private String target;

    @Column(name = "information", length = 128)
    private String information;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public void setAuthorLogin(String authorLogin) {
        this.authorLogin = authorLogin;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getOperationType() {
        return String.valueOf(operationType);
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getTargetType() {
        return String.valueOf(targetType);
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}

