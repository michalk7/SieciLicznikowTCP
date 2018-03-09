package monitor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import agent.Agent;

public class MonitorGUI {

	private ArrayList<String> adresyAgentow;
	private ArrayList<SingleConnection> polaczenia;
	private AgentConnectionModel model;
	private SingleConnection masterConnection;
	private ArrayList<Agent> agenciWewnetrzni;

	public MonitorGUI() {
		adresyAgentow = new ArrayList<>();
		polaczenia = new ArrayList<>();
		agenciWewnetrzni = new ArrayList<>();
		String adr = "";
		while(adr == null || adr.isEmpty()) {
			adr = (String)JOptionPane.showInputDialog(null, "Podaj adres IP i port dowolnego agenta w sieci w formacie IP:port, uwaga tego agenta nie będzie można usunąć!", "Master Agent", JOptionPane.PLAIN_MESSAGE);
			if(!adr.isEmpty()) {
				String[] tmp = adr.split("\\:");
				if(tmp.length != 2) {
					adr = null;
					JOptionPane.showMessageDialog(null,
						    "Proszę podać IP i port oddzielone znakiem ':'",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		SingleConnection con = new SingleConnection(this, adr);
		masterConnection = con;
		showGui();
		con.start();
	}

	public void showGui() {
	    SwingUtilities.invokeLater( new Runnable() {
	      public void run() {
	        JFrame f = new JFrame("Sieć liczników");
	        String[] names = {"IP:Port", "Time"};
	        model = new AgentConnectionModel(polaczenia, names);
	        JTable jtab = new JTable(model);
	        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
	        JButton add = new JButton("Add agent");
	        JButton syn = new JButton("Send SYN");
	        JButton del = new JButton("Remove agent");
	        syn.addActionListener(synListener);
	        del.addActionListener(delListener);
	        add.addActionListener(addListener);
	        buttonPanel.add(add);
	        buttonPanel.add(syn);
	        buttonPanel.add(del);
	        f.add( new JScrollPane(jtab), BorderLayout.CENTER);
	        f.add(buttonPanel, BorderLayout.SOUTH);
	        f.pack();
	        f.setLocationRelativeTo(null);
	        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	        f.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					for(SingleConnection con : polaczenia) {
		        		con.setStop(true);
		        	}
		        	masterConnection.setStop(true);
		        	for(Agent a : agenciWewnetrzni) {
			        	try(Socket socket = new Socket(a.getIPadress(), a.getPort());
								PrintWriter out = new PrintWriter(socket.getOutputStream(), true)	) {
								out.println("EXIT");
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		        	}
		        	try {
						Thread.sleep(600);  //dajemy czas na miniecie thread.slepp(500) w masterConnection
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
	        	
	        });
	        f.setVisible(true);
	      }
	   });
	}
	
	private ActionListener synListener = e -> {
		String[] tab = new String[polaczenia.size()];
		for(int i = 0; i < tab.length; i++) {
			tab[i] = polaczenia.get(i).getFullAdress();
		}
		String tmp = (String)JOptionPane.showInputDialog(null, 
				"Do którego agenta chcesz wysłać polecenie SYN?", "Send Syn", JOptionPane.QUESTION_MESSAGE, null, tab, 0);
		try {
			new Thread(() -> {
				String[] tmp1 = tmp.split("\\:");
				int i = Integer.parseInt(tmp1[1]);
				try(Socket socket = new Socket(tmp1[0], i);
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true)	) {
						out.println("SYN");
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}).start();
		} catch(Exception exc) {
			
		}
	};
	
	private ActionListener delListener = e -> {
		String[] tab = new String[polaczenia.size()];
		HashMap<String, Integer> numerAdres = new HashMap<>();
		for(int i = 1; i < tab.length; i++) {
			tab[i] = polaczenia.get(i).getFullAdress();
			numerAdres.put(polaczenia.get(i).getFullAdress(), i);
		}
		String tmp = (String)JOptionPane.showInputDialog(null, 
				"Którego agenta chcesz usunąć?", "Send EXIT", JOptionPane.QUESTION_MESSAGE, null, tab, 0);
		try {
			new Thread(() -> {
				String[] tmp1 = tmp.split("\\:");
				int i = Integer.parseInt(tmp1[1]);
				try(Socket socket = new Socket(tmp1[0], i);
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true)	) {
						int number = numerAdres.get(tmp);
						polaczenia.get(number).setStop(true);
						out.println("EXIT");
						for(int j = number; j < polaczenia.size(); j++) {
							polaczenia.get(j).setNumber(j-1);
						}
						polaczenia.remove(number);
						adresyAgentow.remove(tmp);
						model.fireTableRowsDeleted(number, number);
						SingleConnection.setCounter(SingleConnection.getCounter()-1);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}).start();
		} catch(Exception exc) {
			
		}
	};
	
	private ActionListener addListener = e -> {
		String tab[] = new String[polaczenia.size()];
		int i = 0;
		for(SingleConnection con : polaczenia) {
			tab[i] = con.getFullAdress();
			i++;
		}
		String option = (String)JOptionPane.showInputDialog(null, 
				"Jakiego agenta wybierasz jako wprowadzającego?", 
				"Wybór agenta wprowadzającego", JOptionPane.QUESTION_MESSAGE, null, tab, tab[0]);
		if(option != null && !option.isEmpty()) {
			String[] tmp = option.split("\\:");
			i = Integer.parseInt(tmp[1]);
			String licznikString = (String)JOptionPane.showInputDialog(null, 
					"Wpisz początkową wartość licznika", "Wartość licznika", JOptionPane.QUESTION_MESSAGE);
			long licznik;
			try {
				licznik = Long.parseLong(licznikString);
			} catch(Exception exc) {
				JOptionPane.showMessageDialog(null,
					    "Proszę podać liczbę całkowitą jako wartość licznika",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			String portString = (String)JOptionPane.showInputDialog(null, 
					"Wpisz numer portu dla nowego agenta", "Port Agenta", JOptionPane.QUESTION_MESSAGE);
			int port;
			try {
				port = Integer.parseInt(portString);
			} catch(Exception exc) {
				JOptionPane.showMessageDialog(null,
					    "Proszę podać liczbę całkowitą jako numer portu agenta",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			Agent a = new Agent(licznik, port, tmp[0], i, false);
			agenciWewnetrzni.add(a);
		}
	};
	
	public SingleConnection getMasterConnection() {
		return masterConnection;
	}

	public void setMasterConnection(SingleConnection masterConnection) {
		this.masterConnection = masterConnection;
	}

	public AgentConnectionModel getModel() {
		return model;
	}

	public synchronized ArrayList<String> getAdresyAgentow() {
		return adresyAgentow;
	}
	
	public synchronized void addNewAgentAdress(String adress) {
		adresyAgentow.add(adress);
	}
	
	public synchronized ArrayList<SingleConnection> getPolaczenia() {
		return polaczenia;
	}

	public synchronized void addNewAgentConnection(SingleConnection con) {
		polaczenia.add(con);
	}
	
}
