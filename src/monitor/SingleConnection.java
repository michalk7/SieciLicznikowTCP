package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SingleConnection extends Thread {
	
	private MonitorGUI master;
	private String fullAdress;
	private String IPAdress;
	private int portAgenta;
	private static int counter = 0;
	private int number;		//-1 polaczenie administracyjne, 0 polaczenie odczytujace czas agenta od polaczenia administracyjnego, dalej to kolejne polaczenia czasu
	private long time;
	private boolean stop;
	
	public SingleConnection(MonitorGUI master, String adress) {
		counter++;
		number = counter - 2;
		this.master = master;
		this.fullAdress = adress;
		String[] tmp = adress.split("\\:");
		this.IPAdress = tmp[0];
		int p = Integer.parseInt(tmp[1]);
		this.portAgenta = p;
	}

	@Override
	public void run() {
		try(Socket socket = new Socket(IPAdress, portAgenta);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))	) {
			
			if(counter == 1) {  //polaczenie administracyjne
				master.addNewAgentAdress(fullAdress);
				net(socket, out, in);
				for(String s : master.getAdresyAgentow()) {
					SingleConnection con = new SingleConnection(master, s);
					master.addNewAgentConnection(con);
					con.start();
				}
				while(!stop) {
					Thread.sleep(500);
					List<String> tmp = laterNet(socket, out, in);
					if(tmp.containsAll(master.getAdresyAgentow())) {
						tmp.removeAll(master.getAdresyAgentow());
						master.getAdresyAgentow().addAll(tmp);
						int i = number;
						for(String s : tmp) {
							SingleConnection con = new SingleConnection(master, s);
							i++;
							master.addNewAgentConnection(con);
							master.getModel().fireTableRowsInserted(i, i);
							con.start();
						}
					} else {
						master.getAdresyAgentow().retainAll(tmp);
					}
				}
			} else {
				while(!stop) {
					Thread.sleep(100);
					out.println("CLK");
					Long l = Long.parseLong(in.readLine());
					master.getModel().setValueAt(l, number, 1);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	
	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		SingleConnection.counter = counter;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getFullAdress() {
		return fullAdress;
	}
	
	public void setFullAdress(String adress) {
		fullAdress = adress;
	}
	
	public synchronized long getTime() {
		return time;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	private synchronized void net(Socket socket, PrintWriter out, BufferedReader in) throws IOException {
		out.println("NET");
		String inputLine;
		while(!(inputLine = in.readLine()).equals("END")) {
			master.addNewAgentAdress(inputLine);
		}
	}
	
	private synchronized List<String> laterNet(Socket socket, PrintWriter out, BufferedReader in) throws IOException {
		out.println("NET");
		String inputLine;
		List<String> tmp = new ArrayList<>();
		tmp.add(fullAdress);
		while(!(inputLine = in.readLine()).equals("END")) {
			tmp.add(inputLine);
		}
		return tmp;
	}

	public void setTime(Long val) {
		time = val;
	}
	
	public void setTimeTimer(Long time) {
		this.time = time;
		master.getModel().fireTableCellUpdated(number, 1);
	}
}
