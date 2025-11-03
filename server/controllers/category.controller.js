const {catModel} = require('../models/category.model');

exports.addCat = async (req, res, next) => {
    let dataRes = { msg: 'OK' };

    try {
        const { name } = req.body; 
        if (!name || name.trim() === '') {
            throw new Error("Tên danh mục không được để trống");
        }

        const cat = new catModel({
            name: name.trim()
        });

        await cat.save();

        dataRes.data = cat;

    } catch (error) {
        dataRes.msg = error.message;
    }

    res.json(dataRes);
};

exports.updateCat = async (req, res, next) => {
    let dataRes = {msg: 'OK'};
    try {
        const { _id } = req.params;
        const { name } = req.body;

        const data = await catModel.findByIdAndUpdate(_id, { name }, { new: true });
        dataRes.data = data;
    } catch (error) {
        console.log(error.message);
        dataRes.msg = error.message;
    }
    res.json(dataRes);
}

exports.deleteCat = async (req, res, next) => {
    let dataRes = { msg: 'OK' };

    try {
        const { _id } = req.params;

        if (!_id) {
            throw new Error("Thiếu ID danh mục cần xóa");
        }

        const deleted = await catModel.findByIdAndDelete(_id);

        if (!deleted) {
            throw new Error("Không tìm thấy danh mục để xóa");
        }

        dataRes.msg = "Xóa danh mục thành công!";
        dataRes.data = deleted;

    } catch (error) {
        console.error(error.message);
        dataRes.msg = error.message;
    }

    res.json(dataRes);
};

exports.getListCat = async (req, res, next) => {
    let dataRes = {msg: 'OK'};
    try {
        let list = await catModel.find();
        dataRes.data = list;
    } catch (error) {
        console.log(error.message);
        dataRes.msg = error.message;
    }
    res.json(dataRes);
};