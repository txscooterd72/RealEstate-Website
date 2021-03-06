
import beans.Owner;
import beans.Property;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sanip
 */
public class BrowseProperties extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Create a session
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(0);
        String ownerID = (String) session.getAttribute("ownerID");
        String adPurpose = (String) request.getParameter("adPurpose");
        String propertySuburb = (String) request.getParameter("propertySuburb");
        ArrayList<Owner> ownersList = new ArrayList<>();
        ArrayList<Property> propertiesList = new ArrayList<>();
        ArrayList<String> suburbsList = new ArrayList<>();

        try {
            // Create JDBC instance
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // Create a connection with database
            Connection conn = DriverManager.getConnection(
                                      "jdbc:mysql://saniprealestate-ap-southeast-2a.c3mrp52txpvn.ap-southeast-2.rds.amazonaws.com:3306/ssx-real-estate","saniprealestate", "saniprealestate");

            // Create a SQL Statement
            Statement stmt = conn.createStatement();

            // Create a SQL Statement String
            String sql = "SELECT "
                    + "owners.ownerID, "
                    + "owners.firstName, "
                    + "owners.lastName, "
                    + "DATE_FORMAT(DATE(owners.memberSince),'%m/%d/%Y') AS since, "
                    + "COUNT(properties.propertyID) AS totalProperties  "
                    + "FROM properties LEFT JOIN owners "
                    + "ON properties.ownerID = owners.ownerID "
                    + "GROUP BY ownerID "
                    + "ORDER BY totalProperties DESC";
            
            // Execute SQL statement
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Owner ownerBean = new Owner();
                ownerBean.setOwnerID(rs.getString("ownerID"));
                ownerBean.setFirstName(rs.getString("firstName"));
                ownerBean.setLastName(rs.getString("lastName"));
                ownerBean.setMemberSince(rs.getString("since"));
                ownerBean.setTotalProperties(rs.getString("totalProperties"));
                ownersList.add(ownerBean);
            }

            sql = "SELECT *, "
                    + "FORMAT(propertyPrice,2) as price, "
                    + "DATE_FORMAT(DATE(dateCreated),'%m/%d/%Y') as created "
                    + "FROM properties INNER JOIN owners "
                    + "ON properties.ownerID = owners.ownerID "
                    + "WHERE adPurpose LIKE '" + adPurpose + "' "
                    + "AND "
                    + "propertySuburb LIKE '" + propertySuburb + "' "
                    + "ORDER BY adType DESC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Property propertyBean = new Property();
                propertyBean.setPropertyID(rs.getString("propertyID"));
                propertyBean.setOwnerID(rs.getString("ownerID"));
                propertyBean.setOwnerFirstName(rs.getString("firstName"));
                propertyBean.setOwnerLastName(rs.getString("lastName"));
                propertyBean.setAdTitle(rs.getString("adTitle"));
                propertyBean.setAdPurpose(rs.getString("adPurpose"));
                propertyBean.setPropertyType(rs.getString("propertyType"));
                propertyBean.setPropertySize(rs.getString("propertySize"));
                if((propertyBean.getAdPurpose()).equals("For rent")){
                    propertyBean.setPropertyPrice(rs.getString("price") + " p/w");
                } else {
                    propertyBean.setPropertyPrice(rs.getString("price"));
                }
                propertyBean.setPropertyAddress(rs.getString("propertyAddress"));
                propertyBean.setPropertySuburb(rs.getString("propertySuburb"));
                propertyBean.setPropertyState(rs.getString("propertyState"));
                propertyBean.setPropertyPostcode(rs.getString("propertyPostcode"));
                propertyBean.setPropertyBedrooms(rs.getString("propertyBedrooms"));
                propertyBean.setPropertyBathrooms(rs.getString("propertyBathrooms"));
                propertyBean.setPropertyCarSpaces(rs.getString("propertyCarSpaces"));
                propertyBean.setPropertyDescription(rs.getString("propertyDescription"));
                propertyBean.setAdType(rs.getString("adType"));
                propertyBean.setDateCreated(rs.getString("created"));
                propertiesList.add(propertyBean);
            }
            
            sql = "SELECT DISTINCT propertySuburb FROM properties ORDER BY propertySuburb";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                suburbsList.add(rs.getString("propertySuburb"));
            }
            
            // Set ownerID and owner beans to session
            session.setAttribute("ownerID", ownerID);
            session.setAttribute("ownersList", ownersList);
            session.setAttribute("propertiesList", propertiesList);
            session.setAttribute("suburbsList", suburbsList);
            session.setAttribute("adPurpose", adPurpose);
            session.setAttribute("propertySuburb", propertySuburb);

            // Clean password data from session
            session.setAttribute("password", "");
            session.setAttribute("newPassword", "");
            session.setAttribute("cfmNewPassword", "");
            session.setAttribute("error", null);
            session.setAttribute("messageModal", "");

            // Redirect to owner dashboard
            response.sendRedirect("/browse.jsp");
            //request.getRequestDispatcher("/browse.jsp").forward(request, response);

            // Close connections
            rs.close();
            stmt.close();
            conn.close();

            // Catch errors and display message
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            session.setAttribute("messageModal", "true");
            session.setAttribute("message", "An error occurred trying to load home page!<br/>Please try again.");
            session.setAttribute("error", ex.getMessage());
            session.setAttribute("action", "index.jsp");
            session.setAttribute("buttonAction", "messageModal");
            session.setAttribute("buttonLabel", "Try again");
            response.sendRedirect("/home.jsp");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
