package pt.isel.otp.repository

import org.springframework.data.jpa.repository.JpaRepository
import pt.isel.otp.domain.entity.Station

interface StationRepository : JpaRepository<Station, String>
