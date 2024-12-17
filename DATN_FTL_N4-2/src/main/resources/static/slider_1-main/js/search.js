let debounceTimer;

document.getElementById('input').addEventListener('input', function() {
	const query = this.value.trim();
	const suggestionBox = document.getElementById('suggestionBox');

	if (query.length > 0) {
		// Hiển thị loading ngay lập tức
		suggestionBox.style.display = 'block';
		suggestionBox.innerHTML = `
         <div class="loading-container">
    <img src="https://www.icegif.com/wp-content/uploads/2023/09/icegif-576.gif" alt="Loading..." style="width: 150px; height: 150px;">
    <p style="color:#999999	; margin-top: 17px; font-size: 22px;font-weight: bold;">Đang tìm kiếm kết quả cho...</p>
</div>

        `;

		// Hủy yêu cầu trước đó nếu người dùng gõ tiếp
		clearTimeout(debounceTimer);

		// Gửi yêu cầu tìm kiếm sau 500ms khi người dùng ngừng gõ
		debounceTimer = setTimeout(() => {
			fetch(`/api/searchMovies?query=${query}`)
				.then(response => response.json())
				.then(data => {
					showSuggestions(data);
				})
				.catch(error => {
					console.error('Error:', error);
					clearSuggestions();
				});
		}, 500); // Debounce 500ms
	} else {
		clearSuggestions();
	}
});

// Hiển thị danh sách gợi ý
function showSuggestions(suggestions) {
	const suggestionBox = document.getElementById('suggestionBox');
	suggestionBox.innerHTML = ''; // Xóa nội dung cũ

	if (!Array.isArray(suggestions) || suggestions.length === 0) {
		suggestionBox.innerHTML = `
            <div style="text-align: center; margin-top: 20px;">
                <img src="https://homepage.momocdn.net/next-js/_next/static/public/cinema/not-found.svg" alt="Không tìm thấy gợi ý" style="width: 150px; height: 150px; margin-bottom: 10px;">
                <p style="font-weight: bold; font-size: 22px; color: #999999;">Không tìm thấy gợi ý nào...</p>
            </div>
        `;
		suggestionBox.style.display = 'block';
		return;
	}

	// Hiển thị suggestion box nếu có kết quả
	suggestionBox.style.display = 'block';

	// Duyệt qua danh sách gợi ý và tạo phần tử HTML
	suggestions.forEach(movie => {
		const div = document.createElement('div');
		div.className = 'suggestion-item';

		div.onclick = () => {
			window.location.href = `/details/${movie.movieId}`;
		};

		const img = document.createElement('img');
		img.src = movie.image;
		img.alt = movie.movieName;
		img.className = 'suggestion-image';

		const textContainer = document.createElement('div');
		textContainer.className = 'text-container';

		const nameSpan = document.createElement('span');
		nameSpan.innerText = movie.movieName;
		nameSpan.className = 'suggestion-name';

		const genreSpan = document.createElement('span');
		genreSpan.innerText = movie.genre;
		genreSpan.className = 'suggestion-genre';

		textContainer.appendChild(nameSpan);
		textContainer.appendChild(genreSpan);

		div.appendChild(img);
		div.appendChild(textContainer);
		suggestionBox.appendChild(div);
	});
}

// Xóa danh sách gợi ý và ẩn các phần tử liên quan
function clearSuggestions() {
	const suggestionBox = document.getElementById('suggestionBox');
	suggestionBox.style.display = 'none';
	suggestionBox.innerHTML = ''; // Xóa nội dung cũ
}
