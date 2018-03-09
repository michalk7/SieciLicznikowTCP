package agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ReceivedConnection extends Thread {
	
	private Socket socket;
	private Agent agent;
	
	public ReceivedConnection(Agent agent, Socket socket) {
		this.agent = agent;
		this.socket = socket;
	}

	@Override
	public void run() {	
		try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);	) {
				
				String inputLine;
				while(((inputLine = in.readLine()) != null)) {
					if(inputLine.equals("ADD")) {	//ADD oznacza, ze mamy dodać adres agenta ktory wysłał nam to polecenie
						inputLine = in.readLine();	//czekamy na jego adres, wyśle go nam
						agent.addNewAgentAdress(inputLine);
					}
					if(inputLine.equals("CLK")) {
						out.println(agent.getTime());
					}
					if(inputLine.equals("NET")) {
						for( String s : agent.getAdresyAgentow()) {
							out.println(s);
						}
						out.println("END");
					}
					if(inputLine.equals("RMV")) {		//RMV oznacza Remove me, usuwamy agenta który nam to wysłał z naszej listy kontaktów
						agent.getAdresyAgentow().remove(in.readLine());
						inputLine = "SYN";		//pózniej musimy dokonać synchronizacji
					}
					if(inputLine.equals("SYN")) {	
						for(String fullAdress : agent.getAdresyAgentow()) {
							String[] com = {"CLK"};
							new CreatedConnection(agent, fullAdress, false, com).start();
						}
						agent.getCzasyOdAgentow().add(agent.getTime());
						while(agent.getCzasyOdAgentow().size() != (agent.getAdresyAgentow().size()+1)) {
							//musimy mieć czasy od każdego, plus 1, bo na liście kontaktów nie mamy siebie, a dodajemy swój czas
						}
						long suma = 0L;
						for (Long t : agent.getCzasyOdAgentow()) {
							suma += t;
						}
						agent.setTime(suma / agent.getCzasyOdAgentow().size());
						agent.getCzasyOdAgentow().clear();
					}
					if(inputLine.equals("EXIT")) {		//EXIT oznacza, że ktoś nas wyprasza z sieci, mamy się usunąć i wyłączyć
						for(String fullAdress : agent.getAdresyAgentow()) {
							String[] com = {"RMV"}; //remove me from list
							new CreatedConnection(agent, fullAdress, false, com).start();
						}
						agent.getListeningThread().interrupt();
						agent.getTimer().cancel();
						agent.getTimer().purge();
						if(agent.isIndependent()) {
							Thread.sleep(200);
							System.exit(0);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
	
	
}
