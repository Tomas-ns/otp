package pt.isel.otp.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.isel.otp.domain.entity.User
import pt.isel.otp.domain.enums.UserStatus
import java.util.UUID

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `save and find user by id`() {
        val user = User(email = "test@test.com", displayName = "Test")
        val saved = userRepository.save(user)
        assertNotNull(saved.id)
        val found = userRepository.findById(saved.id!!)
        assertTrue(found.isPresent)
        assertEquals("test@test.com", found.get().email)
    }

    @Test
    fun `findById returns empty for unknown UUID`() {
        assertFalse(userRepository.findById(UUID.randomUUID()).isPresent)
    }

    @Test
    fun `save user with DISABLED status`() {
        val user = User(email = "disabled@test.com", status = UserStatus.DISABLED)
        val saved = userRepository.save(user)
        assertEquals(UserStatus.DISABLED, saved.status)
    }

    @Test
    fun `save user with DELETED status`() {
        val user = User(email = "deleted@test.com", status = UserStatus.DELETED)
        val saved = userRepository.save(user)
        assertEquals(UserStatus.DELETED, saved.status)
    }

    @Test
    fun `save user without email`() {
        val user = User(displayName = "No Email")
        val saved = userRepository.save(user)
        assertNull(saved.email)
    }

    @Test
    fun `findAll returns all users`() {
        userRepository.save(User(email = "a@a.com"))
        userRepository.save(User(email = "b@b.com"))
        assertEquals(2, userRepository.findAll().size)
    }

    @Test
    fun `delete user`() {
        val user = userRepository.save(User(email = "del@del.com"))
        userRepository.delete(user)
        assertFalse(userRepository.findById(user.id!!).isPresent)
    }
}
