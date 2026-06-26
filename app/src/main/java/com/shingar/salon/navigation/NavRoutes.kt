package com.shingar.salon.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val APPOINTMENTS = "appointments"
    const val ADD_APPOINTMENT = "add_appointment"
    const val EDIT_APPOINTMENT = "edit_appointment/{appointmentId}"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val SERVICES = "services"
    const val STAFF = "staff"
    const val OFFERS = "offers"
    const val PAYMENTS = "payments"
    const val REPORTS = "reports"
    const val AI_ASSISTANT = "ai_assistant"
    const val SETTINGS = "settings"

    fun editAppointment(id: String) = "edit_appointment/$id"
    fun customerDetail(id: String) = "customer_detail/$id"
}
