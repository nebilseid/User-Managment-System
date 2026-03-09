package com.sliide.usermanagement.domain.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {

    // --- isValidName ---

    @Test fun `name - empty string is invalid`() = assertFalse(isValidName(""))
    @Test fun `name - single character is invalid`() = assertFalse(isValidName("A"))
    @Test fun `name - two letters is valid`() = assertTrue(isValidName("Al"))
    @Test fun `name - full name with space is valid`() = assertTrue(isValidName("Alice Johnson"))
    @Test fun `name - hyphenated name is valid`() = assertTrue(isValidName("Mary-Jane"))
    @Test fun `name - name with apostrophe is valid`() = assertTrue(isValidName("O'Brien"))
    @Test fun `name - leading and trailing spaces are trimmed`() = assertTrue(isValidName("  Bob  "))
    @Test fun `name - all spaces is invalid`() = assertFalse(isValidName("   "))
    @Test fun `name - contains digit is invalid`() = assertFalse(isValidName("Alice2"))
    @Test fun `name - contains @ symbol is invalid`() = assertFalse(isValidName("Ali@ce"))
    @Test fun `name - unicode letters are valid`() = assertTrue(isValidName("Ångström"))
    @Test fun `name - name with multiple hyphens is valid`() = assertTrue(isValidName("Jean-Claude Van"))

    // --- isValidEmail ---

    @Test fun `email - empty string is invalid`() = assertFalse(isValidEmail(""))
    @Test fun `email - missing @ is invalid`() = assertFalse(isValidEmail("userexample.com"))
    @Test fun `email - missing domain is invalid`() = assertFalse(isValidEmail("user@"))
    @Test fun `email - missing TLD is invalid`() = assertFalse(isValidEmail("user@example"))
    @Test fun `email - single char TLD is invalid`() = assertFalse(isValidEmail("user@example.c"))
    @Test fun `email - simple valid email`() = assertTrue(isValidEmail("user@example.com"))
    @Test fun `email - subdomain is valid`() = assertTrue(isValidEmail("user@mail.example.com"))
    @Test fun `email - plus addressing is valid`() = assertTrue(isValidEmail("user+tag@example.com"))
    @Test fun `email - dots in local part are valid`() = assertTrue(isValidEmail("first.last@example.com"))
    @Test fun `email - hyphen in domain is valid`() = assertTrue(isValidEmail("user@my-domain.org"))
    @Test fun `email - uppercase letters are valid`() = assertTrue(isValidEmail("User@Example.COM"))
    @Test fun `email - missing local part is invalid`() = assertFalse(isValidEmail("@example.com"))
    @Test fun `email - spaces are invalid`() = assertFalse(isValidEmail("user @example.com"))
    @Test fun `email - double @ is invalid`() = assertFalse(isValidEmail("user@@example.com"))
    @Test fun `email - leading dot in local part is invalid`() = assertFalse(isValidEmail(".user@example.com"))
    @Test fun `email - trailing dot before @ is invalid`() = assertFalse(isValidEmail("user.@example.com"))
    @Test fun `email - consecutive dots in local part are invalid`() = assertFalse(isValidEmail("user..name@example.com"))
    @Test fun `email - consecutive dots in domain are invalid`() = assertFalse(isValidEmail("user@exam..ple.com"))
    @Test fun `email - domain starting with hyphen is invalid`() = assertFalse(isValidEmail("user@-example.com"))
    @Test fun `email - local part exceeding 64 chars is invalid`() = assertFalse(isValidEmail("${"a".repeat(65)}@example.com"))
    @Test fun `email - total length exceeding 254 chars is invalid`() = assertFalse(isValidEmail("user@${"a".repeat(246)}.com"))
}
