// ==========================================
// Kết nối WebSocket
// ==========================================
let stompClient = null; // WebSocket client
let isConnected = false;
let clientId = null; // ID duy nhất cho mỗi tab
let tabName = null; // Tên tab của người dùng

function connectWebSocket() {
    const socket = new SockJS('/seat-selection'); // URL endpoint từ server Spring Boot
    stompClient = Stomp.over(socket); // Khởi tạo STOMP client

    // Lấy clientId từ sessionStorage hoặc tạo mới
    clientId = sessionStorage.getItem('clientId');
    if (!clientId) {
        clientId = `client-${Date.now()}`; // Tạo clientId mới nếu chưa có
        sessionStorage.setItem('clientId', clientId); // Lưu lại để dùng sau khi F5
    }

    stompClient.connect({}, function (frame) {
        isConnected = true;
        // clientId = `client-${Date.now()}`; // ID duy nhất cho mỗi tab
        tabName = `Tab-${Math.random().toString(36).substring(2, 8)}`; // Tên tab tạm thời
        console.log('WebSocket connected:', frame);

        // Subscribe để nhận thông báo cập nhật ghế
        stompClient.subscribe('/topic/seats', function (message) {
            const seatUpdate = JSON.parse(message.body);
            // if (seatUpdate.clientId !== clientId) { // Bỏ qua tin nhắn từ chính tab này
            //     console.log('Cập nhật từ tab khác:', seatUpdate);
            //     updateSeatStatus(seatUpdate); // Cập nhật trạng thái ghế
            // }
            if (Array.isArray(seatUpdate)) {
                // Nếu là danh sách ghế (tab mới mở nhận được toàn bộ trạng thái)
                updateAllSeatStatus(seatUpdate);
            } else if (seatUpdate.clientId !== clientId) {
                // Nếu là cập nhật từ tab khác
                updateSeatStatus(seatUpdate);
            }

            if (seatUpdate.type === 'seat-booked') {
                // Xử lý khi ghế đã được đặt
                handleSeatBooked(seatUpdate.bookedSeats);
            } else if (seatUpdate.type === 'seat-update') {
                // Xử lý các cập nhật khác
                updateSeatStatus(seatUpdate);
            }
            // sessionStorage.clear();
        });




        // Yêu cầu trạng thái ghế khi mới kết nối
        stompClient.send("/app/seat/status", {}, {});
    }, function (error) {
        console.error('WebSocket connection error:', error);
    });

    // Xử lý khi tab đóng
    window.addEventListener('beforeunload', function () {
        if (isConnected) {
            stompClient.send("/app/seat/disconnect", {}, JSON.stringify({ clientId }));
            stompClient.disconnect(() => {
                console.log('WebSocket disconnected');
            });
        }
    });

}

//===================================================================
// Cập nhật trạng thái ghế
function updateAllSeatStatus(seatUpdates) {

    // Duyệt qua tất cả trạng thái ghế được gửi từ server
    seatUpdates.forEach(seatUpdate => {
        const seatElement = document.querySelector(`[data-seat-id="${seatUpdate.seatId}"]`);

        // Cập nhật giao diện ghế dựa trên trạng thái
        if (seatUpdate.selected) {
            if (seatElement) {
                seatElement.classList.add('selected'); // Đánh dấu ghế đã chọn
                if (seatUpdate.clientId !== clientId) {
                    // Chỉ vô hiệu hóa nếu ghế do tab khác chọn
                    seatElement.classList.add('disabled');
                    seatElement.setAttribute('disabled', 'true');
                }
            }
        } else {
            if (seatElement) {
                seatElement.classList.remove('selected'); // Bỏ đánh dấu ghế
                if (seatUpdate.clientId !== clientId) {
                    // Chỉ kích hoạt lại nếu ghế do tab khác chọn
                    seatElement.classList.remove('disabled');
                    seatElement.removeAttribute('disabled');
                }
            }
        }
    });

    // Lưu trạng thái ghế vào sessionStorage để duy trì khi làm mới trang
    const seatStatusMap = seatUpdates.reduce((acc, seatUpdate) => {
        acc[seatUpdate.seatId] = {
            selected: seatUpdate.selected,
            clientId: seatUpdate.clientId,
        };
        return acc;
    }, {});

    sessionStorage.setItem('allSeats', JSON.stringify(seatStatusMap));

    console.log("%c[Cập nhật tất cả trạng thái ghế]:", "color: green; font-weight: bold;", seatUpdates);
}


function updateSeatStatus(seatUpdate) {
    const seatElement = document.querySelector(`[data-seat-id="${seatUpdate.seatId}"]`);

    // Lưu trạng thái ghế vào sessionStorage
    const allSeats = JSON.parse(sessionStorage.getItem('allSeats')) || {};

    if (seatUpdate.selected) {
        allSeats[seatUpdate.seatId] = {
            selected: true,
            clientId: seatUpdate.clientId,
        };

        if (seatElement) {
            seatElement.classList.add('selected'); // Đánh dấu ghế là đã chọn
            if (seatUpdate.clientId !== clientId) {
                // Chỉ vô hiệu hóa ghế nếu clientId khác
                seatElement.classList.add('disabled');
                seatElement.setAttribute('disabled', 'true');
            }else {
                seatElement.classList.remove('disabled');
                seatElement.removeAttribute('disabled');
            }
        }
    } else {
        delete allSeats[seatUpdate.seatId]; // Xóa ghế khỏi danh sách đã chọn
        // Kích hoạt lại ghế trên giao diện
        if (seatElement) {
            seatElement.classList.remove('selected');
            seatElement.classList.remove('disabled');
            seatElement.removeAttribute('disabled');
        }
    }

    sessionStorage.setItem('allSeats', JSON.stringify(allSeats)); // Cập nhật sessionStorage
}

function restoreAllSeatsStatus() {
    const allSeats = JSON.parse(sessionStorage.getItem('allSeats')) || {};

    Object.keys(allSeats).forEach(seatId => {
        const seatData = allSeats[seatId];
        const seatElement = document.querySelector(`[data-seat-id="${seatId}"]`);

        if (seatElement) {
            if (seatData.selected) {
                seatElement.classList.add('selected'); // Đánh dấu ghế là đã chọn

                // Nếu ghế không phải của tab hiện tại, vô hiệu hóa
                if (seatData.clientId !== clientId) {
                    seatElement.classList.add('disabled');
                    seatElement.setAttribute('disabled', 'true');
                } else {
                    // Nếu là ghế của tab hiện tại, đảm bảo ghế không bị khóa
                    seatElement.classList.remove('disabled');
                    seatElement.removeAttribute('disabled');
                }

            } else {
                // Nếu ghế không được chọn, kích hoạt lại
                seatElement.classList.remove('selected');
                seatElement.classList.remove('disabled');
                seatElement.removeAttribute('disabled');
            }
        }
    });

    console.log("%c[Phục hồi trạng thái ghế từ sessionStorage]:", "color: blue; font-weight: bold;", allSeats);
}

// ==========================================
// Xử lý chọn ghế
// ==========================================
let selectedSeats = []; // Mảng lưu ghế đã chọn cùng giá

function saveSelectedSeatsForTab() {
    const allSeats = JSON.parse(sessionStorage.getItem('allSelectedSeats')) || {};
    allSeats[clientId] = selectedSeats; // Lưu ghế theo clientId của tab hiện tại
    sessionStorage.setItem('allSelectedSeats', JSON.stringify(allSeats));
}

// ==========================================
// Lưu ghế vào sessionStorage
// ==========================================
function saveSeatsToSession() {
    if (selectedSeats.length === 0) {
        sessionStorage.removeItem('selectedSeats'); // Xóa dữ liệu khi không có ghế chọn
    } else {
        const seatData = selectedSeats.reduce((acc, seat) => {
            acc[seat.seatId] = seat;
            return acc;
        }, {});
        sessionStorage.setItem('selectedSeats', JSON.stringify(seatData)); // Lưu lại trạng thái ghế
    }
}

// ==========================================
// Khôi phục trạng thái ghế từ sessionStorage
// ==========================================
function restoreSeatsFromSession() {
    const storedSeats = JSON.parse(sessionStorage.getItem('selectedSeats')) || {};

    // Làm rỗng mảng `selectedSeats` để đồng bộ lại
    selectedSeats.length = 0;

    // Phục hồi trạng thái UI và mảng `selectedSeats`
    Object.keys(storedSeats).forEach(seatId => {
        const seat = storedSeats[seatId];
        const seatElement = document.querySelector(`[data-seat-id="${seatId}"]`);

        if (seatElement) {
            seatElement.classList.add('selected'); // Cập nhật giao diện
        }

        // Đồng bộ vào mảng `selectedSeats`
        selectedSeats.push(seat);
    });

    // Cập nhật giao diện
    updateSelectedSeatsDisplay();
    updateTotalAmount();
}

// ==========================================
// Xử lý chọn ghế
// ==========================================
// Lưu trữ các bộ đếm của từng ghế
// Định nghĩa thời gian (quy đổi ra giây)
const SEAT_HOLD_TIME = 180; // 3 phút (thời gian giữ ghế)
const SEAT_LOCK_TIME = 180; // 3 phút (thời gian khóa ghế sau khi hết giờ 2 lần)

const seatTimers = {};
const seatMissedCount = {}; // Lưu số lần ghế hết giờ

// Khi tải lại trang, khôi phục trạng thái bộ đếm
window.addEventListener("DOMContentLoaded", () => {
    const savedSeats = JSON.parse(sessionStorage.getItem("seatTimers")) || {};
    const missedCounts = JSON.parse(sessionStorage.getItem("seatMissedCount")) || {};
    const lockedSeats = JSON.parse(sessionStorage.getItem("lockedSeats")) || {};

    Object.keys(savedSeats).forEach(seatId => {
        const seat = document.querySelector(`[data-seat-id="${seatId}"]`);
        const remainingTime = savedSeats[seatId].remainingTime;
        const timestamp = savedSeats[seatId].timestamp;

        // Tính lại thời gian còn lại dựa trên thời gian đã trôi qua
        const elapsed = Math.floor((Date.now() - timestamp) / 1000);
        const adjustedTime = remainingTime - elapsed;

        if (adjustedTime > 0) {
            startSeatTimer(seat, adjustedTime);
        } else {
            handleSeatTimeout(seat);
        }
    });

    // Khôi phục số lần ghế hết giờ
    Object.keys(missedCounts).forEach(seatId => {
        seatMissedCount[seatId] = missedCounts[seatId];
    });

    // Khôi phục trạng thái ghế bị khóa
    Object.keys(lockedSeats).forEach(seatId => {
        const unlockTime = lockedSeats[seatId];
        const remainingLockTime = unlockTime - Date.now();

        if (remainingLockTime > 0) {
            const seat = document.querySelector(`[data-seat-id="${seatId}"]`);
            if (seat) {
                seat.setAttribute("disabled", "true");
                seat.classList.add("locked");

                // Đặt lại timeout để tự động mở khóa
                setTimeout(() => {
                    seat.removeAttribute("disabled");
                    seat.classList.remove("locked");

                    // Xóa khỏi lockedSeats sau khi mở khóa
                    const updatedLockedSeats = JSON.parse(sessionStorage.getItem("lockedSeats")) || {};
                    delete updatedLockedSeats[seatId];
                    sessionStorage.setItem("lockedSeats", JSON.stringify(updatedLockedSeats));

                    console.log(`Ghế ${seatId} đã được mở khóa sau khi tải lại trang.`);
                }, remainingLockTime);
            }
        } else {
            // Xóa ghế khỏi lockedSeats nếu đã hết hạn khóa
            const updatedLockedSeats = JSON.parse(sessionStorage.getItem("lockedSeats")) || {};
            delete updatedLockedSeats[seatId];
            sessionStorage.setItem("lockedSeats", JSON.stringify(updatedLockedSeats));
        }
    });
});

// Hàm bắt đầu bộ đếm thời gian
function startSeatTimer(seat, remainingTime) {
    const seatId = seat.getAttribute("data-seat-id");
    console.log(`Bắt đầu giữ ghế ${seatId} với thời gian còn lại: ${remainingTime} giây.`);

    seatTimers[seatId] = setInterval(() => {
        const minutes = Math.floor(remainingTime / 60);
        const seconds = remainingTime % 60;
        console.log(`Ghế ${seatId}: còn ${minutes} phút ${seconds} giây.`);
        remainingTime -= 1;

        // Cập nhật thời gian còn lại vào sessionStorage
        updateSessionTimer(seatId, remainingTime);

        if (remainingTime < 0) {
            clearInterval(seatTimers[seatId]);
            delete seatTimers[seatId];
            handleSeatTimeout(seat);
        }
    }, 1000); // Mỗi giây cập nhật một lần
}

// Xử lý khi hết giờ ghế
function handleSeatTimeout(seat) {
    const seatId = seat.getAttribute("data-seat-id");
    seat.classList.remove("selected");
    removeSeat(seatId);
    sendSeatUpdate({ seatId, selected: false });
    console.log(`Thời gian giữ ghế ${seatId} đã hết. Ghế đã bị hủy.`);

    // Tăng số lần ghế hết giờ
    seatMissedCount[seatId] = (seatMissedCount[seatId] || 0) + 1;
    sessionStorage.setItem("seatMissedCount", JSON.stringify(seatMissedCount));

    // Nếu ghế hết giờ 2 lần, khóa ghế trong SEAT_LOCK_TIME
    if (seatMissedCount[seatId] >= 2) {
        seat.setAttribute("disabled", "true");
        seat.classList.add("locked");
        console.warn(`Ghế ${seatId} bị khóa trong ${SEAT_LOCK_TIME / 60} phút do hết giờ 2 lần.`);

        // Lưu thời gian hết hạn khóa vào sessionStorage
        const unlockTime = Date.now() + SEAT_LOCK_TIME * 1000;
        const lockedSeats = JSON.parse(sessionStorage.getItem("lockedSeats")) || {};
        lockedSeats[seatId] = unlockTime;
        sessionStorage.setItem("lockedSeats", JSON.stringify(lockedSeats));

        setTimeout(() => {
            seat.removeAttribute("disabled");
            seat.classList.remove("locked");
            console.log(`Ghế ${seatId} đã được mở khóa sau ${SEAT_LOCK_TIME / 60} phút.`);

            // Xóa ghế khỏi lockedSeats
            const updatedLockedSeats = JSON.parse(sessionStorage.getItem("lockedSeats")) || {};
            delete updatedLockedSeats[seatId];
            sessionStorage.setItem("lockedSeats", JSON.stringify(updatedLockedSeats));
        }, SEAT_LOCK_TIME * 1000);
    }

    // Xóa ghế khỏi sessionStorage
    const storedSeats = JSON.parse(sessionStorage.getItem("selectedSeats")) || {};
    delete storedSeats[seatId];
    sessionStorage.setItem("selectedSeats", JSON.stringify(storedSeats));
    if (Object.keys(storedSeats).length === 0) {
        sessionStorage.removeItem("selectedSeats");
    }
}

// Cập nhật sessionStorage khi chọn ghế
function updateSessionTimer(seatId, remainingTime) {
    const savedSeats = JSON.parse(sessionStorage.getItem("seatTimers")) || {};
    savedSeats[seatId] = {
        remainingTime: remainingTime,
        timestamp: Date.now()
    };
    sessionStorage.setItem("seatTimers", JSON.stringify(savedSeats));
}

// Hàm xử lý chọn ghế
function selectSeat(seat) {
    if (seat.hasAttribute("disabled") || seat.classList.contains("locked")) {
        console.warn("Ghế này đã bị khóa tạm thời.");
        return;
    }

    const seatId = seat.getAttribute("data-seat-id");
    const seatName = seat.innerText;
    const seatPrice = parseInt(seat.value, 10);

    if (seat.classList.contains("selected")) {
        // Bỏ chọn ghế và hủy bộ đếm nếu có
        seat.classList.remove("selected");
        removeSeat(seatId);
        sendSeatUpdate({ seatId, seatName, selected: false });
        clearTimeout(seatTimers[seatId]); // Hủy bộ đếm thời gian
        delete seatTimers[seatId];

        // Xóa khỏi sessionStorage
        const savedSeats = JSON.parse(sessionStorage.getItem("seatTimers")) || {};
        delete savedSeats[seatId];
        sessionStorage.setItem("seatTimers", JSON.stringify(savedSeats));
        console.log(`Đã hủy giữ ghế ${seatId}.`);
    } else {
        // Chọn ghế và bắt đầu đếm ngược
        seat.classList.add("selected");
        addSeat(seatId, seatName, seatPrice);
        sendSeatUpdate({ seatId, seatName, selected: true });

        startSeatTimer(seat, SEAT_HOLD_TIME); // Thời gian giữ ghế
    }

    saveSeatsToSession();          // Lưu trạng thái ghế vào sessionStorage
    updateSelectedSeatsDisplay(); // Cập nhật danh sách ghế đã chọn
    updateTotalAmount();          // Cập nhật tổng tiền
}

// Hàm xử lý khi nhấn nút "Hủy tất cả ghế đang chọn"
document.getElementById("cancelAllSeatsButton").addEventListener("click", () => {
    // Lấy tất cả các ghế đang chọn
    const selectedSeats = document.querySelectorAll(".seat.selected");

    if (selectedSeats.length === 0) {
        console.warn("Không có ghế nào để hủy.");
        return;
    }

    selectedSeats.forEach(seat => {
        // Gọi hàm selectSeat để bỏ chọn từng ghế
        selectSeat(seat);
    });

    // Cập nhật giao diện và dữ liệu
    updateSelectedSeatsDisplay(); // Cập nhật danh sách ghế hiển thị
    updateTotalAmount();          // Cập nhật tổng tiền
    console.log(`Đã hủy tất cả ${selectedSeats.length} ghế đang chọn.`);
});



function addSeat(seatId, seatName, seatPrice) {
    selectedSeats.push({ seatId, seatName, seatPrice });
}

function removeSeat(seatId) {
    const index = selectedSeats.findIndex(seat => seat.seatId === seatId);
    if (index !== -1) {
        selectedSeats.splice(index, 1);
    }
}
function sendSeatUpdate(seatUpdate) {
    if (stompClient && isConnected) {
        const payload = {
            ...seatUpdate,
            clientId, // Gửi clientId duy nhất của tab
            tabName,  // Gửi tabName để hiển thị trên console
        };
        stompClient.send("/app/seat/select", {}, JSON.stringify(payload));
        console.log("Gửi cập nhật ghế qua WebSocket:", payload);
    }
}

function restoreSelectedSeatsForTab() {
    const allSeats = JSON.parse(sessionStorage.getItem('allSelectedSeats')) || {};
    const storedSeats = allSeats[clientId] || []; // Lấy ghế của tab hiện tại

    if (Array.isArray(storedSeats)) {
        storedSeats.forEach(seat => {
            const seatElement = document.querySelector(`[data-seat-id="${seat.seatId}"]`);
            if (seatElement) {
                seatElement.classList.add('selected'); // Đánh dấu ghế là đã chọn
            }
            if (!selectedSeats.some(s => s.seatId === seat.seatId)) {
                selectedSeats.push(seat);
            }
        });
        updateSelectedSeatsDisplay(); // Cập nhật hiển thị danh sách ghế
        updateTotalAmount();          // Cập nhật tổng tiền
    }
}
function removeBookedSeatsFromSession() {
    const storedSeats = JSON.parse(sessionStorage.getItem('selectedSeats')) || {};
    const bookedSeats = document.querySelectorAll('.seat.seat-booked'); // Lấy tất cả ghế có class 'seat-booked'

    // Duyệt qua các ghế có class 'seat-booked'
    bookedSeats.forEach(seat => {
        const seatId = seat.getAttribute('data-seat-id');
        if (storedSeats[seatId]) {
            delete storedSeats[seatId]; // Xóa ghế khỏi dữ liệu trong sessionStorage
        }
    });

    // Cập nhật lại sessionStorage
    if (Object.keys(storedSeats).length === 0) {
        sessionStorage.removeItem('selectedSeats'); // Xóa hoàn toàn nếu không còn ghế nào
    } else {
        sessionStorage.setItem('selectedSeats', JSON.stringify(storedSeats));
    }
}
// ==========================================
// Hiển thị trạng thái kết nối
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
    removeBookedSeatsFromSession();
    connectWebSocket(); // Kết nối WebSocket
    restoreAllSeatsStatus();      // Phục hồi trạng thái các ghế đã disable
    restoreSelectedSeatsForTab(); // Phục hồi ghế đã chọn của tab hiện tại
    restoreSeatsFromSession();    // Phục hồi trạng thái ghế từ sessionStorage
});



// ==========================================
// Cập nhật hiển thị
// ==========================================
function updateSelectedSeatsDisplay() {
    const display = document.getElementById('selectedSeatsDisplay');
    if (selectedSeats.length === 0) {
        display.innerText = 'Chưa có ghế nào được chọn.';
    } else {
        const seatNames = selectedSeats.map(seat => seat.seatName).join(', ');
        display.innerText = `Đã chọn: ${seatNames}`;
    }
}

function updateTotalAmount() {
    const totalAmountDisplay = document.getElementById('totalAmount');
    if (selectedSeats.length === 0) {
        totalAmountDisplay.innerText = ''; // Xóa nội dung hiển thị khi không có ghế chọn
    } else {
        const total = selectedSeats.reduce((sum, seat) => sum + seat.seatPrice, 0);
        totalAmountDisplay.innerText = `Tổng tiền: ${total.toLocaleString()} VNĐ`;
    }
}



function deselectAllSeats() {
    // Lặp qua danh sách các ghế đang được chọn
    selectedSeats.forEach(seat => {
        const seatElement = document.querySelector(`[data-seat-id="${seat.seatId}"]`);
        if (seatElement) {
            seatElement.classList.remove('selected'); // Bỏ class 'selected'
        }
    });

    // Xóa danh sách ghế đang được chọn và làm rỗng sessionStorage
    selectedSeats.length = 0; // Làm rỗng mảng selectedSeats
    sessionStorage.removeItem('selectedSeats'); // Xóa dữ liệu ghế đã chọn khỏi sessionStorage

    // Cập nhật lại giao diện
    updateSelectedSeatsDisplay(); // Xóa danh sách ghế hiển thị
    updateTotalAmount();          // Đặt lại tổng tiền
}


function handleSeatBooked(bookedSeats) {
    // Bỏ chọn tất cả ghế hiện tại và xóa dữ liệu trong session
    deselectAllSeats();
    sessionStorage.removeItem('selectedSeats'); // Xóa dữ liệu ghế đã chọn khỏi sessionStorage

    // Đánh dấu các ghế đã được đặt
    bookedSeats.forEach(seatId => {
        const seatElement = document.querySelector(`[data-seat-id="${seatId}"]`);
        if (seatElement) {
            seatElement.classList.add('seat-booked'); // Thêm class để hiển thị trạng thái "đã đặt"
            seatElement.setAttribute('disabled', 'true'); // Khóa ghế
        }
    });

    // Hiển thị thông báo cho người dùng
    Swal.fire({
        title: "Cập nhật trạng thái!",
        text: "Một số ghế đã được đặt. Các ghế bạn đã chọn đã bị hủy. Vui lòng chọn lại.",
        icon: "info"
    });

    // Xóa tất cả ghế đã chọn khỏi mảng selectedSeats và sessionStorage
    selectedSeats = [];

    // Cập nhật giao diện lại sau khi ghế bị đặt
    updateSelectedSeatsDisplay();
    updateTotalAmount();
}


// ==========================================
// Sự kiện xử lý thay đổi số lượng sản phẩm
// ==========================================

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
// ==========================================
// Kiểm tra và xác thực khi đặt vé
// ==========================================
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

// ==========================================
// Sự kiện trở về trang trước
// ==========================================
document.getElementById("backButton").addEventListener("click", function () {
    window.history.back();
});


function submitBooking(button) {
    // Lọc và lấy danh sách các ghế đang chọn (không bao gồm ghế đã đặt trước)
    const selectedSeats = [...document.querySelectorAll('.selected')].filter(seat => {
        return !seat.classList.contains('seat-booked'); // Loại bỏ ghế đã được đặt trước
    }).map(seat => {
        const seatId = seat.getAttribute('data-seat-id'); // Lấy seatId
        return {
            seatId: seatId, // Lưu seatId
            seatName: seat.innerText // Hoặc bạn có thể lưu seatName
        };
    });

    // Nếu không có ghế nào được chọn, thông báo và ngăn gửi form
    if (selectedSeats.length === 0) {
        Swal.fire({
            title: "Không có ghế hợp lệ!",
            text: "Vui lòng chọn ghế hợp lệ để đặt vé.",
            icon: "warning"
        });
        return;
    }

    // Lấy form từ nút được nhấn
    const form = button.closest('form');

    // Lấy showtimeId từ input ẩn
    const showtimeIdInput = form.querySelector('input[name="showtimeId"]');
    const showtimeId = showtimeIdInput ? showtimeIdInput.value : null; // Lấy giá trị từ input ẩn

    // Lấy thông tin sản phẩm và số lượng
    const products = [...document.querySelectorAll('.draggable-content')].map(row => {
        const quantityInput = row.querySelector('.comboQuantity');
        const quantity = parseInt(quantityInput.value, 10);
        const productIdElement = row.querySelector('.product-id'); // Phần tử chứa ID sản phẩm

        const productId = productIdElement ? productIdElement.innerText : null; // Lấy ID sản phẩm từ h5

        return { productId, quantity };
    }).filter(product => product.quantity > 0); // Lọc sản phẩm có số lượng lớn hơn 0

    // Gán dữ liệu vào các thẻ input hidden
    document.getElementById('selectedSeats').value = JSON.stringify(selectedSeats); // Gửi thông tin ghế
    document.getElementById('products').value = JSON.stringify(products);

    // Ghi lại dữ liệu để kiểm tra
    console.log('Selected Seats:', selectedSeats);
    console.log('Products:', products);
    console.log('Showtime ID:', showtimeId); // Ghi lại showtimeId

    // Gửi form
    form.submit();
}
