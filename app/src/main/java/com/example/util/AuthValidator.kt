package com.example.util

/**
 * Validates authentication inputs against strict security and format requirements.
 */
object AuthValidator {
    // RFC 5322 compliant regex pattern for valid email format
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,30}$")

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Email address cannot be empty.")
        }
        if (!EMAIL_REGEX.matches(trimmed)) {
            return ValidationResult(false, "Please enter a valid email address (e.g., name@gmail.com).")
        }
        return ValidationResult(true)
    }

    fun validateUsername(username: String): ValidationResult {
        val clean = username.trim().removePrefix("@")
        if (clean.isEmpty()) {
            return ValidationResult(false, "Username cannot be empty.")
        }
        if (clean.length < 3) {
            return ValidationResult(false, "Username must be at least 3 characters.")
        }
        if (clean.length > 30) {
            return ValidationResult(false, "Username cannot exceed 30 characters.")
        }
        if (!USERNAME_REGEX.matches(clean)) {
            return ValidationResult(false, "Username may only contain letters, numbers, and underscores.")
        }
        return ValidationResult(true)
    }

    /**
     * Enforces required password complexity rules:
     * - Minimum 8 characters
     * - Maximum 64 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "Password must be at least 8 characters long.")
        }
        if (password.length > 64) {
            return ValidationResult(false, "Password cannot exceed 64 characters.")
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, "Password must contain at least one uppercase letter (A-Z).")
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, "Password must contain at least one lowercase letter (a-z).")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "Password must contain at least one number (0-9).")
        }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (!hasSpecial) {
            return ValidationResult(false, "Password must contain at least one special character (e.g. !@#$%^&*).")
        }
        return ValidationResult(true)
    }

    fun validateConfirmPassword(password: String, confirm: String): ValidationResult {
        if (confirm.isEmpty()) {
            return ValidationResult(false, "Please confirm your password.")
        }
        if (password != confirm) {
            return ValidationResult(false, "Passwords do not match.")
        }
        return ValidationResult(true)
    }
}
