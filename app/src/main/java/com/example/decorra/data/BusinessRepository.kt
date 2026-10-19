package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class BusinessProfile(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val about: String = ""
) {
    val isEmpty: Boolean
        get() = listOf(name, phone, email, address, about).all { it.isBlank() }
}

data class BankDetails(
    val bankName: String = "",
    val accountHolder: String = "",
    val accountNumber: String = "",
    val branchCode: String = "",
    val reference: String = ""
) {
    val isComplete: Boolean
        get() = bankName.isNotBlank() && accountHolder.isNotBlank() && accountNumber.isNotBlank()
}

/** The decorator's business details, shared with clients. Stored on this device. */
object BusinessRepository {
    private const val FILE = "business.json"

    private val _profile = MutableStateFlow(BusinessProfile())
    val profile: StateFlow<BusinessProfile> = _profile.asStateFlow()

    private val _bank = MutableStateFlow(BankDetails())
    val bank: StateFlow<BankDetails> = _bank.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching {
                val root = JSONObject(text)
                root.optJSONObject("profile")?.let {
                    _profile.value = BusinessProfile(
                        name = it.optString("name"),
                        phone = it.optString("phone"),
                        email = it.optString("email"),
                        address = it.optString("address"),
                        about = it.optString("about")
                    )
                }
                root.optJSONObject("bank")?.let {
                    _bank.value = BankDetails(
                        bankName = it.optString("bankName"),
                        accountHolder = it.optString("accountHolder"),
                        accountNumber = it.optString("accountNumber"),
                        branchCode = it.optString("branchCode"),
                        reference = it.optString("reference")
                    )
                }
            }
        }
    }

    fun saveProfile(profile: BusinessProfile) {
        _profile.value = profile
        save()
    }

    fun saveBank(bank: BankDetails) {
        _bank.value = bank
        save()
    }

    private fun save() {
        val p = _profile.value
        val b = _bank.value
        val root = JSONObject()
            .put(
                "profile",
                JSONObject()
                    .put("name", p.name)
                    .put("phone", p.phone)
                    .put("email", p.email)
                    .put("address", p.address)
                    .put("about", p.about)
            )
            .put(
                "bank",
                JSONObject()
                    .put("bankName", b.bankName)
                    .put("accountHolder", b.accountHolder)
                    .put("accountNumber", b.accountNumber)
                    .put("branchCode", b.branchCode)
                    .put("reference", b.reference)
            )
        Storage.write(FILE, root.toString())
    }
}