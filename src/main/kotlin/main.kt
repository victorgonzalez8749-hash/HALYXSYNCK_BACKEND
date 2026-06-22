package com.halyxsync

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database

fun main() {

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    //  CONEXIÓN A RAILWAY
    Database.connect(
        url = "jdbc:mysql://yamanote.proxy.rlwy.net:15333/railway",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "wCBmaDvXklgIKBSrKfmtaLYbHMxwLGib"
    )

    println("==================================================")
    println("Conexion exitosa a la base de datos en la nube!")
    println("==================================================")

    configureSerialization()
    configureRouting()
}