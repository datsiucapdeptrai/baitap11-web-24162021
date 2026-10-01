document.addEventListener(
    "DOMContentLoaded",
    async function () {

        const categorySelect =
            document.getElementById("categorySelect");

        categorySelect.addEventListener(
            "change",
            handleCategoryChange
        );

        await loadCategories();
        await loadAllProducts();
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

        const categorySelect =
            document.getElementById("categorySelect");

        data.categories.forEach(category => {
            const option =
                document.createElement("option");

            option.value = category.categoryId;
            option.textContent = category.categoryName;

            categorySelect.appendChild(option);
        });

    } catch (error) {
        showError(error.message);
    }
}

async function loadAllProducts() {
    const query = `
        query {
            productsByPriceAsc {
                id
                name
                price
                quantity
                description
                categoryId
                categoryName
                images {
                    imageUrl
                }
            }
        }
    `;

    showLoading(true);
    hideError();

    try {
        const data = await graphqlRequest(query);

        document.getElementById("productTitle")
            .textContent =
                "Tất cả sản phẩm - Giá thấp đến cao";

        renderProducts(
            data.productsByPriceAsc || []
        );

    } catch (error) {
        showError(error.message);
        renderProducts([]);

    } finally {
        showLoading(false);
    }
}

async function loadProductsByCategory(categoryId) {
    const query = `
        query ProductsByCategory($categoryId: ID!) {
            productsByCategory(categoryId: $categoryId) {
                id
                name
                price
                quantity
                description
                categoryId
                categoryName
                images {
                    imageUrl
                }
            }
        }
    `;

    showLoading(true);
    hideError();

    try {
        const data = await graphqlRequest(
            query,
            {
                categoryId: categoryId
            }
        );

        const categorySelect =
            document.getElementById("categorySelect");

        const selectedOption =
            categorySelect.options[
                categorySelect.selectedIndex
            ];

        document.getElementById("productTitle")
            .textContent =
                "Sản phẩm thuộc danh mục: " +
                selectedOption.textContent;

        renderProducts(
            data.productsByCategory || []
        );

    } catch (error) {
        showError(error.message);
        renderProducts([]);

    } finally {
        showLoading(false);
    }
}

async function handleCategoryChange(event) {
    const categoryId = event.target.value;

    if (!categoryId) {
        await loadAllProducts();
        return;
    }

    await loadProductsByCategory(categoryId);
}

function renderProducts(products) {
    const container =
        document.getElementById("productContainer");

    const emptyMessage =
        document.getElementById("emptyMessage");

    container.innerHTML = "";

    if (!products || products.length === 0) {
        emptyMessage.classList.remove("d-none");
        return;
    }

    emptyMessage.classList.add("d-none");

    products.forEach(product => {
        const column =
            document.createElement("div");

        column.className =
            "col-sm-12 col-md-6 col-lg-4";

        const imageContent =
            createProductImage(product);

        const categoryName =
            product.categoryName ||
            "Chưa phân loại";

        column.innerHTML = `
            <div class="card h-100 shadow-sm">

                ${imageContent}

                <div class="card-body d-flex flex-column">

                    <div class="mb-2">
                        <span class="badge bg-primary">
                            ${escapeHtml(categoryName)}
                        </span>
                    </div>

                    <h5 class="card-title">
                        ${escapeHtml(product.name)}
                    </h5>

                    <p class="card-text text-secondary">
                        ${escapeHtml(
                            product.description ||
                            "Chưa có mô tả"
                        )}
                    </p>

                    <p class="mb-2">
                        Số lượng:
                        <strong>
                            ${product.quantity}
                        </strong>
                    </p>

                    <h5 class="text-danger mb-3">
                        ${formatCurrency(product.price)}
                    </h5>

                    <a href="/products/${product.id}"
                       class="btn btn-outline-primary mt-auto">
                        Xem chi tiết
                    </a>

                </div>

            </div>
        `;

        container.appendChild(column);
    });
}

function createProductImage(product) {
    const images =
        Array.isArray(product.images)
            ? product.images
            : [];

    if (images.length === 0 ||
        !images[0] ||
        !images[0].imageUrl) {

        return `
            <div class="bg-light text-secondary
                        d-flex align-items-center
                        justify-content-center"
                 style="height: 220px;">
                Chưa có hình ảnh
            </div>
        `;
    }

    const imageUrl =
        getProductImageUrl(
            images[0].imageUrl
        );

    return `
        <img src="${escapeHtml(imageUrl)}"
             alt="${escapeHtml(product.name)}"
             class="card-img-top"
             style="height: 220px;
                    object-fit: cover;"
             onerror="
                this.onerror = null;
                this.style.display = 'none';
             ">
    `;
}

function getProductImageUrl(imageUrl) {
    if (!imageUrl) {
        return "";
    }

    if (imageUrl.startsWith("http://") ||
        imageUrl.startsWith("https://") ||
        imageUrl.startsWith("/")) {
        return imageUrl;
    }

    return "/uploads/products/" +
        encodeURIComponent(imageUrl);
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

function showLoading(visible) {
    document.getElementById("loading")
        .classList.toggle(
            "d-none",
            !visible
        );
}

function showError(message) {
    const errorBox =
        document.getElementById("errorBox");

    errorBox.textContent =
        message || "Không thể tải dữ liệu";

    errorBox.classList.remove("d-none");
}

function hideError() {
    const errorBox =
        document.getElementById("errorBox");

    errorBox.textContent = "";
    errorBox.classList.add("d-none");
}

function escapeHtml(value) {
    const element =
        document.createElement("div");

    element.textContent =
        value == null
            ? ""
            : String(value);

    return element.innerHTML;
}