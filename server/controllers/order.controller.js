const { cartModel } = require("../models/cart.model");
const { billModel, ORDER_STATUSES } = require("../models/bill.model");
const { billDetailModel } = require("../models/billDetail.model");
const { pModel } = require("../models/product.model");

exports.PlaceOrder = async (req, res, next) => {
    let dataRes = {msg: 'OK'};
    
    try {
        // SỬA: Dùng req.body thay vì req.params cho POST request
        const {id_user, address} = req.body;
        console.log('Request body:', req.body);

        if (!id_user || !address) {
            throw new Error("Thieu thong tin nguoi dung hoac dia chi");
        }

        // KIỂM TRA ID_USER CÓ ĐÚNG ĐỊNH DẠNG KHÔNG?
        if (typeof id_user !== 'string' || id_user.length < 1) {
            throw new Error("ID user không hợp lệ");
        }

        const cartItem = await cartModel.find({id_user: id_user}).populate('id_product');
        console.log('Cart items found:', cartItem);

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
            status: 'pending' // Thêm trạng thái
        });
        
        const savedBill = await newBill.save();
        console.log('Bill saved:', savedBill._id);

        // TẠO BILL DETAILS VÀ UPDATE SỐ LƯỢNG
        for (const item of cartItem) {
            // Tạo bill detail
            const newBillDetails = new billDetailModel({
                id_bill: savedBill._id,
                id_product: item.id_product._id,
                price: item.id_product.price,
                quantity: item.quantity
            });
            await newBillDetails.save();

            // Update số lượng sản phẩm - SỬA: Dùng updateOne thay vì findByIdAndUpdate
            await pModel.updateOne(
                { _id: item.id_product._id },
                { 
                    $inc: { 
                        quantity: -item.quantity,
                        total_sold: item.quantity
                    } 
                }
            );
        }

        // XÓA GIỎ HÀNG
        await cartModel.deleteMany({id_user: id_user});

        dataRes.data = {
            bill: savedBill, 
            totalAmount: totalAmount,
            items: cartItem.length
        };
        dataRes.msg = "Đặt hàng thành công!";

    } catch (error) {
        console.error('PlaceOrder Error:', error);
        dataRes.data = null;
        dataRes.msg = error.message;
        
        // Trả về status code phù hợp
        res.status(400).json(dataRes);
        return;
    }

    res.json(dataRes);
}

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
        status: bill.status,
        products: details.map((item) => ({
          product_id: item.id_product?._id,
          name: item.id_product?.name,
          price: item.price,
          quantity: item.quantity,
          image: item.id_product?.image,
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

const ORDER_STATUS_LABELS = {
  pending: "Chờ xử lý",
  processing: "Đang chuẩn bị",
  shipping: "Đang giao",
  delivered: "Hoàn thành",
  cancelled: "Đã hủy",
};

const mapBillToResponse = (bill, detailMap) => {
  const items = detailMap.get(String(bill._id)) || [];
  const customer = bill.id_user || {};

  return {
    _id: bill._id,
    code: `CH-${String(bill._id).slice(-6).toUpperCase()}`,
    created_date: bill.created_date,
    total_amount: bill.total_amount,
    address: bill.address,
    status: bill.status || "pending",
    status_label: ORDER_STATUS_LABELS[bill.status] || "Chờ xử lý",
    customer: {
      id: customer._id,
      name: customer.name || "Khách hàng",
      email: customer.email || "",
      phone: customer.phone || "",
      address: customer.address || bill.address,
    },
    items: items.map((item) => ({
      id: item.id_product?._id,
      name: item.id_product?.name || "Sản phẩm",
      price: item.price,
      quantity: item.quantity,
      image: item.id_product?.image,
    })),
    item_count: items.reduce((sum, cur) => sum + (cur.quantity || 0), 0),
  };
};

exports.GetAdminOrders = async (req, res) => {
  const dataRes = { msg: "OK", data: [] };
  try {
    const { status, q } = req.query || {};
    const filter = {};
    if (status && ORDER_STATUSES.includes(status)) {
      filter.status = status;
    }

    const bills = await billModel
      .find(filter)
      .populate({ path: "id_user", select: "name email phone address" })
      .sort({ created_date: -1 });

    const billIds = bills.map((bill) => bill._id);
    const details = await billDetailModel
      .find({ id_bill: { $in: billIds } })
      .populate({ path: "id_product", select: "name image price" });

    const detailMap = new Map();
    details.forEach((detail) => {
      const key = String(detail.id_bill);
      if (!detailMap.has(key)) {
        detailMap.set(key, []);
      }
      detailMap.get(key).push(detail);
    });

    let payload = bills.map((bill) => mapBillToResponse(bill, detailMap));

    if (q) {
      const search = q.trim().toLowerCase();
      payload = payload.filter((order) => {
        return (
          order.code.toLowerCase().includes(search) ||
          order.customer.name.toLowerCase().includes(search) ||
          order.customer.email.toLowerCase().includes(search)
        );
      });
    }

    dataRes.data = payload;
    res.json(dataRes);
  } catch (error) {
    console.error("GetAdminOrders error", error);
    dataRes.msg = error.message;
    res.status(500).json(dataRes);
  }
};

exports.UpdateOrderStatus = async (req, res) => {
  const dataRes = { msg: "OK", data: null };
  try {
    const { orderId } = req.params;
    const { status } = req.body || {};

    if (!ORDER_STATUSES.includes(status)) {
      throw new Error("Trạng thái không hợp lệ.");
    }

    const updated = await billModel
      .findByIdAndUpdate(
        orderId,
        { status },
        { new: true }
      )
      .populate({ path: "id_user", select: "name email phone address" });

    if (!updated) {
      res.status(404).json({ msg: "Không tìm thấy đơn hàng." });
      return;
    }

    const details = await billDetailModel
      .find({ id_bill: orderId })
      .populate({ path: "id_product", select: "name image price" });

    const detailMap = new Map([[String(orderId), details]]);
    dataRes.data = mapBillToResponse(updated, detailMap);
    res.json(dataRes);
  } catch (error) {
    console.error("UpdateOrderStatus error", error);
    dataRes.msg = error.message;
    res.status(400).json(dataRes);
  }
};

const buildRangeDefaults = (range) => {
  const end = new Date();
  end.setHours(23, 59, 59, 999);
  const start = new Date(end);

  if (range === "year") {
    start.setFullYear(start.getFullYear() - 1);
  } else if (range === "month") {
    start.setMonth(start.getMonth() - 5);
  } else {
    start.setDate(start.getDate() - 29);
  }

  start.setHours(0, 0, 0, 0);
  return { start, end };
};

const formatSeriesKey = (date, range) => {
  const d = new Date(date);
  if (range === "year") {
    return {
      key: `${d.getFullYear()}`,
      label: `${d.getFullYear()}`,
      order: new Date(`${d.getFullYear()}-01-01`).getTime(),
    };
  }
  if (range === "month") {
    const month = (d.getMonth() + 1).toString().padStart(2, "0");
    const label = `${month}/${d.getFullYear()}`;
    return {
      key: `${d.getFullYear()}-${month}`,
      label,
      order: new Date(`${d.getFullYear()}-${month}-01`).getTime(),
    };
  }
  const day = d.getDate().toString().padStart(2, "0");
  const month = (d.getMonth() + 1).toString().padStart(2, "0");
  const label = `${day}/${month}`;
  const key = `${d.getFullYear()}-${month}-${day}`;
  const order = new Date(`${d.getFullYear()}-${month}-${day}`).getTime();
  return { key, label, order };
};

exports.GetRevenueReport = async (req, res) => {
  const dataRes = { msg: "OK", data: null };
  try {
    const { range = "day", from, to } = req.query || {};
    const normalizedRange = ["day", "month", "year"].includes(range)
      ? range
      : "day";

    let startDate;
    let endDate;

    if (from) {
      startDate = new Date(from);
      startDate.setHours(0, 0, 0, 0);
    }
    if (to) {
      endDate = new Date(to);
      endDate.setHours(23, 59, 59, 999);
    }

    if (!startDate || !endDate) {
      const defaults = buildRangeDefaults(normalizedRange);
      startDate = startDate || defaults.start;
      endDate = endDate || defaults.end;
    }

    if (startDate > endDate) {
      throw new Error("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc.");
    }

    const match = {
      created_date: {
        $gte: startDate,
        $lte: endDate,
      },
    };

    const bills = await billModel
      .find(match)
      .sort({ created_date: 1 })
      .lean();

    const totalOrders = bills.length;
    const totalRevenue = bills.reduce(
      (sum, bill) => sum + (bill.total_amount || 0),
      0
    );
    const averageOrderValue = totalOrders ? totalRevenue / totalOrders : 0;

    const seriesMap = new Map();
    bills.forEach((bill) => {
      const meta = formatSeriesKey(bill.created_date, normalizedRange);
      if (!seriesMap.has(meta.key)) {
        seriesMap.set(meta.key, {
          label: meta.label,
          revenue: 0,
          orders: 0,
          order: meta.order,
        });
      }
      const entry = seriesMap.get(meta.key);
      entry.revenue += bill.total_amount || 0;
      entry.orders += 1;
    });

    const revenueSeries = Array.from(seriesMap.values()).sort(
      (a, b) => a.order - b.order
    );

    const billIds = bills.map((bill) => bill._id);
    let topProducts = [];
    if (billIds.length) {
      const details = await billDetailModel
        .find({ id_bill: { $in: billIds } })
        .populate({ path: "id_product", select: "name image price" })
        .lean();

      const productMap = new Map();
      details.forEach((detail) => {
        const productId =
          detail.id_product?._id?.toString() || detail.id_product?.toString();
        if (!productId) {
          return;
        }
        if (!productMap.has(productId)) {
          productMap.set(productId, {
            product_id: productId,
            name: detail.id_product?.name || "Sản phẩm",
            image: detail.id_product?.image || null,
            quantity: 0,
            revenue: 0,
          });
        }
        const entry = productMap.get(productId);
        const price = detail.price || detail.id_product?.price || 0;
        entry.quantity += detail.quantity || 0;
        entry.revenue += (detail.quantity || 0) * price;
      });

      topProducts = Array.from(productMap.values())
        .sort((a, b) => b.quantity - a.quantity)
        .slice(0, 5);
    }

    dataRes.data = {
      totals: {
        totalRevenue,
        totalOrders,
        averageOrderValue,
      },
      series: revenueSeries,
      topProducts,
      filters: {
        range: normalizedRange,
        from: startDate,
        to: endDate,
      },
    };

    res.json(dataRes);
  } catch (error) {
    console.error("GetRevenueReport error", error);
    dataRes.msg = error.message;
    res.status(400).json(dataRes);
  }
};
