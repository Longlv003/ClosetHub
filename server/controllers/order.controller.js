const { cartModel } = require("../models/cart.model");
const { billModel } = require("../models/bill.model");
const { billDetailModel } = require("../models/billDetail.model");
const { pModel } = require("../models/product.model");

exports.PlaceOrder = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    const { id_user, address } = req.body;
    console.log("Request body:", req.body);

    if (!id_user || !address) {
      throw new Error("Thieu thong tin nguoi dung hoac dia chi");
    }

    // KIỂM TRA ID_USER CÓ ĐÚNG ĐỊNH DẠNG KHÔNG?
    if (typeof id_user !== "string" || id_user.length < 1) {
      throw new Error("ID user không hợp lệ");
    }

    const cartItem = await cartModel
      .find({ id_user: id_user })
      .populate("id_product");
    console.log("Cart items found:", cartItem);

    if (cartItem.length === 0) {
      throw new Error("Gio hang trong");
    }

    let totalAmount = 0;

    // KIỂM TRA TỒN KHO
    for (const item of cartItem) {
      if (!item.id_product) {
        throw new Error("Sản phẩm trong giỏ hàng không tồn tại");
      }

      const product = await pModel.findById(item.id_product._id);
      if (!product) {
        throw new Error(`Sản phẩm không tồn tại: ${item.id_product._id}`);
      }

      if (product.quantity < item.quantity) {
        throw new Error(`Sản phẩm ${product.name} hết hàng`);
      }

      totalAmount += item.id_product.price * item.quantity;
    }

    // TẠO BILL
    const newBill = new billModel({
      id_user: id_user,
      address: address,
      created_date: new Date(),
      total_amount: totalAmount,
      status: "pending", // Thêm trạng thái
    });

    const savedBill = await newBill.save();
    console.log("Bill saved:", savedBill._id);

    // TẠO BILL DETAILS VÀ UPDATE SỐ LƯỢNG
    for (const item of cartItem) {
      // Tạo bill detail
      const newBillDetails = new billDetailModel({
        id_bill: savedBill._id,
        id_product: item.id_product._id,
        price: item.id_product.price,
        quantity: item.quantity,
      });
      await newBillDetails.save();

      // Update số lượng sản phẩm - SỬA: Dùng updateOne thay vì findByIdAndUpdate
      await pModel.updateOne(
        { _id: item.id_product._id },
        {
          $inc: {
            quantity: -item.quantity,
            total_sold: item.quantity,
          },
        }
      );
    }

    // XÓA GIỎ HÀNG
    await cartModel.deleteMany({ id_user: id_user });

    dataRes.data = {
      bill: savedBill,
      totalAmount: totalAmount,
      items: cartItem.length,
    };
    dataRes.msg = "Đặt hàng thành công!";
  } catch (error) {
    console.error("PlaceOrder Error:", error);
    dataRes.data = null;
    dataRes.msg = error.message;

    // Trả về status code phù hợp
    res.status(400).json(dataRes);
    return;
  }

  res.json(dataRes);
};

exports.GetOrderHistory = async (req, res, next) => {
  let dataRes = { msg: "OK", data: null };

  try {
    const { id_user } = req.params; // Lấy id_user từ URL, ví dụ: /orders/:id_user

    if (!id_user) {
      throw new Error("Thiếu thông tin id_user");
    }

    // Lấy tất cả hóa đơn của user
    const bills = await billModel.find({ id_user }).sort({ created_date: -1 });

    if (!bills || bills.length === 0) {
      dataRes.data = [];
      return res.json(dataRes); // Trả về mảng rỗng nếu chưa có đơn hàng
    }

    // Lấy chi tiết từng hóa đơn
    const history = [];
    for (const bill of bills) {
      const details = await billDetailModel
        .find({ id_bill: bill._id })
        .populate("id_product");

      history.push({
        bill_id: bill._id,
        created_date: bill.created_date,
        total_amount: bill.total_amount,
        address: bill.address,
        products: details.map((item) => ({
          product_id: item.id_product._id,
          name: item.id_product.name,
          price: item.price,
          quantity: item.quantity,
          image: item.id_product.image,
        })),
      });
    }

    dataRes.data = history;
  } catch (error) {
    dataRes.msg = error.message;
    dataRes.data = null;
  }

  res.json(dataRes);
};
