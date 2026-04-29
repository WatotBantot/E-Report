package app;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import models.ComplaintDetail;
import models.Credential;
import models.UserInfo;
import models.UserSession;
import config.UIConfig;
import features.views.AllReportsView;
import features.views.ComplaintDetailView;
import features.views.ComplaintStatusUpdateView;
import features.views.DashboardView;
import features.views.HomepageView;
import features.views.LoginView;
import features.views.MyProfileView;
import features.views.MyReportsView;
import features.views.RegisterView;
import features.views.SubmitReportView;
import features.views.UserManagementView;
import services.controller.DatabaseController;

public class E_Report extends JFrame {
    private CardLayout cardLayout;
    private JPanel container;

    // ==================== Session / User Data ====================
    private UserSession userSession;
    private UserInfo userInfo;
    private Credential credential;
    private ComplaintDetail currentComplaint;
    private List<ComplaintDetail> complaintList = new ArrayList<>();

    // ==================== Resident Dashboard Data ====================
    private int[] residentDashboardStats = new int[4];
    private List<Object[]> residentDashboardReports = new ArrayList<>();

    // ==================== Captain Dashboard Data ====================
    private int[] captainDashboardStats = new int[4];
    private double[] captainLineValues = new double[0];
    private String[] captainLineLabels = new String[0];
    private String[] captainLineDetails = new String[0];
    private String captainLineGraphTitle = "";
    private String[] captainCategoryLabels = new String[0];
    private int[] captainCategoryValues = new int[0];
    private String[] captainStatusLabels = new String[0];
    private int[] captainStatusBackgroundTotals = new int[0];
    private int[] captainStatusValues = new int[0];
    private int captainStatusTotal = 0;
    private String[] captainSourceLabels = new String[0];
    private int[] captainSourceValues = new int[0];
    private int captainSourceTotal = 0;

    // ==================== Secretary Dashboard Data ====================
    private int[] secretaryDashboardStats = new int[4];
    private List<Object[]> secretaryReportDataList = new ArrayList<>();

    // ==================== Global Counters ====================
    private int totalReportByUser;
    private int totalReport;
    private int totalReportByDate;
    private int totalReportByStatus;
    private int totalReportByRole;

    private String returnRoute = "dashboard";

    public E_Report() {
        DatabaseController.initializeDatabase();

        setTitle("Barangay Malacañang E-Reporting System");
        setIconImage(new ImageIcon(getClass().getResource("/assets/images/barangay_logo.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(UIConfig.WIDTH, UIConfig.HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        container.add(new HomepageView(this), "home");

        add(container);

        cardLayout.show(container, "home");
        setVisible(true);
    }

    public void navigate(String route) {
        getContentPane().removeAll();

        switch (route.toLowerCase()) {
            case "login" -> add(new LoginView(this));
            case "home" -> add(new HomepageView(this));
            case "register" -> add(new RegisterView(this));
            case "dashboard" -> add(new DashboardView(this));
            case "profile" -> add(new MyProfileView(this));
            case "myreport" -> add(new MyReportsView(this));
            case "reports" -> add(new AllReportsView(this));
            case "usermanagement" -> add(new UserManagementView(this));
            case "submitreport" -> add(new SubmitReportView(this));
            case "updatestatus" -> add(new ComplaintStatusUpdateView(this));
            case "complaintdetail" -> add(new ComplaintDetailView(this));
        }

        revalidate();

        repaint();

    }

    public void logout() {
        clearSessionData();
        navigate("home");
    }

    private void clearSessionData() {
        this.userSession = null;
        this.userInfo = null;
        this.credential = null;
        this.currentComplaint = null;
        this.complaintList.clear();
        clearDashboardData();
    }

    private void clearDashboardData() {
        this.residentDashboardStats = new int[4];
        this.residentDashboardReports.clear();

        this.captainDashboardStats = new int[4];
        this.captainLineValues = new double[0];
        this.captainLineLabels = new String[0];

        this.secretaryDashboardStats = new int[4];
        this.secretaryReportDataList.clear();
    }

    // ==================== Session / User Encapsulation ====================

    public void setUserSession(UserSession us) {
        this.userSession = us;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public void setUserInfo(UserInfo ui) {
        this.userInfo = ui;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setCredential(Credential cred) {
        this.credential = cred;
    }

    public Credential getCredential() {
        return credential;
    }

    /** Convenience helper so panels never build the full name themselves. */
    public String getCurrentUserFullName() {
        StringBuilder sb = new StringBuilder();
        if (userInfo.getFName() != null)
            sb.append(userInfo.getFName()).append(" ");
        if (userInfo.getLName() != null)
            sb.append(userInfo.getLName());
        String name = sb.toString().trim();
        return name.isEmpty() ? null : name;
    }

    public String getCurrentUserRole() {
        if (userSession == null)
            return null;
        String role = userSession.getRole();
        return role == null ? null : role;
    }

    // ==================== Complaint Encapsulation ====================

    public void setCurrentComplaint(ComplaintDetail cd) {
        this.currentComplaint = cd;
    }

    public ComplaintDetail getCurrentComplaint() {
        return currentComplaint;
    }

    public void setComplaintList(List<ComplaintDetail> list) {
        this.complaintList = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<ComplaintDetail> getComplaintList() {
        return new ArrayList<>(complaintList);
    }

    // ==================== Resident Dashboard ====================

    public void setResidentDashboardStats(int total, int pending, int inProgress, int resolved) {
        this.residentDashboardStats = new int[] { total, pending, inProgress, resolved };
    }

    public int[] getResidentDashboardStats() {
        return residentDashboardStats.clone();
    }

    public void setResidentDashboardReports(List<Object[]> reports) {
        this.residentDashboardReports = reports != null ? new ArrayList<>(reports) : new ArrayList<>();
    }

    public List<Object[]> getResidentDashboardReports() {
        return new ArrayList<>(residentDashboardReports);
    }

    // ==================== Captain Dashboard ====================

    public void setCaptainDashboardStats(int total, int pending, int inProgress, int resolved) {
        this.captainDashboardStats = new int[] { total, pending, inProgress, resolved };
    }

    public int[] getCaptainDashboardStats() {
        return captainDashboardStats.clone();
    }

    public void setCaptainLineGraphData(double[] values, String[] labels, String[] details, String title) {
        this.captainLineValues = values != null ? values.clone() : new double[0];
        this.captainLineLabels = labels != null ? labels.clone() : new String[0];
        this.captainLineDetails = details != null ? details.clone() : new String[0];
        this.captainLineGraphTitle = title != null ? title : "";
    }

    public double[] getCaptainLineValues() {
        return captainLineValues.clone();
    }

    public String[] getCaptainLineLabels() {
        return captainLineLabels.clone();
    }

    public String[] getCaptainLineDetails() {
        return captainLineDetails.clone();
    }

    public String getCaptainLineGraphTitle() {
        return captainLineGraphTitle;
    }

    public void setCaptainCategoryData(String[] labels, int[] values) {
        this.captainCategoryLabels = labels != null ? labels.clone() : new String[0];
        this.captainCategoryValues = values != null ? values.clone() : new int[0];
    }

    public String[] getCaptainCategoryLabels() {
        return captainCategoryLabels.clone();
    }

    public int[] getCaptainCategoryValues() {
        return captainCategoryValues.clone();
    }

    public void setCaptainStatusData(String[] labels, int[] bgTotals, int[] values, int total) {
        this.captainStatusLabels = labels != null ? labels.clone() : new String[0];
        this.captainStatusBackgroundTotals = bgTotals != null ? bgTotals.clone() : new int[0];
        this.captainStatusValues = values != null ? values.clone() : new int[0];
        this.captainStatusTotal = total;
    }

    public String[] getCaptainStatusLabels() {
        return captainStatusLabels.clone();
    }

    public int[] getCaptainStatusBackgroundTotals() {
        return captainStatusBackgroundTotals.clone();
    }

    public int[] getCaptainStatusValues() {
        return captainStatusValues.clone();
    }

    public int getCaptainStatusTotal() {
        return captainStatusTotal;
    }

    public void setCaptainSourceData(String[] labels, int[] values, int total) {
        this.captainSourceLabels = labels != null ? labels.clone() : new String[0];
        this.captainSourceValues = values != null ? values.clone() : new int[0];
        this.captainSourceTotal = total;
    }

    public String[] getCaptainSourceLabels() {
        return captainSourceLabels.clone();
    }

    public int[] getCaptainSourceValues() {
        return captainSourceValues.clone();
    }

    public int getCaptainSourceTotal() {
        return captainSourceTotal;
    }

    // ==================== Secretary Dashboard ====================

    public void setSecretaryDashboardStats(int total, int pending, int inProgress, int resolved) {
        this.secretaryDashboardStats = new int[] { total, pending, inProgress, resolved };
    }

    public int[] getSecretaryDashboardStats() {
        return secretaryDashboardStats.clone();
    }

    public void setSecretaryReportDataList(List<Object[]> reports) {
        this.secretaryReportDataList = reports != null ? new ArrayList<>(reports) : new ArrayList<>();
    }

    public List<Object[]> getSecretaryReportDataList() {
        return new ArrayList<>(secretaryReportDataList);
    }

    // ==================== Global Stats ====================

    public void setTotalReportByUser(int val) {
        this.totalReportByUser = val;
    }

    public int getTotalReportByUser() {
        return totalReportByUser;
    }

    public void setTotalReport(int val) {
        this.totalReport = val;
    }

    public int getTotalReport() {
        return totalReport;
    }

    public void setTotalReportByDate(int val) {
        this.totalReportByDate = val;
    }

    public int getTotalReportByDate() {
        return totalReportByDate;
    }

    public void setTotalReportByStatus(int val) {
        this.totalReportByStatus = val;
    }

    public int getTotalReportByStatus() {
        return totalReportByStatus;
    }

    public void setTotalReportByRole(int val) {
        this.totalReportByRole = val;
    }

    public int getTotalReportByRole() {
        return totalReportByRole;
    }

    public void setReturnRoute(String route) {
        this.returnRoute = route;
    }

    public String getReturnRoute() {
        return returnRoute;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(E_Report::new);
    }
}