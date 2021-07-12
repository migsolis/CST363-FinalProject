

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
@WebServlet("/FdaReport")
public class FdaReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	// DB Location
	static final String DB_URL = "jdbc:mysql://cst363.bienz.us/drug_store";
	
	// DB Credentials
	static final String USER = "root";
	static final String PASS = "Cst363pass!";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FdaReport() {
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
	   String sql = "SELECT * FROM drug_store.FDA_Report";
	   
	   // Response content
	   response.setContentType("text/html");
	   PrintWriter out = response.getWriter();
	   
	   
	   
	   try ( Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)     ) {
	      
	      PreparedStatement pstmt = conn.prepareStatement(sql);
	      ResultSet rs = pstmt.executeQuery();
	     
	      // HTML Output below
	      
	      
	      
	      out.println("<!DOCTYPE HTML><html><head> " +
	         "<link rel=\"stylesheet\" type=\"text/css\" href=\"drug_store.css\"/>"
	         + "</head><body>");
	      
	      out.println("<h1>Report results:</h1><br/>");
	      
	      out.println("<table cellspacing=\"0\" cellpadding=\"2\" " +
	      " border =\"1\">");
	      out.println("<tr>");
	      out.println("<th>Doctor Name</th><th>Drug Name</th>");
	      out.println("<th>Total Prescribed</th>");
	      out.println("</tr>");
	      
	      while (rs.next()) {
	         // TODO
	         out.println("<tr>");
	         out.println("<td>" + rs.getString("doctor_name") + "</td>");
	         out.println("<td>" + rs.getString("generic_name") + "</td>");
	         out.println("<td>" + rs.getInt("total_prescribed") + "</td>");
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
