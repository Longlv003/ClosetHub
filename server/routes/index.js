var express = require("express");
var router = express.Router();

/* GET home page. */
router.get("/", function (req, res, next) {
  res.render("index", { title: "Express" });
});

// Trang đăng nhập admin
router.get("/login", (req, res) => {
  res.render("login");
});

// Admin views
const renderAdminDashboard = (req, res) => {
  res.render("admin_dashboard");
};

const renderAdminProducts = (req, res) => {
  res.render("admin_products");
};

const renderAdminOrders = (req, res) => {
  res.render("admin_orders");
};

const renderAdminReports = (req, res) => {
  res.render("admin_reports");
};

const renderAdminCustomers = (req, res) => {
  res.render("admin_customers");
};

router.get("/admin", renderAdminDashboard);
router.get("/admin/products", renderAdminProducts);
router.get("/admin/orders", renderAdminOrders);
router.get("/admin/customers", renderAdminCustomers);
router.get("/admin/reports", renderAdminReports);

module.exports = router;
