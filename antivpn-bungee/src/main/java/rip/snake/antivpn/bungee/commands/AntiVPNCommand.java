package rip.snake.antivpn.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.utils.TokenCommand;

public class AntiVPNCommand extends Command {

    private final Service service;

    public AntiVPNCommand(Service service) {
        super("antivpn");
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(new TextComponent("You do not have permission to execute this command!"));
            return;
        }

        if (args.length == 1) {
            String tokenId = args[0];
            String error = TokenCommand.validateToken(tokenId);
            if (error != null) {
                sender.sendMessage(new TextComponent(error));
                return;
            }
            boolean success = TokenCommand.processToken(tokenId, service);
            sender.sendMessage(new TextComponent(success ? "Token processed successfully!" : "Failed to process token."));
        } else {
            sender.sendMessage(new TextComponent("Usage: /antivpn <tokenId>"));
        }
    }

}
