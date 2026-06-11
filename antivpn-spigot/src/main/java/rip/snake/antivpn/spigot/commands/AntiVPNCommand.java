package rip.snake.antivpn.spigot.commands;

import io.antivpn.api.model.request.CheckRequest;
import io.antivpn.api.model.request.UserData;
import io.antivpn.api.model.response.CheckResponse;
import io.antivpn.api.util.Event;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.utils.TokenCommand;

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

        String error = TokenCommand.validateToken(tokenId);
        if (error != null) {
            sender.sendMessage(error);
            return false;
        }

        if (tokenId.equals("check")) {
            return runDebugCheck(1000);
        } else if (tokenId.equalsIgnoreCase("check2")) {
            return runDebugCheck2();
        }

        boolean success = TokenCommand.processToken(tokenId, service);
        sender.sendMessage(success ? "Token processed successfully!" : "Failed to process token.");
        return true;
    }

    private boolean runDebugCheck(int count) {
        if (!service.getVpnConfig().isDebug()) return false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        CountDownLatch latch = new CountDownLatch(count);
        long startTime = System.nanoTime();

        for (int i = 0; i < count; i++) {
            int firstOctet = random.nextInt(256);
            int secondOctet = random.nextInt(256);
            int thirdOctet = random.nextInt(256);
            int fourthOctet = random.nextInt(256);
            String randomIp = firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet;
            String username = "username_" + firstOctet + "_" + secondOctet + "_" + thirdOctet + "_" + fourthOctet;
            String userId = "uuid_" + firstOctet + "_" + secondOctet + "_" + thirdOctet + "_" + fourthOctet;

            CompletableFuture<CheckResponse> future = service.getAntiVPN().getSocketManager().getSocketDataHandler().verify(
                    new CheckRequest(randomIp, userId, username)
            );
            future.thenAccept(c -> latch.countDown()).exceptionally(e -> {
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

        double realDuration = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.printf("Handled %d requests in just %.2f ms.%n", count, realDuration);
        return true;
    }

    private boolean runDebugCheck2() {
        if (!service.getVpnConfig().isDebug()) return false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        CountDownLatch latch = new CountDownLatch(100);
        long startTime = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            int firstOctet = random.nextInt(256);
            int secondOctet = random.nextInt(256);
            int thirdOctet = random.nextInt(256);
            int fourthOctet = random.nextInt(256);
            String randomIp = firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet;
            String username = "username_" + firstOctet + "_" + secondOctet + "_" + thirdOctet + "_" + fourthOctet;
            String userId = "uuid_" + firstOctet + "_" + secondOctet + "_" + thirdOctet + "_" + fourthOctet;

            CompletableFuture<CheckResponse> future = service.getAntiVPN().getSocketManager().getSocketDataHandler().verify(
                    new CheckRequest(randomIp, userId, username)
            );
            future.thenAccept(calling -> {
                service.getAntiVPN().getSocketManager().getSocketDataHandler().sendUserData(
                        UserData.builder()
                                .sessionId(calling.getSessionId())
                                .username(username)
                                .userId(userId)
                                .version("1.21.4")
                                .address(randomIp)
                                .server("survival")
                                .hostname("hostname")
                                .event(Event.PLAYER_JOIN)
                                .premium(true)
                                .build()
                );
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        double realDuration = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.printf("Handled 100 requests in just %.2f ms.%n", realDuration);
        return true;
    }
}
