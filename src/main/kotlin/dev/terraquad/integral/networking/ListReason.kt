package dev.terraquad.integral.networking

enum class ListReason {
    JOIN,
    RELOAD,
    SET_MODPACK,
    GET_COMMAND;

    fun friendlyString(): String = when (this) {
        JOIN -> "join"
        RELOAD -> "reload"
        SET_MODPACK -> "set modpack"
        GET_COMMAND -> "get command"
    }
}