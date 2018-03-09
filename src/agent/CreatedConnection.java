package agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class CreatedConnection extends Thread {

	private Agent agent;
	private boolean connectionWithMaster;
	private String IPAgenta;
	private int portAgenta;
	private String[] commandsToRun;

	public CreatedConnection(Agent agent, String IPAgenta, int portAgenta, boolean connectionWithMaster, String[] commandsToRun) {
		this.agent = agent;
		this.IPAgenta = IPAgenta;
		this.portAgenta = portAgenta;
		this.connectionWithMaster = connectionWithMaster;
		this.commandsToRun = commandsToRun;
	}
	
	public CreatedConnection(Agent agent, String adress, boolean connectionWithMaster, String[] commandsToRun) {
		this.agent = agent;
		String[] tmp = adress.split("\\:");
		this.IPAgenta = tmp[0];
		int p = Integer.parseInt(tmp[1]);
		this.portAgenta = p;
		this.connectionWithMaster = connectionWithMaster;
		this.commandsToRun = commandsToRun;
	}

	@Override
	public void run() {
		try(Socket socket = new Socket(IPAgenta, portAgenta);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			if(connectionWithMaster) {		//tu następuje proces pierwszego połączenia, jest to połączenie z agentem wprowadzającym
				agent.addNewAgentAdress(IPAgenta + ":" + portAgenta);
				add(socket, out);
				net(socket, out, in);
				for(String fullAdress : agent.getAdresyAgentow()) {	//początek synchronizacji
					if(!fullAdress.equals(IPAgenta+":"+portAgenta)) {
						String[] com = {"ADD", "CLK"};
						new CreatedConnection(agent, fullAdress, false, com).start();
					} else {
						clk(socket, out, in);
					}
				}
				agent.addTimeToList(agent.getTime());
				while(agent.getCzasyOdAgentow().size() != (agent.getAdresyAgentow().size()+1)) {
					
				}
				long suma = 0L;
				for (Long t : agent.getCzasyOdAgentow()) {
					suma += t;
				}
				agent.setTime(suma / agent.getCzasyOdAgentow().size());
				agent.getCzasyOdAgentow().clear();	//koniec synchronizacji
				for(String fullAdress : agent.getAdresyAgentow()) {		//Wysyłamy polecenie SYN do agentów, bo sami już to zrobiliśmy
					if(!fullAdress.equals(IPAgenta+":"+portAgenta)) {
						String[] com = {"SYN"};
						new CreatedConnection(agent, fullAdress, false, com).start();
					} else {
						syn(socket, out);
					}
				}
				close(socket, out);
			} else {
				for(String com : commandsToRun) {
					if(com.equals("ADD")) {
						add(socket, out);
					}
					if(com.equals("CLK")) {
						clk(socket, out, in);
					}
					if(com.equals("NET")) {
						net(socket, out, in);
					}
					if(com.equals("SYN")) {
						syn(socket, out);
					}
					if(com.equals("RMV")) {
						rmv(socket, out);
					}
				}
				close(socket, out);
			}
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void add(Socket socket, PrintWriter out) {
		out.println("ADD");
		String agentAdress = socket.getLocalAddress().getHostAddress() + ":" + agent.getPort();
		out.println(agentAdress);
	}
	
	private synchronized void net(Socket socket, PrintWriter out, BufferedReader in) throws IOException {
		out.println("NET");
		String agentAdress = socket.getLocalAddress().getHostAddress() + ":" + agent.getPort();
		String inputLine;
		while(!(inputLine = in.readLine()).equals("END")) {
			if(!inputLine.equals(agentAdress))
				agent.addNewAgentAdress(inputLine);
		}
		System.out.println("Ksiażka adresowa odebrana: " + agent.getAdresyAgentow());
	}
	
	private synchronized void clk(Socket socket, PrintWriter out, BufferedReader in) throws IOException {
		out.println("CLK");
		String inputLine;
		inputLine = in.readLine();
		try {
			long t = Long.parseLong(inputLine);
			agent.addTimeToList(t);
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Z odpowiedzi CLK przyszło: " + inputLine);
		}
	}
	
	private synchronized void syn(Socket socket, PrintWriter out) {
		out.println("SYN");
	}
	
	private synchronized void close(Socket socket, PrintWriter out) throws IOException	{
		out.println("CLOSE");
		socket.close();
	}
	
	private synchronized void rmv(Socket socket, PrintWriter out)	{
		String agentAdress = socket.getInetAddress().getHostAddress() + ":" + agent.getPort();
		out.println("RMV");
		out.println(agentAdress);
	}
	
}
