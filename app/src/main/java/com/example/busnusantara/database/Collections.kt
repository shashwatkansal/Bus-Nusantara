package com.example.busnusantara.database

// Collections to have in Database
enum class Collections {
    ROUTES {
        override fun toString(): String {
            return "Routes"
        }
    },
    ORDERS {
        override fun toString(): String {
            return "Orders"
        }
    },
    AGENTS {
        override fun toString(): String {
            return "Agents"
        }
    },
}