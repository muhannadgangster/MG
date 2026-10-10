package com.example.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun testInvalidEmailFormats() {
        // Empty and blank
        assertFalse(AuthValidator.validateEmail("").isValid)
        assertFalse(AuthValidator.validateEmail("   ").isValid)

        // Missing @
        assertFalse(AuthValidator.validateEmail("plainaddress").isValid)

        // Missing domain or TLD
        assertFalse(AuthValidator.validateEmail("user@").isValid)
        assertFalse(AuthValidator.validateEmail("user@domain").isValid)
        assertFalse(AuthValidator.validateEmail("@domain.com").isValid)
        assertFalse(AuthValidator.validateEmail("user@.com").isValid)

        // Invalid characters and spaces
        assertFalse(AuthValidator.validateEmail("user name@gmail.com").isValid)
        assertFalse(AuthValidator.validateEmail("user@@gmail.com").isValid)
    }

    @Test
    fun testValidEmailFormats() {
        assertTrue(AuthValidator.validateEmail("creator@gmail.com").isValid)
        assertTrue(AuthValidator.validateEmail("john.doe+filter@example.com").isValid)
        assertTrue(AuthValidator.validateEmail("adv.chaudharymustafa@gmail.com").isValid)
        assertTrue(AuthValidator.validateEmail("alex@tech.mg").isValid)
    }

    @Test
    fun testWeakPasswords() {
        // Shorter than 8 chars
        assertFalse("Too short", AuthValidator.validatePassword("Ab1!").isValid)
        assertFalse("7 chars", AuthValidator.validatePassword("Abcde1!").isValid)

        // Missing uppercase letter
        assertFalse("No uppercase", AuthValidator.validatePassword("password123!").isValid)

        // Missing lowercase letter
        assertFalse("No lowercase", AuthValidator.validatePassword("PASSWORD123!").isValid)

        // Missing number
        assertFalse("No number", AuthValidator.validatePassword("Password!@#").isValid)

        // Missing special character
        assertFalse("No special char", AuthValidator.validatePassword("Password123").isValid)

        // Longer than 64 characters
        val tooLong = "A1!" + "a".repeat(65)
        assertFalse("Too long", AuthValidator.validatePassword(tooLong).isValid)
    }

    @Test
    fun testStrongValidPasswords() {
        assertTrue(AuthValidator.validatePassword("SecurePass123!").isValid)
        assertTrue(AuthValidator.validatePassword("Admin@2026#Lg").isValid)
        assertTrue(AuthValidator.validatePassword("XyZ_987654$").isValid)
    }

    @Test
    fun testConfirmPasswordMatching() {
        // Passwords match
        assertTrue(AuthValidator.validateConfirmPassword("SecurePass123!", "SecurePass123!").isValid)

        // Mismatched
        assertFalse(AuthValidator.validateConfirmPassword("SecurePass123!", "DifferentPass123!").isValid)
        assertFalse(AuthValidator.validateConfirmPassword("SecurePass123!", "SecurePass123?").isValid)

        // Empty confirm password
        assertFalse(AuthValidator.validateConfirmPassword("SecurePass123!", "").isValid)
    }

    @Test
    fun testUsernameValidation() {
        // Empty or too short
        assertFalse(AuthValidator.validateUsername("").isValid)
        assertFalse(AuthValidator.validateUsername("ab").isValid)

        // Valid usernames
        assertTrue(AuthValidator.validateUsername("alexrivera").isValid)
        assertTrue(AuthValidator.validateUsername("@alex_rivera").isValid)
        assertTrue(AuthValidator.validateUsername("creator_99").isValid)

        // Invalid characters (spaces, special symbols)
        assertFalse(AuthValidator.validateUsername("alex rivera").isValid)
        assertFalse(AuthValidator.validateUsername("alex@rivera").isValid)
    }
}
