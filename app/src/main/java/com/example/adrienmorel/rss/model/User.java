package com.example.adrienmorel.rss.model;

import com.example.adrienmorel.rss.Router;
import com.orm.SugarRecord;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class User extends SugarRecord {

    public User() {}

    public String email;
    public String password;

    public String mkToken() {

        return Jwts.builder()
                .setSubject(email)
                .signWith(SignatureAlgorithm.HS256, Router.key)
                .compact();
    }
}
