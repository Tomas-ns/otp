package pt.isel.otp.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.enums.PredictionType
import java.util.Optional

interface PredictionRepository : JpaRepository<Prediction, Long> {
    fun findFirstByStation_IdAndTypeOrderByCreatedAtDesc(
        stationId: String,
        type: PredictionType,
    ): Optional<Prediction>

    @Query(
        """
        SELECT p FROM Prediction p
        WHERE p.id IN (
            SELECT MAX(p2.id) FROM Prediction p2
            WHERE p2.type = :type
            GROUP BY p2.station.id
        )
        """,
    )
    fun findLatestByType(type: PredictionType): List<Prediction>
}
