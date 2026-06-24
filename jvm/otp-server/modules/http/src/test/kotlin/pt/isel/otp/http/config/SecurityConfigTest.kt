package pt.isel.otp.http.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import pt.isel.otp.http.controller.AuthController
import pt.isel.otp.service.AuthService

@WebMvcTest(controllers = [AuthController::class])
@Import(SecurityConfig::class)
class SecurityConfigTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @Test
    fun `auth endpoints are publicly accessible`() {
        mockMvc.perform(
            post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"idToken":"test-token"}""")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `auth refresh endpoint is publicly accessible`() {
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"test"}""")
        )
            .andExpect(status().isOk)
    }
}
