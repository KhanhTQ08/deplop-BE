
	
		
		document.addEventListener("DOMContentLoaded", function () {
		    const collapseItems = document.querySelectorAll('.collapse');

		    // Khôi phục trạng thái từ LocalStorage
		    collapseItems.forEach(item => {
		        const id = item.getAttribute('id');
		        const state = localStorage.getItem(id);

		        if (state === 'true') {
		            item.classList.add('show'); // Mở mục nếu được lưu là "true"
		        } else {
		            item.classList.remove('show'); // Đóng mục nếu được lưu là "false"
		        }
		    });

		    // Lưu trạng thái khi đóng/mở
		    collapseItems.forEach(item => {
		        item.addEventListener('shown.bs.collapse', function () {
		            const id = item.getAttribute('id');
		            localStorage.setItem(id, 'true'); // Lưu trạng thái mở
		        });

		        item.addEventListener('hidden.bs.collapse', function () {
		            const id = item.getAttribute('id');
		            localStorage.setItem(id, 'false'); // Lưu trạng thái đóng
		        });
		    });
		});
	// JavaScript: Bật/Tắt sidebar
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    sidebar.classList.toggle('open');
}

