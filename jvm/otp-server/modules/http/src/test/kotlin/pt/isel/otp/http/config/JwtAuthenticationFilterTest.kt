package pt.isel.otp.http.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import pt.isel.otp.service.JwtService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JwtAuthenticationFilterTest {
    @Mock private lateinit var jwtService: JwtService
    @Mock private lateinit var filterChain: FilterChain

    private lateinit var filter: JwtAuthenticationFilter

    @BeforeEach
    fun setup() {
        filter = JwtAuthenticationFilter(jwtService)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sets authentication for valid Bearer token`() {
        val userId = UUID.randomUUID()
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer valid-token")
        `when`(jwtService.getUserIdFromToken("valid-token")).thenReturn(userId)
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals(userId, auth.principal)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `clears context for invalid token`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer invalid-token")
        `when`(jwtService.getUserIdFromToken("invalid-token")).thenThrow(RuntimeException("Invalid"))
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `passes through when no Authorization header`() {
        val request = MockHttpServletRequest()
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `passes through when Authorization header is blank`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "")
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `passes through when Authorization header lacks Bearer prefix`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Basic some-token")
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `passes through when Authorization header is Bearer with blank token`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer ")
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `handles UUID parse error gracefully`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer invalid-token")
        `when`(jwtService.getUserIdFromToken("invalid-token")).thenThrow(IllegalArgumentException("Invalid UUID"))
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(any(), any())
    }

    @Test
    fun `sets empty authorities list`() {
        val userId = UUID.randomUUID()
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer valid-token")
        `when`(jwtService.getUserIdFromToken("valid-token")).thenReturn(userId)
        filter.doFilter(request, MockHttpServletResponse(), filterChain)
        val auth = SecurityContextHolder.getContext().authentication
        assertTrue(auth.authorities.isEmpty())
    }
}
