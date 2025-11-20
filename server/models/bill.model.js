var db = require('./db');

const ORDER_STATUSES = ["pending", "processing", "shipping", "delivered", "cancelled"];

const BillSchema = new db.mongoose.Schema(
  {
    created_date: { type: Date, default: Date.now },
    id_user: {
      type: db.mongoose.Schema.Types.ObjectId,
      ref: "userModel",
      required: true,
    },
    total_amount: { type: Number, required: true },
    address: { type: String },
    status: {
      type: String,
      enum: ORDER_STATUSES,
      default: "pending",
    },
  },
  {
    collection: "bill",
  }
);

let billModel = db.mongoose.model("billModel", BillSchema);
module.exports = { billModel, ORDER_STATUSES };