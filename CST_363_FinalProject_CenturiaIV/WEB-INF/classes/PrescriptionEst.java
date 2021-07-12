

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class fill_prescription
 */
@WebServlet("/PrescriptionEst")
public class PrescriptionEst extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	  // database URL
   static final String DB_URL = "jdbc:mysql://cst363.bienz.us/drug_store";
   
   // database credentials
   static final String USER = "root";
   static final String PASS = "Cst363pass!";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PrescriptionEst() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	   
	   NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
	   
      // SQL query
      String sql = "select p.prescription_id, p.date, dc.name_last, ph.pharmacy_name, sd.trade_name, \n" + 
         "    p.qty, u.uom_description, p.refills_allowed, p.refills_taken, pd.unit_price,\n" + 
         "    p.qty * pd.unit_price as est_cost\n" + 
         "from prescriptions p \n" + 
         "    join pharmacies ph on p.pharmacy_id = ph.pharmacy_id \n" + 
         "    join doctors dc on p.doctor_id = dc.doctor_id\n" + 
         "    join pharmacy_drugs pd on p.pharmacy_drug = pd.pharmacy_drug_id\n" + 
         "    join supplier_drugs sd on pd.supplier_id = sd.supplier_id\n" + 
         "    join uom u on pd.uom = u.uom_id\n" + 
         "where p.patient_id = ? and p.prescription_id = ?\n" + 
         "order by p.date desc;";
      
      // Set response content type
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      
      // get input data from form
      String patient_id = request.getParameter("patient_id");
      String prescription_id = request.getParameter("prescription_id");
      
      try ( Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
         // prepared select
         PreparedStatement pstmt = conn.prepareStatement(sql);
         pstmt.setString(1, patient_id);
         pstmt.setString(2, prescription_id);
         ResultSet rs = pstmt.executeQuery();
         rs.next();
         
         // start html output
         out.println("<!DOCTYPE HTML><html>"
            + "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"drug_store.css\"/>"
            + "</head><body>");
         out.println("<h1>Prescription: " + rs.getInt("prescription_id") + "</h1>");
         
         
         // begin table and column headings
         out.println("<table>");
         out.println("<tr>");
         out.println("<th>Date</th>");
         out.println("<th>Doctor</th>");
         out.println("<th>Pharmacy Name</th>");
         out.println("<th>Drug</>");
         out.println("</tr>");
         
         // create row with data for each row from result set
     
         out.println("<tr>");
         out.println("<td>" + rs.getDate("date") + "</td>");
         out.println("<td> Dr. " + rs.getString("name_last") + "</td>");
         out.println("<td>" + rs.getString("pharmacy_name") + "</td>");
         out.println("<td>" + rs.getString("trade_name") + "</td>");
         out.println("</tr>");
         
         out.println("<tr>");
         out.println("<th>Quantity</th>");
         out.println("<th>Unit of Measure</th>");
         out.println("<th>Refills Allowed</th>");
         out.println("<th>Fills Taken</th>");
         out.println("</tr>");
         
         out.println("<tr>");
         out.println("<td>" + rs.getInt("qty") + "</td>");
         out.println("<td>" + rs.getString("uom_description") + "</td>");
         out.println("<td>" + rs.getInt("refills_allowed") + "</td>");
         out.println("<td>" + rs.getInt("refills_taken") + "</td>");
         out.println("</tr>");
         
         out.println("<tr>");
         out.println("<th>Unit Price:</th><td>" + currencyFormatter.format(rs.getDouble("unit_price")) + "</td>");
         out.println("</tr>");
         out.println("<tr>");
         out.println("<th>Estimated Cost:</th><td>" + currencyFormatter.format(rs.getDouble("est_cost")) + "</td>");
         out.println("</tr>");
                              
         out.println("</table><br>");
         
         out.println("<table>");
         out.println("<tr>");
         out.println("<td>");
         out.println("<form>" +
            "<input type=\"button\" value=\"Go Back\" onclick=\"history.back()\"/>");
         out.println("</form></td>");
         out.println("<td>");
         out.println("<form action=\"FillPrescription\" method=\"POST\">" + 
            "<input type=\"hidden\" name=\"patient_id\" value=\"" + patient_id + "\"/>" +
            "<input type=\"hidden\" name=\"prescription_id\" value=\"" + rs.getInt("prescription_id") + "\"/>" +
            "<input type=\"submit\" value=\"Continue\"/></form>");
         out.println("</td>");
         out.println("</tr>");
         out.println("</table><br>");
         out.println("<br><a href=\"prescription_search.html\">Back to prescription search screen.</a>");
         out.println("</body></html>");
         rs.close();
      } catch (SQLException e) {
         
      }
	   
		doGet(request, response);
	}

}
