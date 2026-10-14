# Cozy Cafe - Android Game

Game mô phỏng quản lý quán cà phê đồ họa Isometric 3D với phong cách ấm cúng (Cozy Low-Poly).

## Tự động xuất file APK trên GitHub (GitHub Actions)

Quy trình tự động xuất file APK (`.github/workflows/build_apk.yml`) đã được thiết lập sẵn.

### Cách tải file APK sau khi push code lên GitHub:
1. Đẩy code lên repository GitHub của bạn (`git push origin main`).
2. Vào tab **Actions** trên GitHub repository.
3. Chọn workflow **Build and Export Android APK**.
4. Khi workflow hoàn tất (dấu tích xanh), nhấn vào run đó:
   - Kéo xuống mục **Artifacts**.
   - Nhấn tải **CozyCafe-APK** (chứa file `CozyCafe-debug.apk` đã ký sẵn để cài đặt trực tiếp trên điện thoại Android).

### Cách kích hoạt build APK thủ công bất kỳ lúc nào:
1. Vào tab **Actions** trên GitHub.
2. Chọn **Build and Export Android APK** ở danh sách bên trái.
3. Nhấn nút **Run workflow** -> chọn nhánh `main` -> bấm **Run workflow**.

### Tự động tạo Release kèm APK:
- Khi bạn tạo một tag mới (ví dụ: `v1.0.0`) hoặc tạo một GitHub Release, workflow sẽ tự động đính kèm file APK trực tiếp vào trang Releases của GitHub.
