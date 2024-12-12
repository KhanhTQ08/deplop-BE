const Toast = Swal.mixin({
  toast: true,
  position: "top-end",  // Vị trí của toast
  showConfirmButton: false,  // Không cần nút xác nhận
  timer: 3000,  // Thời gian hiển thị thông báo
  timerProgressBar: true,  // Hiển thị thanh tiến độ
  didOpen: (toast) => {
    toast.onmouseenter = Swal.stopTimer;  // Dừng timer khi hover
    toast.onmouseleave = Swal.resumeTimer;  // Tiếp tục timer khi rời chuột
  }
});

$(document).ready(function() {
  // Gửi yêu cầu OTP
  $("#forgotPasswordForm").submit(function(e) {
    e.preventDefault();

    const email = $("#email").val();

    $.post("/forgot-password", { email: email })
      .done(function(response) {
        // Hiển thị Toast khi gửi OTP thành công
        if (response.message) {
          Toast.fire({
            icon: 'success',
            title: response.message
          }).then(() => {
            // Hiển thị modal để nhập OTP
            $("#otpModal").modal("show");
          });
        }
      })
      .fail(function(response) {
        // Hiển thị Toast nếu gửi OTP thất bại
        if (response.responseJSON.error) {
          Toast.fire({
            icon: 'error',
            title: response.responseJSON.error
          });
        }
      });
  });

  // Lắng nghe sự kiện 'input' trên các ô OTP
  $(".otp-input").on("input", function() {
    const currentIndex = $(this).index(".otp-input"); // Lấy chỉ số của ô hiện tại
    const nextInput = $(".otp-input").eq(currentIndex + 1); // Lấy ô tiếp theo
    if (this.value.length === 1 && nextInput.length) {
      nextInput.focus(); // Chuyển focus tới ô tiếp theo
    }
  });

  // Quay lại ô trước khi xóa (nếu có)
  $(".otp-input").on("keydown", function(e) {
    if (e.key === "Backspace" && this.value === "") {
      const currentIndex = $(this).index(".otp-input");
      const prevInput = $(".otp-input").eq(currentIndex - 1); // Lấy ô trước đó
      if (prevInput.length) {
        prevInput.focus(); // Chuyển focus về ô trước
      }
    }
  });

  // Xử lý xác thực OTP và thay đổi mật khẩu
  $("#verifyOtpForm").submit(function (e) {
    e.preventDefault();

    // Lấy mã OTP từ các ô
    let otp = "";
    for (let i = 1; i <= 6; i++) {
      otp += $("#otp" + i).val();
    }

    if (otp.trim() === "") {
      Toast.fire({
        icon: 'error',
        title: "Mã OTP không được để trống."
      });
      return;
    }

    // Gửi yêu cầu xác thực OTP
    $.post("/verify-otp", { otp: otp })
      .done(function(response) {
        // Hiển thị Toast nếu OTP hợp lệ
        if (response.message) {
          Toast.fire({
            icon: 'success',
            title: response.message
          }).then(() => {
            // Hiển thị modal thay đổi mật khẩu sau khi OTP hợp lệ
            $("#resetPasswordModal").modal("show");
          });
        }
      })
      .fail(function(response) {
        // Hiển thị Toast nếu OTP không hợp lệ
        if (response.responseJSON.error) {
          Toast.fire({
            icon: 'error',
            title: response.responseJSON.error
          });
        }
      });
  });

  // Xử lý thay đổi mật khẩu
  $('#resetPasswordForm').on('submit', function(e) {
    e.preventDefault(); // Ngừng hành động mặc định của form (reload trang)

    const newPassword = $('#newPassword').val();
    const confirmPassword = $('#confirmPassword').val();

    // Kiểm tra xem mật khẩu mới có khớp với xác nhận không
    if (newPassword !== confirmPassword) {
        Toast.fire({
            icon: 'error',
            title: 'Mật khẩu và xác nhận mật khẩu không khớp.'
        });
        return;
    }

    // Gửi yêu cầu thay đổi mật khẩu
    $.ajax({
        type: 'POST',
        url: '/reset-password',
        data: {
            newPassword: newPassword,
            confirmPassword: confirmPassword
        },
        success: function(response) {
            Toast.fire({
                icon: 'success',
                title: response.message
            });
            setTimeout(function() {
                window.location.href = '/login';
            }, 3000);
            $('#resetPasswordModal').modal('hide');
        },
        error: function(response) {
            Toast.fire({
                icon: 'error',
                title: response.responseJSON.error
            });
        }
    });
  });
});
