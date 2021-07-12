

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DataLookup
 */
@WebServlet("/DataLookup")
public class DataLookup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// database URL
	static final String DB_URL = "jdbc:mysql://cst363.bienz.us/drug_store";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "Cst363pass!";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.getWriter().append("Served at: ").append(request.getContextPath());

		// SQL statements
		String selectDoctorSql = "SELECT doctor_id, CONCAT(name_last, ', ', name_first) AS doctor_name FROM doctors ORDER BY name_last";
		String selectPatientSql = "SELECT patient_id, CONCAT(name_last, ', ', name_first, ' (*-', RIGHT(ssn,4),')') AS patient_name FROM patients ORDER BY name_last";
		String selectPharmacySql = "SELECT pharmacy_id, CONCAT(pharmacy_name, ' (', city, ',', state,')') AS pharmacy FROM pharmacies ORDER BY pharmacy_name";
		String selectDrugSql = "SELECT pharmacy_drug_id, CONCAT(sd.trade_name, ' (', d.generic_name , ')') AS name, uom FROM pharmacy_drugs pd JOIN supplier_drugs sd "
				+ "ON sd.supplier_id = pd.supplier_id AND sd.drug_id = pd.drug_id JOIN drugs d ON d.drug_id = pd.drug_id WHERE pharmacy_id = ?";
		String selectUomSql = "SELECT uom_description FROM pharmacy_drugs pd JOIN uom u ON u.uom_id = pd.uom WHERE pd.pharmacy_drug_id = ?";

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		try ( Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)   ) {
			// prepare select
			PreparedStatement pstmt;
			ResultSet rs;

			String requestType = request.getParameter("rt");
			if(requestType != null) {
			switch(requestType) {
			
			case "doctors":
				pstmt = conn.prepareStatement(selectDoctorSql);
				rs = pstmt.executeQuery();
				out.println("<select name = 'doctor_id' onchange=\"getPatients(\'patients\')\">");
				out.println("<option></option>");
				// create row with data for each row from result set
				while (rs.next()) {
					out.println("<option value="+rs.getString("doctor_id")+">"+rs.getString("doctor_name")+"</option>");
				}
				rs.close();
				pstmt.close();
				break;
				
			case "patients":
				pstmt = conn.prepareStatement(selectPatientSql);
				rs = pstmt.executeQuery();
				out.println("<td><select name='patient_id' onchange='testTxt(this.value)'>");
				out.println("<option></option>");
				// create row with data for each row from result set
				while (rs.next()) {
					out.println("<option value="+rs.getString("patient_id")+">"+rs.getString("patient_name")+"</option>");
				}
				rs.close();
				pstmt.close();
				break;
				
			case "pharmacies":
				pstmt = conn.prepareStatement(selectPharmacySql);
				rs = pstmt.executeQuery();
				out.println("<td><select name = 'pharmacy_id' onchange=\"getDrugs(\'drugs\',this.value)\">");
				out.println("<option></option>");
				// create row with data for each row from result set
				while (rs.next()) {
					out.println("<option value="+rs.getString("pharmacy_id")+">"+rs.getString("pharmacy")+"</option>");
				}
				rs.close();
				pstmt.close();
				break;
			
			case "drugs":
				pstmt = conn.prepareStatement(selectDrugSql);
				pstmt.setString(1,  request.getParameter("p"));
				rs = pstmt.executeQuery();
				out.println("<td><select name = 'pharmacy_drug_id' onchange=\"getUom(\'uom\',this.value)\">");
				out.println("<option></option>");
				// create row with data for each row from result set
				while (rs.next()) {
					out.println("<option value="+rs.getString("pharmacy_drug_id")+">"+rs.getString("name")+"</option>");
				}
				rs.close();
				pstmt.close();
				break;
				
			case "uom":
				pstmt = conn.prepareStatement(selectUomSql);
				pstmt.setString(1,  request.getParameter("p"));
				rs = pstmt.executeQuery();
				while (rs.next()) {
					out.println("<input type='text' name='uom' value='"+rs.getString("uom_description")+"' disabled/>");
				}
				rs.close();
				pstmt.close();
				break;
			}

			out.println("</select>");

			}
		} catch (SQLException e) {
			// Handle errors
			e.printStackTrace();  // stack trace goes to Tomcat console.
		}  

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);

		
	}

}