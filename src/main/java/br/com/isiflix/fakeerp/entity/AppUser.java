package br.com.isiflix.fakeerp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuário de login. Mapeado para a tabela tbl_users.
 * Os escopos (coluna scopes) vão para a claim "scope" do JWT.
 * A senha é armazenada no formato do DelegatingPasswordEncoder, ex.: {bcrypt}$2a$...
 */
@Entity
@Table(name = "tbl_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    /** Escopos JWT separados por espaço, ex.: "report:read policy:read". */
    @Column(nullable = false)
    private String scopes = "";

    protected AppUser() {
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getScopes() {
        return scopes;
    }
}
