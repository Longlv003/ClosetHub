const {catModel} = require('../models/category.model');
const {pModel} = require('../models/product.model');

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

exports.GetTopCategories = async (req, res, next) => {
    let dataRes = { msg: 'OK' };
    
    try {
        // Đếm số sản phẩm theo từng category
        const categoryCounts = await pModel.aggregate([
            {
                $group: {
                    _id: "$catID",
                    productCount: { $sum: 1 }
                }
            },
            {
                $sort: { productCount: -1 }
            },
            {
                $limit: 4
            }
        ]);

        // Lấy thông tin chi tiết của các category
        const categoryIds = categoryCounts.map(item => item._id);
        const categories = await catModel.find({
            _id: { $in: categoryIds }
        });

        // Kết hợp thông tin
        const result = categories.map(category => {
            const countInfo = categoryCounts.find(item => item._id.toString() === category._id.toString());
            return {
                _id: category._id,
                name: category.name,
                productCount: countInfo ? countInfo.productCount : 0
            };
        });

        // Sắp xếp lại theo số lượng sản phẩm giảm dần
        result.sort((a, b) => b.productCount - a.productCount);

        dataRes.data = result;
        dataRes.total = result.length;
        dataRes.msg = "Lấy danh sách danh mục phổ biến thành công";
        
    } catch (error) {
        console.error('Lỗi khi lấy danh mục phổ biến:', error);
        dataRes.data = null;
        dataRes.msg = 'Lỗi server: ' + error.message;
    }
    
    res.json(dataRes);
};