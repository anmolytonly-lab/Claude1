package com.shingar.salon.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String = "",
    val amount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
)

enum class AppointmentStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val birthday: LocalDate? = null,
    val notes: String = "",
    val totalVisits: Int = 0,
    val totalSpending: Double = 0.0,
    val joinDate: LocalDate = LocalDate.now(),
    val lastVisit: LocalDate? = null
)

data class SalonService(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val price: Double = 0.0,
    val duration: Int = 30,
    val category: ServiceCategory = ServiceCategory.GENERAL,
    val description: String = "",
    val isActive: Boolean = true
)

enum class ServiceCategory(val label: String) {
    MAKEUP("Makeup"),
    HAIR("Hair"),
    SKIN("Skin Care"),
    NAILS("Nails"),
    BODY("Body Care"),
    GENERAL("General")
}

data class Staff(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val role: String = "Stylist",
    val isActive: Boolean = true,
    val appointmentsCompleted: Int = 0,
    val rating: Float = 4.5f,
    val joinDate: LocalDate = LocalDate.now()
)

data class Offer(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val discount: Int = 0,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(30),
    val isActive: Boolean = true,
    val termsAndConditions: String = ""
)

data class Payment(
    val id: String = UUID.randomUUID().toString(),
    val appointmentId: String = "",
    val customerName: String = "",
    val serviceName: String = "",
    val amount: Double = 0.0,
    val status: PaymentStatus = PaymentStatus.UNPAID,
    val method: PaymentMethod = PaymentMethod.CASH,
    val date: LocalDate = LocalDate.now()
)

enum class PaymentStatus(val label: String) {
    PAID("Paid"),
    UNPAID("Unpaid"),
    PARTIAL("Partial")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card")
}

data class SalonSettings(
    val salonName: String = "Shingar Sallon",
    val phone: String = "+91 98765 43210",
    val address: String = "123 Beauty Street, Mumbai, India",
    val businessHours: String = "10:00 AM - 8:00 PM",
    val ownerName: String = "Salon Owner"
)

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val isFromUser: Boolean = true,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class DashboardStats(
    val todayAppointments: Int = 0,
    val totalCustomers: Int = 0,
    val todayEarnings: Double = 0.0,
    val pendingPayments: Int = 0,
    val activeOffers: Int = 0,
    val monthlyRevenue: Double = 0.0
)
