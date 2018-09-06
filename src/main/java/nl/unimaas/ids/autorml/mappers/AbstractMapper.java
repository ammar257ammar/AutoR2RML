package nl.unimaas.ids.autorml.mappers;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import nl.unimaas.ids.util.PrefixPrintWriter;

public abstract class AbstractMapper implements MapperInterface {
	final static String ROW_NUM_NAME = "rownum";
	
	Connection connection;
	String outputGraph;
	String baseUri;
	
	public AbstractMapper() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void close() throws SQLException {
		if(connection != null && !connection.isClosed())
			connection.close();
	}
	
	@SuppressWarnings("resource")
	void generateImportAndGraph(PrintStream ps, String baseDir) {
		this.baseUri = this.baseUri.replaceAll("/$", "");
		if (this.outputGraph == null) {
			this.outputGraph = this.baseUri + "/graph" + baseDir;
		}
		PrintWriter writer = new PrefixPrintWriter(ps);
		writer.println("@prefix rr: <http://www.w3.org/ns/r2rml#>."); 
		writer.println("@prefix kraken: <" + this.baseUri + ">.");
		writer.flush();
	}
	
	// TODO: should we put generateR2RML in the MapperInterface?
	void generateR2RML(String table, String fromQuery, String[] columns, PrintStream ps, String label) throws Exception {
		generateR2RML(table, fromQuery, columns, ps, label, null);
	}
	
	@SuppressWarnings("resource")
	void generateR2RML(String uriPath, String fromQuery, String[] columns, PrintStream ps, String label, String prefix) throws Exception {
		PrintWriter upper = new PrefixPrintWriter(ps, prefix);
		PrintWriter lower = new PrefixPrintWriter(ps, prefix);


		upper.println("<#" + label + ">");
		upper.println("rr:logicalTable [ rr:sqlQuery \"\"\"");

		lower.println("rr:subjectMap [");
		lower.println("  rr:termType rr:IRI;");
		lower.println("  rr:template \"" + this.baseUri + uriPath + "/{" + ROW_NUM_NAME + "}\";");
		lower.println("  rr:graph <" + this.outputGraph + ">;");
		lower.println("];");
		
		upper.println("  SELECT " + getSqlForRowNum(fromQuery, columns[0]));
		// TODO: upper.println("  SELECT " + getSqlForRowNum(columns[0]));
		// TODO: just change getSqlForRowNum params everywhere it is called.
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			String columnName = getColumnName(column);
			upper.println("    , " + getSqlForColumn(columnName, i));
			lower.println("rr:predicateObjectMap [");
			lower.println("  rr:predicate <" + this.baseUri + uriPath + "/" + columnName + ">;");
			lower.println("  rr:objectMap [ rr:column \"" + columnName + "\" ];");
			lower.println("  rr:graph <" + this.outputGraph + ">;");
			lower.println("];");
		}
		upper.println("  FROM\n    " + fromQuery + " t1;");
		// TODO: upper.println("  FROM\n    " + fromQuery + ";");
		upper.println("\"\"\"];");

		lower.println(".");
		lower.println("\n");

		upper.flush();
		lower.flush();

	}

}
