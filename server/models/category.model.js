const db = require('./db');

const catSchema = new db.mongoose.Schema(
    {
        name: {type: String, required: true}
    }, 
    {collection: 'catgories'},
);

let catModel = db.mongoose.model('catModel', catSchema);
module.exports = {catModel};