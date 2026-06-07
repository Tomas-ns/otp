package pt.isel.otp.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import pt.isel.otp.domain.enums.TransportType

@Entity
@Table(name = "stations")
class Station(
    @Id
    @Column(length = 64)
    val id: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val latitude: Double,
    @Column(nullable = false)
    val longitude: Double,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val transportType: TransportType,
)
