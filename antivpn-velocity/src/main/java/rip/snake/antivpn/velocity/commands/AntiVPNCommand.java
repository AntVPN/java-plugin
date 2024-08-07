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
import rip.snake.antivpn.commons.Service;

public final class AntiVPNCommand {

    public static BrigadierCommand createAntiVPNCommand(Service service) {
        LiteralCommandNode<CommandSource> antivpnNode = LiteralArgumentBuilder
                .<CommandSource>literal("antivpn")
                .requires(source -> (source instanceof ConsoleCommandSource))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("tokenId", StringArgumentType.word())
                        .executes(context -> {
                            String tokenId = context.getArgument("tokenId", String.class);
                            // Assuming a method processTokenId exists to handle the tokenId
                            boolean success = processTokenId(tokenId, service);
                            CommandSource source = context.getSource();
                            if (success) {
                                source.sendMessage(Component.text("Token processed successfully!", NamedTextColor.GREEN));
                            } else {
                                source.sendMessage(Component.text("Failed to process token.", NamedTextColor.RED));
                            }
                            return success ? 1 : 0;
                        })
                )
                .build();
        return new BrigadierCommand(antivpnNode);
    }

    // Assuming this method exists to handle the tokenId
    private static boolean processTokenId(String tokenId, Service service) {
        service.getVpnConfig().setSecret(tokenId);

        service.getAntiVPN().getAntiVPNConfig().withApiKey(tokenId);
        service.getAntiVPN().getSocketManager().reconnect();

        return service.saveConfig();
    }
}
