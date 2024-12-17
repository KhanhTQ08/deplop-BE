/* ===========================
    BIẾN TOÀN CỤC & WEBSOCKET
=========================== */
const selectedSeats = []; // Ghế chọn của client hiện tại
let allSelectedSeats = []; // Danh sách ghế đã chọn toàn cục
const seatTimers = new Map(); // Lưu timer giữ ghế
let stompClient = null; // WebSocket client
let clientId = "client_" + Math.floor(Math.random() * 1000); // ID duy nhất cho client

/* ===========================
    KẾT NỐI WEBSOCKET
=========================== */
function connectWebSocket() {
    console.log("🛠️ Đang kết nối WebSocket...");

    const socket = new SockJS('/ws-seat'); // Endpoint WebSocket từ backend
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log("✅ WebSocket kết nối thành công!");

        // Gửi yêu cầu cập nhật trạng thái tất cả ghế
        stompClient.send("/app/seat/requestStatus", {}, {});

        // Lắng nghe cập nhật ghế theo thời gian thực từ tất cả các client
        stompClient.subscribe('/topic/seats', (message) => {
            const seatUpdate = JSON.parse(message.body);
            console.log(`🎯 Ghế cập nhật: ${seatUpdate.seatId} - ${seatUpdate.status}`);
            updateSeatRealTime(seatUpdate);
        });

        // Lắng nghe trạng thái ghế khi client tải lại trang
        stompClient.subscribe('/topic/seatStatus', (message) => {
            const allSeatStatus = JSON.parse(message.body);
            console.log("🔄 Cập nhật trạng thái tất cả ghế từ server...");
            updateAllSeats(allSeatStatus);
        });
    }, (error) => {
        console.error("❌ Kết nối WebSocket thất bại: ", error);
    });
}

/* ===========================
    GỬI & NHẬN TRẠNG THÁI GHẾ
=========================== */
function sendSeatUpdate(seatId, status) {
    if (stompClient) {
        console.log(`📤 Gửi trạng thái: ${seatId} - ${status}`);
        stompClient.send("/app/seat/update", {}, JSON.stringify({
            seatId: seatId,
            status: status,
            updatedBy: clientId
        }));
    }
}

function updateSeatRealTime(seatUpdate) {
    const seat = document.querySelector(`[data-seat-id='${seatUpdate.seatId}']`);
    if (seat) {
        if (seatUpdate.status === 'selected') {
            seat.classList.add('selected');
            seat.setAttribute('data-status', 'selected');
            seat.setAttribute('data-updated-by', seatUpdate.updatedBy);
        } else {
            seat.classList.remove('selected');
            seat.setAttribute('data-status', 'available');
            seat.removeAttribute('data-updated-by');
        }
    }
    // Cập nhật danh sách ghế đã chọn toàn cục
    if (seatUpdate.status === 'selected' && !allSelectedSeats.includes(seatUpdate.seatId)) {
        allSelectedSeats.push(seatUpdate.seatId);
    } else if (seatUpdate.status === 'available') {
        allSelectedSeats = allSelectedSeats.filter(seat => seat !== seatUpdate.seatId);
    }
}

function updateAllSeats(allSeatStatus) {
    Object.values(allSeatStatus).forEach(seatUpdate => {
        const seat = document.querySelector(`[data-seat-id='${seatUpdate.seatId}']`);
        if (seat) {
            if (seatUpdate.status === 'selected') {
                seat.classList.add('selected');
                seat.setAttribute('data-status', 'selected');
                seat.setAttribute('data-updated-by', seatUpdate.updatedBy);
            } else {
                seat.classList.remove('selected');
                seat.setAttribute('data-status', 'available');
                seat.removeAttribute('data-updated-by');
            }
        }
    });
    console.log("✅ Cập nhật trạng thái ghế trên giao diện.");
}

/* ===========================
    XỬ LÝ CHỌN GHẾ
=========================== */
function selectSeat(seat) {
    const seatId = seat.getAttribute('data-seat-id');
    const seatName = seat.innerText;
    const status = seat.getAttribute('data-status');

    if (status === 'selected' && seat.getAttribute('data-updated-by') === clientId) {
        resetSeat(seat, seatId);
    } else if (status === 'available') {
        console.log(`✅ Chọn ghế: ${seatName}`);
        seat.classList.add('selected');
        seat.setAttribute('data-status', 'selected');
        seat.setAttribute('data-updated-by', clientId);
        selectedSeats.push(seatName);

        // Cập nhật trạng thái ghế
        sendSeatUpdate(seatId, 'selected');
        startSeatTimer(seatId, seat);
    }

    updateSelectedSeatsDisplay();
    updateTotalAmount();
}

function startSeatTimer(seatId, seat) {
    // Ngừng khởi tạo timer mới nếu ghế đã có timer
    if (seatTimers.has(seatId)) return;

    console.log(`⏳ Giữ ghế: ${seat.innerText} trong 2 phút.`);

    // Tạo timer mới, sẽ hết hạn sau 2 phút (120,000 milliseconds)
    const timer = setTimeout(() => {
        alert(`⏰ Ghế ${seat.innerText} đã hết thời gian giữ!`);
        resetSeat(seat, seatId); // Reset ghế khi hết thời gian giữ
    }, 5 * 60 * 1000);

    // Lưu timer vào trong seatTimers map với seatId là khóa
    seatTimers.set(seatId, timer);
}

function resetSeat(seat, seatId) {
    console.log(`🔄 Hủy chọn ghế: ${seat.innerText}`);

    // Xóa ghế khỏi danh sách ghế đã chọn
    const seatName = seat.innerText;
    const index = selectedSeats.indexOf(seatName);
    if (index > -1) selectedSeats.splice(index, 1);

    // Reset trạng thái của ghế và xóa timer
    seat.classList.remove('selected');
    seat.setAttribute('data-status', 'available');
    seat.removeAttribute('data-updated-by');

    // Gửi tín hiệu cập nhật trạng thái ghế qua WebSocket
    sendSeatUpdate(seatId, 'available'); // <--- Thêm dòng này

    // Xóa timer nếu tồn tại
    if (seatTimers.has(seatId)) {
        clearTimeout(seatTimers.get(seatId));
        seatTimers.delete(seatId); // Xóa timer khỏi seatTimers map
    }

    // Cập nhật giao diện cho danh sách ghế đã chọn và tổng số tiền
    updateSelectedSeatsDisplay();
    updateTotalAmount();
}



/* ===========================
    CẬP NHẬT GIAO DIỆN
=========================== */
function updateSelectedSeatsDisplay() {
    const display = document.getElementById('selectedSeatsDisplay');
    display.innerText = selectedSeats.length === 0
        ? 'Chưa có ghế nào được chọn.'
        : selectedSeats.join(', ');
}

function updateTotalAmount() {
    const totalAmountDisplay = document.getElementById('totalAmount');
    let total = 0;

    selectedSeats.forEach(seatName => {
        const seat = Array.from(document.getElementsByClassName('seat')).find(s => s.innerText === seatName);
        const seatPrice = parseInt(seat?.value);
        if (!isNaN(seatPrice)) total += seatPrice;
    });

    totalAmountDisplay.innerText = total.toLocaleString('vi-VN') + ' VNĐ';
}

/* ===========================
    XỬ LÝ XÁC NHẬN ĐẶT VÉ
=========================== */
function confirmBooking() {
    if (selectedSeats.length === 0) {
        Swal.fire({
            title: "Thông báo!",
            text: "Vui lòng chọn ghế trước khi đặt vé!",
            icon: "error"
        });
        return;
    }
    const comboModal = new bootstrap.Modal(document.getElementById('comboModal'));
    comboModal.show();
}

/* ===========================
    KHỞI ĐỘNG
=========================== */
document.addEventListener('DOMContentLoaded', connectWebSocket);
document.getElementById("backButton").addEventListener("click", () => window.history.back());

function submitBooking(button) {
    // Lưu ghế đã chọn vào selectedSeatsClient
    const selectedSeat = [...document.querySelectorAll('.selected')]
        .filter(seat => seat.getAttribute('data-updated-by') === clientId) // Lọc ghế theo clientId
        .map(seat => {
            return {
                seatId: seat.getAttribute('data-seat-id'),
                seatName: seat.innerText
            };
        });


    // Lấy form từ nút được nhấn
    const form = button.closest('form');

    // Lấy showtimeId từ input ẩn
    const showtimeIdInput = form.querySelector('input[name="showtimeId"]');
    const showtimeId = showtimeIdInput ? showtimeIdInput.value : null;

    // Lấy thông tin sản phẩm và số lượng
    const products = [...document.querySelectorAll('.draggable-content')].map(row => {
        const quantityInput = row.querySelector('.comboQuantity');
        const quantity = parseInt(quantityInput.value) || 0;
        const productIdElement = row.querySelector('.product-id');

        const productId = productIdElement ? productIdElement.innerText : null;

        return { productId, quantity };
    }).filter(product => product.quantity > 0);

    // Gán dữ liệu vào các thẻ input hidden để gửi thông tin client và ghế
    document.getElementById('selectedSeats').value = JSON.stringify(selectedSeat);
    document.getElementById('products').value = JSON.stringify(products);

    // Kiểm tra dữ liệu trước khi gửi form
    console.log('Selected Seats:', document.getElementById('selectedSeats').value);
    console.log('Products:', document.getElementById('products').value);

    // Gửi form
    form.submit();
}


/*JS MODAL*/
// Lấy các phần tử sản phẩm và tổng tiền combo
const products = document.querySelectorAll('.draggable-content');
const comboTotalAmountElement = document.getElementById('comboTotalAmount1');

// Hàm cập nhật tổng số tiền của mỗi sản phẩm
function updateProductTotalAmount(productElement) {
    const quantityInput = productElement.querySelector('input.comboQuantity');
    const priceElement = productElement.querySelector('p.price');
    const totalAmountElement = productElement.querySelector('div.comboTotalAmount');

    // Xử lý trường hợp ô nhập số lượng bị trống hoặc không hợp lệ
    const quantity = parseInt(quantityInput.value, 10) || 0;
    const price = parseFloat(priceElement.dataset.price);

    if (isNaN(price)) {
        totalAmountElement.textContent = '0 VND';
        return;
    }

    const totalAmount = quantity * price; // nhân với 1000 cho đơn vị tiền
    totalAmountElement.textContent = formatPrice(totalAmount);
    console.log()
}

// Hàm cập nhật tổng tiền của tất cả các sản phẩm (grand total)
function updateGrandTotal() {
    let grandTotal = 0;
    products.forEach((productElement) => {
        const totalAmountElement = productElement.querySelector('div.comboTotalAmount');
        const totalAmount = parseFloat(totalAmountElement.textContent.replace(/\D/g, ''));
        if (!isNaN(totalAmount)) {
            grandTotal += totalAmount;
        }
    });
    comboTotalAmountElement.textContent = formatPrice(grandTotal);
}

// Hàm định dạng giá tiền (thêm dấu phẩy và chấm)
function formatPrice(price) {
    return price.toLocaleString('vi-VN' , {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
    });
}

// Hàm kiểm tra và xác thực giá trị nhập vào
function validateInput(inputElement) {
    // Lấy giá trị từ input
    let value = inputElement.value.trim();

    // Kiểm tra nếu giá trị là một số và không âm
    if (value === "" || isNaN(value) || Number(value) < 0) {
        inputElement.value = 0; // Nếu không hợp lệ, gán lại giá trị 0
    } else {
        inputElement.value = Math.max(0, Number(value)); // Gán giá trị hợp lệ (không âm)
    }

    // Cập nhật tổng tiền sản phẩm
    const productElement = inputElement.closest('.draggable-content');
    updateProductTotalAmount(productElement);
    updateGrandTotal();
}

// Thêm sự kiện cho các nút tăng và giảm số lượng
products.forEach((productElement) => {
    const quantityInput = productElement.querySelector('input.comboQuantity');
    const priceElement = productElement.querySelector('p.price');
    const btnIncrease = productElement.querySelector('.btn-increase');
    const btnDecrease = productElement.querySelector('.btn-decrease');

    // Nút tăng số lượng
    btnIncrease.addEventListener('click', () => {
        quantityInput.value = parseInt(quantityInput.value, 10) + 1;
        const price = parseFloat(priceElement.textContent.replace(' VND', '').replace('.', '').replace(',', '.')) || 0; // Lấy giá từ priceElement
        priceElement.dataset.price = price; // Cập nhật giá trị vào data-price
        updateProductTotalAmount(productElement);
        updateGrandTotal();
    });

    // Nút giảm số lượng
    btnDecrease.addEventListener('click', () => {
        let currentValue = parseInt(quantityInput.value, 10);
        if (currentValue > 0) {
            quantityInput.value = currentValue - 1;
        }
        const price = parseFloat(priceElement.textContent.replace(' VND', '').replace('.', '').replace(',', '.')) || 0; // Lấy giá từ priceElement
        priceElement.dataset.price = price; // Cập nhật giá trị vào data-price
        updateProductTotalAmount(productElement);
        updateGrandTotal();
    });

    // Sự kiện thay đổi giá trị ô input
    quantityInput.addEventListener('input', () => {
        const quantity = parseInt(quantityInput.value, 10) || 0;
        const price = parseFloat(priceElement.textContent.replace(' VND', '').replace('.', '').replace(',', '.')) || 0; // Lấy giá từ priceElement
        priceElement.dataset.price = price; // Cập nhật giá trị vào data-price
        updateProductTotalAmount(productElement);
        updateGrandTotal();
    });
});
/*JS MODAL*/
