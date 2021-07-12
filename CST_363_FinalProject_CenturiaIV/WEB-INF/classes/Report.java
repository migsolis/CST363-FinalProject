

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
 * Servlet implementation class FdaReport
 */
@WebServlet("/Report")
public class Report extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	// DB Location
	static final String DB_URL = "jdbc:mysql://cst363.bienz.us/drug_store";
	
	// DB Credentials
	static final String USER = "root";
	static final String PASS = "Cst363pass!";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Report() {
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
	
	   // SQL Code
	   String sql = "SELECT D.drug_id, D.generic_name, S.trade_name, SUM(P.qty)AS TotalSold " + 
	      "FROM drug_store.drugs D " +
	      "JOIN drug_store.supplier_drugs S ON D.drug_id = S.drug_id " + 
	      "JOIN drug_store.pharmacy_drugs pd on S.drug_id = pd.drug_id and S.supplier_id = pd.supplier_id " + 
	      "JOIN drug_store.prescriptions P ON P.pharmacy_id = pd.pharmacy_id and P.pharmacy_drug = pd.pharmacy_drug_id " + 
	      "WHERE month(P.date) = ? " + 
	      "GROUP by D.drug_id, D.generic_name, S.trade_name;";
	   
	   // Response content
	   response.setContentType("text/html");
	   PrintWriter out = response.getWriter();
	   
	   // get input data from form
	   String start = request.getParameter("start");
	   
	   // Do some translations from digits to month:
	   String month;
	   switch (start) {
	      case "1": month = "January";
	                  break;
	      case "2": month = "February";
                          break;
              case "3": month = "March";
                          break;	
              case "4": month = "April";
                          break;
              case "5": month = "May";
                          break;
              case "6": month = "June";
                          break;
              case "7": month = "July";
                          break;
              case "8": month = "August";
                          break;
              case "9": month = "September";
                          break;   
              case "10": month = "October";
                          break;   
              case "11": month = "November";
                          break;  
              case "12": month = "December";
                          break;                            
              default: month = "NULL";
	   }
	      
	   try ( Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)     ) {
	      
	      PreparedStatement pstmt = conn.prepareStatement(sql);
	      pstmt.setString(1, start);
	      ResultSet rs = pstmt.executeQuery();
	     
	      // HTML Output below
	      
	      out.println("<!DOCTYPE HTML><html>"
              + "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"drug_store.css\"/>"
              + "</head><body>");
	          
	     // Heading tables
	      out.println("<h1>Manager Report results for the month of " + month + ":</h1><br/>");
	      
	      out.println("<table>");
	      out.println("<tr>");
	      out.print("<th>Drug ID</th>");
	      out.println("<th>Generic Name</th>");
	      out.print("<th>Trade Name</th>");
	      out.println("<th>Total Sold</th>");
	      //out.println("<th>Date</th>");
	      out.print("</tr>");
	           
	      
	    	      
	     while (rs.next()) {
	        System.out.println("hello friend");
	         // TODO
          out.println("<tr>");
          out.println("<td>" + rs.getString("drug_id") + "</td>");
          out.println("<td>" + rs.getString("generic_name") + "</td>");
          out.println("<td>" + rs.getString("trade_name") + "</td>");
          //out.println("<td>" + rs.getString("Qty") + "</td>");
          //out.println("<td>" + rs.getString("date") + "</td>");
          out.println("<td>" + rs.getInt("TotalSold") + "</td>");
          out.println("</tr>");
	      }
	      rs.close();
	      
	      // Close out tables
	      out.println("</table>");
	      out.println("<br/>");
	      out.println("</body></html>");
	      
	      pstmt.close();
	      
	   } catch (SQLException e) {
	      // Handle errors
	      e.printStackTrace();
	   }
	   
	   // TODO Auto-generated method stub
		doGet(request, response);
	}

}