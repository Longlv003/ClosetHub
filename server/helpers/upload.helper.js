const fs = require('fs');
const path = require('path');

exports.uploadSingleFile = async (file, folderName) => {
    let dataRes = {msg: 'OK'};
    try {
        if (!file) {
            throw new Error("File not found");
        }

        const destDir = path.join(__dirname, `../public/images/${folderName}`);

        if (!fs.existsSync(destDir)) {
            fs.mkdirSync(destDir, { recursive: true });
        }
        
        // Đặt tên file duy nhất (tránh trùng)
        const newFileName = Date.now() + '-' + file.originalname;
        const tempPath = file.path;
        const targetPath = path.join(destDir, newFileName);

        // Di chuyển file
        fs.renameSync(tempPath, targetPath);

        // Trả về tên file (không phải URL đầy đủ)
        return newFileName;
    } catch (error) {
        dataRes.msg = error.message;
    }
};