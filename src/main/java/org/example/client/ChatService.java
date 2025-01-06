package org.example.client;

import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChatService {
    private static final int BUFFER_SIZE = 1024;
    private String oponentIp;
    private int oponentPort;
    private String oponentNick;
    private volatile boolean running = true;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Consumer<String> messageCallback;

    public void startServer(Consumer<String> onMessageReceived) {
        this.messageCallback = onMessageReceived;
        if (selector != null && selector.isOpen()) {
            stopServer();
        }

        Thread serverThread = new Thread(() -> {
            running = true;
            try {
                getIpPortUsername();
                initializeServer();
                runServerLoop();
            } catch (Exception e) {
                System.err.println("Błąd serwera czatu: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void initializeServer() throws IOException {
        System.out.println("Inicjalizacja serwera czatu na porcie: " + ClientStart.clientPort);
        this.selector = Selector.open();
        this.serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(ClientStart.clientPort));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Serwer czatu uruchomiony na porcie: " + ClientStart.clientPort);
    }

    private void runServerLoop() {
        while (running) {
            try {

                if (ClientStart.getServerService().getPlayerNumber(ClientStart.roomToken) != 2) {
                    System.out.println("Przeciwnik opuścił pokój, oczekiwanie na nowego gracza...");
                    waitingForNewPlayer();
                    return;
                }

                if (selector.select(1000) > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid()) continue;

                        try {
                            if (key.isAcceptable()) {
                                handleAccept(key);
                            } else if (key.isReadable()) {
                                handleRead(key);
                            }
                        } catch (IOException e) {
                            System.err.println("Błąd podczas obsługi połączenia: " + e.getMessage());
                            key.cancel();
                        }
                    }
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("Błąd w pętli serwera: " + e.getMessage());
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Nowe połączenie zaakceptowane");
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            clientChannel.close();
            key.cancel();
            System.out.println("Połączenie zamknięte przez klienta");
        } else {
            buffer.flip();
            String message = new String(buffer.array(), 0, buffer.limit());
            if (messageCallback != null) {
                Platform.runLater(() -> messageCallback.accept(message));
            }
        }
    }

    private void waitingForNewPlayer() {
        System.out.println("Oczekiwanie na nowego gracza...");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (ClientStart.getServerService().getPlayerNumber(ClientStart.roomToken) == 2) {
                    scheduler.shutdown();
                    getIpPortUsername();
                    System.out.println("Znaleziono nowego gracza: " + oponentNick);


                    Platform.runLater(() -> startServer(messageCallback));
                }
            } catch (RemoteException e) {
                System.err.println("Błąd sprawdzania gotowości pokoju: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void sendMessage(String message) {
        if (oponentIp == null || oponentPort == 0) {
            System.err.println("Brak danych przeciwnika - nie można wysłać wiadomości");
            return;
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.connect(new InetSocketAddress(oponentIp, oponentPort));
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                socketChannel.write(buffer);
                System.out.println("Wiadomość wysłana: " + message);
                return;
            } catch (IOException e) {
                System.err.println("Próba " + (attempt + 1) + " wysłania wiadomości nie powiodła się: " + e.getMessage());
                if (attempt < 2) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    public void getIpPortUsername() throws RemoteException {
        try {
            String result = ClientStart.roomService.getOponent(ClientStart.roomToken, ClientStart.userToken);
            if (result == null || result.isEmpty()) {
                System.err.println("Nie można pobrać informacji o oponencie - brak danych.");
                return;
            }

            String[] parts = result.split("/");
            if (parts.length < 3) {
                System.err.println("Nieprawidłowy format danych przeciwnika: " + result);
                return;
            }

            String newIp = parts[0];
            int newPort = Integer.parseInt(parts[1]);
            String newNick = parts[2];

            if (newIp != null && !newIp.isEmpty() && newPort > 0) {
                oponentIp = newIp;
                oponentPort = newPort;
                oponentNick = newNick;
                System.out.println("Zaktualizowano dane przeciwnika: " + oponentNick + " (" + oponentIp + ":" + oponentPort + ")");
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania danych przeciwnika: " + e.getMessage());
            throw new RemoteException("Błąd pobierania danych przeciwnika", e);
        }
    }

    public void stopServer() {
        System.out.println("Zatrzymywanie serwera czatu...");
        running = false;

        if (selector != null) {
            try {

                for (SelectionKey key : selector.keys()) {
                    try {
                        if (key.channel() != null) {
                            key.channel().close();
                            key.cancel();
                        }
                    } catch (IOException ex) {
                        System.err.println("Błąd podczas zamykania kanału: " + ex.getMessage());
                    }
                }


                try {
                    if (serverSocketChannel != null) {
                        serverSocketChannel.close();
                    }
                } catch (IOException e) {
                    System.err.println("Błąd podczas zamykania serverSocketChannel: " + e.getMessage());
                }

                try {
                    selector.close();
                } catch (IOException e) {
                    System.err.println("Błąd podczas zamykania selektora: " + e.getMessage());
                }

                selector = null;
                serverSocketChannel = null;

                System.out.println("Serwer czatu zatrzymany");
            } catch (Exception e) {
                System.err.println("Błąd podczas zamykania serwera: " + e.getMessage());
            }
        }


        oponentIp = null;
        oponentPort = 0;
        oponentNick = null;
    }

    public String getOponentNick() {
        return oponentNick != null ? oponentNick : "Nieznany przeciwnik";
    }
}