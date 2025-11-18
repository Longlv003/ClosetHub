const { cartModel } = require("../models/cart.model");
const { pModel } = require("../models/product.model");
const { pVariantModel } = require("../models/product.variants.model");

exports.addToCart = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    const { id_user, id_product, id_variant, quantity } = req.body;
    const qtyToAdd = quantity || 1;

    // Validate input
    if (!id_user || !id_product || !id_variant) {
      throw new Error("Missing required fields");
    }

    // Kiểm tra product có tồn tại không
    const product = await pModel.findById(id_product);
    if (!product) {
      throw new Error("Product not found");
    }

    // Kiểm tra variant
    const variant = await pVariantModel.findById(id_variant);
    if (!variant) {
      throw new Error("Variant not found");
    }

    // Kiểm tra variant có thuộc product không
    if (variant.product_id.toString() !== id_product) {
      throw new Error("Variant does not belong to product");
    }

    // Kiểm tra tồn kho khi add
    if (variant.quantity < qtyToAdd) {
      throw new Error("Insufficient stock");
    }

    // Kiểm tra cart đã có variant này chưa
    let cartItem = await cartModel.findOne({
      id_user,
      id_product,
      id_variant,
    });

    if (cartItem) {
      const newTotalQty = cartItem.quantity + qtyToAdd;

      if (variant.quantity < newTotalQty) {
        throw new Error("Insufficient stock");
      }

      cartItem.quantity = newTotalQty;
      await cartItem.save();
    } else {
      cartItem = await cartModel.create({
        id_user,
        id_product,
        id_variant,
        quantity: qtyToAdd,
      });
    }

    dataRes.data = cartItem;
  } catch (error) {
    dataRes.msg = error.message;
    dataRes.data = null;
  }

  res.json(dataRes);
};
