// src/main/resources/static/js/firebase-init.js

// --- !!! THAY THẾ BẰNG CẤU HÌNH FIREBASE WEB APP CỦA BẠN !!! ---
const firebaseConfig = {
    apiKey: "AIzaSyDMXxyfkxel7kY8jT70UOzx6rmSaoKLUL4",
    authDomain: "medconnect-1eba7.firebaseapp.com",
    projectId: "medconnect-1eba7",
    storageBucket: "medconnect-1eba7.firebasestorage.app",
    messagingSenderId: "76478248567",
    appId: "1:76478248567:web:2b69f29349e2322b127872",
    measurementId: "G-4WZ0PRHPS3"
};
// -----------------------------------------------------------------

// --- !!! THAY THẾ BẰNG VAPID KEY CỦA BẠN !!! ---
const VAPID_KEY = "BB4IrqfAm1mx7cdquaATWFBSLQH13HvN0TenAP0MYOJOD1xYegRHMjjMlqFU6JlgoE393FFdb4V4VpwrbdI8-IQ";
// -------------------------------------------------

let firebaseApp;
let messaging;

try {
    // Initialize Firebase
    firebaseApp = firebase.initializeApp(firebaseConfig);
    console.log("Firebase initialized successfully.");

    // Initialize Firebase Cloud Messaging and get a reference to the service
    messaging = firebase.messaging();
    console.log("Firebase Messaging initialized.");

    // --- Hàm yêu cầu quyền và lấy token ---
    function requestPermissionAndGetToken() {
        console.log('Requesting permission...');
        Notification.requestPermission().then((permission) => {
            if (permission === 'granted') {
                console.log('Notification permission granted.');
                // Get token
                messaging.getToken({ vapidKey: VAPID_KEY })
                    .then((currentToken) => {
                        if (currentToken) {
                            console.log('FCM Token:', currentToken);
                            // Gửi token lên server
                            sendTokenToServer(currentToken);
                            // Lưu token vào localStorage để không hỏi lại liên tục (tùy chọn)
                            localStorage.setItem('fcm_token', currentToken);
                        } else {
                            console.log('No registration token available. Request permission to generate one.');
                            // Có thể hiển thị hướng dẫn cho người dùng bật thông báo
                        }
                    }).catch((err) => {
                    console.log('An error occurred while retrieving token. ', err);
                    // Xử lý lỗi (ví dụ: trình duyệt không hỗ trợ,...)
                });
            } else {
                console.log('Unable to get permission to notify.');
                // Có thể thông báo cho người dùng biết họ sẽ không nhận được thông báo real-time
            }
        });
    }

    // --- Hàm gửi token lên backend ---
    function sendTokenToServer(token) {
        // Lấy CSRF token (nếu backend có bật CSRF) - Cần điều chỉnh tùy theo cách Spring Security gửi token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch('/api/users/update-fcm-token', { // Gọi API bạn đã tạo
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ token: token })
        })
            .then(response => {
                if (response.ok) {
                    console.log('Token sent to server successfully.');
                } else {
                    console.error('Failed to send token to server. Status:', response.status);
                }
            })
            .catch(error => {
                console.error('Error sending token to server:', error);
            });
    }

    // --- Lắng nghe tin nhắn khi ứng dụng đang mở (Foreground) ---
    messaging.onMessage((payload) => {
        console.log('Message received. ', payload);

        // Payload thường chứa trong 'data' nếu bạn gửi bằng data payload từ backend
        const notificationTitle = payload.data?.title || 'Thông báo mới';
        const notificationBody = payload.data?.body || '';

        // --- Cập nhật giao diện người dùng ---
        // Ví dụ: Hiển thị thông báo dạng toast/popup đơn giản
        showForegroundNotification(notificationTitle, notificationBody);

        // Ví dụ: Cập nhật số lượng trên icon chuông (cần có element với id="notification-count")
        updateNotificationBadge();

    });

    // --- Gọi hàm yêu cầu quyền khi trang tải xong ---
    // Chỉ yêu cầu nếu chưa có token hoặc token cũ (tùy logic)
    const storedToken = localStorage.getItem('fcm_token');
    if (!storedToken) { // Chỉ yêu cầu lần đầu hoặc nếu token bị xóa
        // Bạn có thể thêm một nút "Bật thông báo" thay vì hỏi ngay lập tức
        // requestPermissionAndGetToken();
        console.log("Chưa có FCM token. Người dùng cần cấp quyền.")
    } else {
        console.log("Đã có FCM token:", storedToken);
        // Có thể gửi lại token lên server mỗi khi load trang để đảm bảo server luôn có token mới nhất
        sendTokenToServer(storedToken);
    }
    // Tạm thời gọi luôn để test
    requestPermissionAndGetToken();


} catch (e) {
    console.error("Error initializing Firebase or Messaging:", e);
    // Thông báo lỗi cho người dùng nếu cần
}

// --- Hàm ví dụ hiển thị thông báo popup đơn giản ---
function showForegroundNotification(title, body) {
    // Tạo một div đơn giản để hiển thị thông báo
    const notificationDiv = document.createElement('div');
    notificationDiv.style.position = 'fixed';
    notificationDiv.style.top = '20px';
    notificationDiv.style.right = '20px';
    notificationDiv.style.padding = '15px';
    notificationDiv.style.backgroundColor = '#4CAF50'; // Màu xanh lá
    notificationDiv.style.color = 'white';
    notificationDiv.style.borderRadius = '5px';
    notificationDiv.style.zIndex = '1000';
    notificationDiv.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
    notificationDiv.innerHTML = `<strong>${title}</strong><br>${body}`;

    document.body.appendChild(notificationDiv);

    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        notificationDiv.remove();
    }, 5000);
}

// --- Hàm ví dụ cập nhật số trên icon chuông ---
function updateNotificationBadge() {
    const badgeElement = document.getElementById('notification-count');
    if (badgeElement) {
        let currentCount = parseInt(badgeElement.textContent || '0', 10);
        currentCount++;
        badgeElement.textContent = currentCount;
        badgeElement.classList.remove('hidden'); // Hiện badge nếu đang ẩn
    }
    // Bạn có thể gọi API để lấy số thông báo chưa đọc từ DB thay vì chỉ +1
}

// Thêm một nút để người dùng chủ động bật thông báo
// Ví dụ: <button id="enable-notifications-btn">Bật thông báo</button>
const enableButton = document.getElementById('enable-notifications-btn');
if (enableButton) {
    enableButton.addEventListener('click', requestPermissionAndGetToken);
}