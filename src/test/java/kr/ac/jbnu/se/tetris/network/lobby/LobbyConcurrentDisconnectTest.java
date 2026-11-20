package kr.ac.jbnu.se.tetris.network.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LobbyConcurrentDisconnectTest {

	private static final int PORT = 18771;

	LobbyServer server;
	Thread.UncaughtExceptionHandler previousHandler;
	final List<Throwable> workerErrors = new CopyOnWriteArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> workerErrors.add(e));
		server = new LobbyServer();
		server.start(PORT);
		Thread.sleep(80);
	}

	@AfterEach
	void tearDown() throws Exception {
		server.close();
		Thread.setDefaultUncaughtExceptionHandler(previousHandler);
	}

	@Test
	void bothClientsDisconnectAtOnce() throws Exception {
		ExecutorService exec = Executors.newFixedThreadPool(2);
		try {
			for (int round = 0; round < 100; round++) {
				LobbyClient host = new LobbyClient();
				LobbyClient guest = new LobbyClient();
				host.connect("127.0.0.1", PORT, "H" + round);
				LobbyMessage created = host.createRoom("room" + round, RoomVisibility.PUBLIC);
				guest.connect("127.0.0.1", PORT, "G" + round);
				guest.joinRoom(created.roomId, null);

				CountDownLatch ready = new CountDownLatch(2);
				CountDownLatch go = new CountDownLatch(1);
				Future<?> f1 = exec.submit(() -> closeWhenReleased(host, ready, go));
				Future<?> f2 = exec.submit(() -> closeWhenReleased(guest, ready, go));
				assertTrue(ready.await(5, TimeUnit.SECONDS));
				go.countDown();
				f1.get(5, TimeUnit.SECONDS);
				f2.get(5, TimeUnit.SECONDS);
			}
			long deadline = System.currentTimeMillis() + 5000;
			while (server.getRegistry().roomCount() > 0 && System.currentTimeMillis() < deadline) {
				Thread.sleep(20);
			}
			assertEquals(0, server.getRegistry().roomCount(), "빈 방은 모두 삭제되어야 한다");
			Thread.sleep(200);
			assertTrue(workerErrors.isEmpty(), "worker 스레드 예외: " + workerErrors);
		} finally {
			exec.shutdownNow();
		}
	}

	@Test
	void remainingPeerReceivesPeerDisconnected() throws Exception {
		try (LobbyClient host = new LobbyClient(); LobbyClient guest = new LobbyClient()) {
			host.connect("127.0.0.1", PORT, "Host");
			BlockingQueue<LobbyMessage> pushes = new LinkedBlockingQueue<>();
			host.setPushHandler(pushes::add);
			LobbyMessage created = host.createRoom("pair", RoomVisibility.PUBLIC);
			guest.connect("127.0.0.1", PORT, "Guest");
			guest.joinRoom(created.roomId, null);

			guest.close();

			LobbyMessage msg = pushes.poll(5, TimeUnit.SECONDS);
			assertNotNull(msg, "PEER_DISCONNECTED 미수신");
			assertEquals(LobbyMessageType.PEER_DISCONNECTED.name(), msg.type);
			assertTrue(workerErrors.isEmpty(), "worker 스레드 예외: " + workerErrors);
		}
	}

	private static void closeWhenReleased(LobbyClient c, CountDownLatch ready, CountDownLatch go) {
		ready.countDown();
		try {
			go.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		c.close();
	}
}
