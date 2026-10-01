package vn.iostart.productmanagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class CategoryIconStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path storageDirectory = Paths
            .get("uploads", "categories")
            .toAbsolutePath()
            .normalize();

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể tạo thư mục upload Category",
                    exception
            );
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename()
        );

        String extension = StringUtils
                .getFilenameExtension(originalFilename);

        if (extension == null
                || !ALLOWED_EXTENSIONS.contains(
                        extension.toLowerCase(Locale.ROOT)
                )) {
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh JPG, JPEG, PNG, GIF hoặc WEBP"
            );
        }

        String storedFilename =
                UUID.randomUUID() + "." +
                extension.toLowerCase(Locale.ROOT);

        Path targetFile = storageDirectory
                .resolve(storedFilename)
                .normalize();

        if (!targetFile.startsWith(storageDirectory)) {
            throw new IllegalArgumentException(
                    "Tên file không hợp lệ"
            );
        }

        try {
            Files.copy(
                    file.getInputStream(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể lưu ảnh Category",
                    exception
            );
        }

        return "/uploads/categories/" + storedFilename;
    }

    public void delete(String iconUrl) {
        if (!StringUtils.hasText(iconUrl)) {
            return;
        }

        String filename = Paths.get(iconUrl)
                .getFileName()
                .toString();

        Path file = storageDirectory
                .resolve(filename)
                .normalize();

        if (!file.startsWith(storageDirectory)) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Không làm thất bại thao tác chính nếu không xóa được ảnh cũ.
        }
    }
}