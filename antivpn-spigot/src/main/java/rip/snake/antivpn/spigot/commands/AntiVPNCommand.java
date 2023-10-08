package rip.snake.antivpn.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.snake.antivpn.core.Service;

public final class AntiVPNCommand implements CommandExecutor {
    private final Service service;

    public AntiVPNCommand(Service service) { this.service = service; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage("This command can only be run from the console.");
            return false;
        }
        
        if (args.length == 0) {
            sender.sendMessage("Please provide a tokenId.");
            return false;
        }

        String tokenId = args[0];
        boolean success = processTokenId(tokenId, service);
        sender.sendMessage(success ? "Token processed successfully!" : "Failed to process token.");
        return true;
    }

    private boolean processTokenId(String tokenId, Service service) {
        service.getVpnConfig().setSecret(tokenId);
        service.getSocketManager().reconnect();
        return service.saveConfig();
    }
}
