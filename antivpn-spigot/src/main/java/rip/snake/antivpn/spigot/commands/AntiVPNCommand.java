package rip.snake.antivpn.spigot.commands;

import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.snake.antivpn.commons.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public final class AntiVPNCommand implements CommandExecutor {
    private final Service service;

    public AntiVPNCommand(Service service) {
        this.service = service;
    }

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

        if (tokenId.equals("check")) {
            if (!service.getVpnConfig().isDebug()) return false;
            ThreadLocalRandom random = ThreadLocalRandom.current();

            CountDownLatch latch = new CountDownLatch(1000);

            long startTime = System.nanoTime();

            for (int i = 0; i < 1000; i++) {
                int firstOctet = random.nextInt(256);
                int secondOctet = random.nextInt(256);
                int thirdOctet = random.nextInt(256);
                int fourthOctet = random.nextInt(256);
                String randomIp = firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet;

                CompletableFuture<CheckResponse> checkResponseWatchableInvoker = service.getAntiVPN().getSocketManager().getSocketDataHandler().verify(
                        new CheckRequest(randomIp, firstOctet + "_" + secondOctet + "_" + thirdOctet + "_" + fourthOctet)
                );

                checkResponseWatchableInvoker.thenAccept(calling -> {
                    latch.countDown();
                }).exceptionally(e -> {
                    e.printStackTrace();
                    latch.countDown();
                    return null;
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            long endTime = System.nanoTime() - startTime;
            long duration = endTime / 1000;

            double realDuration = duration / 1000.0;
            System.out.printf("Handled 1000 requests in just %.2f seconds.", realDuration).println();
            return true;
        }

        boolean success = processTokenId(tokenId, service);
        sender.sendMessage(success ? "Token processed successfully!" : "Failed to process token.");
        return true;
    }

    private boolean processTokenId(String tokenId, Service service) {
        service.getVpnConfig().setSecret(tokenId);
        service.getAntiVPN().getAntiVPNConfig().withApiKey(tokenId);
        service.getAntiVPN().getSocketManager().reconnect();
        return service.saveConfig();
    }
}
