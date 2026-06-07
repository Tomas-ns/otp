package pt.isel.otp.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import pt.isel.otp.domain.enums.PredictionType
import java.time.Instant

@Entity
@Table(name = "predictions")
class Prediction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    val station: Station,
    @Column(nullable = false)
    val occupancyLevel: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val type: PredictionType,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)
