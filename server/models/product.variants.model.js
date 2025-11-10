const db = require("./db");

const pVariantSchema = new db.mongoose.Schema({
  product_id: { type: db.mongoose.Types.ObjectId, ref: "pModel" },
});
