package rip.snake.antivpn.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.utils.TokenCommand;

public final class AntiVPNCommand {

    public static BrigadierCommand createAntiVPNCommand(Service service) {
        LiteralCommandNode<CommandSource> antivpnNode = LiteralArgumentBuilder
                .<CommandSource>literal("antivpn")
                .requires(source -> (source instanceof ConsoleCommandSource))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("tokenId", StringArgumentType.word())
                        .executes(context -> {
                            String tokenId = context.getArgument("tokenId", String.class);
                            boolean success = TokenCommand.processToken(tokenId, service);
                            CommandSource source = context.getSource();
                            source.sendMessage(Component.text(
                                    success ? "Token processed successfully!" : "Failed to process token.",
                                    success ? NamedTextColor.GREEN : NamedTextColor.RED
                            ));
                            return success ? 1 : 0;
                        })
                )
                .build();
        return new BrigadierCommand(antivpnNode);
    }

}
