package com.aatech.plugin

import com.aatech.config.api_config.CURRENT_VERSION
import com.aatech.routes.health
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



fun Application.configureRouting() {
    routing {
        hello()
        health()
    }
}

private fun Routing.hello() {
    get("/") {
        call.respondText(HelloMessage)
    }
}
