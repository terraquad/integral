package dev.terraquad.integral.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder

@FunctionalInterface
interface Subcommand<S> {
    fun getBuilder(): LiteralArgumentBuilder<S>
}