package rip.snake.antivpn.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import rip.snake.antivpn.commons.Service;

public class AntiVPNCommand extends Command {

    private final Service service;

    public AntiVPNCommand(Service service) {
        super("antivpn");  // Command name
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
            boolean success = processTokenId(tokenId, service);
            if (success) {
                sender.sendMessage(new TextComponent("Token processed successfully!"));
            } else {
                sender.sendMessage(new TextComponent("Failed to process token."));
            }
        } else {
            sender.sendMessage(new TextComponent("Usage: /antivpn <tokenId>"));
        }
    }

    private boolean processTokenId(String tokenId, Service service) {
        service.getVpnConfig().setSecret(tokenId);
        return service.saveConfig();
    }
}
