

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


@WebServlet("/CreatePrescription")
public class CreatePrescription extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

	// database URL
	static final String DB_URL = "jdbc:mysql://cst363.bienz.us/drug_store";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "Cst363pass!";

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// SQL statements
      String writePrescriptionSql =  "INSERT INTO prescriptions (prescription_id, pharmacy_id, doctor_id,"
      		+ "patient_id, pharmacy_drug, date, qty, refills_taken, refills_allowed)"
      		+ "SELECT MAX(prescription_id)+1, ?, ?, ?, ?, CURDATE(), ?, 0, ? FROM prescriptions;";
      String readLastPrescriptionSql = "SELECT MAX(prescription_id) AS prescription_id FROM prescriptions;";
      String readPrescriptionSql = "SELECT prescription_id, doctor, patient, pharmacy, drug, qty, uom, refills FROM prescription_details WHERE prescription_id = ?;";
			
		// Set response content type
		response.setContentType("text/html"); 
		PrintWriter out = response.getWriter();
		
		// get input data from form
		String doctor_id = request.getParameter("doctor_id");
		String patient_id = request.getParameter("patient_id");
		String pharmacy_id = request.getParameter("pharmacy_id");
		String pharmacy_drug_id = request.getParameter("pharmacy_drug_id");
		String qty = request.getParameter("qty");
		String refills = request.getParameter("refills");
		String prescription_id = "";

		try ( Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)   ) {
			// prepare update
			PreparedStatement pstmt = conn.prepareStatement(writePrescriptionSql);
			pstmt.setString(1,  pharmacy_id); 
			pstmt.setString(2,  doctor_id); 
			pstmt.setString(3,  patient_id); 
			pstmt.setString(4,  pharmacy_drug_id); 
			pstmt.setString(5,  qty);
			pstmt.setString(6,  refills);
			pstmt.executeUpdate();
			
			//read last created Prescription
			pstmt = conn.prepareStatement(readLastPrescriptionSql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				prescription_id = Integer.toString(rs.getInt("prescription_id"));
			}
			
			// start html output
			out.println("<!DOCTYPE HTML><html><body>");
			
			//read Prescription data
			pstmt = conn.prepareStatement(readPrescriptionSql);
			pstmt.setString(1, prescription_id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				out.println("<h2>Prescription Entry Confirmation</h2>");
				out.println("<p><b>Prescription ID:</b> "+rs.getInt("prescription_id")+"</p>");
				out.println("<p><b>Doctor:</b> "+rs.getString("doctor")+"</p>");
				out.println("<p><b>Patient:</b> "+rs.getString("patient")+"</p>");
				out.println("<p><b>Pharmacy:</b> "+rs.getString("pharmacy")+"</p>");
				out.println("<p><b>Drug:</b> "+rs.getString("drug")+"</p>");
				out.println("<p><b>Qty:</b> "+rs.getInt("qty")+"</p>");
				out.println("<p><b>UOM:</b> "+rs.getString("uom")+"</p>");
				out.println("<p><b>Refills:</b> "+rs.getInt("refills")+"</p>");
			}
			
            // end of HTML document
			out.println("</body></html>");
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// Handle errors
			e.printStackTrace();  // stack trace goes to Tomcat console.
		}  
	}

}
