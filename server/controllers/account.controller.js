const { userModel } = require("../models/account.model");
const { billModel } = require("../models/bill.model");
const { billDetailModel } = require("../models/billDetail.model");
const bcrypt = require("bcrypt");
const { uploadSingleFile } = require("../helpers/upload.helper");

const baseLoginHandler = async (req, res, { requiredRoles = [] } = {}) => {
  try {
    const { email, pass } = req.body;

    if (!email || !pass) {
      return res.status(400).json({ error: "Missing email or password" });
    }

    const user = await userModel.findByEmailPasswd(email, pass);
    if (!user) {
      return res.status(401).json({ error: "Incorrect login credentials" });
    }

    if (!user.is_active) {
      return res
        .status(403)
        .json({ error: "Account is locked. Please contact admin" });
    }

    if (
      requiredRoles.length > 0 &&
      (!user.role || !requiredRoles.includes(user.role))
    ) {
      return res
        .status(403)
        .json({ error: "Không có quyền truy cập trang quản trị" });
    }

    const token = await userModel.makeAuthToken(user);
    return res.status(200).json({
      message: "Login successful",
      data: { user, token },
    });
  } catch (error) {
    console.log(error.message);
    return res.status(400).send(error);
  }
};

exports.doLogin = (req, res, next) => baseLoginHandler(req, res);

exports.doAdminLogin = (req, res, next) =>
  baseLoginHandler(req, res, { requiredRoles: ["admin"] });

exports.doReg = async (req, res, next) => {
  try {
    const salt = await bcrypt.genSalt(10);

    const existed = await userModel.findOne({ email: req.body.email });
    if (existed) {
      return res.status(400).json({ error: "Email already exists" });
    }

    const user = new userModel(req.body);

    user.pass = await bcrypt.hash(req.body.pass, salt);

    if (req.file) {
      const fileName = await uploadSingleFile(req.file, "avatars");
      user.image = fileName; // lưu tên file vào DB
    }

    const token = await userModel.makeAuthToken(user);

    let newUser = await user.save();

    return res.status(200).json({
      message: "Register successfully",
      data: { newUser, token },
    });
  } catch (error) {
    console.log(error.message);
    return res.status(400).send(error.message);
  }
};

exports.UploadAvatar = async (req, res, next) => {
  let dataRes = { msg: "OK" };
  try {
    const { _id } = req.params;

    if (!req.file) throw new Error("Không có file tải lên");

    const user = await userModel.findById(_id);
    if (!user) throw new Error("Không tìm thấy người dùng");

    const fileName = await uploadSingleFile(req.file, "avatars");
    user.image = fileName;
    await user.save();

    dataRes.msg = "Cập nhật ảnh đại diện thành công";
    dataRes.data = user;
  } catch (error) {
    console.error("UploadAvatar Error:", error);
    dataRes.msg = error.message;
    dataRes.data = null;
  }
  res.json(dataRes);
};

exports.updateUserStatus = async (req, res) => {
  try {
    const { _id } = req.params;
    const { role, is_active } = req.body;

    const user = await userModel.findById(_id);
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    // Không cho admin tự khóa mình
    if (req.user._id.equals("admin")) {
      return res
        .status(400)
        .json({ error: "admin cannot modify your own status or role" });
    }

    // Chỉ cập nhật nếu có dữ liệu
    if (role) user.role = role;
    if (typeof is_active === "boolean") user.is_active = is_active;

    await user.save();

    return res.status(200).json({
      message: "User updated successfully",
      data: user,
    });
  } catch (error) {
    console.log(error.message);
    return res.status(400).send(error);
  }
};

exports.GetAllAccount = async (req, res, next) => {
  let dataRes = { msg: "OK" };
  try {
    let list = await userModel.find();
    dataRes.data = list;
  } catch (error) {
    dataRes.data = null;
    dataRes.msg = error.message;
  }
  res.json(dataRes);
};

exports.UpdateUser = async (req, res, next) => {
  let dataRes = { msg: "OK" };

  try {
    const { _id } = req.params;
    const { email, phone, name, address, is_active } = req.body; // đọc text fields từ multipart

    const user = await userModel.findById(_id);
    if (!user) throw new Error("Người dùng không tồn tại");

    let updateData = {};

    if (email && email !== user.email) {
      const existingUser = await userModel.findOne({ email });
      if (existingUser) throw new Error("Email đã tồn tại");
      updateData.email = email;
    }

    if (phone) updateData.phone = phone;
    if (name) updateData.name = name;
    if (address) updateData.address = address;
    if (typeof is_active !== "undefined") {
      updateData.is_active =
        typeof is_active === "string" ? is_active === "true" : !!is_active;
    }

    // ✅ Thêm name và address
    if (name) updateData.name = name;
    if (address) updateData.address = address;

    // ✅ Nếu có file upload
    if (req.file) {
      const fileName = await uploadSingleFile(req.file, "avatars");
      updateData.image = fileName;
    }

    if (Object.keys(updateData).length === 0) {
      throw new Error("Không có dữ liệu để cập nhật");
    }

    const updatedUser = await userModel
      .findByIdAndUpdate(_id, updateData, { new: true })
      .select("-pass -token");

    if (!updatedUser) throw new Error("Cập nhật thất bại");

    dataRes.msg = "Cập nhật thông tin thành công";
    dataRes.data = updatedUser;
  } catch (error) {
    console.error("UpdateUser Error:", error);
    dataRes.data = null;
    dataRes.msg = error.message;
  }

  res.json(dataRes);
};

exports.DeleteUser = async (req, res) => {
  const dataRes = { msg: "OK", data: null };
  try {
    const { id } = req.params;
    const user = await userModel.findById(id);
    if (!user) {
      return res.status(404).json({ msg: "Không tìm thấy khách hàng" });
    }
    if (user.role === "admin") {
      throw new Error("Không thể xóa tài khoản admin.");
    }
    await userModel.findByIdAndDelete(id);
    dataRes.msg = "Đã xóa khách hàng.";
    res.json(dataRes);
  } catch (error) {
    console.error("DeleteUser error", error);
    dataRes.msg = error.message;
    res.status(400).json(dataRes);
  }
};

exports.GetAdminCustomerOverview = async (req, res) => {
  const dataRes = { msg: "OK", data: [] };
  try {
    const customers = await userModel
      .find({ role: { $ne: "admin" } })
      .select("name email phone address role is_active createdAt image");

    const customerIds = customers.map((c) => c._id);
    const bills = await billModel
      .find({ id_user: { $in: customerIds } })
      .select("id_user total_amount created_date")
      .lean();

    const billMap = new Map();
    bills.forEach((bill) => {
      const key = String(bill.id_user);
      if (!billMap.has(key)) {
        billMap.set(key, {
          totalOrders: 0,
          totalSpent: 0,
          lastOrderDate: null,
        });
      }
      const entry = billMap.get(key);
      entry.totalOrders += 1;
      entry.totalSpent += bill.total_amount || 0;
      if (
        !entry.lastOrderDate ||
        new Date(bill.created_date) > entry.lastOrderDate
      ) {
        entry.lastOrderDate = new Date(bill.created_date);
      }
    });

    dataRes.data = customers.map((customer) => {
      const stats = billMap.get(String(customer._id)) || {
        totalOrders: 0,
        totalSpent: 0,
        lastOrderDate: null,
      };
      return {
        _id: customer._id,
        name: customer.name,
        email: customer.email,
        phone: customer.phone,
        address: customer.address,
        image: customer.image,
        is_active: customer.is_active,
        totalOrders: stats.totalOrders,
        totalSpent: stats.totalSpent,
        lastOrderDate: stats.lastOrderDate,
        createdAt: customer.createdAt,
      };
    });

    res.json(dataRes);
  } catch (error) {
    console.error("GetAdminCustomerOverview error", error);
    dataRes.msg = error.message;
    res.status(500).json(dataRes);
  }
};

exports.GetCustomerDetail = async (req, res) => {
  const dataRes = { msg: "OK", data: null };
  try {
    const { id } = req.params;
    const customer = await userModel
      .findById(id)
      .select("-pass -token")
      .lean();
    if (!customer) {
      return res.status(404).json({ msg: "Không tìm thấy khách hàng" });
    }

    const bills = await billModel
      .find({ id_user: id })
      .sort({ created_date: -1 })
      .lean();
    const billIds = bills.map((bill) => bill._id);
    const details = await billDetailModel
      .find({ id_bill: { $in: billIds } })
      .populate({ path: "id_product", select: "name image catID price" })
      .lean();

    const detailMap = new Map();
    const productBehavior = new Map();
    details.forEach((detail) => {
      const billKey = String(detail.id_bill);
      if (!detailMap.has(billKey)) {
        detailMap.set(billKey, []);
      }
      detailMap.get(billKey).push(detail);

      const productId =
        detail.id_product?._id?.toString() || detail.id_product?.toString();
      if (!productId) return;
      if (!productBehavior.has(productId)) {
        productBehavior.set(productId, {
          product_id: productId,
          name: detail.id_product?.name || "Sản phẩm",
          image: detail.id_product?.image || null,
          quantity: 0,
          revenue: 0,
        });
      }
      const entry = productBehavior.get(productId);
      const price = detail.price || detail.id_product?.price || 0;
      entry.quantity += detail.quantity || 0;
      entry.revenue += price * (detail.quantity || 0);
    });

    const orders = bills.map((bill) => {
      const items = detailMap.get(String(bill._id)) || [];
      return {
        _id: bill._id,
        created_date: bill.created_date,
        total_amount: bill.total_amount,
        address: bill.address,
        status: bill.status,
        items: items.map((item) => ({
          product_id: item.id_product?._id,
          name: item.id_product?.name || "Sản phẩm",
          image: item.id_product?.image,
          quantity: item.quantity,
          price: item.price,
        })),
      };
    });

    const totalOrders = bills.length;
    const totalSpent = bills.reduce(
      (sum, bill) => sum + (bill.total_amount || 0),
      0
    );
    const avgOrderValue = totalOrders ? totalSpent / totalOrders : 0;
    const lastOrderDate = bills[0]?.created_date || null;

    const topProducts = Array.from(productBehavior.values())
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 5);

    dataRes.data = {
      customer,
      summary: {
        totalOrders,
        totalSpent,
        avgOrderValue,
        lastOrderDate,
      },
      behavior: {
        topProducts,
      },
      orders,
    };

    res.json(dataRes);
  } catch (error) {
    console.error("GetCustomerDetail error", error);
    dataRes.msg = error.message;
    res.status(400).json(dataRes);
  }
};
