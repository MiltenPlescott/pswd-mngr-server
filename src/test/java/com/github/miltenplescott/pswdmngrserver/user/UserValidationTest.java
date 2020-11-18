/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {

    private static EntityManagerFactory emf;
    private static String property_name;
    private static Validator validator;

    private User user;
    private boolean violationFound;

    public UserValidationTest() {
    }

    @BeforeAll
    public static void initAll() {
        emf = Persistence.createEntityManagerFactory("test-resource-local");
        property_name = User_.username.getName();
        emf.close();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    public void initEach() {
        user = new User();
        violationFound = false;
    }

    @AfterEach
    public void tearDownEach() {
    }

    @AfterAll
    public static void tearDownAll() {
    }

    private static Class<? extends Annotation> getAnnotationClass(ConstraintViolation<User> violation) {
        return violation.getConstraintDescriptor().getAnnotation().annotationType();
    }

    @Test
    public void ifUsernameIsNull_usernameValidationFails() {
        user.setUsername(null);
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == NotNull.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameIsEmpty_usernameValidationFails() {
        user.setUsername("");
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == Size.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameIsTooShortWhitespace_usernameValidationFails() {
        user.setUsername(" ".repeat(User.USERNAME_MIN_LENGTH - 1));
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == Size.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameIsCorrectLengthWhitespace_usernameValidationFails() {
        user.setUsername(" ".repeat(User.USERNAME_MIN_LENGTH));
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == Pattern.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameIsTooLongWhitespace_usernameValidationFails() {
        user.setUsername(" ".repeat(User.USERNAME_MAX_LENGTH + 1));
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == Pattern.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameIsTooShort_usernameValidationFails() {
        user.setUsername("x".repeat(User.USERNAME_MIN_LENGTH - 1));
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == Size.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameIsTooLong_usernameValidationFails() {
        user.setUsername("x".repeat(User.USERNAME_MAX_LENGTH + 1));
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
        for (ConstraintViolation<User> cv : violations) {
            if (getAnnotationClass(cv) == Size.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifUsernameContainsNonAlphanumeric_usernameValidationFails() {
        for (char c = 0; c <= 255; c++) {
            if (!Character.isLetterOrDigit(c)) {
                violationFound = false;
                user.setUsername("x".repeat(User.USERNAME_MIN_LENGTH) + c);
                Set<ConstraintViolation<User>> violations = validator.validateProperty(user, property_name);
                for (ConstraintViolation<User> cv : violations) {
                    if (getAnnotationClass(cv) == Pattern.class) {
                        violationFound = true;
                    }
                }
                assertTrue(violationFound);
            }
        }
    }

}
