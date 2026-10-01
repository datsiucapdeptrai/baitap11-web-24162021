package vn.iostart.productmanagement.entity;

public enum OrderStatus {
    NEW,             // Đơn hàng mới
    CONFIRMED,       // Đã xác nhận
    PREPARING,       // Chuẩn bị hàng
    SHIPPING,        // Vận chuyển
    DELIVERING,      // Đang giao hàng
    DELIVERED,       // Đã giao
    CANCELLED,       // Đơn hàng hủy
    RETURNED         // Đơn hàng hoàn
}