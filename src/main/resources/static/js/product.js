let currentPage = 0;
let currentSize = 5;
let currentKeyword = "";
let currentCategoryId = null;
let productModal;

document.addEventListener(
	"DOMContentLoaded",
	   async function () {

	       const modalElement =
	           document.getElementById("productModal");

	       /*
	        * Nếu không phải trang Product thì dừng,
	        * tránh khởi tạo Modal bằng phần tử null.
	        */
	       if (!modalElement) {
	           return;
	       }

	       productModal =
	           new bootstrap.Modal(modalElement);

        document
            .getElementById("btnAddProduct")
            .addEventListener(
                "click",
                openAddProductModal
            );

        document
            .getElementById("searchForm")
            .addEventListener(
                "submit",
                searchProducts
            );

        document
            .getElementById("btnReset")
            .addEventListener(
                "click",
                resetSearch
            );

        document
            .getElementById("productForm")
            .addEventListener(
                "submit",
                saveProduct
            );

        await loadCategories();
        await loadProducts();
    }
);

async function loadCategories() {
    const query = `
        query {
            categories {
                categoryId
                categoryName
            }
        }
    `;

    try {
        const data = await graphqlRequest(query);

        const categoryFilter =
            document.getElementById(
                "categoryFilter"
            );

        const productCategory =
            document.getElementById(
                "productCategory"
            );

        data.categories.forEach(category => {
            const filterOption =
                document.createElement("option");

            filterOption.value =
                category.categoryId;

            filterOption.textContent =
                category.categoryName;

            categoryFilter.appendChild(
                filterOption
            );

            const formOption =
                document.createElement("option");

            formOption.value =
                category.categoryId;

            formOption.textContent =
                category.categoryName;

            productCategory.appendChild(
                formOption
            );
        });

    } catch (error) {
        showProductAlert(
            error.message,
            "danger"
        );
    }
}

async function loadProducts() {
    const query = `
        query ProductPage(
            $keyword: String
            $categoryId: ID
            $page: Int!
            $size: Int!
        ) {
            productPage(
                keyword: $keyword
                categoryId: $categoryId
                page: $page
                size: $size
            ) {
                content {
                    id
                    name
                    price
                    quantity
                    description
                    categoryId
                    categoryName
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

    showLoading(true);

    try {
        const data = await graphqlRequest(
            query,
            {
                keyword: currentKeyword,
                categoryId: currentCategoryId,
                page: currentPage,
                size: currentSize
            }
        );

        renderProductTable(
            data.productPage.content
        );

        renderPagination(
            data.productPage
        );

    } catch (error) {
        showProductAlert(
            error.message,
            "danger"
        );

    } finally {
        showLoading(false);
    }
}

function renderProductTable(products) {
    const tableBody =
        document.getElementById(
            "productTableBody"
        );

    const emptyMessage =
        document.getElementById(
            "emptyMessage"
        );

    tableBody.innerHTML = "";

    if (!products || products.length === 0) {
        emptyMessage.classList.remove("d-none");
        return;
    }

    emptyMessage.classList.add("d-none");

    products.forEach(product => {
        const row =
            document.createElement("tr");

        row.innerHTML = `
            <td>${product.id}</td>

            <td>
                <a href="/products/${product.id}"
                   class="fw-semibold
                          text-decoration-none">
                    ${escapeHtml(product.name)}
                </a>
            </td>

            <td>
                ${escapeHtml(
                    product.categoryName ||
                    "Chưa phân loại"
                )}
            </td>

            <td class="text-danger fw-semibold">
                ${formatCurrency(product.price)}
            </td>

            <td>${product.quantity}</td>

            <td>
                ${escapeHtml(
                    product.description || ""
                )}
            </td>

            <td>
                <a href="/products/${product.id}"
                   class="btn btn-info btn-sm">
                    Chi tiết
                </a>

                <button type="button"
                        class="btn btn-warning btn-sm
                               btn-edit-product"
                        data-id="${product.id}">
                    Sửa
                </button>

                <button type="button"
                        class="btn btn-danger btn-sm
                               btn-delete-product"
                        data-id="${product.id}">
                    Xóa
                </button>
            </td>
        `;

        tableBody.appendChild(row);
    });

    document
        .querySelectorAll(".btn-edit-product")
        .forEach(button => {
            button.addEventListener(
                "click",
                function () {
                    openEditProductModal(
                        button.dataset.id
                    );
                }
            );
        });

    document
        .querySelectorAll(".btn-delete-product")
        .forEach(button => {
            button.addEventListener(
                "click",
                function () {
                    deleteProduct(
                        button.dataset.id
                    );
                }
            );
        });
}

function renderPagination(pageData) {
    const pagination =
        document.getElementById(
            "productPagination"
        );

    pagination.innerHTML = "";

    if (pageData.totalPages <= 1) {
        return;
    }

    pagination.appendChild(
        createPageButton(
            "Trước",
            pageData.page - 1,
            pageData.first
        )
    );

    for (
        let index = 0;
        index < pageData.totalPages;
        index++
    ) {
        pagination.appendChild(
            createPageButton(
                String(index + 1),
                index,
                false,
                index === pageData.page
            )
        );
    }

    pagination.appendChild(
        createPageButton(
            "Sau",
            pageData.page + 1,
            pageData.last
        )
    );
}

function createPageButton(
    label,
    page,
    disabled,
    active = false
) {
    const item =
        document.createElement("li");

    item.className = "page-item";

    if (disabled) {
        item.classList.add("disabled");
    }

    if (active) {
        item.classList.add("active");
    }

    const button =
        document.createElement("button");

    button.type = "button";
    button.className = "page-link";
    button.textContent = label;
    button.disabled = disabled;

    button.addEventListener(
        "click",
        function () {
            if (disabled) {
                return;
            }

            currentPage = page;
            loadProducts();
        }
    );

    item.appendChild(button);

    return item;
}

function searchProducts(event) {
    event.preventDefault();

    currentKeyword =
        document
            .getElementById("searchKeyword")
            .value
            .trim();

    const categoryValue =
        document
            .getElementById("categoryFilter")
            .value;

    currentCategoryId =
        categoryValue || null;

    currentSize = Number(
        document
            .getElementById("pageSize")
            .value
    );

    currentPage = 0;

    loadProducts();
}

function resetSearch() {
    document.getElementById(
        "searchKeyword"
    ).value = "";

    document.getElementById(
        "categoryFilter"
    ).value = "";

    document.getElementById(
        "pageSize"
    ).value = "5";

    currentKeyword = "";
    currentCategoryId = null;
    currentSize = 5;
    currentPage = 0;

    loadProducts();
}

function openAddProductModal() {
    document
        .getElementById("productForm")
        .reset();

    document.getElementById(
        "productId"
    ).value = "";

    document.getElementById(
        "productModalTitle"
    ).textContent = "Thêm sản phẩm";

    hideModalError();

    productModal.show();
}

async function openEditProductModal(id) {
    const query = `
        query ProductById($id: ID!) {
            productById(id: $id) {
                id
                name
                price
                quantity
                description
                categoryId
            }
        }
    `;

    try {
        const data = await graphqlRequest(
            query,
            {
                id: id
            }
        );

        const product = data.productById;

        document.getElementById(
            "productId"
        ).value = product.id;

        document.getElementById(
            "productName"
        ).value = product.name;

        document.getElementById(
            "productPrice"
        ).value = product.price;

        document.getElementById(
            "productQuantity"
        ).value = product.quantity;

        document.getElementById(
            "productCategory"
        ).value = product.categoryId || "";

        document.getElementById(
            "productDescription"
        ).value = product.description || "";

        document.getElementById(
            "productModalTitle"
        ).textContent = "Cập nhật sản phẩm";

        hideModalError();

        productModal.show();

    } catch (error) {
        showProductAlert(
            error.message,
            "danger"
        );
    }
}

async function saveProduct(event) {
    event.preventDefault();

    const saveButton =
        document.getElementById("btnSaveProduct");

    const id =
        document.getElementById("productId").value;

    const imageInput =
        document.getElementById("productImages");

    const imageFiles =
        imageInput ? imageInput.files : [];

    const input = {
        name: document
            .getElementById("productName")
            .value
            .trim(),

        price: Number(
            document
                .getElementById("productPrice")
                .value
        ),

        quantity: Number(
            document
                .getElementById("productQuantity")
                .value
        ),

        description: document
            .getElementById("productDescription")
            .value
            .trim(),

        categoryId: document
            .getElementById("productCategory")
            .value
    };

    if (!input.name) {
        showModalError(
            "Tên sản phẩm không được để trống"
        );
        return;
    }

    if (!input.categoryId) {
        showModalError(
            "Bạn phải chọn danh mục"
        );
        return;
    }

    saveButton.disabled = true;
    saveButton.textContent = "Đang lưu...";

    try {
        let savedProductId;

        if (id) {
            const result =
                await updateProduct(id, input);

            savedProductId =
                result.updateProduct.id;

        } else {
            const result =
                await createProduct(input);

            savedProductId =
                result.createProduct.id;
        }

        /*
         * GraphQL chỉ lưu thông tin sản phẩm.
         * Nếu người dùng chọn ảnh thì gọi tiếp
         * API multipart để upload ảnh.
         */
        if (imageFiles.length > 0) {
            await uploadProductImages(
                savedProductId,
                imageFiles
            );
        }

        productModal.hide();

        showProductAlert(
            id
                ? "Cập nhật sản phẩm thành công"
                : "Thêm sản phẩm thành công",
            "success"
        );

        await loadProducts();

    } catch (error) {
        showModalError(error.message);

    } finally {
        saveButton.disabled = false;
        saveButton.textContent = "Lưu";
    }
}

async function createProduct(input) {
    const mutation = `
        mutation CreateProduct(
            $input: ProductInput!
        ) {
            createProduct(input: $input) {
                id
            }
        }
    `;

    return graphqlRequest(
        mutation,
        {
            input: input
        }
    );
}

async function updateProduct(id, input) {
    const mutation = `
        mutation UpdateProduct(
            $id: ID!
            $input: ProductInput!
        ) {
            updateProduct(
                id: $id
                input: $input
            ) {
                id
            }
        }
    `;

    return graphqlRequest(
        mutation,
        {
            id: id,
            input: input
        }
    );
}
async function uploadProductImages(
    productId,
    files
) {
    const formData = new FormData();

    Array.from(files).forEach(file => {
        formData.append(
            "images",
            file
        );
    });

    const response = await fetch(
        `/api/products/${productId}/images`,
        {
            method: "POST",
            body: formData
        }
    );

    let responseBody = null;

    try {
        responseBody =
            await response.json();
    } catch (error) {
        // Không làm gì nếu response không phải JSON.
    }

    if (!response.ok) {
        throw new Error(
            responseBody?.message
            || "Upload hình ảnh thất bại"
        );
    }

    return responseBody;
}
async function deleteProduct(id) {
    const confirmed = confirm(
        "Bạn có chắc muốn xóa sản phẩm này?"
    );

    if (!confirmed) {
        return;
    }

    const mutation = `
        mutation DeleteProduct($id: ID!) {
            deleteProduct(id: $id)
        }
    `;

    try {
        await graphqlRequest(
            mutation,
            {
                id: id
            }
        );

        showProductAlert(
            "Xóa sản phẩm thành công",
            "success"
        );

        await loadProducts();

    } catch (error) {
        showProductAlert(
            error.message,
            "danger"
        );
    }
}

function formatCurrency(value) {
    return new Intl.NumberFormat(
        "vi-VN",
        {
            style: "currency",
            currency: "VND"
        }
    ).format(Number(value));
}

function escapeHtml(value) {
    const element =
        document.createElement("div");

    element.textContent =
        value == null ? "" : String(value);

    return element.innerHTML;
}

function showLoading(visible) {
    document
        .getElementById("productLoading")
        .classList.toggle(
            "d-none",
            !visible
        );
}

function showProductAlert(message, type) {
    const alertBox =
        document.getElementById(
            "productAlert"
        );

    alertBox.className =
        `alert alert-${type}`;

    alertBox.textContent = message;

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

function showModalError(message) {
    const errorBox =
        document.getElementById(
            "modalError"
        );

    errorBox.textContent = message;
    errorBox.classList.remove("d-none");
}

function hideModalError() {
    document
        .getElementById("modalError")
        .classList.add("d-none");
}