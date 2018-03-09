package agent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Agent {

	private long time = 0L;
	private int port;
	private String IPadress;
	private List<String> adresyAgentow;
	private List<Long> czasyOdAgentow;
	private Timer timer;
	private Thread listeningThread;
	private boolean independent;		//to oznacza czy agent jest samodzielnym programem czy wywyołany z monitora
	
	public Agent(long time, int port) {
		adresyAgentow = new ArrayList<>();
		czasyOdAgentow = new ArrayList<>();
		this.time = time;
		this.port = port;
		independent = true;
		startTimer();
		listeningThread = new Thread(() -> listenOnPort());
		listeningThread.start();
	}
	
	public Agent(long time, int port, String IPAgentaWprowadzajacego, int portAgentaWprowadzajacego) {
		this(time, port, IPAgentaWprowadzajacego, portAgentaWprowadzajacego, true);
	}
	
	public Agent(long time, int port, String IPAgentaWprowadzajacego, int portAgentaWprowadzajacego, boolean independent) {
		adresyAgentow = new ArrayList<>();
		czasyOdAgentow = new ArrayList<>();
		this.time = time;
		this.port = port;
		this.independent = independent;
		startTimer();
		listeningThread = new Thread(() -> listenOnPort());
		listeningThread.start();
		new CreatedConnection(this, IPAgentaWprowadzajacego, portAgentaWprowadzajacego, true, null).start();;
	}
	
	private void startTimer() {
		System.out.println("Time na wejsciu " + time);
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				time += 1;
			}
			
		}, 0, 1);
	}
	
	public boolean isIndependent() {
		return independent;
	}

	public Timer getTimer() {
		return timer;
	}

	public Thread getListeningThread() {
		return listeningThread;
	}

	public synchronized void setAdresyAgentow(List<String> adresyAgentow) {
		this.adresyAgentow = adresyAgentow;
	}

	public synchronized List<String> getAdresyAgentow() {
		return adresyAgentow;
	}
	
	public synchronized void addNewAgentAdress(String adres) {
		adresyAgentow.add(adres);
	}
	
	public synchronized void addTimeToList(long time) {
		czasyOdAgentow.add(time);
	}

	public synchronized void setTime(long time) {
		this.time = time;
	}

	public synchronized long getTime() {
		return time;
	}

	public synchronized List<Long> getCzasyOdAgentow() {
		return czasyOdAgentow;
	}

	public int getPort() {
		return port;
	}

	public String getIPadress() {
		return IPadress;
	}

	private void listenOnPort() {
		try(ServerSocket listenSocket = new ServerSocket(port);) {
			port = listenSocket.getLocalPort();
			System.out.println("Agent is listening on port: " + port);
			IPadress = listenSocket.getInetAddress().getHostAddress();
			while(true) {
				Socket newAgentSocket = listenSocket.accept();
				new ReceivedConnection(this, newAgentSocket).start();
			}
		} catch (IOException e) {
			System.err.println("Cannot bind listening socket");
			e.printStackTrace();
		}
	}
}
