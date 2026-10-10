package com.example.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `validateEmail accepts standard and gmail email addresses`() {
        val validEmails = listOf(
            "user@gmail.com",
            "john.doe@example.com",
            "creator+beta@domain.co.uk",
            "admin_user@lg.app"
        )
        for (email in validEmails) {
            val result = AuthValidator.validateEmail(email)
            assertTrue("Expected $email to be valid", result.isValid)
        }
    }

    @Test
    fun `validateEmail rejects invalid or empty email addresses`() {
        val invalidEmails = listOf(
            "",
            "   ",
            "notanemail",
            "fake@",
            "@gmail.com",
            "user@domain",
            "user@.com",
            "user space@gmail.com"
        )
        for (email in invalidEmails) {
            val result = AuthValidator.validateEmail(email)
            assertFalse("Expected '$email' to be rejected", result.isValid)
            assertTrue("Error message should be present", !result.errorMessage.isNullOrBlank())
        }
    }

    @Test
    fun `validatePassword accepts compliant passwords`() {
        val validPasswords = listOf(
            "P@ssword123",
            "Secure#2026!",
            "MySecret9$",
            "A1b2c3d4!xyz"
        )
        for (pwd in validPasswords) {
            val result = AuthValidator.validatePassword(pwd)
            assertTrue("Expected '$pwd' to be valid", result.isValid)
        }
    }

    @Test
    fun `validatePassword rejects non-compliant passwords`() {
        // Less than 8 chars
        assertFalse(AuthValidator.validatePassword("P@ss1").isValid)

        // Greater than 64 chars
        assertFalse(AuthValidator.validatePassword("A".repeat(60) + "1!a" + "B".repeat(10)).isValid)

        // Missing uppercase
        assertFalse(AuthValidator.validatePassword("p@ssword123").isValid)

        // Missing lowercase
        assertFalse(AuthValidator.validatePassword("P@SSWORD123").isValid)

        // Missing number
        assertFalse(AuthValidator.validatePassword("P@sswordXYZ").isValid)

        // Missing special character
        assertFalse(AuthValidator.validatePassword("Password123").isValid)
    }

    @Test
    fun `validateConfirmPassword verifies password matching`() {
        assertTrue(AuthValidator.validateConfirmPassword("P@ssword123", "P@ssword123").isValid)
        assertFalse(AuthValidator.validateConfirmPassword("P@ssword123", "P@ssword321").isValid)
        assertFalse(AuthValidator.validateConfirmPassword("P@ssword123", "").isValid)
    }

    @Test
    fun `validateUsername verifies username rules`() {
        assertTrue(AuthValidator.validateUsername("johndoe").isValid)
        assertTrue(AuthValidator.validateUsername("@johndoe").isValid)
        assertTrue(AuthValidator.validateUsername("user_123").isValid)

        // Too short (< 3)
        assertFalse(AuthValidator.validateUsername("ab").isValid)

        // Invalid characters
        assertFalse(AuthValidator.validateUsername("user name").isValid)
        assertFalse(AuthValidator.validateUsername("user!").isValid)

        // Empty
        assertFalse(AuthValidator.validateUsername("").isValid)
    }
}
