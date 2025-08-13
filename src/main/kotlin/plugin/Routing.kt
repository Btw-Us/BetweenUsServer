package com.aatech.plugin

import com.aatech.config.api_config.CURRENT_VERSION
import com.aatech.routes.allLogInRoutes
import com.aatech.routes.allPersonalChatRoutes
import com.aatech.routes.allUsersRoutes
import com.aatech.routes.authToken
import com.aatech.routes.checkHealth
import com.aatech.routes.personalChatRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val HelloMessage = """
    🚀 BetweenUsServer Chat API v${CURRENT_VERSION}
    
    Welcome to the BetweenUsServer Chat Application!
    
    📱 Features:
    • Real-time messaging
    • User authentication & management
    • Friend relationships
    • File sharing & media support
    • WebSocket connections for live chat
    
    📚 API Documentation:
    • REST API: /api/v1/*
    • WebSocket: /ws/v1/*
    
    🔧 Status: Server is running and ready to connect!
    
    Built with ❤️ using Ktor, MySQL & MongoDB
"""


fun Application.configureRouting(
) {
    routing {
        hello()
        checkHealth()
        authToken()
        allPersonalChatRoutes()
        allLogInRoutes()
        allUsersRoutes()
    }
}

private fun Routing.hello() {
    get("/") {
        call.respondText(HelloMessage)
    }
}
