package pt.isel.otp.repository

import org.springframework.data.jpa.repository.JpaRepository
import pt.isel.otp.domain.entity.User
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>
