package com.example.decorra.data

/** The signed-in person, filled in by AccountRepository. */
object UserSession {
    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""
    var phone: String = ""

    val fullName: String get() = "$firstName $lastName".trim()

    fun set(account: Account) {
        firstName = account.firstName
        lastName = account.lastName
        email = account.email
        phone = account.phone
    }

    fun clear() {
        firstName = ""
        lastName = ""
        email = ""
        phone = ""
    }
}