const nodemailer = require("nodemailer");
require("dotenv").config();

const transporter = nodemailer.createTransport({
  host: process.env.MAIL_HOST,
  port: Number(process.env.MAIL_PORT || 587),
  secure: process.env.MAIL_SECURE === "true",
  auth: {
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS,
  },
});

const defaultFrom = process.env.MAIL_FROM || process.env.MAIL_USER;

const sendMail = async ({ to, subject, text, html }) => {
  if (!defaultFrom) {
    throw new Error("MAIL_FROM or MAIL_USER must be configured");
  }

  await transporter.sendMail({
    from: defaultFrom,
    to,
    subject,
    text,
    html,
  });
};

module.exports = { sendMail };


