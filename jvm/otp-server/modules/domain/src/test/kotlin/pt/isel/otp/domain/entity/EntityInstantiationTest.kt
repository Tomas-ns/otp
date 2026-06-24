package pt.isel.otp.domain.entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pt.isel.otp.domain.enums.*
import java.net.InetAddress
import java.time.Instant
import java.util.UUID

class EntityInstantiationTest {
    @Test
    fun `Station created with all fields`() {
        val s = Station(id = "test_station", name = "Test Station", latitude = 38.7, longitude = -9.1, transportType = TransportType.METRO)
        assertEquals("test_station", s.id)
        assertEquals("Test Station", s.name)
        assertEquals(38.7, s.latitude)
        assertEquals(-9.1, s.longitude)
        assertEquals(TransportType.METRO, s.transportType)
    }

    @Test
    fun `Station with TRAIN transport type`() {
        val s = Station(id = "train_station", name = "Train Station", latitude = 38.7, longitude = -9.1, transportType = TransportType.TRAIN)
        assertEquals(TransportType.TRAIN, s.transportType)
    }

    @Test
    fun `User created with default values`() {
        val u = User(email = "test@test.com")
        assertNull(u.id)
        assertEquals("test@test.com", u.email)
        assertEquals(UserStatus.ACTIVE, u.status)
        assertNotNull(u.createdAt)
        assertNotNull(u.updatedAt)
        assertNull(u.lastLoginAt)
    }

    @Test
    fun `User with DISABLED status`() {
        val u = User(email = "disabled@test.com", status = UserStatus.DISABLED)
        assertEquals(UserStatus.DISABLED, u.status)
    }

    @Test
    fun `User with DELETED status`() {
        val u = User(email = "deleted@test.com", status = UserStatus.DELETED)
        assertEquals(UserStatus.DELETED, u.status)
    }

    @Test
    fun `User with all optional fields`() {
        val u = User(
            email = "full@test.com",
            displayName = "Full User",
            avatarUrl = "http://avatar",
            status = UserStatus.ACTIVE,
        )
        assertEquals("Full User", u.displayName)
        assertEquals("http://avatar", u.avatarUrl)
    }

    @Test
    fun `Prediction created with all fields`() {
        val station = Station("s", "S", 1.0, 2.0, TransportType.METRO)
        val user = User(email = "u@u.com")
        val p = Prediction(station = station, user = user, occupancyLevel = 4, type = PredictionType.COMPLETE)
        assertNull(p.id)
        assertEquals(station, p.station)
        assertEquals(user, p.user)
        assertEquals(4, p.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, p.type)
        assertNotNull(p.createdAt)
    }

    @Test
    fun `Prediction without user for LIMITED type`() {
        val station = Station("s", "S", 1.0, 2.0, TransportType.METRO)
        val p = Prediction(station = station, user = null, occupancyLevel = 2, type = PredictionType.LIMITED)
        assertNull(p.user)
        assertEquals(PredictionType.LIMITED, p.type)
    }

    @Test
    fun `Prediction with explicit id`() {
        val station = Station("s", "S", 1.0, 2.0, TransportType.METRO)
        val p = Prediction(id = 42L, station = station, occupancyLevel = 5, type = PredictionType.COMPLETE)
        assertEquals(42L, p.id)
    }

    @Test
    fun `OAuthAccount created correctly`() {
        val user = User(email = "oauth@test.com")
        val oa = OAuthAccount(
            user = user,
            provider = OAuthProvider.GOOGLE,
            providerSubject = "google-id-123",
            providerEmail = "oauth@test.com",
            providerUsername = "OAuthUser",
        )
        assertNull(oa.id)
        assertEquals(user, oa.user)
        assertEquals(OAuthProvider.GOOGLE, oa.provider)
        assertEquals("google-id-123", oa.providerSubject)
        assertEquals("oauth@test.com", oa.providerEmail)
        assertEquals("OAuthUser", oa.providerUsername)
        assertNotNull(oa.createdAt)
        assertNull(oa.lastUsedAt)
    }

    @Test
    fun `OAuthAccount with null optional fields`() {
        val user = User(email = "min@test.com")
        val oa = OAuthAccount(user = user, provider = OAuthProvider.GOOGLE, providerSubject = "sub")
        assertNull(oa.providerEmail)
        assertNull(oa.providerUsername)
    }

    @Test
    fun `RefreshToken created correctly`() {
        val user = User(email = "rt@test.com")
        val rt = RefreshToken(
            user = user,
            tokenHash = "a" .repeat(64),
            expiresAt = Instant.now().plusSeconds(86400),
            userAgent = "Chrome",
            ipAddress = InetAddress.getByName("127.0.0.1"),
        )
        assertNull(rt.id)
        assertEquals(user, rt.user)
        assertEquals(64, rt.tokenHash.length)
        assertNotNull(rt.expiresAt)
        assertNull(rt.revokedAt)
        assertNotNull(rt.createdAt)
        assertEquals("Chrome", rt.userAgent)
        assertEquals(InetAddress.getByName("127.0.0.1"), rt.ipAddress)
    }

    @Test
    fun `RefreshToken without optional fields`() {
        val user = User(email = "rt2@test.com")
        val rt = RefreshToken(user = user, tokenHash = "b".repeat(64), expiresAt = Instant.now().plusSeconds(3600))
        assertNull(rt.userAgent)
        assertNull(rt.ipAddress)
        assertNull(rt.replacedBy)
    }

    @Test
    fun `RefreshToken with replacedBy chain`() {
        val user = User(email = "chain@test.com")
        val rt1 = RefreshToken(user = user, tokenHash = "c".repeat(64), expiresAt = Instant.now().plusSeconds(3600))
        val rt2 = RefreshToken(user = user, tokenHash = "d".repeat(64), expiresAt = Instant.now().plusSeconds(7200))
        rt1.replacedBy = rt2
        assertEquals(rt2, rt1.replacedBy)
    }

    @Test
    fun `User updatedAt is mutable`() {
        val u = User(email = "mutable@test.com")
        val old = u.updatedAt
        u.updatedAt = Instant.now().plusSeconds(10)
        assertTrue(u.updatedAt.isAfter(old))
    }

    @Test
    fun `User lastLoginAt is mutable`() {
        val u = User(email = "login@test.com")
        val now = Instant.now()
        u.lastLoginAt = now
        assertEquals(now, u.lastLoginAt)
    }

    @Test
    fun `OAuthAccount lastUsedAt is mutable`() {
        val user = User(email = "used@test.com")
        val oa = OAuthAccount(user = user, provider = OAuthProvider.GOOGLE, providerSubject = "sub")
        assertNull(oa.lastUsedAt)
        val now = Instant.now()
        oa.lastUsedAt = now
        assertEquals(now, oa.lastUsedAt)
    }
}
