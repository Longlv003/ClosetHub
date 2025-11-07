//const mongoose = require('mongoose');
const {cartModel} = require('../models/cart.model');
const { mongoose } = require('../models/db');
const {pModel} = require('../models/product.model');

exports.GetListMyCart = async (req, res, next) => {
    let dataRes = {msg: 'OK'};

    try {
        const userId = new mongoose.Types.ObjectId(req.params.id_user);
        const cartItems = await cartModel.find({ id_user: userId })
            .populate("id_product"); 
        dataRes.data = cartItems;

    } catch (error) {
        dataRes.msg = error.message;
    }

    res.json(dataRes);
}

exports.addToCart = async (req, res, next) => {
    let dataRes = {msg: 'OK'};
    try {
        const {id_user, id_product, quantity} = req.body;
        const qtyToAdd = quantity || 1;
        
        if (!id_user || !id_product) {
            throw new Error("Thieu thong tin nguoi dung hoac san pham");
        }

        // Kiểm tra sp tồn tại không?
        const product = await pModel.findById(id_product);
        if (!product) {
            throw new Error("Khong tim thay san pham");
        }

        // Kiểm tra số lượng tồn kho (chỉ kiểm tra, không trừ)
        if (product.qty < qtyToAdd) {
            throw new Error("So luong san pham trong kho khong du");
        }

        // Kiểm tra cart có sp chưa?
        let cartItem = await cartModel.findOne({id_user, id_product});
        if (cartItem) {
            // Kiểm tra tổng số lượng sau khi thêm
            const newTotalQuantity = cartItem.quantity + qtyToAdd;
            if (product.qty < newTotalQuantity) {
                throw new Error("So luong san pham trong kho khong du");
            }
            cartItem.quantity = newTotalQuantity;
            await cartItem.save();
        } else {
            cartItem = new cartModel({
                id_user,
                id_product,
                quantity: qtyToAdd
            });
            await cartItem.save();
        }

        // KHÔNG trừ số lượng ở đây - sẽ trừ khi thanh toán
        // product.qty -= qtyToAdd;
        // await product.save();

        dataRes.data = cartItem;
    } catch (error) {
        dataRes.data = null;
        dataRes.msg = error.message;
    }

    res.json(dataRes);
}

exports.UpdateCartQuantity = async (req, res, next) => {
    let dataRes = { msg: 'OK' };
    try {
        const {_id, newQuantity} = req.params;
        // const _id = req.body._id;
        // const newQuantity = req.body.quantity;

        if (!newQuantity || newQuantity < 1) {
            throw new Error("Số lượng không hợp lệ");
        }

        const cartItem = await cartModel.findById(_id);
        if (!cartItem) {
            throw new Error("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        const updated = await cartModel.findByIdAndUpdate(
            _id,
            { quantity: newQuantity },
            { new: true }
        );

        //dataRes.data = updated;
         
        const cartItems = await cartModel.find({ _id: _id })
            .populate("id_product"); 
        dataRes.data = cartItems;
    } catch (error) {
        dataRes.data = null;
        dataRes.msg = error.message;
    }
    res.json(dataRes);
};

exports.DeleteCartItem = async (req, res, next) => {
    let dataRes = { msg: 'OK' };
    try {
        const _id = req.params._id; 

        if (!_id) {
            throw new Error("Thiếu ID sản phẩm trong giỏ hàng");
        }

        const deleted = await cartModel.findByIdAndDelete(_id);

        if (!deleted) {
            throw new Error("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        dataRes.data = deleted;
    } catch (error) {
        dataRes.data = null;
        dataRes.msg = error.message;
    }

    res.json(dataRes);
};