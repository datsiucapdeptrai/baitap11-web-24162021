let categoryModal;
let currentPage = 0;
let currentSize = 5;
let currentKeyword = "";

const categoryCache = new Map();

document.addEventListener("DOMContentLoaded", function () {
    categoryModal = new bootstrap.Modal(
        document.getElementById("categoryModal")
    );

    loadCategories();

    document.getElementById("btnAddCategory")
        .addEventListener("click", openAddModal);

    document.getElementById("btnSearch")
        .addEventListener("click", searchCategories);

    document.getElementById("btnReset")
        .addEventListener("click", resetSearch);

    document.getElementById("pageSize")
        .addEventListener("change", function () {
            currentSize = Number(this.value);
            currentPage = 0;
            loadCategories();
        });

    document.getElementById("searchKeyword")
        .addEventListener("keyup", function (event) {
            if (event.key === "Enter") {
                searchCategories();
            }
        });

    document.getElementById("categoryForm")
        .addEventListener("submit", saveCategory);

    document.getElementById("categoryIcon")
        .addEventListener("input", previewIcon);
});

async function loadCategories() {
    const query = `
        query CategoryPage(
            $keyword: String,
            $page: Int!,
            $size: Int!
        ) {
            categoryPage(
                keyword: $keyword,
                page: $page,
                size: $size
            ) {
                content {
                    categoryId
                    categoryName
                    icon
                }
                page
                size
                totalElements
                totalPages
                first
                last
            }
        }
    `;

    try {
        showLoadingRow();

        const data = await graphqlRequest(query, {
            keyword: currentKeyword,
            page: currentPage,
            size: currentSize
        });

        renderCategories(data.categoryPage.content);
        renderPagination(data.categoryPage);
        renderPageInformation(data.categoryPage);

    } catch (error) {
        showMessage(error.message, "danger");
        renderCategories([]);
    }
}

function showLoadingRow() {
    document.getElementById("categoryTableBody").innerHTML = `
        <tr>
            <td colspan="4" class="text-center">
                Đang tải dữ liệu...
            </td>
        </tr>
    `;
}

function renderCategories(categories) {
    const tableBody =
        document.getElementById("categoryTableBody");

    tableBody.innerHTML = "";
    categoryCache.clear();

    if (!categories || categories.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="4"
                    class="text-center text-muted">
                    Không tìm thấy danh mục.
                </td>
            </tr>
        `;
        return;
    }

    categories.forEach(category => {
        categoryCache.set(
            String(category.categoryId),
            category
        );

        const iconContent = category.icon
            ? `
                <img src="${escapeHtml(getIconUrl(category.icon))}"
                     alt="${escapeHtml(category.categoryName)}"
                     class="img-thumbnail"
                     style="width: 80px;
                            height: 80px;
                            object-fit: cover;">
              `
            : `<span class="text-muted">Chưa có icon</span>`;

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${category.categoryId}</td>

            <td>
                ${escapeHtml(category.categoryName)}
            </td>

            <td>
                ${iconContent}
            </td>

            <td>
                <button type="button"
                        class="btn btn-warning btn-sm btn-edit"
                        data-id="${category.categoryId}">
                    Sửa
                </button>

                <button type="button"
                        class="btn btn-danger btn-sm btn-delete"
                        data-id="${category.categoryId}">
                    Xóa
                </button>
            </td>
        `;

        tableBody.appendChild(row);
    });

    document.querySelectorAll(".btn-edit")
        .forEach(button => {
            button.addEventListener("click", function () {
                openEditModal(this.dataset.id);
            });
        });

    document.querySelectorAll(".btn-delete")
        .forEach(button => {
            button.addEventListener("click", function () {
                deleteCategory(this.dataset.id);
            });
        });
}

function renderPagination(pageData) {
    const pagination =
        document.getElementById("categoryPagination");

    pagination.innerHTML = "";

    if (pageData.totalPages <= 1) {
        return;
    }

    pagination.appendChild(
        createPageItem(
            "Trước",
            pageData.page - 1,
            pageData.first
        )
    );

    for (let index = 0;
         index < pageData.totalPages;
         index++) {

        pagination.appendChild(
            createPageItem(
                String(index + 1),
                index,
                false,
                index === pageData.page
            )
        );
    }

    pagination.appendChild(
        createPageItem(
            "Sau",
            pageData.page + 1,
            pageData.last
        )
    );
}

function createPageItem(
    label,
    targetPage,
    disabled,
    active = false
) {
    const item = document.createElement("li");

    item.className = "page-item";

    if (disabled) {
        item.classList.add("disabled");
    }

    if (active) {
        item.classList.add("active");
    }

    const button = document.createElement("button");

    button.type = "button";
    button.className = "page-link";
    button.textContent = label;

    button.addEventListener("click", function () {
        if (disabled || active) {
            return;
        }

        currentPage = targetPage;
        loadCategories();
    });

    item.appendChild(button);

    return item;
}

function renderPageInformation(pageData) {
    const information =
        document.getElementById("pageInformation");

    if (pageData.totalElements === 0) {
        information.textContent = "Không có dữ liệu";
        return;
    }

    information.textContent =
        `Trang ${pageData.page + 1}/${pageData.totalPages} - ` +
        `Tổng ${pageData.totalElements} danh mục`;
}

function searchCategories() {
    currentKeyword =
        document.getElementById("searchKeyword")
            .value
            .trim();

    currentPage = 0;
    loadCategories();
}

function resetSearch() {
    document.getElementById("searchKeyword").value = "";
    document.getElementById("pageSize").value = "5";

    currentKeyword = "";
    currentPage = 0;
    currentSize = 5;

    loadCategories();
}

function openAddModal() {
    const form =
        document.getElementById("categoryForm");

    form.reset();
    form.classList.remove("was-validated");

    document.getElementById("categoryId").value = "";

    document.getElementById("categoryModalTitle")
        .textContent = "Thêm danh mục";

    hideIconPreview();

    categoryModal.show();
}

function openEditModal(categoryId) {
    const category =
        categoryCache.get(String(categoryId));

    if (!category) {
        showMessage(
            "Không tìm thấy dữ liệu danh mục",
            "danger"
        );
        return;
    }

    const form =
        document.getElementById("categoryForm");

    form.reset();
    form.classList.remove("was-validated");

    document.getElementById("categoryId").value =
        category.categoryId;

    document.getElementById("categoryName").value =
        category.categoryName || "";

    document.getElementById("categoryIcon").value =
        category.icon || "";

    document.getElementById("categoryModalTitle")
        .textContent = "Cập nhật danh mục";

    showIconPreview(category.icon);

    categoryModal.show();
}

async function saveCategory(event) {
    event.preventDefault();

    const form =
        document.getElementById("categoryForm");

    if (!form.checkValidity()) {
        form.classList.add("was-validated");
        return;
    }

    const categoryId =
        document.getElementById("categoryId")
            .value;

    const categoryName =
        document.getElementById("categoryName")
            .value
            .trim();

    const icon =
        document.getElementById("categoryIcon")
            .value
            .trim();

    const input = {
        categoryName: categoryName,
        icon: icon === "" ? null : icon
    };

    const saveButton =
        document.getElementById("btnSaveCategory");

    saveButton.disabled = true;
    saveButton.textContent = "Đang lưu...";

    try {
        if (categoryId === "") {
            await createCategory(input);

            showMessage(
                "Thêm danh mục thành công",
                "success"
            );
        } else {
            await updateCategory(categoryId, input);

            showMessage(
                "Cập nhật danh mục thành công",
                "success"
            );
        }

        categoryModal.hide();
        await loadCategories();

    } catch (error) {
        showMessage(error.message, "danger");

    } finally {
        saveButton.disabled = false;
        saveButton.textContent = "Lưu";
    }
}

async function createCategory(input) {
    const mutation = `
        mutation CreateCategory($input: CategoryInput!) {
            createCategory(input: $input) {
                categoryId
                categoryName
                icon
            }
        }
    `;

    return graphqlRequest(mutation, {
        input: input
    });
}

async function updateCategory(categoryId, input) {
    const mutation = `
        mutation UpdateCategory(
            $id: ID!,
            $input: CategoryInput!
        ) {
            updateCategory(
                id: $id,
                input: $input
            ) {
                categoryId
                categoryName
                icon
            }
        }
    `;

    return graphqlRequest(mutation, {
        id: categoryId,
        input: input
    });
}

async function deleteCategory(categoryId) {
    const accepted = confirm(
        "Bạn có chắc muốn xóa danh mục này không?"
    );

    if (!accepted) {
        return;
    }

    const mutation = `
        mutation DeleteCategory($id: ID!) {
            deleteCategory(id: $id)
        }
    `;

    try {
        await graphqlRequest(mutation, {
            id: categoryId
        });

        showMessage(
            "Xóa danh mục thành công",
            "success"
        );

        if (currentPage > 0 &&
            categoryCache.size === 1) {
            currentPage--;
        }

        await loadCategories();

    } catch (error) {
        showMessage(error.message, "danger");
    }
}

function previewIcon() {
    const icon =
        document.getElementById("categoryIcon")
            .value
            .trim();

    showIconPreview(icon);
}

function showIconPreview(icon) {
    if (!icon) {
        hideIconPreview();
        return;
    }

    document.getElementById("currentIcon").src =
        getIconUrl(icon);

    document.getElementById("currentIconArea")
        .classList
        .remove("d-none");
}

function hideIconPreview() {
    document.getElementById("currentIcon").src = "";

    document.getElementById("currentIconArea")
        .classList
        .add("d-none");
}

function getIconUrl(icon) {
    if (!icon) {
        return "";
    }

    if (icon.startsWith("http://") ||
        icon.startsWith("https://") ||
        icon.startsWith("/")) {
        return icon;
    }

    if (icon.startsWith("uploads/")) {
        return "/" + icon;
    }

    return "/uploads/categories/" +
        encodeURIComponent(icon);
}

function showMessage(message, type) {
    const messageBox =
        document.getElementById("messageBox");

    messageBox.className = `alert alert-${type}`;
    messageBox.textContent = message;

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

function escapeHtml(value) {
    const element =
        document.createElement("div");

    element.textContent =
        value == null ? "" : String(value);

    return element.innerHTML;
}