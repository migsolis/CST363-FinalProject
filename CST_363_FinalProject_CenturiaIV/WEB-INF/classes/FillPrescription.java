

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FillPrescription
 */
@WebServlet("/FillPrescription")
public class FillPrescription extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	// database URL
   static final String DB_URL = "jdbc:mysql://cst363.bienz.us/drug_store";
   
   // database credentials
   static final String USER = "root";
   static final String PASS = "Cst363pass!";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FillPrescription() {
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
		
	   // Used to format decimal into currency
	   NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
      
      // Set response content type
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      
      // get input data from form
      String patient_id = request.getParameter("patient_id");
      String prescription_id = request.getParameter("prescription_id");
            
      try {
         
         String sql;
         PreparedStatement pstmt;
         ResultSet rs;
         
         // Establishes connection to server
         Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
         
         try {
            
            // disables auto committ  allowing us to perform a transaction
            conn.setAutoCommit(false);
            
            // Get data used for price calc and refill info
            sql = "select p.qty * pd.unit_price as price_total, refills_allowed, refills_taken\n" + 
               "    from prescriptions p join pharmacy_drugs pd on p.pharmacy_drug = pd.pharmacy_drug_id \n" + 
               "    where p.prescription_id = ?;";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, prescription_id);
            rs = pstmt.executeQuery();
            rs.next();
            double price_total = rs.getDouble("price_total");
            int refills_allowed = rs.getInt("refills_allowed");
            int refills_taken = rs.getInt("refills_taken");
            
            // Checks if there are refill available 
            if (refills_taken > refills_allowed) {
               out.println("<!DOCTYPE HTML><html>"
                  + "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"drug_store.css\"/>"
                  + "</head><body>");
               out.println("<h3>Prescription: " + prescription_id + ", has no remaining refills.</h3>");
               out.println("<p>If you believe this is incorrect please contact your healthcare provider.</p>");
               out.println("</body></html>");
               rs.close();
               conn.close();
            }
            else{
            
               // get current date to be entered into refill table
               Date current_date = Date.valueOf(LocalDate.now());
               
               // get the highest_refill_id and increment it by one
               sql = "select max(refill_id) as highest_refill_id from refill;";
               pstmt = conn.prepareStatement(sql);
               rs = pstmt.executeQuery();
               rs.next();
               int refill_id = rs.getInt("highest_refill_id") + 1;
                        
               // inserts new fill request refill table
               sql = "insert into refill values (?, ?, ?, ?);";
               pstmt = conn.prepareStatement(sql);
               pstmt.setInt(1, refill_id);
               pstmt.setString(2, prescription_id);
               pstmt.setDate(3, current_date);
               pstmt.setDouble(4, price_total);
               pstmt.executeUpdate();
               
               // updates the refills taken after fill request is submitted
               sql = "update prescriptions set refills_taken = refills_taken + 1 where prescription_id = ?;";
               pstmt = conn.prepareStatement(sql);
               pstmt.setString(1, prescription_id);
               pstmt.executeUpdate();
               
               // query to retrieve prescription fill request data to be displayed
               sql = "select p.prescription_id, p.date, ph.pharmacy_name, sd.trade_name, \n" + 
                  "    p.qty, u.uom_description, p.refills_allowed, p.refills_taken, r.refill_date, r.price_total\n" + 
                  "from prescriptions p \n" + 
                  "    join pharmacies ph on p.pharmacy_id = ph.pharmacy_id \n" + 
                  "    join pharmacy_drugs pd on p.pharmacy_drug = pd.pharmacy_drug_id\n" + 
                  "    join supplier_drugs sd on pd.supplier_id = sd.supplier_id\n" + 
                  "    join uom u on pd.uom = u.uom_id\n" + 
                  "    join refill r on p.prescription_id = r.prescription_id\n" + 
                  "where p.patient_id = ? and p.prescription_id = ? and r.refill_id = ?;";
               pstmt = conn.prepareStatement(sql);
               pstmt.setString(1, patient_id);
               pstmt.setString(2, prescription_id);
               pstmt.setInt(3, refill_id);
               rs = pstmt.executeQuery();
               rs.next();
               
               // calcs remaining refills
               int refills_remaining = rs.getInt("refills_allowed" ) - rs.getInt("refills_taken") + 1;
               
               // start html output
               out.println("<!DOCTYPE HTML><html>"
                  + "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"drug_store.css\"/>"
                  + "</head><body>");
               out.println("<h3>Prescription: " + prescription_id + ", Fill Request Submitted</h3>");
               
               
               // begin table and column headings
               out.println("<table>");
               out.println("<tr>");
               out.println("<th>Prescription Date</th>");
               out.println("<th>Pharmacy Name</th>");
               out.println("<th>Drug</>");
               out.println("<th>Quantity</th>");
               out.println("<th>Unit of Measure</th>");
               out.println("<th>Remaining Refills</th>");
               out.println("<th>Fill Date</th>");
      
               out.println("</tr>");
               
               // create row with data for each row from result set
           
               out.println("<tr>");
               out.println("<td>" + rs.getDate("date") + "</td>");
               out.println("<td>" + rs.getString("pharmacy_name") + "</td>");
               out.println("<td>" + rs.getString("trade_name") + "</td>");
               out.println("<td>" + rs.getInt("qty") + "</td>");
               out.println("<td>" + rs.getString("uom_description") + "</td>");
               out.println("<td>" + refills_remaining + "</td>");
               out.println("<td>" + rs.getDate("refill_date") + "</td>");
               out.println("</tr>");
               
               out.println("<tr>");
               out.println("<th>Total: </th><td>" + currencyFormatter.format(rs.getDouble("price_total")) + "</td>");
               out.println("</tr>");
                                    
               out.println("</table><br>");
               
               out.println("<form action=\"PrescriptionSearch\" method=\"POST\">");
               out.println("<input type=\"hidden\" name=\"patient_id\" value=\"" + patient_id + "\"/>");
               out.println("<input type=\"submit\" value=\"Go Back\"/>");
               out.println("</form>");
      
               out.println("<br><a href=\"prescription_search.html\">Back to prescription search screen.</a>");
               out.println("</body></html>");
               rs.close();
               
               // commits the transaction
               conn.commit();
            }
         
         }catch(SQLException e) {
            // rolls back if exception occurs
            conn.rollback();
         }finally {
            // sets auto commit back to true and closes connection
            conn.setAutoCommit(true);
            conn.close();
         }
         
      } catch (SQLException se) {
         se.printStackTrace();
      }
	   
	   doGet(request, response);
	}

}
