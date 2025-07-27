package com.aatech.routes

import com.aatech.config.api_config.LoginRoutes
import com.aatech.config.api_config.checkAuth
import com.aatech.database.mysql.model.User
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Routing.userRoutes() {
    post(LoginRoutes.LogInOrRegister.path) {
        checkAuth { checkAuth ->
            val loggedUser = call.receive<User>()
//            TODO: Implement login or register logic from here 
        }
    }
}
///What will be the login machanism for this should whatsapp send body with encryption or without