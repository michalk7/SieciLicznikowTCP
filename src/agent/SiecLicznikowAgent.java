package agent;

import java.util.List;


public class SiecLicznikowAgent {
	static long time = 0L;
	static List<String> adresyAgentow;
	
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.err.println("usage: java SiecLicznikowAgent <wartosc licznika> <port> <IP agenta wprowadzajacego> <port agenta wprowadzajacego>");
			System.exit(1);
		}
		
		try {
			time = Long.parseLong(args[0]);
		} catch (Exception e) {
			System.err.println("Podaj prawidłowo czas!");
			System.exit(2);
		};
		
		//tu mamy wartosc domyslna dla portu, zero oznacza, że program sam sobie znajdzie wolny port
		int portAgenta = 0;
		
		switch(args.length) {
		case 1:
			new Agent(time, portAgenta);
			break;
		case 2:
			try {
				portAgenta = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.err.println("Niepoprawnie wprowadzony nr portu nowego Agenta");
				System.exit(3);
			}
			new Agent(time, portAgenta);
			break;
		case 3:
			String agentWIP = "";
			if(args[1].matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
				agentWIP = args[1];
			} else {
				System.err.println("Wrong AgentIP format");
				System.exit(4);
			}
			int portAgentaW = 0;
			try {
				portAgentaW = Integer.parseInt(args[2]);
			} catch (Exception e) {
				System.err.println("Niepoprawnie wprowadzony nr portu Agenta wprowadzającego");
				System.exit(5);
			}
			//odpalam agenta z ip i portem wprowadzajacego, bez portu agenta
			new Agent(time, portAgenta, agentWIP, portAgentaW);
			break;
		case 4:
			try {
				portAgenta = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.err.println("Niepoprawnie wprowadzony nr portu nowego Agenta");
				System.exit(3);
			}
			agentWIP = "";
			if(args[2].matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
				agentWIP = args[2];
			} else {
				System.err.println("Wrong AgentIP format");
				System.exit(4);
			}
			portAgentaW = 0;
			try {
				portAgentaW = Integer.parseInt(args[3]);
			} catch (Exception e) {
				System.err.println("Niepoprawnie wprowadzony nr portu Agenta wprowadzającego");
				System.exit(5);
			}
			//odpalam agenta ze wszystkimi argumentami
			new Agent(time, portAgenta, agentWIP, portAgentaW);
			break;
		}
		
	}
}
