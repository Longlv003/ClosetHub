var express = require("express");
var router = express.Router();

/* GET home page. */
router.get("/", function (req, res, next) {
  res.render("index", { title: "Express" });
});

// ví dụ trong routes/index.js hoặc app.js
router.get("/login", (req, res) => {
  res.render("login"); // render views/login.ejs
});

module.exports = router;
