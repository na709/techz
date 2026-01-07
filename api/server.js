const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');

const app = express();
const port = 3000; // port

app.use(cors());
app.use(express.json());


const IMAGE_BASE_URL = "http://160.250.247.5/images/";

// 3. connect database
const db = mysql.createPool({
    //vps mysql config
    host : 'localhost', //comment dòng này nếu chạy local
    //host: '160.250.247.5', // ip vps 
    user: 'techz_admin',  
    password: '123123',   
    database: 'techz',    
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Kiểm tra kết nối
db.getConnection((err, connection) => {
    if (err) {
        console.error('❌ Lỗi kết nối Database:', err.message);
    } else {
        console.log('✅ Đã kết nối thành công tới MySQL trên VPS!');
        connection.release();
    }
});

// ===============================================
// các endpoint API
// ===============================================

// list products
// http://localhost:3000/api/products
app.get('/api/products', (req, res) => {
    const sql = "SELECT * FROM San_Pham";
    
    db.query(sql, (err, results) => {
        if (err) return res.status(500).json({ error: err.message });

        const products = results.map(item => {
            return {
                ...item,
                hinh_anh_full: item.hinh_anh ? (IMAGE_BASE_URL + item.hinh_anh) : null
            };
        });

        res.json(products);
    });
});

// product details
// http://localhost:3000/api/products/1
app.get('/api/products/:id', (req, res) => {
    const id = req.params.id;
    const sql = "SELECT * FROM San_Pham WHERE id_san_pham = ?";
    
    db.query(sql, [id], (err, results) => {
        if (err) return res.status(500).json({ error: err.message });
        if (results.length === 0) return res.status(404).json({ message: "Không tìm thấy sản phẩm" });

        const product = results[0];
        product.hinh_anh_full = product.hinh_anh ? (IMAGE_BASE_URL + product.hinh_anh) : null;
        
        res.json(product);
    });
});

// auth - login 
// POST http://localhost:3000/api/login
app.post('/api/login', (req, res) => {
    const { username, password } = req.body;
    const sql = "SELECT * FROM Khach_Hang WHERE ten_dang_nhap = ? AND mat_khau = ?";
    
    db.query(sql, [username, password], (err, results) => {
        if (err) return res.status(500).json({ error: err.message });
        
        if (results.length > 0) {
            const user = results[0];
            res.json({ 
                success: true, 
                message: "Đăng nhập thành công!",
                user: {
                    id: user.id_khach_hang,
                    name: user.ten_khach_hang,
                    email: user.email
                }
            });
        } else {
            res.status(401).json({ success: false, message: "Sai tài khoản hoặc mật khẩu" });
        }
    });
});

// ===============================================
// start server
// ===============================================
app.listen(port, () => {
    console.log(`🚀 API Server đang chạy tại: http://localhost:${port}`);
});