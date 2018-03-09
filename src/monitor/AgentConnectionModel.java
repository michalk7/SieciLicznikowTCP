package monitor;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class AgentConnectionModel extends AbstractTableModel {
	
	private List<SingleConnection> rows;
	private String[] columnNames;
	
	public AgentConnectionModel(List<SingleConnection> agents, String[] columnsNames) {
		rows = agents;
		this.columnNames = columnsNames;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SingleConnection con = rows.get(rowIndex);
		switch(columnIndex) {
		case 0: return con.getFullAdress();
		case 1: return con.getTime();
		}
		return null;
	}

	@Override
	public String getColumnName(int index) {
		return columnNames[index];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 1)
			return true;
		return false;
	}

	@Override
	public void setValueAt(Object val, int rowIndex, int columnIndex) {
		SingleConnection con = rows.get(rowIndex);
		switch(columnIndex) {
		case 0: 
			con.setFullAdress((String)val);
			break;
		case 1:
			con.setTime((Long)val);
			break;
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	
}
