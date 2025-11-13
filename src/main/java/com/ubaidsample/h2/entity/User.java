/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.h2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

@Data
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "user_name")
        })
@NoArgsConstructor
@AllArgsConstructor
@Comment("Stores user information")
public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "user_id", nullable = false, updatable = false)
	@Comment("Unique identifier for each user")
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 50)
	@Comment("User given name")
    private String userName;

    @Column(name = "email", nullable = false, length = 50)
	@Comment("User given email")
    private String email;

    @Column(name = "password", nullable = false, length = 255)
	@Comment("User given password")
    private String password;
}