/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VaultValidationTest {

    private static EntityManagerFactory emf;
    private static String propertyName;
    private static Validator validator;

    private VaultEntry vaultEntry;
    private boolean violationFound;

    public VaultValidationTest() {
    }

    @BeforeAll
    public static void initAll() {
        // emf required for populating metamodel attributes
        emf = Persistence.createEntityManagerFactory("test-resource-local");
        propertyName = VaultEntry_.encData.getName();
        emf.close();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    public void initEach() {
        vaultEntry = new VaultEntry();
        violationFound = false;
    }

    @AfterEach
    public void tearDownEach() {

    }

    @AfterAll
    public static void tearDownAll() {

    }

    private static Class<? extends Annotation> getAnnotationClass(ConstraintViolation<VaultEntry> violation) {
        return violation.getConstraintDescriptor().getAnnotation().annotationType();
    }

    @Test
    public void ifEncDataIsNull_encDataValidationFails() {
        vaultEntry.setEncData(null);
        Set<ConstraintViolation<VaultEntry>> violations = validator.validateProperty(vaultEntry, propertyName);
        for (ConstraintViolation<VaultEntry> cv : violations) {
            if (getAnnotationClass(cv) == NotNull.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifEncDataIsTooLong_encDataValidationFails() {
        vaultEntry.setEncData(new byte[VaultEntry.ENC_DATA_MAX_LENGTH + 1]);
        Set<ConstraintViolation<VaultEntry>> violations = validator.validateProperty(vaultEntry, propertyName);
        for (ConstraintViolation<VaultEntry> cv : violations) {
            if (getAnnotationClass(cv) == Size.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

    @Test
    public void ifEncDataIsTooShort_encDataValidationFails() {
        vaultEntry.setEncData(new byte[VaultEntry.ENC_DATA_MIN_LENGTH - 1]);
        Set<ConstraintViolation<VaultEntry>> violations = validator.validateProperty(vaultEntry, propertyName);
        for (ConstraintViolation<VaultEntry> cv : violations) {
            if (getAnnotationClass(cv) == Size.class) {
                violationFound = true;
            }
        }
        assertTrue(violationFound);
    }

}
