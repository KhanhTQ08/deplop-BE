document.getElementById('input').addEventListener('input', function () {
    const query = this.value;

    if (query.length > 0) {
        fetch(`/index/api/searchMovies?query=${query}`)
            .then(response => response.json())
            .then(data => {
                showSuggestions(data);
            })
            .catch(error => console.error('Error:', error));
    } else {
        clearSuggestions();
    }
});

function showSuggestions(suggestions) {
    const suggestionBox = document.getElementById('suggestionBox');
    suggestionBox.innerHTML = '';

   if (!Array.isArray(suggestions) || suggestions.length === 0) {
    suggestionBox.innerHTML = `
        <div style="text-align: center; margin-top: 50px;">
            <img src="https://homepage.momocdn.net/next-js/_next/static/public/cinema/not-found.svg" alt="Không tìm thấy gợi ý" style="width: 150px; height: 150px; margin-bottom: 20px;">
            <p style="font-weight: bold; font-size: 22px; text-align: center; color: #777;">Không tìm thấy gợi ý nào.</p>
        </div>
    `;
    suggestionBox.style.display = 'block';
    return;
}

    suggestionBox.style.display = 'block';

    suggestions.forEach(movie => {
        const div = document.createElement('div');
        div.className = 'suggestion-item';

        div.onclick = () => {
            window.location.href = `/index/details/${movie.movieId}`;
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

function clearSuggestions() {
    const suggestionBox = document.getElementById('suggestionBox');
    suggestionBox.style.display = 'none';
}
