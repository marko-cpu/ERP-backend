package com.app.erp.security;

import com.app.erp.entity.Role;
import com.app.erp.entity.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private static final long serialVersionUID = 1L;
    private User user;

    public UserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authories = new ArrayList<>();

        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            authories.add(new SimpleGrantedAuthority(role.getName()));
        }

        return authories;
    }

    @Override
    public String getPassword() {
        return  user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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

    public String getFullname() {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    public void setFirstName(String firstName) {
        this.user.setFirstName(firstName);
    }

    public User getUser() {
        return user;
    }

    public void setLastName(String lastName) {
        this.user.setLastName(lastName);
    }


    public boolean hasRole(String roleName) {
        return user != null && user.hasRole(roleName);
    }



}
