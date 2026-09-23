package com.portifolio.fiscalambiental.security;

import com.portifolio.fiscalambiental.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String cargo = usuario.getCargo().toUpperCase();
        return List.of(
            new SimpleGrantedAuthority("ROLE_" + cargo),
            new SimpleGrantedAuthority(cargo)
        );
    }

    @Override
    public String getPassword() {
        return usuario.getSenhaHash();
    }

    @Override
    public String getUsername() {
        return usuario.getCpf();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getAtivo();
    }
}
