const db = require("./db");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcrypt");
require("dotenv").config();

const tokenSecret = process.env.TOKEN_SEC_KEY || "default_secret";

const userSchema = new db.mongoose.Schema(
  {
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    pass: { type: String, required: true },
    name: { type: String, trim: true },
    address: { type: String, trim: true },
    phone: { type: String, trim: true },
    role: { type: String, default: "user" },
    is_active: { type: Boolean, default: true },
    image: { type: String },
    token: { type: String },
  },
  {
    collection: "account",
    timestamps: true,
  }
);

userSchema.statics.findByEmailPasswd = async function (email, pass) {
  const user = await this.findOne({ email });
  if (!user) {
    return null;
  }

  const isMatch = await bcrypt.compare(pass, user.pass);
  if (!isMatch) {
    return null;
  }

  return user;
};

userSchema.statics.makeAuthToken = async function (user) {
  const payload = {
    _id: user._id,
    email: user.email,
    role: user.role,
  };

  const token = jwt.sign(payload, tokenSecret, { expiresIn: "24h" });
  user.token = token;

  // Save immediately only if document already exists in DB
  if (!user.isNew) {
    await user.save();
  }

  return token;
};

const userModel = db.mongoose.model("userModel", userSchema);

module.exports = { userModel };
