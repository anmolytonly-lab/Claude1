package com.shingar.salon.data.repository

import com.shingar.salon.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object SalonRepository {

    private val _appointments = MutableStateFlow(generateSampleAppointments())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _customers = MutableStateFlow(generateSampleCustomers())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _services = MutableStateFlow(generateSampleServices())
    val services: StateFlow<List<SalonService>> = _services.asStateFlow()

    private val _staff = MutableStateFlow(generateSampleStaff())
    val staff: StateFlow<List<Staff>> = _staff.asStateFlow()

    private val _offers = MutableStateFlow(generateSampleOffers())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    private val _payments = MutableStateFlow(generateSamplePayments())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _settings = MutableStateFlow(SalonSettings())
    val settings: StateFlow<SalonSettings> = _settings.asStateFlow()

    fun getDashboardStats(): DashboardStats {
        val today = LocalDate.now()
        val todayAppts = _appointments.value.filter { it.dateTime.toLocalDate() == today }
        val paidToday = _payments.value.filter { it.date == today && it.status == PaymentStatus.PAID }
        val pendingPayments = _payments.value.count { it.status == PaymentStatus.UNPAID }
        val activeOffers = _offers.value.count { it.isActive && !it.endDate.isBefore(today) }
        val monthlyPayments = _payments.value.filter {
            it.date.month == today.month && it.date.year == today.year && it.status == PaymentStatus.PAID
        }

        return DashboardStats(
            todayAppointments = todayAppts.size,
            totalCustomers = _customers.value.size,
            todayEarnings = paidToday.sumOf { it.amount },
            pendingPayments = pendingPayments,
            activeOffers = activeOffers,
            monthlyRevenue = monthlyPayments.sumOf { it.amount }
        )
    }

    fun addAppointment(appointment: Appointment) {
        _appointments.value = _appointments.value + appointment
        _payments.value = _payments.value + Payment(
            appointmentId = appointment.id,
            customerName = appointment.customerName,
            serviceName = appointment.serviceName,
            amount = appointment.amount,
            status = appointment.paymentStatus,
            method = appointment.paymentMethod,
            date = appointment.dateTime.toLocalDate()
        )
    }

    fun updateAppointment(appointment: Appointment) {
        _appointments.value = _appointments.value.map {
            if (it.id == appointment.id) appointment else it
        }
    }

    fun deleteAppointment(id: String) {
        _appointments.value = _appointments.value.filter { it.id != id }
    }

    fun addCustomer(customer: Customer) {
        _customers.value = _customers.value + customer
    }

    fun updateCustomer(customer: Customer) {
        _customers.value = _customers.value.map {
            if (it.id == customer.id) customer else it
        }
    }

    fun addService(service: SalonService) {
        _services.value = _services.value + service
    }

    fun updateService(service: SalonService) {
        _services.value = _services.value.map {
            if (it.id == service.id) service else it
        }
    }

    fun deleteService(id: String) {
        _services.value = _services.value.filter { it.id != id }
    }

    fun addStaff(member: Staff) {
        _staff.value = _staff.value + member
    }

    fun updateStaff(member: Staff) {
        _staff.value = _staff.value.map {
            if (it.id == member.id) member else it
        }
    }

    fun addOffer(offer: Offer) {
        _offers.value = _offers.value + offer
    }

    fun updateOffer(offer: Offer) {
        _offers.value = _offers.value.map {
            if (it.id == offer.id) offer else it
        }
    }

    fun deleteOffer(id: String) {
        _offers.value = _offers.value.filter { it.id != id }
    }

    fun updatePaymentStatus(id: String, status: PaymentStatus, method: PaymentMethod) {
        _payments.value = _payments.value.map {
            if (it.id == id) it.copy(status = status, method = method) else it
        }
    }

    fun updateSettings(newSettings: SalonSettings) {
        _settings.value = newSettings
    }

    private fun generateSampleAppointments(): List<Appointment> {
        val today = LocalDate.now()
        return listOf(
            Appointment(
                customerName = "Priya Sharma",
                customerPhone = "+91 98765 11111",
                serviceId = "1", serviceName = "Bridal Makeup",
                staffId = "1", staffName = "Neha",
                dateTime = LocalDateTime.of(today, LocalTime.of(10, 0)),
                status = AppointmentStatus.CONFIRMED,
                amount = 15000.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.UPI
            ),
            Appointment(
                customerName = "Anita Verma",
                customerPhone = "+91 98765 22222",
                serviceId = "2", serviceName = "Party Makeup",
                staffId = "2", staffName = "Ritu",
                dateTime = LocalDateTime.of(today, LocalTime.of(11, 30)),
                status = AppointmentStatus.PENDING,
                amount = 5000.0
            ),
            Appointment(
                customerName = "Meena Patel",
                customerPhone = "+91 98765 33333",
                serviceId = "3", serviceName = "Hair Styling",
                staffId = "1", staffName = "Neha",
                dateTime = LocalDateTime.of(today, LocalTime.of(14, 0)),
                status = AppointmentStatus.CONFIRMED,
                amount = 2000.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.CASH
            ),
            Appointment(
                customerName = "Kavita Singh",
                customerPhone = "+91 98765 44444",
                serviceId = "4", serviceName = "Facial",
                staffId = "3", staffName = "Pooja",
                dateTime = LocalDateTime.of(today, LocalTime.of(15, 30)),
                status = AppointmentStatus.PENDING,
                amount = 1500.0
            ),
            Appointment(
                customerName = "Sunita Gupta",
                customerPhone = "+91 98765 55555",
                serviceId = "5", serviceName = "Nails",
                staffId = "2", staffName = "Ritu",
                dateTime = LocalDateTime.of(today, LocalTime.of(16, 0)),
                status = AppointmentStatus.COMPLETED,
                amount = 800.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.CARD
            ),
            Appointment(
                customerName = "Deepika Jain",
                customerPhone = "+91 98765 66666",
                serviceId = "7", serviceName = "Hair Spa",
                staffId = "1", staffName = "Neha",
                dateTime = LocalDateTime.of(today.plusDays(1), LocalTime.of(10, 0)),
                status = AppointmentStatus.CONFIRMED,
                amount = 2500.0
            )
        )
    }

    private fun generateSampleCustomers(): List<Customer> {
        return listOf(
            Customer(name = "Priya Sharma", phone = "+91 98765 11111", email = "priya@email.com",
                birthday = LocalDate.of(1995, 3, 15), totalVisits = 12, totalSpending = 45000.0,
                joinDate = LocalDate.now().minusMonths(6), lastVisit = LocalDate.now()),
            Customer(name = "Anita Verma", phone = "+91 98765 22222", email = "anita@email.com",
                birthday = LocalDate.of(1990, 7, 22), totalVisits = 8, totalSpending = 28000.0,
                joinDate = LocalDate.now().minusMonths(4), lastVisit = LocalDate.now().minusDays(5)),
            Customer(name = "Meena Patel", phone = "+91 98765 33333", email = "meena@email.com",
                birthday = LocalDate.of(1988, 11, 8), totalVisits = 15, totalSpending = 52000.0,
                joinDate = LocalDate.now().minusYears(1), lastVisit = LocalDate.now()),
            Customer(name = "Kavita Singh", phone = "+91 98765 44444", email = "kavita@email.com",
                birthday = LocalDate.of(1992, 1, 30), totalVisits = 5, totalSpending = 12000.0,
                joinDate = LocalDate.now().minusMonths(2), lastVisit = LocalDate.now().minusDays(10)),
            Customer(name = "Sunita Gupta", phone = "+91 98765 55555", email = "sunita@email.com",
                birthday = LocalDate.of(1985, 5, 12), totalVisits = 20, totalSpending = 78000.0,
                joinDate = LocalDate.now().minusYears(2), lastVisit = LocalDate.now()),
            Customer(name = "Deepika Jain", phone = "+91 98765 66666", email = "deepika@email.com",
                birthday = LocalDate.of(1993, 9, 5), totalVisits = 3, totalSpending = 8500.0,
                joinDate = LocalDate.now().minusMonths(1), lastVisit = LocalDate.now().minusDays(3)),
            Customer(name = "Rashmi Dubey", phone = "+91 98765 77777", email = "rashmi@email.com",
                birthday = LocalDate.of(1991, 12, 25), totalVisits = 10, totalSpending = 35000.0,
                joinDate = LocalDate.now().minusMonths(8), lastVisit = LocalDate.now().minusDays(7))
        )
    }

    private fun generateSampleServices(): List<SalonService> {
        return listOf(
            SalonService(id = "1", name = "Bridal Makeup", price = 15000.0, duration = 180,
                category = ServiceCategory.MAKEUP, description = "Complete bridal makeup with hairstyling"),
            SalonService(id = "2", name = "Party Makeup", price = 5000.0, duration = 90,
                category = ServiceCategory.MAKEUP, description = "Glamorous party look makeup"),
            SalonService(id = "3", name = "Hair Styling", price = 2000.0, duration = 60,
                category = ServiceCategory.HAIR, description = "Professional hair styling and setting"),
            SalonService(id = "4", name = "Facial", price = 1500.0, duration = 60,
                category = ServiceCategory.SKIN, description = "Deep cleansing facial treatment"),
            SalonService(id = "5", name = "Nails", price = 800.0, duration = 45,
                category = ServiceCategory.NAILS, description = "Manicure, pedicure, and nail art"),
            SalonService(id = "6", name = "Waxing", price = 1200.0, duration = 45,
                category = ServiceCategory.BODY, description = "Full body or specific area waxing"),
            SalonService(id = "7", name = "Threading", price = 200.0, duration = 15,
                category = ServiceCategory.SKIN, description = "Eyebrow and face threading"),
            SalonService(id = "8", name = "Hair Spa", price = 2500.0, duration = 90,
                category = ServiceCategory.HAIR, description = "Deep conditioning hair spa treatment"),
            SalonService(id = "9", name = "Hair Color", price = 3000.0, duration = 120,
                category = ServiceCategory.HAIR, description = "Professional hair coloring"),
            SalonService(id = "10", name = "Mehndi", price = 3500.0, duration = 120,
                category = ServiceCategory.GENERAL, description = "Bridal and party mehndi designs")
        )
    }

    private fun generateSampleStaff(): List<Staff> {
        return listOf(
            Staff(id = "1", name = "Neha", phone = "+91 99999 11111", role = "Senior Stylist",
                appointmentsCompleted = 156, rating = 4.8f, joinDate = LocalDate.now().minusYears(3)),
            Staff(id = "2", name = "Ritu", phone = "+91 99999 22222", role = "Makeup Artist",
                appointmentsCompleted = 98, rating = 4.6f, joinDate = LocalDate.now().minusYears(2)),
            Staff(id = "3", name = "Pooja", phone = "+91 99999 33333", role = "Skin Specialist",
                appointmentsCompleted = 72, rating = 4.7f, joinDate = LocalDate.now().minusYears(1)),
            Staff(id = "4", name = "Suman", phone = "+91 99999 44444", role = "Junior Stylist",
                appointmentsCompleted = 45, rating = 4.3f, joinDate = LocalDate.now().minusMonths(6))
        )
    }

    private fun generateSampleOffers(): List<Offer> {
        return listOf(
            Offer(
                title = "Bridal Season Special",
                description = "Get 20% off on complete bridal packages including makeup, mehndi & hair styling",
                discount = 20,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(45),
                isActive = true
            ),
            Offer(
                title = "O+ Facial Free with ₹2500 Package",
                description = "Book any service worth ₹2500 or more and get a complimentary O+ facial treatment",
                discount = 0,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(30),
                isActive = true
            ),
            Offer(
                title = "Monsoon Hair Spa Deal",
                description = "Hair Spa + Hair Cut combo at just ₹1999. Save ₹1500!",
                discount = 40,
                startDate = LocalDate.now().minusDays(5),
                endDate = LocalDate.now().plusDays(25),
                isActive = true
            ),
            Offer(
                title = "Refer & Earn",
                description = "Refer a friend and both get 15% off on your next visit",
                discount = 15,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(60),
                isActive = true
            )
        )
    }

    private fun generateSamplePayments(): List<Payment> {
        val today = LocalDate.now()
        return listOf(
            Payment(customerName = "Priya Sharma", serviceName = "Bridal Makeup",
                amount = 15000.0, status = PaymentStatus.PAID, method = PaymentMethod.UPI, date = today),
            Payment(customerName = "Anita Verma", serviceName = "Party Makeup",
                amount = 5000.0, status = PaymentStatus.UNPAID, method = PaymentMethod.CASH, date = today),
            Payment(customerName = "Meena Patel", serviceName = "Hair Styling",
                amount = 2000.0, status = PaymentStatus.PAID, method = PaymentMethod.CASH, date = today),
            Payment(customerName = "Kavita Singh", serviceName = "Facial",
                amount = 1500.0, status = PaymentStatus.UNPAID, method = PaymentMethod.CASH, date = today),
            Payment(customerName = "Sunita Gupta", serviceName = "Nails",
                amount = 800.0, status = PaymentStatus.PAID, method = PaymentMethod.CARD, date = today),
            Payment(customerName = "Rashmi Dubey", serviceName = "Hair Spa",
                amount = 2500.0, status = PaymentStatus.PAID, method = PaymentMethod.UPI, date = today.minusDays(1)),
            Payment(customerName = "Deepika Jain", serviceName = "Hair Color",
                amount = 3000.0, status = PaymentStatus.PAID, method = PaymentMethod.CARD, date = today.minusDays(1)),
            Payment(customerName = "Priya Sharma", serviceName = "Facial",
                amount = 1500.0, status = PaymentStatus.PAID, method = PaymentMethod.UPI, date = today.minusDays(2)),
            Payment(customerName = "Meena Patel", serviceName = "Waxing",
                amount = 1200.0, status = PaymentStatus.PAID, method = PaymentMethod.CASH, date = today.minusDays(3))
        )
    }
}
