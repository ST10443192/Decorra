package com.example.decorra.navigation

object Routes {
    const val HOME = "home"
    const val QUOTES = "quotes"
    const val CHATS = "chats"
    const val PROFILE = "profile"
    const val PORTFOLIO = "portfolio"

    const val ARG_THEME_ID = "themeId"
    const val THEME_DETAIL = "theme/{$ARG_THEME_ID}"
    fun themeDetail(id: String) = "theme/$id"

    const val CUSTOMIZE = "customize"
    const val EVENT_DATE = "event_date"
    const val BREAKDOWN = "quote_breakdown"

    const val ARG_QUOTE_ID = "quoteId"
    const val QUOTE_DETAIL = "quote/{$ARG_QUOTE_ID}"
    fun quoteDetail(id: String) = "quote/$id"

    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    const val ARG_SECTION = "sectionKey"
    const val SECTION = "section/{$ARG_SECTION}"
    fun section(key: String) = "section/$key"

    const val SECTION_EVENTS = "events"
    const val SECTION_PAYMENTS = "payments"
    const val SECTION_SAVED = "saved"
    const val SECTION_INSPIRATION = "inspiration"
    const val SECTION_DESIGNS = "designs"

    // Decorator side
    const val EVENTS = "admin_events"
    const val EVENT_DETAIL = "event/{$ARG_QUOTE_ID}"
    fun eventDetail(id: String) = "event/$id"

    const val ADMIN_CHAT = "admin_chat"
    const val MANAGE = "manage"

    const val ADMIN_SECTION = "admin_section/{$ARG_SECTION}"
    fun adminSection(key: String) = "admin_section/$key"

    const val ADMIN_BUSINESS = "business"
    const val ADMIN_TEAM = "team"
    const val ADMIN_BANK = "bank"
    const val ADMIN_INVENTORY = "admin_inventory"
    const val ADMIN_ALERTS = "admin_alerts"
}