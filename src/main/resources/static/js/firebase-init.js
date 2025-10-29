const firebaseConfig = {
    apiKey: "AIzaSyDMXxyfkxel7kY8jT70UOzx6rmSaoKLUL4",
    authDomain: "medconnect-1eba7.firebaseapp.com",
    projectId: "medconnect-1eba7",
    storageBucket: "medconnect-1eba7.firebasestorage.app",
    messagingSenderId: "76478248567",
    appId: "1:76478248567:web:2b69f29349e2322b127872",
    measurementId: "G-4WZ0PRHPS3"
};

const VAPID_KEY = "BB4IrqfAm1mx7cdquaATWFBSLQH13HvN0TenAP0MYOJOD1xYegRHMjjMlqFU6JlgoE393FFdb4V4VpwrbdI8-IQ";

let firebaseApp;
let messaging;

// --- Các biến tham chiếu đến phần tử DOM ---
let notificationBell = null;
let notificationDropdown = null;
let notificationList = null;
let noNotificationsMessage = null;
let notificationCountBadge = null;
let markAllReadBtn = null;
let enableNotificationsBtn = null;
let csrfToken = null; // Sẽ lấy từ window
let csrfHeader = null; // Sẽ lấy từ window

let unreadNotifications = [];

// --- Chờ DOM sẵn sàng trước khi truy cập phần tử ---
document.addEventListener('DOMContentLoaded', () => {
    // Lấy tham chiếu đến các phần tử DOM sau khi DOM đã tải
    notificationBell = document.getElementById('notification-bell');
    notificationDropdown = document.getElementById('notification-dropdown');
    notificationList = document.getElementById('notification-list');
    noNotificationsMessage = document.getElementById('no-notifications');
    notificationCountBadge = document.getElementById('notification-count');
    markAllReadBtn = document.getElementById('mark-all-read-btn');
    enableNotificationsBtn = document.getElementById('enable-notifications-btn');

    // Lấy CSRF từ biến toàn cục window (được inject bởi Thymeleaf trong header.html)
    csrfToken = window.csrfToken || null;
    csrfHeader = window.csrfHeaderName || null;

    // Chỉ khởi tạo Firebase nếu các phần tử thông báo tồn tại (người dùng đăng nhập)
    if (notificationBell) {
        initializeFirebaseAndMessaging();
    } else {
        console.log("Notification elements not found, likely not logged in. Skipping Firebase init.");
    }
});

// --- Hàm khởi tạo Firebase và Messaging ---
function initializeFirebaseAndMessaging() {
    try {
        firebaseApp = firebase.initializeApp(firebaseConfig);
        messaging = firebase.messaging();
        setupEventListeners();

        checkNotificationStatusAndGetToken();

        // --- Lắng nghe tin nhắn foreground ---
        messaging.onMessage((payload) => {
            const notificationTitle = payload.data?.title || 'Thông báo mới';
            const notificationBody = payload.data?.body || '';
            showForegroundNotification(notificationTitle, notificationBody);
            // Cập nhật UI ngay lập tức hoặc sau một khoảng trễ nhỏ
            setTimeout(fetchNotifications, 500); // Gọi fetchNotifications sau khi nhận tin nhắn
        });

    } catch (e) {
        console.error("Error initializing Firebase or Messaging:", e);
        if (enableNotificationsBtn) enableNotificationsBtn.style.display = 'none';
    }
}

// --- Hàm gắn các Event Listener ---
function setupEventListeners() {
    if (notificationBell && notificationDropdown) {
        notificationBell.addEventListener('click', (event) => {
            event.stopPropagation();
            const isHiddenCurrently = notificationDropdown.classList.contains('hidden');
            notificationDropdown.classList.toggle('hidden');
            if (isHiddenCurrently) { // Fetch chỉ khi mở dropdown
                fetchNotifications();
            }
        });

        // Đóng dropdown khi click bên ngoài
        document.addEventListener('click', (event) => {
            if (notificationDropdown && !notificationBell.contains(event.target) && !notificationDropdown.contains(event.target)) {
                notificationDropdown.classList.add('hidden');
            }
        });
    }

    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', handleMarkAllReadClick);
    }

    if (enableNotificationsBtn) {
        enableNotificationsBtn.addEventListener('click', requestPermissionAndGetToken);
    }
}


// --- Function to request permission and get token ---
function requestPermissionAndGetToken() {
    Notification.requestPermission().then((permission) => {
        if (permission === 'granted') {
            if (enableNotificationsBtn) {
                enableNotificationsBtn.style.display = 'none';
            }
            getTokenAndSendToServer(); // Gọi hàm lấy token
        } else {
            console.log('Unable to get permission to notify.');
            updateEnableButtonStatus('denied'); // Cập nhật trạng thái nút
        }
    }).catch(err => {
        console.error('Error requesting permission:', err);
        updateEnableButtonStatus('default'); // Cập nhật trạng thái nút
    });
}

// --- Hàm lấy token và gửi lên server ---
function getTokenAndSendToServer() {
    messaging.getToken({ vapidKey: VAPID_KEY })
        .then((currentToken) => {
            if (currentToken) {
                console.log('FCM Token:', currentToken);
                sendTokenToServer(currentToken);
                localStorage.setItem('fcm_token', currentToken);
                updateEnableButtonStatus('granted'); // Cập nhật trạng thái nút
            } else {
                console.log('No registration token available. Request permission to generate one.');
                updateEnableButtonStatus('default'); // Cập nhật trạng thái nút
            }
        }).catch((err) => {
        console.log('An error occurred while retrieving token. ', err);
        updateEnableButtonStatus('default'); // Cập nhật trạng thái nút
    });
}

// --- Function to send token to server ---
function sendTokenToServer(token) {
    // Sử dụng csrfToken và csrfHeader đã lấy từ window
    if (!csrfToken || !csrfHeader) {
        console.error("CSRF token or header not found (in sendTokenToServer). Cannot send token to server.");
        return;
    }
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        [csrfHeader]: csrfToken // Sử dụng biến đã lấy từ window
    };

    fetch('/api/users/update-fcm-token', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ token: token })
    })
        .then(response => {
            if (response.ok) {
                console.log('Token sent to server successfully.');
            } else {
                console.error('Failed to send token to server. Status:', response.status);
                response.text().then(text => console.error('Server response:', text));
            }
        })
        .catch(error => {
            console.error('Error sending token to server:', error);
        });
}


// --- Function to fetch unread notifications ---
async function fetchNotifications() {
    // Chỉ fetch nếu các thành phần UI tồn tại
    if (!notificationList) return;

    try {
        const response = await fetch('/api/notifications/unread', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            try { // Cố gắng đọc lỗi dưới dạng text
                const errorText = await response.text();
                console.log('Error response body:', errorText);
            } catch (e) {
                console.log('Could not read error response body.');
            }
            // Hiển thị lỗi trên UI
            notificationList.innerHTML = '<div class="px-4 py-3 text-sm text-red-500 text-center">Lỗi khi tải thông báo.</div>';
            updateBadge(0);
            return; // Dừng lại nếu fetch lỗi
        }

        unreadNotifications = await response.json();
        updateNotificationUI(unreadNotifications);

    } catch (error) {
        console.error('Error fetching notifications:', error);
        notificationList.innerHTML = '<div class="px-4 py-3 text-sm text-red-500 text-center">Lỗi mạng khi tải thông báo.</div>';
        updateBadge(0);
    }
}

// --- Function to update badge and dropdown list ---
function updateNotificationUI(notifications) {
    // Kiểm tra lại các phần tử DOM trước khi thao tác
    if (!notificationList || !noNotificationsMessage || !markAllReadBtn) return;

    const count = notifications.length;
    updateBadge(count);

    if (count === 0) {
        notificationList.innerHTML = ''; // Xóa list cũ
        noNotificationsMessage.style.display = 'block';
        markAllReadBtn.disabled = true;
    } else {
        noNotificationsMessage.style.display = 'none';
        notificationList.innerHTML = ''; // Xóa list cũ
        markAllReadBtn.disabled = false;

        notifications.forEach(notif => {
            const item = createNotificationItem(notif);
            notificationList.appendChild(item);
        });
    }
}

// --- Function to update the notification count badge ---
function updateBadge(count) {
    if (notificationCountBadge) {
        notificationCountBadge.textContent = count;
        if (count > 0) {
            notificationCountBadge.classList.remove('hidden');
        } else {
            notificationCountBadge.classList.add('hidden');
        }
    }
}

// --- Function to create a single notification item HTML (NO LINK) ---
function createNotificationItem(notification) {
    const div = document.createElement('div');
    // Thêm class và data attribute
    div.className = 'px-4 py-3 hover:bg-gray-100 cursor-pointer border-b border-gray-100 last:border-b-0'; // Thêm border
    div.dataset.notificationId = notification.notificationId;

    // Định dạng thời gian
    const timeAgo = formatTimeAgo(notification.sentAt);

    // Tạo nội dung HTML
    div.innerHTML = `
        <div class="flex justify-between items-start">
             <p class="text-sm font-semibold text-gray-800 truncate pr-2">${notification.notificationType || 'Thông báo'}</p>
             <p class="text-xs text-gray-400 whitespace-nowrap">${timeAgo}</p>
        </div>
        <p class="text-sm text-gray-600 mt-1">${notification.message}</p>
    `;

    // Add click listener ONLY for marking as read
    div.addEventListener('click', handleNotificationClick); // Sẽ dùng CSRF từ window

    return div;
}


// --- Handler for clicking a notification item (NO REDIRECT) ---
async function handleNotificationClick(event) {
    const item = event.currentTarget;
    const notificationId = item.dataset.notificationId;

    // Sử dụng csrfToken và csrfHeader đã lấy từ window
    if (!csrfToken || !csrfHeader) {
        console.error("CSRF token or header not found (in handleNotificationClick). Cannot mark notification as read.");
        return;
    }

    try {
        const headers = {
            'Accept': 'application/json',
            [csrfHeader]: csrfToken // Sử dụng biến đã lấy
        };

        const response = await fetch(`/api/notifications/mark-read/${notificationId}`, {
            method: 'POST',
            headers: headers
        });

        if (response.ok) {
            item.remove();
            unreadNotifications = unreadNotifications.filter(n => n.notificationId !== parseInt(notificationId));
            updateNotificationUI(unreadNotifications); // Cập nhật lại UI

        } else {
            console.error('Failed to mark notification as read:', response.statusText);
            alert('Đã xảy ra lỗi khi đánh dấu thông báo.');
        }
    } catch (error) {
        console.error('Error marking notification as read:', error);
        alert('Đã xảy ra lỗi mạng khi đánh dấu thông báo.');
    }
}


// --- Handler for clicking "Mark all as read" ---
async function handleMarkAllReadClick() {
    // Sử dụng csrfToken và csrfHeader đã lấy từ window
    if (!csrfToken || !csrfHeader) {
        return; // Dừng thực thi nếu không có token/header
    }

    try {
        const headers = {
            'Accept': 'application/json',
            [csrfHeader]: csrfToken // Sử dụng biến đã lấy
        };
        const response = await fetch('/api/notifications/mark-all-read', {
            method: 'POST',
            headers: headers
        });

        if (response.ok) {
            unreadNotifications = []; // Clear the local array
            updateNotificationUI(unreadNotifications); // Update the UI
            if (notificationDropdown) {
                notificationDropdown.classList.add('hidden'); // Close dropdown
            }
        } else {
            console.error('Failed to mark all notifications as read:', response.statusText);
            alert('Đã xảy ra lỗi khi đánh dấu tất cả thông báo.');
        }
    } catch (error) {
        console.error('Error marking all notifications as read:', error);
        alert('Đã xảy ra lỗi mạng.');
    }
}

// --- Helper function to format time ---
function formatTimeAgo(dateString) {
    if (!dateString) return '';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return '';
        }
        const now = new Date();
        const seconds = Math.floor((now - date) / 1000);

        if (seconds < 5) return "Vừa xong";
        if (seconds < 60) return `${seconds} giây trước`;

        const minutes = Math.floor(seconds / 60);
        if (minutes < 60) return `${minutes} phút trước`;

        const hours = Math.floor(minutes / 60);
        if (hours < 24) return `${hours} giờ trước`;

        const days = Math.floor(hours / 24);
        if (days < 30) return `${days} ngày trước`;

        const months = Math.floor(days / 30);
        if (months < 12) return `${months} tháng trước`;

        const years = Math.floor(days / 365);
        return `${years} năm trước`;

    } catch (e) {
        console.error("Error formatting time:", e, "Input:", dateString);
        return ''; // Trả về chuỗi rỗng nếu có lỗi
    }
}

// --- Function to check notification status and get token if needed ---
function checkNotificationStatusAndGetToken() {
    const storedToken = localStorage.getItem('fcm_token');
    if ('Notification' in window && messaging) { // Đảm bảo messaging đã được khởi tạo
        const permission = Notification.permission;

        if (permission === 'granted') {
            updateEnableButtonStatus('granted');
            if (!storedToken) {
                getTokenAndSendToServer(); // Lấy token mới nếu chưa có
            } else {
                sendTokenToServer(storedToken); // Gửi lại token hiện có
            }
            // Fetch initial notifications sau khi chắc chắn có quyền và token
            fetchNotifications();
        } else if (permission === 'denied') {
            updateEnableButtonStatus('denied');
        } else { // 'default' or not supported
            updateEnableButtonStatus('default');
        }
    } else {
        console.log("Notifications not supported or messaging not initialized.");
        updateEnableButtonStatus('unsupported');
    }
}

// --- Helper to update the enable button's appearance ---
function updateEnableButtonStatus(status) {
    if (!enableNotificationsBtn) return;

    switch (status) {
        case 'granted':
            enableNotificationsBtn.classList.add('hidden');
            enableNotificationsBtn.style.display = 'none';
            break;
        case 'denied':
            enableNotificationsBtn.textContent = "Thông báo bị chặn";
            enableNotificationsBtn.disabled = true;
            enableNotificationsBtn.classList.remove('hidden');
            enableNotificationsBtn.style.display = 'inline-flex';
            enableNotificationsBtn.classList.replace('bg-blue-500','bg-gray-400');
            enableNotificationsBtn.classList.replace('hover:bg-blue-600','hover:bg-gray-400');
            break;
        case 'unsupported':
            enableNotificationsBtn.classList.add('hidden');
            enableNotificationsBtn.style.display = 'none';
            break;
        case 'default':
        default:
            enableNotificationsBtn.textContent = "Bật Thông báo";
            enableNotificationsBtn.disabled = false;
            enableNotificationsBtn.classList.remove('hidden');
            enableNotificationsBtn.style.display = 'inline-flex';
            enableNotificationsBtn.classList.replace('bg-gray-400','bg-blue-500');
            enableNotificationsBtn.classList.replace('hover:bg-gray-400','hover:bg-blue-600');
            break;
    }
}


// --- Function to show foreground notification ---
function showForegroundNotification(title, body) {
    const notificationDiv = document.createElement('div');
    // Styling
    notificationDiv.style.position = 'fixed';
    notificationDiv.style.top = '80px';
    notificationDiv.style.right = '20px';
    notificationDiv.style.padding = '15px 25px 15px 15px';
    notificationDiv.style.backgroundColor = '#2563eb';
    notificationDiv.style.color = 'white';
    notificationDiv.style.borderRadius = '8px';
    notificationDiv.style.zIndex = '1001';
    notificationDiv.style.boxShadow = '0 4px 15px rgba(0,0,0,0.2)';
    notificationDiv.style.maxWidth = '350px';
    notificationDiv.style.fontFamily = 'system-ui, sans-serif';
    notificationDiv.style.opacity = '0';
    notificationDiv.style.transform = 'translateX(100%)';
    notificationDiv.style.transition = 'opacity 0.5s ease, transform 0.5s ease';

    // Nội dung
    notificationDiv.innerHTML = `
        <strong style="font-size: 1.1em; display: block; margin-bottom: 5px;">${title}</strong>
        <span style="font-size: 0.95em;">${body}</span>
    `;

    // Nút đóng
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '&times;';
    closeBtn.style.position = 'absolute';
    closeBtn.style.top = '5px';
    closeBtn.style.right = '8px';
    closeBtn.style.background = 'none';
    closeBtn.style.border = 'none';
    closeBtn.style.color = 'rgba(255, 255, 255, 0.7)';
    closeBtn.style.fontSize = '1.6em';
    closeBtn.style.lineHeight = '1';
    closeBtn.style.cursor = 'pointer';
    closeBtn.style.padding = '0';
    closeBtn.onmouseover = () => { closeBtn.style.color = 'white'; };
    closeBtn.onmouseout = () => { closeBtn.style.color = 'rgba(255, 255, 255, 0.7)'; };
    closeBtn.onclick = (e) => {
        e.stopPropagation();
        notificationDiv.style.opacity = '0';
        notificationDiv.style.transform = 'translateX(100%)';
        setTimeout(() => notificationDiv.remove(), 500);
    };
    notificationDiv.appendChild(closeBtn);

    // Thêm vào body
    document.body.appendChild(notificationDiv);

    // Hiệu ứng xuất hiện
    setTimeout(() => {
        notificationDiv.style.opacity = '1';
        notificationDiv.style.transform = 'translateX(0)';
    }, 50);

    // Tự động đóng
    setTimeout(() => {
        if (document.body.contains(notificationDiv)) {
            closeBtn.onclick({stopPropagation: () => {}}); // Gọi hàm đóng để có hiệu ứng
        }
    }, 5000);
}