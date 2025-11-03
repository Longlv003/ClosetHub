var express = require('express');
var router = express.Router();
var mdw = require('../middleware/api.auth');
var accountCtrl = require('../controllers/account.controller');
var multer = require('multer');
var path = require('path');
var upload = multer({dest: path.join(__dirname, '../public/upload')});
var catCtrl = require('../controllers/category.controller');

// User
router.post('/account/register', upload.single('image'), accountCtrl.doReg);
router.post('/account/login', accountCtrl.doLogin);
router.post('/account/upload-avatar',upload.single("image"), accountCtrl.UploadAvatar);

// Category
router.post('/category/add', catCtrl.addCat);
router.put('/category/edit/:_id', catCtrl.updateCat);
router.delete('/category/delete/:_id', catCtrl.deleteCat);
router.get('/category/list', catCtrl.getListCat);

module.exports = router;
