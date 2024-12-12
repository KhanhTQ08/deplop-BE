// Biến toàn cục để lưu trữ đối tượng nhận diện giọng nói
var recognition;

// Mở modal
function openModal() {
    const modal = document.getElementById('voiceModal');
    modal.classList.add('show'); // Thêm class để hiển thị
    startVoiceRecognition(); // Bắt đầu nhận diện giọng nói
}

// Đóng modal
function closeModal() {
    const modal = document.getElementById('voiceModal');
    modal.classList.remove('show'); // Xóa class để ẩn
    if (recognition) {
        recognition.stop(); // Dừng nhận diện giọng nói khi đóng modal
        recognition = null; // Đặt lại recognition về null
    }
}
function startVoiceRecognition() {
    if ('webkitSpeechRecognition' in window) {
        recognition = new webkitSpeechRecognition(); // Khởi tạo đối tượng nhận diện giọng nói
        recognition.lang = 'vi-VN'; 
        recognition.interimResults = false;
        recognition.maxAlternatives = 1;

        let timeout; // Biến dùng để theo dõi khi người dùng không nói gì

        recognition.onstart = function() {
            console.log('Bắt đầu nhận diện giọng nói...');
            timeout = setTimeout(function() {
                // Nếu không nhận được kết quả sau 3 giây, hiển thị thông báo lỗi
                Swal.fire({
                    title: 'Không có kết quả giọng nói',
                    text: 'Bạn chưa nói gì hoặc không nhận diện được giọng nói!',
                    icon: 'error',
                    confirmButtonText: 'OK'
                }).then(() => {
                    closeModal(); // Đóng modal khi người dùng bấm "OK"
                });
            }, 10000); // 3 giây không có kết quả
        };

        recognition.onresult = function(event) {
            clearTimeout(timeout); // Hủy timeout nếu có kết quả
            var transcript = event.results[0][0].transcript;

            if (transcript.trim() !== "") {
                document.getElementById('input').value = transcript;
                console.log('Kết quả giọng nói: ', transcript);
                closeModal(); // Đóng modal sau khi nhận diện giọng nói
                document.getElementById('searchForm').submit(); // Gửi form để tìm kiếm
            } else {
                console.log('Không có kết quả giọng nói');
            }
        };

        recognition.onerror = function(event) {
            console.error('Lỗi nhận diện giọng nói: ', event.error);

            // Kiểm tra lỗi khác ngoài 'no-speech'
            if (event.error !== 'no-speech') {
                Swal.fire({
                    title: 'Lỗi nhận diện giọng nói',
                    text: 'Đã xảy ra lỗi khi nhận diện giọng nói!',
                    icon: 'error',
                    confirmButtonText: 'OK'
                }).then(() => {
                    closeModal(); // Đóng modal khi người dùng bấm "OK"
                });
            }
        };

        recognition.onend = function() {
            console.log('Kết thúc nhận diện giọng nói.');
        };

        recognition.start(); // Bắt đầu nhận diện giọng nói
    } else {
        alert('Trình duyệt của bạn không hỗ trợ nhận diện giọng nói.');
    }
}

function confirmLogout() {
	    event.preventDefault();  // Ngừng hành động mặc định (chuyển hướng)

    Swal.fire({
        title: 'Xác nhận đăng xuất',
        text: "Bạn có chắc chắn muốn đăng xuất không?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Đăng xuất',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            window.location.href = '/logout'; // Chuyển hướng tới trang đăng xuất
        }
    });
}