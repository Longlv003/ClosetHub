var { pModel } = require("../models/product.model");
const { uploadSingleFile } = require("../helpers/upload.helper");
const mongoose = require("mongoose");
const { pVariantModel } = require("../models/product.variants.model");

exports.addProduct = async (req, res, next) => {
  let dataRes = { msg: "OK" };
  try {
    const { name, catID, description, variants } = req.body;

    if (!name || !catID) {
      throw new Error("Missing required fields");
    }

    // Tạo sản phẩm
    const product = new pModel({
      name,
      catID,
      description: description || "",
    });

    // Upload ảnh nếu có
    if (req.file) {
      const fileName = await uploadSingleFile(req.file, "products");
      product.image = fileName;
    }

    const savedProduct = await product.save();

    // Nếu có variants gửi kèm (dạng mảng JSON)
    if (variants && Array.isArray(JSON.parse(variants))) {
      const variantList = JSON.parse(variants).map((v) => ({
        ...v,
        product_id: savedProduct._id,
      }));
      await pVariantModel.insertMany(variantList);
    }

    // Populate để trả về product có variants
    const fullProduct = await pModel
      .findById(savedProduct._id)
      .populate("variants");

    dataRes.data = fullProduct;
    dataRes.msg = "Product added successfully";
  } catch (error) {
    console.error(error);
    dataRes.msg = error.message;
  }

  res.json(dataRes);
};

exports.EditProduct = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    const { _id } = req.params;
    const { name, description } = req.body;

    if (!_id) {
      throw new Error("Missing product ID");
    }

    // Tìm sản phẩm
    const product = await pModel.findById(_id);
    if (!product) {
      throw new Error("Product not found");
    }

    // Kiểm tra xem sản phẩm có bị xóa mềm không
    if (product.is_deleted) {
      throw new Error("Cannot edit a deleted product");
    }

    // Tạo object update
    let updateData = {};
    if (name) updateData.name = name;
    if (description) updateData.description = description;

    // Upload ảnh nếu có
    if (req.file) {
      const fileName = await uploadSingleFile(req.file, "products");
      updateData.image = fileName;
    }

    if (Object.keys(updateData).length === 0) {
      throw new Error("No data to update");
    }

    // Cập nhật thời gian sửa
    updateData.updatedAt = new Date();

    // Update DB
    const updatedProduct = await pModel.findByIdAndUpdate(_id, updateData, {
      new: true, // trả về bản ghi sau khi update
    });

    dataRes.msg = "Product updated successfully";
    dataRes.data = updatedProduct;
  } catch (error) {
    console.error("EditProduct Error:", error);
    dataRes.msg = error.message;
  }

  res.json(dataRes);
};

exports.DeleteProduct = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    const { _id } = req.params;

    if (!_id) {
      throw new Error("Missing product ID");
    }

    // Tìm sản phẩm
    const product = await pModel.findById(_id);
    if (!product) {
      throw new Error("Product not found");
    }

    // Nếu đã bị xóa rồi thì báo luôn
    if (product.is_deleted) {
      dataRes.msg = "This product has already been deleted";
      dataRes.data = product;
      return res.json(dataRes);
    }

    // Đánh dấu xóa mềm
    product.is_deleted = true;
    product.deleted_at = new Date();

    await product.save();

    // Nếu bạn muốn xóa mềm luôn cả variants đi kèm:
    await pVariantModel.updateMany(
      { product_id: _id },
      { $set: { is_deleted: true, deleted_at: new Date() } }
    );

    dataRes.msg = "Product soft-deleted successfully";
    dataRes.data = product;
  } catch (error) {
    console.error("DeleteProduct Error:", error);
    dataRes.msg = error.message;
  }

  res.json(dataRes);
};

exports.UploadImage = async (req, res, next) => {
  let dataRes = { msg: "OK" };
  try {
    const fileName = await uploadSingleFile(req.file, "products");
    dataRes.data = fileName;
  } catch (error) {
    dataRes.msg = error.message;
  }
  res.json(dataRes);
};

exports.GetListProduct = async (req, res, next) => {
  let dataRes = { msg: "OK" };
  try {
    // Lấy danh sách sản phẩm
    const products = await pModel.find({ is_deleted: { $ne: true } });

    // Lấy tổng quantity của từng sản phẩm từ variants
    const variantQuantities = await pVariantModel.aggregate([
      { $match: { is_deleted: { $ne: true } } },
      { $group: { _id: "$product_id", totalQty: { $sum: "$quantity" } } },
    ]);

    // Chuyển mảng aggregate thành object {product_id: totalQty}
    const quantityMap = {};
    variantQuantities.forEach((v) => {
      quantityMap[v._id.toString()] = v.totalQty;
    });

    // Gắn quantity vào từng sản phẩm
    const result = products.map((p) => ({
      ...p.toObject(),
      quantity: quantityMap[p._id.toString()] || 0,
    }));

    dataRes.data = result;
  } catch (error) {
    dataRes.data = null;
    dataRes.msg = error.message;
  }
  res.json(dataRes);
};

exports.GetProductByCat = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    // Kiểm tra nếu không có catID thì trả về lỗi
    if (!req.query.catID || req.query.catID.trim() === "") {
      return res.status(400).json({
        msg: "Missing parameter: catID",
        data: null,
      });
    }

    // Kiểm tra định dạng ObjectId có hợp lệ không
    if (!mongoose.Types.ObjectId.isValid(req.query.catID)) {
      return res.status(400).json({
        msg: "Invalid category ID format",
        data: null,
      });
    }

    // Tạo điều kiện tìm kiếm theo catID
    const dk = {
      catID: new mongoose.Types.ObjectId(req.query.catID),
      is_deleted: { $ne: true },
    };

    // Lấy danh sách sản phẩm theo catID
    const products = await pModel.find(dk);

    // Nếu không có sản phẩm nào, trả về danh sách rỗng
    if (!products || products.length === 0) {
      dataRes.data = [];
      dataRes.msg = "No products found for this category";
      return res.json(dataRes);
    }

    // Lấy tổng quantity của từng sản phẩm từ variants
    const variantQuantities = await pVariantModel.aggregate([
      {
        $match: {
          is_deleted: { $ne: true },
          product_id: { $in: products.map((p) => p._id) },
        },
      },
      { $group: { _id: "$product_id", totalQty: { $sum: "$quantity" } } },
    ]);

    // Tạo map lưu tổng quantity theo product_id
    const quantityMap = {};
    variantQuantities.forEach((v) => {
      quantityMap[v._id.toString()] = v.totalQty;
    });

    // Gắn quantity vào từng sản phẩm
    const result = products.map((p) => ({
      ...p.toObject(),
      quantity: quantityMap[p._id.toString()] || 0,
    }));

    // Gán dữ liệu trả về
    dataRes.data = result;
    dataRes.msg = "Products retrieved successfully";
  } catch (error) {
    console.error("Error fetching products by category:", error);
    dataRes.data = null;
    dataRes.msg = "Server error: " + error.message;
    return res.status(500).json(dataRes);
  }

  res.json(dataRes);
};

exports.GetTopSellingProducts = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    // Gom nhóm theo product_id để tính tổng quantity và total_sold
    const variantStats = await pVariantModel.aggregate([
      { $match: { is_deleted: { $ne: true } } }, // Bỏ qua các variant bị xóa mềm
      {
        $group: {
          _id: "$product_id",
          totalQty: { $sum: "$quantity" },
          totalSold: { $sum: "$total_sold" },
        },
      },
      { $sort: { totalSold: -1 } }, // Sắp xếp giảm dần theo tổng sold
      { $limit: 10 }, // Giới hạn 10 sản phẩm bán chạy nhất
    ]);

    // Nếu không có kết quả
    if (!variantStats || variantStats.length === 0) {
      dataRes.data = [];
      dataRes.msg = "No selling data found";
      return res.json(dataRes);
    }

    // Lấy danh sách product_id từ kết quả aggregate
    const productIds = variantStats.map((v) => v._id);

    // Lấy thông tin chi tiết sản phẩm từ bảng pModel
    const products = await pModel.find({
      _id: { $in: productIds },
      is_deleted: { $ne: true },
    });

    // Tạo map để tra nhanh thống kê theo product_id
    const statMap = {};
    variantStats.forEach((v) => {
      statMap[v._id.toString()] = {
        quantity: v.totalQty,
        total_sold: v.totalSold,
      };
    });

    // Gắn quantity và total_sold vào từng sản phẩm
    const result = products.map((p) => ({
      ...p.toObject(),
      quantity: statMap[p._id.toString()]?.quantity || 0,
      total_sold: statMap[p._id.toString()]?.total_sold || 0,
    }));

    dataRes.data = result;
    dataRes.msg = "Top-selling products retrieved successfully";
  } catch (error) {
    console.error("Error fetching top-selling products:", error);
    dataRes.data = null;
    dataRes.msg = "Server error: " + error.message;
  }

  res.json(dataRes);
};

exports.UpdateFavorite = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    const { _id, is_favorite } = req.params;

    // Kiểm tra id có tồn tại không
    if (!_id) {
      throw new Error("Thiếu thông tin sản phẩm");
    }

    // Kiểm tra is_favorite có hợp lệ không
    if (is_favorite !== "true" && is_favorite !== "false") {
      throw new Error("Trạng thái favorite không hợp lệ");
    }

    // Chuyển is_favorite từ string sang boolean
    const isFavorite = is_favorite === "true";

    // Kiểm tra sản phẩm
    const product = await pModel.findById(_id);
    if (!product) {
      throw new Error("Sản phẩm không tồn tại");
    }

    // Update favorite
    const updatedProduct = await pModel.findByIdAndUpdate(
      _id,
      {
        $set: {
          is_favorite: isFavorite,
        },
      },
      { new: true }
    );

    dataRes.data = updatedProduct;
    dataRes.msg = isFavorite
      ? "Đã thêm vào yêu thích"
      : "Đã xóa khỏi yêu thích";
  } catch (error) {
    console.error("UpdateFavorite Error:", error);
    dataRes.data = null;
    dataRes.msg = error.message;
  }

  res.json(dataRes);
};

exports.GetFavoriteProducts = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    // Lấy danh sách sản phẩm có is_favorite = true
    const products = await pModel.find({ is_favorite: true }).exec();

    // Lấy tổng quantity cho từng sản phẩm từ bảng variants
    const variantQuantities = await pVariantModel.aggregate([
      { $match: { product_id: { $in: products.map((p) => p._id) } } },
      {
        $group: {
          _id: "$product_id",
          totalQty: { $sum: "$quantity" }, // tính tổng quantity
        },
      },
    ]);

    // Tạo map để tra nhanh quantity theo product_id
    const quantityMap = {};
    variantQuantities.forEach((v) => {
      quantityMap[v._id.toString()] = v.totalQty;
    });

    // Gắn quantity vào từng sản phẩm
    const result = products.map((p) => ({
      ...p.toObject(),
      quantity: quantityMap[p._id.toString()] || 0,
    }));

    dataRes.data = result;
    dataRes.msg = "Favorite products retrieved successfully";
  } catch (error) {
    console.error("Error fetching favorite products:", error);
    dataRes.data = null;
    dataRes.msg = "Server error: " + error.message;
  }

  res.json(dataRes);
};
